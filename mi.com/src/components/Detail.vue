<template>
  <div class="detail-page" v-if="product">
    <div class="detail-container">
      <!-- 面包屑 -->
      <nav class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="breadcrumb-sep">/</span>
        <router-link to="/search">全部商品</router-link>
        <span class="breadcrumb-sep">/</span>
        <span class="current">{{ product.name }}</span>
      </nav>

      <!-- 商品主区域 -->
      <div class="detail-main">
        <!-- 左侧图片 -->
        <div class="detail-gallery">
          <div class="gallery-main">
            <img :src="mainImage" alt="" @error="onImgErr" />
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="detail-info">
          <div class="info-category" v-if="product.category">{{ product.category }}</div>
          <h1 class="info-name">{{ product.name }}</h1>
          <p class="info-desc" v-if="product.description">{{ product.description }}</p>

          <!-- 价格卡片 -->
          <div class="info-price-card">
            <div class="price-main">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.price }}</span>
            </div>
            <div class="price-tags">
              <span class="price-tag">正品保证</span>
              <span class="price-tag">7天退换</span>
            </div>
          </div>

          <!-- 数量选择 -->
          <div class="info-option">
            <span class="option-label">数量</span>
            <div class="qty-stepper">
              <button class="qty-btn" @click="qtyDecrease" :disabled="quantity <= 1">-</button>
              <input class="qty-input" type="number" v-model.number="quantity" min="1" max="99" />
              <button class="qty-btn" @click="qtyIncrease" :disabled="quantity >= 99">+</button>
            </div>
          </div>

          <!-- 操作按钮组 -->
          <div class="info-actions">
            <button @click="addCart" class="btn-primary btn-lg">加入购物车</button>
            <button @click="checkoutNow" class="btn-dark btn-lg">立即购买</button>
            <button @click="toggleFavorite" class="btn-fav" :class="{ active: favored }" title="收藏">
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="favored ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            </button>
          </div>

          <!-- 服务承诺 -->
          <div class="info-promises">
            <div class="promise-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              官方正品
            </div>
            <div class="promise-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
              售后保障
            </div>
            <div class="promise-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
              极速发货
            </div>
          </div>
        </div>
      </div>

      <!-- 优惠券区 -->
      <div class="section-card">
        <div class="section-card-head">
          <h3 class="section-card-title">优惠券</h3>
          <span class="section-card-badge" v-if="availableCoupons.length">{{ availableCoupons.length }}张可领</span>
        </div>
        <div class="coupon-grid" v-if="availableCoupons.length">
          <div class="coupon-item" v-for="c in availableCoupons" :key="c.id">
            <div class="coupon-amount">
              <span class="coupon-yen">¥</span>
              <span class="coupon-num">{{ formatInt(c.reduceAmount) }}</span>
            </div>
            <div class="coupon-info">
              <span class="coupon-title">{{ c.name }}</span>
              <span class="coupon-cond">满{{ formatInt(c.conditionAmount) }}元可用</span>
            </div>
            <button
              class="coupon-claim"
              :class="{ claimed: couponState(c.id) !== 'CAN_CLAIM' }"
              :disabled="couponState(c.id) !== 'CAN_CLAIM'"
              @click="claim(c.id)"
            >{{ couponState(c.id) === 'USED' ? '已使用' : (couponState(c.id) === 'CLAIMED' ? '已领取' : '立即领取') }}</button>
          </div>
        </div>
        <p v-else class="empty-text">暂无可领取优惠券</p>
      </div>

      <!-- 评价区 -->
      <div class="section-card">
        <div class="section-card-head">
          <h3 class="section-card-title">用户评价</h3>
          <span class="section-card-badge" v-if="reviewsList.length">{{ reviewsList.length }}条评价</span>
        </div>
        <div v-if="reviewsList.length" class="review-list">
          <div class="review-item" v-for="r in reviewsList" :key="r.id">
            <div class="review-meta">
              <div class="review-avatar">{{ (r.uname || '匿').charAt(0) }}</div>
              <div class="review-user-info">
                <span class="review-user">{{ r.uname || '匿名用户' }}</span>
                <span class="review-time">{{ formatTime(r.createdTime) }}</span>
              </div>
              <div class="review-stars">
                <span v-for="i in 5" :key="i" :class="{ on: i <= clampRating(r.rating) }">&#9733;</span>
              </div>
            </div>
            <div class="review-body">{{ r.content || '（无文字评价）' }}</div>
          </div>
        </div>
        <div v-else class="empty-review">暂无评价，快来成为第一个评价的人吧</div>

        <!-- 评价表单 -->
        <div class="review-form">
          <h4>发表评价</h4>
          <div class="form-row">
            <span class="form-label">评分</span>
            <div class="star-input">
              <span v-for="i in 5" :key="i" :class="{ on: i <= myRating }" @click="chooseRating(i)">&#9733;</span>
            </div>
          </div>
          <textarea class="form-textarea" v-model="myContent" maxlength="300" placeholder="分享您的使用体验（最多300字）"></textarea>
          <div class="form-btns">
            <button class="btn-primary" @click="submitReview" :disabled="posting || !myRating">提交评价</button>
            <button class="btn-outline" v-if="!isLoggedIn" @click="toLogin">登录后评价</button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="loading-block">
    <div class="spinner"></div>
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { goods, cart, coupons, orders, addresses, reviews as reviewsApi, favorites, fixImageUrl } from '../api'

const router = useRouter()
const route = useRoute()
const product = ref(null)
const availableCoupons = ref([])
const myCoupons = ref([])
const reviewsList = ref([])
const myRating = ref(0)
const myContent = ref('')
const posting = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('uid'))
const favored = ref(false)
const quantity = ref(1)

function qtyDecrease() { if (quantity.value > 1) quantity.value-- }
function qtyIncrease() { if (quantity.value < 99) quantity.value++ }

/** 头像主图 */
const mainImage = computed(() => product.value?.image || '/images/logo_app.png')

function refreshLoginState() { isLoggedIn.value = !!localStorage.getItem('uid') }

/** 初始化：加载商品详情、优惠券、评价、收藏状态 */
onMounted(async () => {
  const id = route.params.id
  const body = await goods.detail(id)
  const p = body.data
  product.value = {
    id: p.gid, name: p.gname,
    description: p.gdetails, price: p.gprice,
    image: fixImageUrl(p.gpic)
  }
  await loadCoupons()
  await loadReviews(id)
  await loadFavorite()
  refreshLoginState()
  window.addEventListener('storage', refreshLoginState)
  window.addEventListener('focus', refreshLoginState)
})

onUnmounted(() => {
  window.removeEventListener('storage', refreshLoginState)
  window.removeEventListener('focus', refreshLoginState)
})

/** 加入购物车 */
async function addCart() {
  const uid = localStorage.getItem('uid')
  if (!uid) { if (confirm('请先登录后再加入购物车')) { router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } }); } return }
  await cart.add(product.value.id, quantity.value)
  quantity.value = 1
  router.push({ name: 'cart' })
}

/** 立即购买：数量固定为1，使用默认地址直接创建订单 */
async function checkoutNow() {
  const uid = localStorage.getItem('uid')
  if (!uid) { if (confirm('请先登录后再购买')) { router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } }); } return }
  try {
    await cart.add(product.value.id, 1)
    const [cartBody, addrBody] = await Promise.all([cart.list(), addresses.list()])
    const cartItems = cartBody.data || []
    if (cartItems.length === 0) { alert('添加购物车失败，请重试'); return }
    const cartId = cartItems[cartItems.length - 1].id
    const addrList = addrBody.data || []
    const defaultAddr = addrList.find(a => a.isDefault === 1) || addrList[0]
    const aid = defaultAddr ? defaultAddr.id : null
    const body = await orders.create([cartId], aid)
    const orderId = body.data ? body.data.id : body.data
    router.push({ path: `/center/orderdetail/${orderId}` })
  } catch (e) { alert(e.message || '下单失败') }
}

/** 收藏/取消收藏 */
async function loadFavorite() {
  if (!localStorage.getItem('uid')) { favored.value = false; return }
  try {
    const body = await favorites.check(product.value.id)
    const d = body.data
    favored.value = (d === true || (d && d.favored === true))
  } catch { favored.value = false }
}

async function toggleFavorite() {
  const uid = localStorage.getItem('uid')
  if (!uid) { if (confirm('请先登录后再收藏商品')) { toLogin(); } return }
  try {
    if (favored.value) { await favorites.remove(product.value.id); favored.value = false }
    else { await favorites.add(product.value.id); favored.value = true }
  } catch {}
}

/** 优惠券 */
async function loadCoupons() {
  try {
    const avail = await coupons.available(); availableCoupons.value = avail.data || []
    const mine = await coupons.my(); myCoupons.value = mine.data || []
  } catch {}
}

async function claim(couponId) {
  try {
    if (couponState(couponId) !== 'CAN_CLAIM') return
    await coupons.receive(couponId); await loadCoupons()
  } catch (e) { alert(e.message) }
}

function couponState(cid) {
  const uc = myCoupons.value.find(x => x.cid === cid)
  if (!uc) return 'CAN_CLAIM'
  return (uc.status || '').toUpperCase() === 'USED' ? 'USED' : 'CLAIMED'
}

/** 评价 */
async function loadReviews(gid) {
  try { const body = await reviewsApi.listByGoods(gid); reviewsList.value = body.data || [] }
  catch { reviewsList.value = [] }
}

function clampRating(n) { return Math.max(0, Math.min(5, Math.round(Number(n || 0)))) }
function chooseRating(i) { myRating.value = Math.max(1, Math.min(5, i)) }

async function submitReview() {
  if (!localStorage.getItem('uid')) { if (confirm('请先登录后再发表评价')) { toLogin(); } return }
  if (!myRating.value) return
  try {
    posting.value = true
    await reviewsApi.add({ gid: product.value.id, rating: myRating.value, content: myContent.value.trim() })
    myContent.value = ''; myRating.value = 0
    await loadReviews(product.value.id)
  } catch {} finally { posting.value = false }
}

/** 工具函数 */
function formatInt(n) { return Number(n || 0).toFixed(0) }
function formatTime(t) {
  if (!t) return ''; const d = new Date(t)
  return Number.isNaN(d.getTime()) ? t : d.toLocaleString()
}
function toLogin() { router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } }) }

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
</script>

<style scoped>
.detail-page {
  background: var(--c-soft);
  padding: var(--s-24) 0 var(--s-80);
}
.detail-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 var(--s-16);
}

/* 面包屑 */
.breadcrumb {
  font-size: 13px;
  color: var(--c-muted);
  margin-bottom: var(--s-20);
  display: flex;
  align-items: center;
  gap: var(--s-8);
}
.breadcrumb a { color: var(--c-muted); transition: color var(--dur-fast) ease; }
.breadcrumb a:hover { color: var(--c-accent); }
.breadcrumb-sep { color: var(--c-muted-light); }
.breadcrumb .current { color: var(--c-ink); font-weight: 500; }

/* ===== 主区域 ===== */
.detail-main {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: var(--s-48);
  background: var(--c-ground);
  border-radius: var(--r-xl);
  padding: var(--s-40);
  margin-bottom: var(--s-20);
  box-shadow: var(--shadow-card);
}

/* 图片 */
.detail-gallery {
  position: sticky;
  top: 80px;
  align-self: start;
}
.gallery-main {
  border-radius: var(--r-lg);
  overflow: hidden;
  background: var(--c-soft-alt);
}
.gallery-main img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: contain;
  display: block;
}

/* 信息区 */
.detail-info {
  display: flex;
  flex-direction: column;
}

.info-category {
  font-size: 12px;
  font-weight: 600;
  color: var(--c-accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: var(--s-8);
}

.info-name {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  line-height: 1.3;
  margin: 0;
  letter-spacing: -0.02em;
}

.info-desc {
  margin-top: var(--s-12);
  font-size: 15px;
  color: var(--c-muted);
  line-height: 1.7;
}

/* 价格卡片 */
.info-price-card {
  margin-top: var(--s-24);
  padding: var(--s-20) var(--s-24);
  background: var(--c-accent-soft);
  border-radius: var(--r-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price-main {
  display: flex;
  align-items: baseline;
}
.price-symbol {
  font-size: 18px;
  font-weight: 600;
  color: var(--c-accent);
  margin-right: 2px;
}
.price-value {
  font-size: 36px;
  font-weight: 800;
  color: var(--c-accent);
  letter-spacing: -0.03em;
  line-height: 1;
}
.price-tags {
  display: flex;
  gap: var(--s-8);
}
.price-tag {
  padding: 3px 10px;
  background: var(--c-ground);
  color: var(--c-accent);
  border-radius: var(--r-sm);
  font-size: 12px;
  font-weight: 500;
}

/* 选项行 */
.info-option {
  margin-top: var(--s-20);
  display: flex;
  align-items: center;
  gap: var(--s-12);
}
.option-label {
  font-size: 14px;
  color: var(--c-muted);
  flex-shrink: 0;
  min-width: 48px;
}
.option-select {
  flex: 1;
  height: 42px;
  padding: 0 var(--s-12);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-sm);
  font-size: 13px;
  color: var(--c-ink);
  outline: none;
  background: var(--c-ground);
  transition: border-color var(--dur-fast) ease;
}
.option-select:focus { border-color: var(--c-accent); }

/* 数量选择器 */
.qty-stepper {
  display: flex;
  align-items: center;
  gap: 0;
}
.qty-btn {
  width: 42px;
  height: 42px;
  border: 1px solid var(--c-hairline);
  background: var(--c-ground);
  font-size: 18px;
  color: var(--c-ink);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dur-fast) ease;
}
.qty-btn:first-child { border-radius: var(--r-sm) 0 0 var(--r-sm); }
.qty-btn:last-child { border-radius: 0 var(--r-sm) var(--r-sm) 0; }
.qty-btn:hover:not(:disabled) { border-color: var(--c-accent); color: var(--c-accent); }
.qty-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qty-input {
  width: 60px;
  height: 42px;
  border: 1px solid var(--c-hairline);
  border-left: none;
  border-right: none;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  outline: none;
  background: var(--c-ground);
  -moz-appearance: textfield;
}
.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }

/* 操作按钮 */
.info-actions {
  margin-top: var(--s-32);
  display: flex;
  gap: var(--s-12);
}
.btn-lg {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--r-md);
}
.btn-fav {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-ground);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-md);
  color: var(--c-muted);
  cursor: pointer;
  transition: all var(--dur-fast) var(--ease-out);
  flex-shrink: 0;
}
.btn-fav:hover { border-color: var(--c-accent); color: var(--c-accent); }
.btn-fav.active {
  border-color: var(--c-accent);
  color: var(--c-accent);
  background: var(--c-accent-soft);
}

/* 服务承诺 */
.info-promises {
  margin-top: var(--s-24);
  display: flex;
  gap: var(--s-24);
  padding-top: var(--s-20);
  border-top: 1px solid var(--c-hairline);
}
.promise-item {
  display: flex;
  align-items: center;
  gap: var(--s-6);
  font-size: 13px;
  color: var(--c-muted);
}
.promise-item svg { color: var(--c-success); flex-shrink: 0; }

/* ===== 分区卡片 ===== */
.section-card {
  background: var(--c-ground);
  border-radius: var(--r-xl);
  padding: var(--s-32);
  margin-bottom: var(--s-16);
  box-shadow: var(--shadow-card);
}
.section-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-20);
  padding-bottom: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.section-card-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
}
.section-card-badge {
  font-size: 12px;
  color: var(--c-accent);
  background: var(--c-accent-soft);
  padding: 2px 10px;
  border-radius: var(--r-full);
  font-weight: 500;
}

/* ===== 优惠券 ===== */
.coupon-grid {
  display: grid;
  gap: var(--s-12);
}
.coupon-item {
  display: flex;
  align-items: center;
  gap: var(--s-16);
  padding: var(--s-16) var(--s-20);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  transition: box-shadow var(--dur-fast) ease;
}
.coupon-item:hover { box-shadow: var(--shadow-md); }
.coupon-amount {
  display: flex;
  align-items: baseline;
  color: var(--c-accent);
  flex-shrink: 0;
}
.coupon-yen { font-size: 14px; font-weight: 600; }
.coupon-num { font-size: 28px; font-weight: 800; letter-spacing: -0.02em; }
.coupon-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
}
.coupon-title { font-size: 14px; font-weight: 600; color: var(--c-ink); }
.coupon-cond { font-size: 12px; color: var(--c-muted); }
.coupon-claim {
  padding: var(--s-8) var(--s-16);
  background: var(--c-accent);
  color: #fff;
  border: none;
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--dur-fast) ease;
  white-space: nowrap;
}
.coupon-claim:hover { background: var(--c-accent-hover); }
.coupon-claim.claimed {
  background: var(--c-hairline);
  color: var(--c-muted);
  cursor: default;
}

.empty-text { color: var(--c-muted); font-size: 14px; }

/* ===== 评价列表 ===== */
.review-list {
  display: grid;
  gap: var(--s-20);
}
.review-item {
  padding-bottom: var(--s-20);
  border-bottom: 1px solid var(--c-hairline);
}
.review-item:last-child { border-bottom: none; padding-bottom: 0; }

.review-meta {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  margin-bottom: var(--s-12);
}
.review-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: var(--c-ink);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.review-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.review-user { font-size: 14px; font-weight: 600; color: var(--c-ink); }
.review-time { font-size: 12px; color: var(--c-muted); margin-top: 1px; }

.review-stars span { color: var(--c-hairline); font-size: 15px; }
.review-stars span.on { color: #FFB400; }

.review-body {
  font-size: 14px;
  color: var(--c-ink-soft);
  line-height: 1.7;
  background: var(--c-soft-alt);
  border-radius: var(--r-md);
  padding: var(--s-12) var(--s-16);
}

.empty-review {
  text-align: center;
  padding: var(--s-32) 0;
  color: var(--c-muted);
  font-size: 14px;
}

/* 评价表单 */
.review-form {
  margin-top: var(--s-24);
  padding-top: var(--s-24);
  border-top: 1px solid var(--c-hairline);
}
.review-form h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  margin: 0 0 var(--s-16);
}
.form-row {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  margin-bottom: var(--s-12);
}
.form-label { font-size: 13px; color: var(--c-muted); }

.star-input span {
  font-size: 24px;
  color: var(--c-hairline);
  cursor: pointer;
  transition: transform var(--dur-fast) var(--ease-spring);
  display: inline-block;
}
.star-input span.on { color: #FFB400; }
.star-input span:hover { transform: scale(1.25); }

.form-textarea {
  width: 100%;
  min-height: 90px;
  padding: var(--s-12);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-md);
  font-size: 14px;
  color: var(--c-ink);
  resize: vertical;
  outline: none;
  background: var(--c-ground);
  font-family: inherit;
  transition: border-color var(--dur-fast) ease;
  box-sizing: border-box;
}
.form-textarea:focus { border-color: var(--c-accent); }

.form-btns {
  margin-top: var(--s-12);
  display: flex;
  gap: var(--s-12);
}

/* 加载 */
.loading-block {
  text-align: center;
  padding: var(--s-96) 0;
  color: var(--c-muted);
}
.loading-block .spinner { margin: 0 auto var(--s-12); }

@media (max-width: 900px) {
  .detail-main {
    grid-template-columns: 1fr;
    gap: var(--s-32);
    padding: var(--s-24);
  }
  .detail-gallery { position: static; }
  .gallery-main img { max-width: 400px; margin: 0 auto; }
}
</style>
