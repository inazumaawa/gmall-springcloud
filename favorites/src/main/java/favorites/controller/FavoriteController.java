package favorites.controller;

import favorites.service.FavoriteService;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    //添加收藏
    @PostMapping("/add")
    public Result addFavorite(@RequestHeader("uid") Integer uid, @RequestParam Integer gid) {
        return favoriteService.addFavorite(uid, gid);
    }

    //取消收藏(通过商品id)
    @DeleteMapping("/delete")
    public Result deleteFavorite(@RequestHeader("uid") Integer uid, @RequestParam Integer gid) {
        return favoriteService.deleteFavorite(uid, gid);
    }

    //取消收藏(通过收藏id)
    @DeleteMapping("/deleteById")
    public Result deleteFavoriteById(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        return favoriteService.deleteFavoriteById(uid, id);
    }

    //检查是否已收藏
    @GetMapping("/check")
    public Result checkFavorite(@RequestHeader("uid") Integer uid, @RequestParam Integer gid) {
        return favoriteService.checkFavorite(uid, gid);
    }

    //获取收藏列表
    @GetMapping("/list")
    public Result listFavorites(@RequestHeader("uid") Integer uid) {
        return favoriteService.listFavorites(uid);
    }
}
