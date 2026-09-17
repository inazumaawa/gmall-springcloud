package pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.easysdk.factory.Factory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AliPay;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pay.config.AliPayConfig;
import pay.mapper.OrderMapper;
import pay.service.CouponServiceFeignClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("alipay")
@Transactional(rollbackFor = Exception.class)
public class AliPayController {
    @Resource
    AliPayConfig aliPayConfig;

    @Resource
    OrderMapper orderMapper;

    @Resource
    CouponServiceFeignClient couponServiceFeignClient;

    private static final String GATEWAY_URL ="https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT ="JSON";
    private static final String CHARSET ="utf-8";
    private static final String SIGN_TYPE ="RSA2";
    @PostMapping("/pay") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public Result pay(AliPay aliPay, HttpServletResponse httpResponse) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setBizContent("{\"out_trade_no\":\"" + aliPay.getTraceNo() + "\","
                + "\"total_amount\":\"" + aliPay.getTotalAmount() + "\","
                + "\"subject\":\"" + aliPay.getSubject() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String form = "";
        try {
            // 调用SDK生成表单
            form = alipayClient.pageExecute(request).getBody();
            return Result.success(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return Result.failure(ResultCodeEnum.FAIL,"未知错误");
        }
    }
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        // 支付宝异步回调判断是否成功
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String tradeNo = params.get("out_trade_no");
            String gmtPayment = params.get("gmt_payment");
            String alipayTradeNo = params.get("trade_no");
            // 支付宝验签
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
                // 更新订单为已支付
                int oid = Integer.parseInt(tradeNo);
                orderMapper.updateByPrimaryKey(oid);
                // 如果订单使用了优惠券，支付成功后核销
                Integer couponId = orderMapper.findCouponIdById(oid);
                if (couponId != null) {
                    Integer uid = orderMapper.findUidById(oid);
                    Map<String, Object> couponReq = new HashMap<>();
                    couponReq.put("ucId", couponId);
                    couponReq.put("orderAmount", new BigDecimal(params.get("total_amount")));
                    couponServiceFeignClient.useCoupon(uid, couponReq);
                }
            }
        }
        return "success";
    }
}