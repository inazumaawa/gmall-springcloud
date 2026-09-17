<template>
  <div class="search-page">
    <div class="search-container">
      <!-- 搜索头部 -->
      <div class="search-header">
        <div class="header-info">
          <h2 class="search-title">{{ keyword ? '搜索结果' : '全部商品' }}</h2>
          <span class="search-count" v-if="!loading">
            共 <strong>{{ list.length }}</strong> 件商品
          </span>
        </div>
        <!-- 排序/筛选项（占位） -->
        <div class="search-sort">
          <button class="sort-btn active">综合</button>
          <button class="sort-btn">新品</button>
          <button class="sort-btn">价格</button>
        </div>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="loading-block">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 商品网格 -->
      <div v-else-if="list.length" class="product-grid">
        <Good v-for="p in list" :key="p.id" :good="p" />
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-visual">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--c-muted-light)" stroke-width="1.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </div>
        <p class="empty-title">暂无结果</p>
        <p class="empty-sub">试试其他关键词</p>
        <router-link to="/search" class="btn-outline" style="margin-top: var(--s-16)">查看全部商品</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { goods, fixImageUrl } from '../api'
import Good from './Good.vue'

const route = useRoute()
const keyword = ref('')
const list = ref([])
const loading = ref(true)

/** 响应路由变化，加载商品 */
watchEffect(async () => {
  loading.value = true
  keyword.value = route.params.keyword || ''
  try {
    const key = keyword.value ? ('search_cache::' + keyword.value) : 'goods_list_cache'
    try {
      const raw = localStorage.getItem(key)
      const prev = raw ? JSON.parse(raw) : []
      if (Array.isArray(prev)) list.value = prev
    } catch {}
    const body = keyword.value ? await goods.search(keyword.value) : await goods.list()
    const arr = (body.data || []).map(p => ({
      id: p.gid,
      name: p.gname,
      price: p.gprice,
      desc: p.gdetails,
      image: fixImageUrl(p.gpic)
    }))
    list.value = arr
    try { localStorage.setItem(key, JSON.stringify(arr)) } catch {}
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.search-page {
  background: var(--c-soft);
  min-height: calc(100vh - 100px);
  padding: var(--s-40) 0 var(--s-64);
}
.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--s-16);
}

/* 搜索头部 */
.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-32);
  flex-wrap: wrap;
  gap: var(--s-16);
}
.header-info {
  display: flex;
  align-items: baseline;
  gap: var(--s-16);
}
.search-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.02em;
}
.search-count {
  font-size: 14px;
  color: var(--c-muted);
}
.search-count strong {
  color: var(--c-accent);
  font-weight: 600;
}

/* 排序按钮 */
.search-sort {
  display: flex;
  gap: var(--s-4);
  background: var(--c-ground);
  border-radius: var(--r-sm);
  padding: 3px;
  border: 1px solid var(--c-hairline);
}
.sort-btn {
  padding: var(--s-6) var(--s-16);
  border: none;
  background: transparent;
  color: var(--c-muted);
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: all var(--dur-fast) var(--ease-out);
}
.sort-btn:hover { color: var(--c-ink); }
.sort-btn.active {
  background: var(--c-accent);
  color: #fff;
}

/* 网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-24);
}

/* 空状态 */
.empty-visual {
  margin-bottom: var(--s-16);
  opacity: 0.4;
}
.empty-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--c-ink);
  margin-bottom: var(--s-4);
}
.empty-sub {
  font-size: 14px;
  color: var(--c-muted);
}

@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } }
</style>
