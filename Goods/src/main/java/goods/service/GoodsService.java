package goods.service;

import model.Goods;

import java.util.List;

public interface GoodsService {
    //商品列表接口
    List<Goods> goodsList();
    //商品详情接口
    Goods goodsDetail(int id);
    //商品搜索接口
    List<Goods> goodSearch(String keyword);
}
