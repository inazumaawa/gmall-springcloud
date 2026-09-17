<template>
  <div class="admin-section">
    <div class="section-head">
      <h2 class="head-title">数据统计</h2>
    </div>

    <div v-if="loading" class="loading-block"><div class="spinner"></div><p>加载中...</p></div>

    <div v-else>
      <!-- 概览卡片 -->
      <div class="cards">
        <div class="card">
          <span class="card-label">总订单数</span>
          <span class="card-val">{{ overview.orderCount || 0 }}</span>
        </div>
        <div class="card">
          <span class="card-label">总销售额(元)</span>
          <span class="card-val">{{ formatMoney(overview.totalSales) }}</span>
        </div>
        <div class="card">
          <span class="card-label">商品种类</span>
          <span class="card-val">{{ topGoods.length }}</span>
        </div>
      </div>

      <!-- 近7天销售趋势 -->
      <div class="chart-block">
        <h3 class="chart-title">近7天销售趋势</h3>
        <div class="bar-list">
          <div v-for="(t, i) in orderTrend" :key="i" class="bar-row">
            <span class="bar-name">{{ t.day }}</span>
            <span class="bar-track"><span class="bar-fill" :style="{ width: trendWidth(t.sales) }"></span></span>
            <span class="bar-val">¥{{ formatMoney(t.sales) }}</span>
          </div>
          <p v-if="!orderTrend.length" class="empty">暂无数据</p>
        </div>
      </div>

      <!-- 商品销量 TOP10 -->
      <div class="chart-block">
        <h3 class="chart-title">商品销量 TOP10</h3>
        <div class="bar-list">
          <div v-for="(g, i) in topGoods" :key="i" class="bar-row">
            <span class="bar-name">{{ g.name }}</span>
            <span class="bar-track"><span class="bar-fill" :style="{ width: goodsWidth(g.sales) }"></span></span>
            <span class="bar-val">{{ g.sales }}</span>
          </div>
          <p v-if="!topGoods.length" class="empty">暂无数据</p>
        </div>
      </div>

      <!-- 订单状态分布 -->
      <div class="chart-block">
        <h3 class="chart-title">订单状态分布</h3>
        <div class="bar-list">
          <div v-for="(s, i) in orderStatus" :key="i" class="bar-row">
            <span class="bar-name">{{ statusLabel(s.status) }}</span>
            <span class="bar-track"><span class="bar-fill status" :style="{ width: statusWidth(s.count) }"></span></span>
            <span class="bar-val">{{ s.count }}</span>
          </div>
          <p v-if="!orderStatus.length" class="empty">暂无数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { admin } from '../api'

const loading = ref(true)
const overview = ref({})
const orderTrend = ref([])
const orderStatus = ref([])
const topGoods = ref([])

const statusMap = {
  PAY: '待付款', PAID: '待发货', SHIPPED: '已发货',
  COMPLETED: '已完成', DONE: '已完成', CANCELLED: '已取消', CANCELED: '已取消'
}

function maxVal(list, key) {
  const arr = (list || []).map(x => Number(x[key] || 0))
  return arr.length ? Math.max(...arr) : 0
}

function trendWidth(v) {
  const m = maxVal(orderTrend.value, 'sales')
  return m ? Math.round((Number(v || 0) / m) * 100) + '%' : '0%'
}
function goodsWidth(v) {
  const m = maxVal(topGoods.value, 'sales')
  return m ? Math.round((Number(v || 0) / m) * 100) + '%' : '0%'
}
function statusWidth(v) {
  const m = maxVal(orderStatus.value, 'count')
  return m ? Math.round((Number(v || 0) / m) * 100) + '%' : '0%'
}

function statusLabel(s) { return statusMap[(s || '').toUpperCase()] || s || '' }

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

onMounted(async () => {
  try {
    const r = await admin.stats()
    const d = r.data || {}
    overview.value = d.overview || {}
    orderTrend.value = d.orderTrend || []
    orderStatus.value = d.orderStatus || []
    topGoods.value = d.topGoods || []
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.admin-section { margin: 0; }
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--s-20); padding-bottom: var(--s-16); border-bottom: 1px solid var(--c-hairline); }
.head-title { font-size: 18px; font-weight: 700; color: var(--c-ink); margin: 0; }

.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--s-16); margin-bottom: var(--s-20); }
.card {
  background: var(--c-ground);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  padding: var(--s-20);
  display: flex;
  flex-direction: column;
  gap: var(--s-8);
}
.card-label { font-size: 13px; color: var(--c-muted); }
.card-val { font-size: 26px; font-weight: 700; color: var(--c-accent); }

.chart-block {
  background: var(--c-ground);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  padding: var(--s-20);
  margin-bottom: var(--s-16);
}
.chart-title { font-size: 15px; font-weight: 600; color: var(--c-ink); margin: 0 0 var(--s-16); }

.bar-list { display: flex; flex-direction: column; gap: var(--s-10); }
.bar-row { display: grid; grid-template-columns: 180px 1fr 80px; align-items: center; gap: var(--s-12); }
.bar-name { font-size: 13px; color: var(--c-ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bar-track { background: var(--c-soft-alt); border-radius: 6px; height: 14px; overflow: hidden; }
.bar-fill { display: block; height: 100%; background: linear-gradient(90deg, #ff6700, #f56c6c); border-radius: 6px; transition: width 0.3s ease; }
.bar-fill.status { background: linear-gradient(90deg, #409eff, #2b7cd9); }
.bar-val { font-size: 12px; color: var(--c-muted); text-align: right; }
.empty { color: var(--c-muted); font-size: 13px; text-align: center; padding: var(--s-12) 0; }

.loading-block { text-align: center; color: var(--c-muted); padding: var(--s-40) 0; }
.loading-block .spinner { margin: 0 auto var(--s-8); }
@keyframes spin { to { transform: rotate(360deg); } }
.spinner { width: 24px; height: 24px; border: 3px solid var(--c-hairline); border-top-color: var(--c-accent); border-radius: 50%; animation: spin .6s linear infinite; }
</style>
