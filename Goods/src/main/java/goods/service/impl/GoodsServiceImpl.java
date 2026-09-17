package goods.service.impl;

import goods.mapper.GoodsMapper;
import goods.service.GoodsService;
import model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsMapper goodsMapper;

    @Override
    @Cacheable(value = "goodsList", unless = "#result == null || #result.size() == 0")
    public List<Goods> goodsList() {
        return goodsMapper.goodsList();
    }

    @Override
    @Cacheable(value = "goodsDetail", key = "#id", unless = "#result == null")
    public Goods goodsDetail(int id) {
        return goodsMapper.goodsDetail(id);
    }

    @Override
    public List<Goods> goodSearch(String keyword) {
        return goodsMapper.goodsSerach(keyword);
    }
}