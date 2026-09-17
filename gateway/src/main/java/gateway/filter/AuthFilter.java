package gateway.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import gateway.config.IgnoreWhiteProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import utils.JwtUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 全局认证过滤器
 * 负责token校验、角色转发、管理员路由权限控制
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        // 跳过不需要验证的路径
        if (matches(url, ignoreWhite.getWhites()))
        {
            return chain.filter(exchange);
        }
        String token = request.getHeaders().getFirst("Authorization");
        if(token==null)
        {
            return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "令牌不能为空");
        }
        // 去掉 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            //解析token
            Map<String,Object> claims = JwtUtil.parseToken(token);
            String userId = claims.get("id").toString();
            String uuid = claims.get("uuid").toString();
            String role = claims.get("role") != null ? claims.get("role").toString() : "user";
            //从redis获取对应token
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(userId);
            if(redisToken == null){
                return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "无效token");
            }
            //对比uuid是否相同
            Map<String,Object> redisClaims = JwtUtil.parseToken(redisToken);
            String redisUUID = redisClaims.get("uuid").toString();
            if(!redisUUID.equals(uuid)){
                return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "过期token");
            }
            // 管理员路由权限校验：/admin/** 及 /auth/下的管理端接口 仅允许 role=admin 访问
            if (!"admin".equals(role)
                    && (url.startsWith("/admin/")
                    || url.equals("/auth/userlist")
                    || url.equals("/auth/deleteuser"))) {
                return unauthorizedResponse(exchange, HttpStatus.FORBIDDEN, "无管理员权限");
            }
            // 设置用户信息到请求头
            addHeader(mutate, "uid", userId);
            addHeader(mutate, "role", role);
            // 内部请求来源参数清除
            removeHeader(mutate, "from-source");
            // 构建新的请求
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        }catch (TokenExpiredException e)
        {
            return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "token过期");
        }catch (JWTVerificationException e) {
            return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "token验证失败");
        } catch (Exception e) {
            return unauthorizedResponse(exchange, HttpStatus.UNAUTHORIZED, "无效token");
        }

    }

    @Override
    public int getOrder() {
        return -100;
    }

    public boolean matches(String str, List<String> strs)
    {
        if (str.isEmpty() || strs.isEmpty())
        {
            return false;
        }
        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : strs)
        {
            if (matcher.match(pattern, str))
            {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, HttpStatus status, String errorMessage) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(errorMessage.getBytes())));
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value)
    {
        if (value == null)
        {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = null;
        try {
            valueEncode = URLEncoder.encode(valueStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        mutate.header(name, valueEncode);

    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name)
    {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

}
