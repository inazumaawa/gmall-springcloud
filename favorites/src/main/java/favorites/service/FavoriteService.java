package favorites.service;

import model.Result;

/**
 * 收藏服务接口
 */
public interface FavoriteService {
    //添加收藏
    Result addFavorite(Integer uid, Integer gid);
    //取消收藏(通过商品id)
    Result deleteFavorite(Integer uid, Integer gid);
    //取消收藏(通过收藏id)
    Result deleteFavoriteById(Integer uid, Integer id);
    //检查是否已收藏
    Result checkFavorite(Integer uid, Integer gid);
    //获取收藏列表
    Result listFavorites(Integer uid);
}
