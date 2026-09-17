package customerService.service;

import model.Cart;

import java.util.List;

public interface CartService {
    void addToCart(Integer uid, Integer goodId, Integer number);
    List<Cart> getCartsByUid(Integer uid);
}
