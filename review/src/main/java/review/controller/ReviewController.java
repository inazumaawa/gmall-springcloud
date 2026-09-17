package review.controller;

import model.Result;
import model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import review.service.ReviewService;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    //新增评价
    @PostMapping("/add")
    public Result addReview(@RequestHeader("uid") Integer uid, @RequestBody Review review) {
        review.setUid(uid);
        return reviewService.addReview(review);
    }

    //删除评价
    @DeleteMapping("/delete")
    public Result deleteReview(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        return reviewService.deleteReview(uid, id);
    }

    //获取商品评价列表
    @GetMapping("/goods")
    public Result listByGoods(@RequestParam Integer gid) {
        return reviewService.listByGoods(gid);
    }

    //获取用户评价列表
    @GetMapping("/user")
    public Result listByUser(@RequestHeader("uid") Integer uid) {
        return reviewService.listByUser(uid);
    }
}
