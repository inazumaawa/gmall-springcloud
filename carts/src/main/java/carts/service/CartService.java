package carts.service;

import model.Cart;
import model.Result;

import java.util.List;

public interface CartService {
    //加入购物车
    Result addToCart(Integer uid, Integer goodId, Integer number);
    //移出购物车
    void deleteFromCart(Integer uid, Integer id);
    //购物车列表
    List<Cart> getCartsByUid(Integer uid);
    //根据前端选中的id集合获取购物车列表
    List<Cart> getCartsByIds(Integer uid, List<Integer> cartIds);
    //根据前端选中的id集合移出购物车
    void deleteCartByUidandCartIds(Integer uid, List<Integer> cartIds);
    //更新购物车数量
    void updateNumber(Integer uid, Integer id, Integer number);
}
