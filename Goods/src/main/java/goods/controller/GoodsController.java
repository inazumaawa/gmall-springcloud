package goods.controller;

import goods.service.GoodsService;
import model.Goods;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RefreshScope
@RequestMapping("/goods")
public class GoodsController {
    @Value("${nginx-config.address}")
    String nginxAddress;
    @Value("${nginx-config.port}")
    String port;
    @Autowired
    private GoodsService goodsService;
    //商品首页列表
    @GetMapping("/list")
    public Result goodsList() {
        List<Goods> goods = goodsService.goodsList();
        //拼接nginx路径
        for (Goods good : goods) {
            String picUrl = good.getGpic();
            good.setGpic(nginxAddress + ":" + port + picUrl);
        }
        return Result.success(goods);
    }
    //商品详情
    @GetMapping("/detail")
    public Result goodsDetail(@RequestParam("id") int id) {
        Goods goods = goodsService.goodsDetail(id);
        goods.setGpic(nginxAddress + ":" + port + goods.getGpic());
        return Result.success(goods);
    }
    //商品搜索
    @GetMapping("/search")
    public Result goodsSearch(@RequestParam("keyword") String keyword) {
        List<Goods> goods = goodsService.goodSearch(keyword);
        for (Goods good : goods) {
            String picUrl = good.getGpic();
            good.setGpic(nginxAddress + ":" + port + picUrl);
        }
        return Result.success(goods);
    }
}
