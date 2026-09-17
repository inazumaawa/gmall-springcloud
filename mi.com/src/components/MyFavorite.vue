<template>
  <div class="favorite-section">
    <div class="section-head">
      <h2 class="head-title">我的收藏</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="rows.length" class="fav-grid">
      <div class="fav-card" v-for="r in rows" :key="r.id">
        <router-link :to="`/detail/${r.gid}`" class="fav-media">
          <img :src="r.image" alt="" @error="onImgErr" />
        </router-link>
        <div class="fav-info">
          <router-link :to="`/detail/${r.gid}`" class="fav-name">{{ r.name }}</router-link>
          <div class="fav-price">¥{{ format(r.price) }}</div>
          <div class="fav-actions">
            <router-link class="btn-primary btn-block" :to="`/detail/${r.gid}`">查看详情</router-link>
            <button class="btn-outline btn-block" @click="remove(r.gid)">取消收藏</button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无收藏</p>
      <router-link to="/search" class="btn-primary" style="margin-top: var(--s-16)">去逛逛</router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { favorites, goods, fixImageUrl } from '../api'

const list = ref([])
const productMap = ref({})
const loading = ref(true)

/** 加载收藏列表及对应商品信息 */
onMounted(async () => { await load() })

async function load() {
  try {
    loading.value = true
    const body = await favorites.list()
    list.value = body.data || []
    const ids = [...new Set(list.value.map(i => i.gid))]
    const pairs = await Promise.all(ids.map(async id => {
      try { const r = await goods.detail(id); return [id, r.data] } catch { return [id, null] }
    }))
    const map = {}; pairs.forEach(([id, p]) => { if (p) map[id] = p })
    productMap.value = map
  } finally { loading.value = false }
}

async function remove(gid) {
  await favorites.remove(gid)
  await load()
}

/** 组装行数据 */
const rows = computed(() => list.value.map(f => {
  const p = productMap.value[f.gid] || {}
  return {
    id: f.id, gid: f.gid,
    name: p.gname || '商品',
    image: fixImageUrl(p.gpic) || '/images/logo_app.png',
    price: Number(p.gprice || 0)
  }
}))

function format(n) { return Number(n || 0).toFixed(2) }

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
.favorite-section { margin: 0; }

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

/* 网格 */
.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--s-16);
}
.fav-card {
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  overflow: hidden;
  background: var(--c-ground);
  transition: box-shadow var(--dur-fast) ease;
}
.fav-card:hover { box-shadow: var(--shadow-md); }

.fav-media {
  display: block;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: var(--c-soft-alt);
}
.fav-media img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 400ms var(--ease-out);
}
.fav-card:hover .fav-media img { transform: scale(1.04); }

.fav-info {
  padding: var(--s-16);
}
.fav-name {
  display: block;
  font-size: 14px;
  color: var(--c-ink);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  transition: color var(--dur-fast) ease;
}
.fav-name:hover { color: var(--c-accent); }

.fav-price {
  margin-top: var(--s-6);
  font-size: 18px;
  font-weight: 700;
  color: var(--c-ink);
  letter-spacing: -0.02em;
}

.fav-actions {
  margin-top: var(--s-12);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-8);
}
.btn-block {
  height: 36px;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--r-sm);
  text-decoration: none;
}

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }

@media (max-width: 768px) { .fav-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
