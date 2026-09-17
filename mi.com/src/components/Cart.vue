<template>
  <div class="cart-page">
    <div class="cart-container">
      <!-- 页头 -->
      <div class="cart-header">
        <div class="header-left">
          <h2 class="cart-title">购物车</h2>
          <span class="cart-count" v-if="rows.length">{{ rows.length }} 件商品</span>
        </div>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="loading-block">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 有商品 -->
      <div v-else-if="rows.length" class="cart-content">
        <!-- 商品列表 -->
        <div class="cart-list">
          <div class="cart-item" v-for="r in rows" :key="r.id">
            <!-- 商品信息 -->
            <div class="item-info">
              <img class="item-thumb" :src="r.image" alt="" @error="onImgErr" />
              <div class="item-detail">
                <router-link :to="`/detail/${r.gid}`" class="item-name">{{ r.name }}</router-link>
                <span class="item-price-mobile">¥{{ format(r.price) }}</span>
              </div>
            </div>

            <!-- 单价（桌面端） -->
            <div class="item-price">
              <span>¥{{ format(r.price) }}</span>
            </div>

            <!-- 数量 -->
            <div class="item-qty">
              <div class="qty-control">
                <button :disabled="r.quantity<=1" @click="changeQty(r.id, r.quantity-1)">-</button>
                <span>{{ r.quantity }}</span>
                <button @click="changeQty(r.id, r.quantity+1)">+</button>
              </div>
            </div>

            <!-- 小计 -->
            <div class="item-total">
              ¥{{ format(r.total) }}
            </div>

            <!-- 操作 -->
            <div class="item-action">
              <button @click="remove(r.id)" class="btn-remove" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="cart-bar">
          <div class="bar-left">
            <span class="bar-label">合计</span>
            <span class="bar-price">¥{{ format(sumTotal) }}</span>
            <span class="bar-hint">（不含运费）</span>
          </div>
          <div class="bar-coupon">
            <select v-model="selectedCouponId" class="coupon-select">
              <option :value="''">不使用优惠券</option>
              <option v-for="c in myCoupons" :key="c.id" :value="c.id">{{ couponLabel(c) }}</option>
            </select>
          </div>
          <button class="btn-primary btn-checkout" @click="goTrade">去结算</button>
        </div>
      </div>

      <!-- 空购物车 -->
      <div v-else class="empty-state">
        <div class="empty-visual">
          <svg width="72" height="72" viewBox="0 0 24 24" fill="none" stroke="var(--c-muted-light)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>
        </div>
        <p class="empty-text">购物车是空的</p>
        <router-link to="/search" class="btn-primary" style="margin-top: var(--s-16)">去逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { cart, orders, addresses, coupons, fixImageUrl } from '../api'

const router = useRouter()
const items = ref([])
const loading = ref(true)
const myCoupons = ref([])
const availCoupons = ref([])
const selectedCouponId = ref('')

/** 加载购物车 */
onMounted(async () => { await load(); await loadCoupons() })

async function load() {
  try {
    const body = await cart.list()
    items.value = body.data || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

/** 删除商品 */
async function remove(id) {
  await cart.remove(id)
  await load()
}

/** 修改数量 */
async function changeQty(id, next) {
  const n = Math.max(1, Number(next || 1))
  await cart.updateNumber(id, n)
  await load()
}

/** 组装行数据 */
const rows = computed(() => (items.value || []).map(i => {
  const price = Number(i.price || 0)
  const qty = Number(i.number || 1)
  return {
    id: i.id, gid: i.gid,
    name: i.gname || '商品',
    image: fixImageUrl(i.gpic) || '/images/logo_app.png',
    quantity: qty, price,
    total: price * qty
  }
}))

/** 合计 */
const sumTotal = computed(() => rows.value.reduce((acc, r) => acc + r.total, 0))

function format(n) { return Number(n || 0).toFixed(2) }

/** 图片fallback */
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

/** 去结算 */
const goTrade = async () => {
  const uid = localStorage.getItem('uid')
  if (!uid) { if (confirm('请先登录后再结算')) { router.push({ name: 'login', query: { redirect: '/cart' } }); } return }
  try {
    const addrRes = await addresses.list()
    const addrList = addrRes.data || []
    if (!addrList.length) {
      if (confirm('您还没有收货地址，是否先去添加？')) { router.push('/center/addresses') }
      return
    }
    const defaultAddr = addrList.find(a => a.isDefault === 1) || addrList[0]
    const aid = defaultAddr.id
    const selectedCarts = items.value.map(i => i.id)
    const body = await orders.create(selectedCarts, aid, selectedCouponId.value || undefined)
    const orderId = body.data ? body.data.id : body.data
    router.push(`/center/orderdetail/${orderId}`)
  } catch (e) { alert(e.message) }
}

async function loadCoupons() {
  try {
    const m = await coupons.my(); const a = await coupons.available()
    myCoupons.value = (m.data || []).filter(c => (String(c.status || '')).toUpperCase() !== 'USED')
    availCoupons.value = a.data || []
  } catch (e) {
    console.error('加载优惠券失败:', e)
    myCoupons.value = []
    availCoupons.value = []
  }
}

function couponLabel(c) {
  const def = availCoupons.value.find(x => x.id === c.cid) || {}
  return `${def.name}（满¥${format(def.conditionAmount)}减¥${format(def.reduceAmount)}）`
}
</script>

<style scoped>
.cart-page {
  background: var(--c-soft);
  min-height: calc(100vh - 100px);
  padding: var(--s-40) 0 var(--s-80);
}
.cart-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 var(--s-16);
}

/* 页头 */
.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-24);
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: var(--s-12);
}
.cart-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.02em;
}
.cart-count {
  font-size: 14px;
  color: var(--c-muted);
}

/* 商品列表 */
.cart-content {
  background: var(--c-ground);
  border-radius: var(--r-xl);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.cart-list {
  padding: var(--s-16) var(--s-24);
}

.cart-item {
  display: grid;
  grid-template-columns: 1fr 120px 130px 120px 50px;
  align-items: center;
  padding: var(--s-20) 0;
  border-bottom: 1px solid var(--c-hairline);
  gap: var(--s-16);
}
.cart-item:last-child { border-bottom: none; }

/* 商品信息 */
.item-info {
  display: flex;
  align-items: center;
  gap: var(--s-16);
}
.item-thumb {
  width: 88px; height: 88px;
  object-fit: cover;
  border-radius: var(--r-md);
  background: var(--c-soft-alt);
  flex-shrink: 0;
}
.item-detail {
  min-width: 0;
}
.item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  text-decoration: none;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--dur-fast) ease;
}
.item-name:hover { color: var(--c-accent); }
.item-price-mobile { display: none; }

/* 单价 */
.item-price {
  font-size: 14px;
  color: var(--c-muted);
  text-align: center;
}

/* 数量 */
.item-qty {
  display: flex;
  justify-content: center;
}
.qty-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-sm);
  overflow: hidden;
}
.qty-control button {
  width: 34px; height: 34px;
  border: none;
  background: var(--c-ground);
  cursor: pointer;
  font-size: 16px;
  color: var(--c-ink);
  transition: background var(--dur-fast) ease;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qty-control button:hover:not(:disabled) { background: var(--c-soft); }
.qty-control button:disabled { color: var(--c-muted-light); cursor: not-allowed; }
.qty-control span {
  width: 44px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--c-ink);
  border-left: 1px solid var(--c-hairline);
  border-right: 1px solid var(--c-hairline);
  line-height: 34px;
}

/* 小计 */
.item-total {
  font-size: 15px;
  font-weight: 700;
  color: var(--c-ink);
  text-align: center;
}

/* 删除 */
.item-action {
  display: flex;
  justify-content: center;
}
.btn-remove {
  background: none;
  border: none;
  color: var(--c-muted-light);
  cursor: pointer;
  padding: var(--s-4);
  border-radius: var(--r-sm);
  transition: all var(--dur-fast) ease;
  display: flex;
}
.btn-remove:hover { color: var(--c-danger); background: var(--c-danger-bg); }

/* ===== 结算栏 ===== */
.cart-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--s-20) var(--s-32);
  background: var(--c-soft-alt);
  border-top: 1px solid var(--c-hairline);
}
.bar-left {
  display: flex;
  align-items: baseline;
  gap: var(--s-8);
}
.bar-label {
  font-size: 15px;
  color: var(--c-muted);
}
.bar-price {
  font-size: 28px;
  font-weight: 800;
  color: var(--c-accent);
  letter-spacing: -0.02em;
}
.bar-hint {
  font-size: 12px;
  color: var(--c-muted-light);
}
.btn-checkout {
  height: 48px;
  padding: 0 var(--s-40);
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--r-md);
}
.bar-coupon {
  display: flex;
  align-items: center;
}
.coupon-select {
  height: 36px;
  padding: 0 var(--s-12);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-sm);
  font-size: 13px;
  color: var(--c-ink);
  background: var(--c-ground);
  outline: none;
  cursor: pointer;
  min-width: 200px;
}
.coupon-select:focus { border-color: var(--c-accent); }

/* 空 */
.empty-visual {
  margin-bottom: var(--s-12);
  opacity: 0.35;
}
.empty-text {
  font-size: 16px;
  color: var(--c-muted);
  margin-bottom: var(--s-4);
}

/* 响应式 */
@media (max-width: 768px) {
  .cart-item {
    grid-template-columns: 1fr auto;
    gap: var(--s-8);
    padding: var(--s-16) 0;
  }
  .item-price, .item-total, .item-qty { display: none; }
  .item-price-mobile { display: block; font-size: 14px; color: var(--c-muted); margin-top: var(--s-4); }
  .item-info { grid-column: 1 / -1; }
  .cart-bar { padding: var(--s-16) var(--s-20); flex-direction: column; gap: var(--s-16); align-items: stretch; }
  .btn-checkout { width: 100%; }
  .cart-list { padding: var(--s-8) var(--s-16); }
}
</style>
