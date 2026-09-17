package customerService.tools;

import customerService.mapper.CartMapper;
import customerService.service.CartService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import model.Cart;
import model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartTool {
    @Autowired
    private CartService cartService;
    @Autowired
    private CartMapper cartMapper;

    /**
     * 添加购物车（传入商品名称即可，后端自动查ID）
     */
    @Tool("添加购物车")
    public String addCart(@ToolMemoryId String userId,
            @P("商品名称") String goodsName,
            @P("数量") Integer num) {
        if (userId == null || userId.isEmpty()) {
            return "添加失败：用户未登录，请先登录后再操作";
        }
        if (goodsName == null || goodsName.isBlank()) {
            return "添加失败：请提供商品名称";
        }
        Goods goods = cartMapper.findGoodsByName(goodsName.trim());
        if (goods == null) {
            return "添加失败：未找到名称为 '" + goodsName + "' 的商品，请确认商品名称是否正确";
        }
        int qty = num != null ? num : 1;
        cartService.addToCart(Integer.valueOf(userId), goods.getGid(), qty);
        return "已成功将 " + goods.getGname() + "（商品ID:" + goods.getGid() + "，数量:" + qty + "）加入购物车";
    }
    /**
     * 查询购物车
     */
    @Tool("查询购物车")
    public String selectCart(@ToolMemoryId String userId)
    {
        if (userId == null || userId.isEmpty()) {
            return "查询失败：用户未登录，请先登录后再操作";
        }
        List<Cart> carts = cartService.getCartsByUid(Integer.valueOf(userId));
        if (carts.isEmpty()) {
            return "您的购物车是空的";
        }
        StringBuilder sb = new StringBuilder("您的购物车中有以下商品：\n");
        for (Cart c : carts) {
            sb.append("- 商品ID: ").append(c.getGid())
              .append(", 数量: ").append(c.getNumber()).append("\n");
        }
        return sb.toString();
    }
}
