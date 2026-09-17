package model;

import lombok.Data;

@Data
public class AliPay {
    private String traceNo;         // 商户订单号（对应 order.id）
    private double totalAmount;     // 支付金额（单位：元）
    private String subject;         // 商品标题（显示在支付宝收银台）
    private String alipayTraceNo;   // 支付宝交易流水号（回调时回填）
}
