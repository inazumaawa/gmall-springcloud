package carts.service.impl;

import carts.mapper.CartMapper;
import carts.service.CartService;
import carts.service.GoodsServiceFeignClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.Cart;
import model.Goods;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    GoodsServiceFeignClient goodsServiceFeignClient;
    @Autowired
    private CartMapper cartMapper;
    @Override
    public Result addToCart(Integer uid, Integer goodId, Integer number) {
        Cart duplicateCart = cartMapper.cartDuplicate(uid, goodId);
        // 检查购物车项是否存在
        if (duplicateCart != null) {
            cartMapper.updateNumberById(duplicateCart.getId(),number+duplicateCart.getNumber());
            return Result.success("成功添加购物车");
        }
        Result result = goodsServiceFeignClient.goodsDetail(goodId);
        if(result.getCode()!=200)
        {
            return result;
        }
        //返回结果的json数据转为String
        String jsonObject= JSON.toJSONString(result.getData());
        //String转为Goods对象
        Goods goods = JSONObject.parseObject(jsonObject,Goods.class);
        Cart cart = new Cart();
        cart.setGname(goods.getGname());
        cart.setNumber(number);
        cart.setPrice(goods.getGprice());
        cart.setGid(goodId);
        cart.setGpic(goods.getGpic());
        cart.setUid(uid);
        cartMapper.insertCart(cart);
        return Result.success("成功添加购物车");
    }

    @Override
    public void deleteFromCart(Integer uid, Integer id) {
        // 归属校验：确保购物车项属于当前用户
        Cart cart = cartMapper.findById(id);
        if (cart == null || !cart.getUid().equals(uid)) {
            return;
        }
        cartMapper.deleteCart(id);
    }


    @Override
    public List<Cart> getCartsByUid(Integer uid) {
        return cartMapper.findAllCartByUid(uid);
    }

    @Override
    public List<Cart> getCartsByIds(Integer uid, List<Integer> cartIds) {
        return cartMapper.findCartsByUidAndCartIds(uid, cartIds);
    }

    @Override
    public void deleteCartByUidandCartIds(Integer uid, List<Integer> cartIds) {
        cartMapper.deleteCartsByUidAndCartIds(uid, cartIds);
    }

    @Override
    public void updateNumber(Integer uid, Integer id, Integer number) {
        // 归属校验：确保购物车项属于当前用户
        Cart cart = cartMapper.findById(id);
        if (cart == null || !cart.getUid().equals(uid)) {
            return;
        }
        cartMapper.updateNumberById(id, number);
    }
}
