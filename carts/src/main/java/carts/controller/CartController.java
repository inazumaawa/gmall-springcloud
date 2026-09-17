package carts.controller;

import carts.service.CartService;
import model.Cart;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    //添加购物车
    @PostMapping("/add")
    public Result addToCart(@RequestHeader("uid") Integer uid, @RequestParam Integer gid, @RequestParam Integer number) {
        return cartService.addToCart(uid, gid, number);
    }
    //移出购物车
    @DeleteMapping("/delete")
    public Result deleteFromCart(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        cartService.deleteFromCart(uid, id);
        return Result.success("成功删除购物车");
    }
    //购物车列表
    @GetMapping("/list")
    public Result getCartList(@RequestHeader("uid") Integer uid) {
        List<Cart> cartList = cartService.getCartsByUid(uid);
        return Result.success(cartList);
    }
    //列表子集
    @PostMapping("/sublist")
    public Result subCartlist(@RequestHeader("uid") Integer uid, @RequestBody List<Integer> cartIds) {
        List<Cart> cartList = cartService.getCartsByIds(uid, cartIds);
        return Result.success(cartList);
    }
    //删除子集列表
    @RequestMapping("/deletelist")
    public Result deleteCartList(@RequestHeader("uid") Integer uid, @RequestBody List<Integer> cartIds) {
        cartService.deleteCartByUidandCartIds(uid, cartIds);
        return Result.success();
    }
    //更新数量
    @RequestMapping("/update")
    public Result updateNumber(@RequestHeader("uid") Integer uid, @RequestParam Integer id, @RequestParam Integer number) {
        cartService.updateNumber(uid, id, number);
        return Result.success();
    }
}
