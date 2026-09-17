<template>
  <div class="orders-section">
    <div class="section-head">
      <h2 class="head-title">我的订单</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="orders.length" class="order-list">
      <div class="order-card" v-for="o in orders" :key="o.id">
        <!-- 订单头 -->
        <div class="order-top">
          <div class="order-meta">
            <span class="order-id">订单号：{{ o.id }}</span>
            <span class="order-time">{{ formatTime(o.createdTime) }}</span>
          </div>
          <span class="order-badge" :class="badgeClass(o.status)">{{ statusLabel(o.status) }}</span>
        </div>

        <!-- 订单信息 -->
        <div class="order-body">
          <div class="order-info-grid">
            <div class="info-item">
              <span class="info-key">收货地址</span>
              <span class="info-val">{{ o.address || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">订单金额</span>
              <span class="info-val price-main">¥{{ formatMoney(o.totalPrice) }}</span>
            </div>
            <div class="info-item" v-if="o.discountAmount">
              <span class="info-key">优惠金额</span>
              <span class="info-val discount">-¥{{ formatMoney(o.discountAmount) }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="order-actions">
            <router-link class="btn-outline btn-sm" :to="`/center/orderdetail/${o.id}`">查看详情</router-link>
            <button v-if="canPay(o.status)" class="btn-primary btn-sm" @click="goPay(o)">去支付</button>
            <button v-if="canCancel(o.status)" class="btn-outline-accent btn-sm" @click="cancelOrder(o.id)">取消</button>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="order-items" v-if="o.orderItems && o.orderItems.length">
          <div class="item-row" v-for="(it, idx) in o.orderItems" :key="idx">
            <span class="item-name">{{ it.gname }}</span>
            <span class="item-qty">x{{ it.number }}</span>
            <span class="item-amount">¥{{ formatMoney(it.price) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无订单</p>
      <router-link to="/search" class="btn-primary" style="margin-top: var(--s-16)">去逛逛</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orders as ordersApi, alipay } from '../api'

const router = useRouter()
const orders = ref([])
const loading = ref(true)

/** 加载订单列表 */
onMounted(async () => {
  try {
    const body = await ordersApi.list()
    orders.value = body.data || []
  } finally {
    loading.value = false
  }
})

/** 取消订单 */
async function cancelOrder(id) {
  try {
    if (!confirm('确定取消该订单吗？')) return
    await ordersApi.remove(id)
    const body = await ordersApi.list()
    orders.value = body.data || []
  } catch (e) { alert(e.message) }
}

/** 去支付 */
async function goPay(o) {
  try {
    const body = await alipay.pay({
      traceNo: String(o.id),
      totalAmount: o.totalPrice || 0,
      subject: '华为商城订单 #' + o.id
    })
    const html = typeof body === 'string' ? body : (body.data || '')
    const payWindow = window.open('', '_blank')
    payWindow.document.write(html)
    payWindow.document.close()
  } catch (e) {
    alert(e.message || '支付请求失败')
  }
}

/** 状态判断 */
function canPay(s) { const x = (s || '').toUpperCase(); return x === 'CREATED' || x === 'PAY' }
function canCancel(s) { const x = (s || '').toUpperCase(); return x === 'CREATED' }

function statusLabel(s) {
  const map = { CREATED: '待付款', PAY: '待付款', PAID: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', DONE: '已完成', CANCELLED: '已取消', CANCELED: '已取消' }
  return map[(s || '').toUpperCase()] || s || ''
}

function badgeClass(s) {
  const map = { CREATED: 'badge-creating', PAY: 'badge-creating', PAID: 'badge-paid', SHIPPED: 'badge-shipped', COMPLETED: 'badge-done', DONE: 'badge-done', CANCELLED: 'badge-cancel', CANCELED: 'badge-cancel' }
  return map[(s || '').toUpperCase()] || ''
}

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString()
}
</script>

<style scoped>
.orders-section { margin: 0; }

/* 头部 */
.section-head {
  margin-bottom: var(--s-20);
  padding-bottom: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.head-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
}

/* 订单列表 */
.order-list {
  display: grid;
  gap: var(--s-16);
}

.order-card {
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  overflow: hidden;
  background: var(--c-ground);
  transition: box-shadow var(--dur-fast) ease;
}
.order-card:hover { box-shadow: var(--shadow-md); }

/* 订单头 */
.order-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--s-12) var(--s-20);
  background: var(--c-soft-alt);
  border-bottom: 1px solid var(--c-hairline);
}
.order-meta {
  display: flex;
  align-items: center;
  gap: var(--s-16);
}
.order-id { font-size: 13px; color: var(--c-muted); }
.order-time { font-size: 12px; color: var(--c-muted-light); }

/* badge */
.order-badge {
  padding: 3px 12px;
  border-radius: var(--r-full);
  font-size: 12px;
  font-weight: 500;
}
.badge-creating { background: var(--c-warning-bg); color: var(--c-warning); }
.badge-paid { background: var(--c-success-bg); color: var(--c-success); }
.badge-shipped { background: #E8F0FE; color: #1967D2; }
.badge-done { background: #F3E8FD; color: #9334E6; }
.badge-cancel { background: var(--c-danger-bg); color: var(--c-danger); }

/* 订单主体 */
.order-body {
  padding: var(--s-20);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--s-24);
  flex-wrap: wrap;
}
.order-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--s-12) var(--s-24);
  flex: 1;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}
.info-key { font-size: 12px; color: var(--c-muted); }
.info-val { font-size: 14px; color: var(--c-ink); font-weight: 500; }
.info-val.price-main { font-size: 18px; font-weight: 700; color: var(--c-accent); }
.info-val.discount { color: var(--c-success); font-weight: 600; }

/* 操作按钮 */
.order-actions {
  display: flex;
  gap: var(--s-8);
  flex-shrink: 0;
  flex-wrap: wrap;
}
.btn-sm {
  height: 36px;
  font-size: 13px;
  padding: 0 var(--s-16);
}

/* 商品列表 */
.order-items {
  padding: var(--s-16) var(--s-20);
  border-top: 1px solid var(--c-hairline);
  background: var(--c-soft-alt);
}
.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--s-6) 0;
  font-size: 13px;
}
.item-name { color: var(--c-ink); font-weight: 500; flex: 1; }
.item-qty { color: var(--c-muted); margin: 0 var(--s-16); }
.item-amount { color: var(--c-accent); font-weight: 600; }

/* 加载/空 */
.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }

@media (max-width: 768px) {
  .order-body { flex-direction: column; }
  .order-info-grid { grid-template-columns: 1fr; }
}
</style>
