package customerService.service.impl;

import customerService.mapper.CartMapper;
import customerService.service.CartService;
import model.Cart;
import model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartMapper cartMapper;

    @Override
    public void addToCart(Integer uid, Integer goodId, Integer number) {
        Cart duplicateCart = cartMapper.cartDuplicate(uid, goodId);
        if (duplicateCart != null) {
            cartMapper.updateNumberById(duplicateCart.getId(),number+duplicateCart.getNumber());
            return ;
        }
        Goods goods = cartMapper.getGoodDetail(goodId);
        Cart cart = new Cart();
        cart.setGname(goods.getGname());
        cart.setNumber(number);
        cart.setPrice(goods.getGprice());
        cart.setGid(goodId);
        cart.setGpic(goods.getGpic());
        cart.setUid(uid);
        cartMapper.insertCart(cart);
    }

    @Override
    public List<Cart> getCartsByUid(Integer uid) {
        return cartMapper.findAllCartByUid(uid);
    }
}
