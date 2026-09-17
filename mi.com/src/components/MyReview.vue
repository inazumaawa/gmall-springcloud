<template>
  <div class="review-section">
    <div class="section-head">
      <h2 class="head-title">我的评价</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="list.length" class="review-list">
      <div class="review-item" v-for="r in list" :key="r.id">
        <div class="review-top">
          <router-link class="review-product" :to="`/detail/${r.gid}`">
            <span class="product-name">{{ goodsMap[r.gid] || `商品#${r.gid}` }}</span>
          </router-link>
          <div class="review-stars">
            <span v-for="i in 5" :key="i" :class="{ on: i <= clampRating(r.rating) }">&#9733;</span>
          </div>
        </div>
        <div class="review-body">{{ r.content || '（无文字评价）' }}</div>
        <div class="review-footer">
          <span class="review-time">{{ formatTime(r.createdTime) }}</span>
          <button class="btn-danger-link" @click="remove(r.id)">删除</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>暂无评价</p>
      <router-link to="/search" class="btn-primary" style="margin-top: var(--s-16)">去逛逛</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reviews, goods } from '../api'

const list = ref([])
const goodsMap = ref({})
const loading = ref(true)

/** 加载我的评价及对应商品名 */
onMounted(async () => { await load() })

async function load() {
  try {
    const body = await reviews.my()
    const data = body.data || []
    list.value = data
    const ids = [...new Set(data.map(r => r.gid))]
    const results = await Promise.allSettled(ids.map(id => goods.detail(id)))
    results.forEach((res, i) => {
      if (res.status === 'fulfilled' && res.value?.data?.gname) {
        goodsMap.value[ids[i]] = res.value.data.gname
      }
    })
  } catch (e) { list.value = [] }
  finally { loading.value = false }
}

function clampRating(n) { const x = Number(n || 0); return Math.max(0, Math.min(5, Math.round(x))) }
function formatTime(t) { if (!t) return ''; const d = new Date(t); return Number.isNaN(d.getTime()) ? '' : d.toLocaleString() }

/** 删除评价 */
async function remove(id) {
  try {
    if (!confirm('确定删除该评价吗？')) return
    await reviews.remove(id)
    await load()
  } catch (e) { alert(e.message) }
}
</script>

<style scoped>
.review-section { margin: 0; }

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

/* 评价列表 */
.review-list {
  display: grid;
  gap: var(--s-16);
}

.review-item {
  padding: var(--s-20);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  background: var(--c-ground);
  transition: box-shadow var(--dur-fast) ease;
}
.review-item:hover { box-shadow: var(--shadow-md); }

.review-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-12);
}
.review-product {
  text-decoration: none;
  flex: 1;
  min-width: 0;
}
.product-name {
  font-size: 14px;
  color: var(--c-ink);
  font-weight: 600;
  transition: color var(--dur-fast) ease;
}
.review-product:hover .product-name { color: var(--c-accent); }

.review-stars span { color: var(--c-hairline); font-size: 15px; margin-left: 1px; }
.review-stars span.on { color: #FFB400; }

.review-body {
  font-size: 14px;
  color: var(--c-ink-soft);
  line-height: 1.7;
  background: var(--c-soft-alt);
  border-radius: var(--r-md);
  padding: var(--s-12) var(--s-16);
  border-left: 3px solid var(--c-accent);
}

.review-footer {
  margin-top: var(--s-12);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.review-time { font-size: 12px; color: var(--c-muted); }

.btn-danger-link {
  background: none;
  border: none;
  font-size: 13px;
  color: var(--c-muted);
  cursor: pointer;
  transition: color var(--dur-fast) ease;
  padding: var(--s-4) var(--s-8);
}
.btn-danger-link:hover { color: var(--c-danger); }

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
