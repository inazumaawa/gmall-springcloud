package review.service;

import model.Result;
import model.Review;

/**
 * 评价服务接口
 */
public interface ReviewService {
    //新增评价
    Result addReview(Review review);
    //删除评价
    Result deleteReview(Integer uid, Integer id);
    //根据商品获取评价列表
    Result listByGoods(Integer gid);
    //根据用户获取评价列表
    Result listByUser(Integer uid);
}
