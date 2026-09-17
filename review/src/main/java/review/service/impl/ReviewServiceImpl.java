package review.service.impl;

import model.Result;
import model.ResultCodeEnum;
import model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import review.mapper.ReviewMapper;
import review.service.ReviewService;

import java.util.List;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Result addReview(Review review) {
        reviewMapper.insertReview(review);
        return Result.success("评价成功");
    }

    @Override
    public Result deleteReview(Integer uid, Integer id) {
        // 归属校验：确保评价属于当前用户
        Review review = reviewMapper.findById(id);
        if (review == null || !review.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "评价不存在或无权操作");
        }
        reviewMapper.deleteReview(id);
        return Result.success("删除评价成功");
    }

    @Override
    public Result listByGoods(Integer gid) {
        List<Review> reviews = reviewMapper.findByGid(gid);
        return Result.success(reviews);
    }

    @Override
    public Result listByUser(Integer uid) {
        List<Review> reviews = reviewMapper.findByUid(uid);
        return Result.success(reviews);
    }
}
