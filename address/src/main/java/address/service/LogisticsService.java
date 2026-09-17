package address.service;

import address.mapper.LogisticsMapper;
import model.LogisticsTrack;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 物流轨迹业务逻辑
 */
@Service
public class LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;

    /** 查询订单物流轨迹 */
    public Result track(Integer orderId) {
        List<LogisticsTrack> tracks = logisticsMapper.findByOrderId(orderId);
        return Result.success(tracks);
    }

    /** 后台新增物流节点 */
    public Result add(LogisticsTrack track) {
        if (track.getOrderId() == null) {
            return Result.failure(ResultCodeEnum.FAIL, "订单ID不能为空");
        }
        if (track.getStatus() == null || track.getStatus().trim().isEmpty()) {
            return Result.failure(ResultCodeEnum.FAIL, "物流状态不能为空");
        }
        if (track.getTrackTime() == null) {
            track.setTrackTime(new Date());
        }
        logisticsMapper.insert(track);
        return Result.success("添加物流节点成功");
    }
}
