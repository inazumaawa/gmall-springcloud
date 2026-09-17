package favorites.service.impl;

import favorites.mapper.FavoriteMapper;
import favorites.service.FavoriteService;
import model.Favorite;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏服务实现类
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Result addFavorite(Integer uid, Integer gid) {
        Favorite exist = favoriteMapper.checkFavorite(uid, gid);
        if (exist != null) {
            return Result.failure(ResultCodeEnum.FAIL, "已收藏该商品");
        }
        Favorite favorite = new Favorite();
        favorite.setUid(uid);
        favorite.setGid(gid);
        favoriteMapper.insertFavorite(favorite);
        return Result.success("收藏成功");
    }

    @Override
    public Result deleteFavorite(Integer uid, Integer gid) {
        Favorite exist = favoriteMapper.checkFavorite(uid, gid);
        if (exist == null) {
            return Result.failure(ResultCodeEnum.FAIL, "未收藏该商品");
        }
        favoriteMapper.deleteFavorite(uid, gid);
        return Result.success("取消收藏成功");
    }

    @Override
    public Result deleteFavoriteById(Integer uid, Integer id) {
        // 归属校验：确保收藏属于当前用户
        Favorite favorite = favoriteMapper.findById(id);
        if (favorite == null || !favorite.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "收藏不存在或无权操作");
        }
        favoriteMapper.deleteFavoriteById(id);
        return Result.success("取消收藏成功");
    }

    @Override
    public Result checkFavorite(Integer uid, Integer gid) {
        Favorite favorite = favoriteMapper.checkFavorite(uid, gid);
        return Result.success(favorite != null);
    }

    @Override
    public Result listFavorites(Integer uid) {
        List<Favorite> favorites = favoriteMapper.findByUid(uid);
        return Result.success(favorites);
    }
}
