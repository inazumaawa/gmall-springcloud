<template>
  <div class="order-detail" v-if="detail">
    <!-- 标题 -->
    <div class="section-head">
      <h2 class="head-title">订单详情 #{{ detail.id }}</h2>
    </div>

    <!-- 状态摘要栏 -->
    <div class="status-bar">
      <div class="status-item">
        <span class="status-key">状态</span>
        <span class="order-badge" :class="badgeClass(detail.status)">{{ statusLabel(detail.status) }}</span>
      </div>
      <div class="status-divider"></div>
      <div class="status-item">
        <span class="status-key">金额</span>
        <span class="status-price">¥{{ formatMoney(detail.totalPrice) }}</span>
      </div>
      <div class="status-divider" v-if="detail.discountAmount"></div>
      <div class="status-item" v-if="detail.discountAmount">
        <span class="status-key">优惠</span>
        <span class="status-discount">-¥{{ formatMoney(detail.discountAmount) }}</span>
      </div>
      <div class="status-spacer"></div>
      <button v-if="canPay(detail.status)" class="btn-primary" @click="goPay">去支付</button>
      <button v-if="canCancel(detail.status)" class="btn-outline-accent" @click="cancelOrder">取消订单</button>
      <button class="btn-outline" @click="deleteOrder">删除</button>
    </div>

    <!-- 物流轨迹 -->
    <div class="block">
      <h3 class="block-title">物流轨迹</h3>
      <div v-if="tracks.length" class="track-list">
        <div v-for="(t, i) in tracks" :key="t.id" class="track-item">
          <div class="track-dot" :class="{ first: i === 0 }"></div>
          <div class="track-info">
            <div class="track-loc">{{ t.status }} · {{ t.location }}</div>
            <div class="track-desc">{{ t.description }}</div>
            <div class="track-time">{{ t.trackTime }}</div>
          </div>
        </div>
      </div>
      <p v-else class="empty-text">暂无物流信息</p>
    </div>

    <!-- 收货地址 -->
    <div class="block" v-if="addresses.length">
      <h3 class="block-title">收货地址</h3>
      <select v-model="selectedAddressId" class="form-select" @change="saveSelectedAddress">
        <option v-for="a in addresses" :key="a.id" :value="a.id">{{ addrLabel(a) }}</option>
      </select>
    </div>

    <!-- 商品列表 -->
    <div class="block">
      <h3 class="block-title">商品清单</h3>
      <div v-if="items.length" class="items-table">
        <div class="table-row table-head">
          <span class="col-img"></span>
          <span class="col-name">商品</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-sub">小计</span>
        </div>
        <div class="table-row" v-for="it in items" :key="it.id">
          <span class="col-img">
            <img :src="fixImageUrl(it.gpic) || '/images/logo_app.png'" alt="" class="item-thumb" @error="onImgErr" />
          </span>
          <span class="col-name">{{ it.gname }}</span>
          <span class="col-price">¥{{ formatMoney(it.price) }}</span>
          <span class="col-qty">{{ it.quantity }}</span>
          <span class="col-sub">¥{{ formatMoney(it.price * it.quantity) }}</span>
        </div>
      </div>
      <p v-else class="empty-text">暂无商品数据</p>
    </div>
  </div>

  <div v-else class="loading-block">
    <div class="spinner"></div>
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orders as ordersApi, addresses as addrApi, alipay, logistics, fixImageUrl } from '../api'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(true)
const addresses = ref([])
const selectedAddressId = ref('')
const items = ref([])
const tracks = ref([])

/** 加载订单详情 */
onMounted(async () => {
  const id = route.params.id
  try {
    const body = await ordersApi.list()
    const orders = body.data || []
    detail.value = orders.find(o => String(o.id) === String(id)) || null
    if (detail.value) { items.value = detail.value.orderItems || [] }
    await loadAddresses()
    await loadTracks()
  } finally { loading.value = false }
})

async function loadTracks() {
  if (!detail.value) return
  try {
    const b = await logistics.track(detail.value.id)
    tracks.value = b.data || []
  } catch {}
}

async function loadAddresses() {
  try {
    const b = await addrApi.list()
    addresses.value = b.data || []
    selectedAddressId.value = detail.value.aid
  } catch {}
}

async function saveSelectedAddress() {
  try {
    await ordersApi.updateAddress(detail.value.id, selectedAddressId.value)
  } catch (e) { alert('修改地址失败: ' + (e.message || '未知错误')) }
}

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function canPay(s) { const x = (s || '').toUpperCase(); return x === 'CREATED' || x === 'PAY' }
function canCancel(s) { const x = (s || '').toUpperCase(); return x === 'CREATED' }

async function cancelOrder() {
  try {
    if (!confirm('确定取消该订单吗？')) return
    await ordersApi.remove(detail.value.id)
    alert('订单已取消')
    const body = await ordersApi.list()
    const orders = body.data || []
    detail.value = orders.find(o => String(o.id) === String(route.params.id)) || null
  } catch (e) { alert(e.message) }
}

async function deleteOrder() {
  try {
    if (!confirm('确定删除该订单吗？此操作不可恢复')) return
    await ordersApi.remove(detail.value.id)
    alert('订单已删除')
    router.push('/center/myorder')
  } catch (e) { alert(e.message) }
}

function statusLabel(s) {
  const map = { CREATED: '待付款', PAY: '待付款', PAID: '待发货', SHIPPED: '已发货', COMPLETED: '已完成', DONE: '已完成', CANCELLED: '已取消', CANCELED: '已取消' }
  return map[(s || '').toUpperCase()] || s || ''
}

function badgeClass(s) {
  const map = { CREATED: 'badge-creating', PAY: 'badge-creating', PAID: 'badge-paid', SHIPPED: 'badge-shipped', COMPLETED: 'badge-done', DONE: 'badge-done', CANCELLED: 'badge-cancel', CANCELED: 'badge-cancel' }
  return map[(s || '').toUpperCase()] || ''
}

function addrLabel(a) {
  return [a.receiverName, a.phone, a.province, a.city, a.district, a.detail].filter(Boolean).join(' ')
}

function onImgErr(e) {
  const el = e.target
  const src = String(el.src || '')
  const base = src.replace(/\.[^./?]+$/, '')
  const alts = [base + '.png', base + '.jpg', base + '.webp', '/images/logo_app.png']
  const tried = Number(el.dataset.altTry || 0)
  const next = alts[Math.min(tried, alts.length - 1)]
  el.dataset.altTry = String(tried + 1)
  if (src !== next) el.src = next
}

async function goPay() {
  if (!detail.value) return
  try {
    const payAmount = Math.max(0, (detail.value.totalPrice || 0) - (detail.value.discountAmount || 0))
    const body = await alipay.pay({
      traceNo: String(detail.value.id),
      totalAmount: payAmount,
      subject: '华为商城订单 #' + detail.value.id
    })
    const html = typeof body === 'string' ? body : (body.data || '')
    const payWindow = window.open('', '_blank')
    payWindow.document.write(html)
    payWindow.document.close()
  } catch (e) { alert(e.message || '支付请求失败') }
}
</script>

<style scoped>
.order-detail { margin: 0; }

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

/* 状态栏 */
.status-bar {
  display: flex;
  align-items: center;
  gap: var(--s-16);
  padding: var(--s-16) var(--s-20);
  background: var(--c-soft-alt);
  border-radius: var(--r-lg);
  margin-bottom: var(--s-20);
  flex-wrap: wrap;
}
.status-item {
  display: flex;
  align-items: center;
  gap: var(--s-8);
}
.status-key { font-size: 13px; color: var(--c-muted); }
.status-price { font-size: 20px; font-weight: 700; color: var(--c-accent); }
.status-discount { font-size: 14px; color: var(--c-success); font-weight: 600; }
.status-divider {
  width: 1px; height: 24px;
  background: var(--c-hairline);
}
.status-spacer { flex: 1; }

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

/* 区块 */
.block {
  margin-bottom: var(--s-20);
}
.block-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  margin: 0 0 var(--s-12);
}
.form-select {
  width: 100%;
  max-width: 480px;
  height: 40px;
  padding: 0 var(--s-12);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-sm);
  font-size: 13px;
  color: var(--c-ink);
  outline: none;
  background: var(--c-ground);
  transition: border-color var(--dur-fast) ease;
}
.form-select:focus { border-color: var(--c-accent); }

/* 商品表格 */
.items-table {
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  overflow: hidden;
}
.table-row {
  display: grid;
  grid-template-columns: 72px 1fr 100px 80px 100px;
  align-items: center;
  padding: var(--s-14) var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
  font-size: 13px;
}
.table-row:last-child { border-bottom: none; }
.table-head {
  background: var(--c-soft-alt);
  font-size: 12px;
  color: var(--c-muted);
  font-weight: 500;
}
.item-thumb {
  width: 52px; height: 52px;
  object-fit: cover;
  border-radius: var(--r-md);
  background: var(--c-soft);
}
.col-sub { color: var(--c-accent); font-weight: 600; }

.empty-text { color: var(--c-muted); font-size: 14px; text-align: center; padding: var(--s-24) 0; }

/* 物流轨迹 */
.track-list { padding: var(--s-4) 0; }
.track-item {
  display: flex;
  gap: var(--s-12);
  padding: var(--s-8) 0;
  position: relative;
}
.track-dot {
  width: 10px; height: 10px;
  margin-top: 4px;
  border-radius: 50%;
  background: var(--c-muted);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}
.track-dot.first { background: var(--c-accent); }
.track-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 4px; top: 18px;
  width: 2px; height: calc(100% - 8px);
  background: var(--c-hairline);
}
.track-info { display: flex; flex-direction: column; gap: 2px; }
.track-loc { font-size: 13px; font-weight: 600; color: var(--c-ink); }
.track-desc { font-size: 13px; color: var(--c-ink); }
.track-time { font-size: 12px; color: var(--c-muted); }

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
