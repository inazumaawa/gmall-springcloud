<template>
    <footer class="footer-wrap">
    <div class="service-bar w">
        <div class="svc-item"><img class="icon-img" src="/images/Footer1.png" alt="品质保证"><span class="text">百强企业 品质保证</span></div>
        <div class="svc-item"><img class="icon-img" src="/images/Footer2.png" alt="退换货"><span class="text">7天退换 15天换货</span></div>
        <div class="svc-item"><img class="icon-img" src="/images/Footer3.png" alt="免运费"><span class="text">48元起免运费</span></div>
        <div class="svc-item"><img class="icon-img" src="/images/Footer4.png" alt="全国联保"><span class="text">官方售后 全国联保</span></div>
    </div>
    <div class="foot-link w">
        <dl>
            <dt>购物指南</dt>
            <dd><a href="#">购前咨询</a></dd>
            <dd><a href="#">以旧换新</a></dd>
            <dd><a href="#">分期购</a></dd>
            <dd><a href="#">企业购</a></dd>
            <dd><a href="#">O2O门店</a></dd>
            <dd><a href="#">教育采购</a></dd>
        </dl>
        <dl>
            <dt>售后服务</dt>
            <dd><a href="#">门店服务</a></dd>
            <dd><a href="#">保修政策</a></dd>
            <dd><a href="#">售后查询</a></dd>
            <dd><a href="#">订单查询</a></dd>
            <dd><a href="#">手机寄修</a></dd>
        </dl>
        <dl>
            <dt>服务支持</dt>
            <dd><a href="#">服务公告</a></dd>
            <dd><a href="#">配件价格</a></dd>
            <dd><a href="#">增值服务</a></dd>
            <dd><a href="#">上门服务</a></dd>
            <dd><a href="#">问题反馈</a></dd>
        </dl>
        <dl>
            <dt>特色服务</dt>
            <dd><a href="#">补贴活动</a></dd>
            <dd><a href="#">以旧换新</a></dd>
        </dl>
        <dl>
            <dt>关于我们</dt>
            <dd><a href="#">公司介绍</a></dd>
            <dd><a href="#">商家中心</a></dd>
            <dd><a href="#">采购项目</a></dd>
            <dd><a href="#">规则中心</a></dd>
        </dl>
        <dl>
            <dt>友情链接</dt>
            <dd><a href="#">华为集团</a></dd>
            <dd><a href="#">华为消费者业务</a></dd>
            <dd><a href="#">华为云</a></dd>
            <dd><a href="#">开发者联盟</a></dd>
        </dl>
        <div class="col-contact">
            <p class="phone">950805</p>
            <p>7x24小时客服热线(仅收市话费)</p>
            <a href="#" class="btn-contact">在线客服</a>
        <div class="follow">
                <span class="label">关注VMALL：</span>
                <a class="sns-link" href="https://weibo.com" target="_blank" rel="noopener"><img class="sns-icon" src="/images/wb.png" alt="微博"></a>
                <a class="sns-link" href="https://weixin.qq.com/" target="_blank" rel="noopener"><img class="sns-icon" src="/images/wx.png" alt="微信"></a>
            </div>
        </div>
    </div> 
    </footer>
</template>

<script>
export default {

}
</script>

<style scoped>
/* ===== Footer ===== */
.footer-wrap {
  background: var(--c-soft);
  padding-top: var(--s-40);
}

/* 服务条 */
.service-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-16);
  padding: var(--s-24) 0;
  border-top: 1px solid var(--c-hairline);
  border-bottom: 1px solid var(--c-hairline);
}
.service-bar .svc-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--s-12);
  font-size: 14px;
  color: var(--c-ink);
}
.service-bar .icon-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
  opacity: 0.6;
}
.service-bar .text {
  font-weight: 500;
}

/* 链接区 */
.foot-link {
  display: flex;
  align-items: flex-start;
  gap: var(--s-32);
  padding: var(--s-40) 0 var(--s-40);
}

.foot-link dl { width: 160px; }
.foot-link dl dt {
  margin-bottom: var(--s-16);
  font-size: 14px;
  font-weight: 600;
  color: var(--c-ink);
}
.foot-link dl dd { margin: var(--s-8) 0 0; }
.foot-link dl dd a {
  font-size: 13px;
  color: var(--c-muted);
  transition: color var(--dur-fast) var(--ease-out);
}
.foot-link dl dd a:hover { color: var(--c-accent); }

/* 联系栏 */
.foot-link .col-contact {
  width: 280px;
  border-left: 1px solid var(--c-hairline);
  text-align: center;
  color: var(--c-muted);
  padding-left: var(--s-32);
  margin-left: auto;
}
.foot-link .col-contact p {
  margin-bottom: var(--s-4);
  font-size: 13px;
}
.foot-link .col-contact .phone {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  letter-spacing: -0.02em;
  margin-bottom: var(--s-4);
}
.foot-link .col-contact .btn-contact {
  display: inline-block;
  width: 130px;
  height: 34px;
  margin: var(--s-12) auto;
  border: 1px solid var(--c-ink);
  font-size: 13px;
  line-height: 34px;
  color: var(--c-ink);
  border-radius: 17px;
  transition: all var(--dur-fast) var(--ease-out);
}
.foot-link .col-contact .btn-contact:hover {
  background: var(--c-ink);
  color: #fff;
}

/* 关注 */
.foot-link .col-contact .follow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--s-8);
  margin-top: var(--s-16);
  font-size: 13px;
}
.foot-link .col-contact .follow .label { color: var(--c-muted); }
.foot-link .col-contact .follow .sns-icon {
  width: 24px; height: 24px;
  display: block;
  opacity: 0.6;
  transition: opacity var(--dur-fast) var(--ease-out);
}
.foot-link .col-contact .follow .sns-icon:hover { opacity: 1; }

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .service-bar { grid-template-columns: repeat(2, 1fr); }
  .foot-link { flex-wrap: wrap; }
  .foot-link dl { width: calc(50% - 16px); }
  .foot-link .col-contact { width: 100%; border-left: none; padding-left: 0; border-top: 1px solid var(--c-hairline); padding-top: var(--s-24); margin-left: 0; }
}
</style>
