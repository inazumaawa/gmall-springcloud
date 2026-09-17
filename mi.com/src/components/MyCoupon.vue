<template>
  <div class="coupon-section">
    <div class="section-head">
      <h2 class="head-title">我的优惠券</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else>
      <!-- 可领取 -->
      <div class="claim-area" v-if="claimableCoupons.length">
        <h3 class="sub-title">可领取</h3>
        <div class="coupon-list">
          <div class="coupon-card" v-for="c in claimableCoupons" :key="c.id">
            <div class="coupon-left">
              <span class="coupon-yen">¥</span>
              <span class="coupon-amount">{{ formatInt(c.reduceAmount) }}</span>
            </div>
            <div class="coupon-body">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-cond">满¥{{ formatInt(c.conditionAmount) }}可用</div>
              <div class="coupon-time">{{ formatDate(c.startTime) }} - {{ formatDate(c.endTime) }}</div>
            </div>
            <button class="btn-primary btn-claim" @click="receiveCoupon(c.id)" :disabled="claimingId === c.id">
              {{ claimingId === c.id ? '领取中' : '领取' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 已领取 -->
      <h3 class="sub-title">已领取</h3>
      <div v-if="rows.length" class="coupon-list">
        <div class="coupon-card" v-for="r in rows" :key="r.id" :class="{ used: r.statusClass === 'used' }">
          <div class="coupon-left">
            <span class="coupon-yen">¥</span>
            <span class="coupon-amount">{{ formatInt(r.amount) }}</span>
          </div>
          <div class="coupon-body">
            <div class="coupon-name">{{ r.title }}</div>
            <div class="coupon-cond">满¥{{ formatInt(r.minAmount) }}可用</div>
            <div class="coupon-meta">
              <span class="badge" :class="r.statusClass">{{ r.statusText }}</span>
              <span class="coupon-time" v-if="r.acquiredAt">领取于 {{ formatTime(r.acquiredAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无优惠券</p>
        <router-link to="/search" class="btn-primary" style="margin-top: var(--s-16)">去逛逛</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { coupons } from '../api'

const mine = ref([])
const avail = ref([])
const loading = ref(true)
const claimingId = ref(null)

/** 加载优惠券 */
async function load() {
  try {
    const m = await coupons.my(); mine.value = m.data || []
    const a = await coupons.all(); avail.value = a.data || []
  } catch (e) { mine.value = []; avail.value = [] }
  finally { loading.value = false }
}

onMounted(async () => { await load() })

/** 领取优惠券 */
async function receiveCoupon(cid) {
  try {
    claimingId.value = cid
    await coupons.receive(cid)
    alert('领取成功')
    await load()
  } catch (e) { alert(e.message || '领取失败') }
  finally { claimingId.value = null }
}

/** 可领取的：在全部列表中但尚未领取 */
const claimableCoupons = computed(() => {
  const myIds = new Set(mine.value.map(uc => uc.couponId))
  return avail.value.filter(c => !myIds.has(c.id))
})

/** 已领取行数据 */
const rows = computed(() => mine.value.map(uc => {
  const c = avail.value.find(x => x.id === uc.cid) || {}
  const s = (uc.status || '').toUpperCase()
  return {
    id: uc.id,
    title: c.name || `优惠券#${uc.cid}`,
    minAmount: c.conditionAmount || 0,
    amount: c.reduceAmount || 0,
    statusText: s === 'USED' ? '已使用' : '可用',
    statusClass: s === 'USED' ? 'used' : 'available',
    acquiredAt: uc.getTime,
    usedAt: uc.useTime
  }
}))

function formatInt(n) { return Number(n || 0).toFixed(0) }
function formatTime(t) { if (!t) return ''; const d = new Date(t); return Number.isNaN(d.getTime()) ? '' : d.toLocaleString() }
function formatDate(t) { if (!t) return ''; const d = new Date(t); return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString() }
</script>

<style scoped>
.coupon-section { margin: 0; }

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

.sub-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  margin: 0 0 var(--s-12);
}

.claim-area {
  margin-bottom: var(--s-24);
  padding-bottom: var(--s-20);
  border-bottom: 1px solid var(--c-hairline);
}

/* 优惠券列表 */
.coupon-list {
  display: grid;
  gap: var(--s-12);
}

.coupon-card {
  display: flex;
  align-items: center;
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  overflow: hidden;
  background: var(--c-ground);
  transition: box-shadow var(--dur-fast) ease;
}
.coupon-card:hover { box-shadow: var(--shadow-md); }
.coupon-card.used { opacity: 0.55; }

.coupon-left {
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: var(--s-16) 0;
  background: var(--c-soft-alt);
  flex-shrink: 0;
  position: relative;
}
.coupon-left::after {
  content: '';
  position: absolute;
  right: 0; top: 10%; height: 80%;
  width: 1px;
  background: var(--c-hairline);
  /* dashed simulate via repeating gradient */
  background: repeating-linear-gradient(to bottom, var(--c-hairline) 0, var(--c-hairline) 4px, transparent 4px, transparent 8px);
}
.coupon-card.used .coupon-left { background: var(--c-soft); }

.coupon-yen { font-size: 14px; color: var(--c-accent); font-weight: 600; }
.coupon-card.used .coupon-yen { color: var(--c-muted); }
.coupon-amount { font-size: 26px; font-weight: 800; color: var(--c-accent); letter-spacing: -0.02em; }
.coupon-card.used .coupon-amount { color: var(--c-muted); }

.coupon-body {
  flex: 1;
  padding: var(--s-12) var(--s-16);
  min-width: 0;
}
.coupon-name { font-size: 14px; font-weight: 600; color: var(--c-ink); }
.coupon-cond { font-size: 12px; color: var(--c-muted); margin-top: var(--s-2); }
.coupon-time { font-size: 11px; color: var(--c-muted-light); margin-top: var(--s-4); }

.coupon-meta {
  display: flex;
  align-items: center;
  gap: var(--s-8);
  margin-top: var(--s-6);
}
.badge {
  padding: 2px 10px;
  border-radius: var(--r-full);
  font-size: 11px;
  font-weight: 500;
}
.badge.available { background: var(--c-success-bg); color: var(--c-success); }
.badge.used { background: var(--c-hairline); color: var(--c-muted); }

.btn-claim {
  margin-right: var(--s-16);
  height: 34px;
  padding: 0 var(--s-16);
  font-size: 13px;
  white-space: nowrap;
}

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
