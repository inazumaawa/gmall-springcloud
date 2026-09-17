<template>
  <div class="home-content">
    <!-- Hero 区：亮色横幅 -->
    <section class="home-hero">
      <div class="hero-inner">
        <h1 class="hero-title">探索品质好物</h1>
        <p class="hero-desc">精选热销单品，为你而来</p>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">{{ goodList.length }}+</span>
            <span class="stat-label">精选商品</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">24h</span>
            <span class="stat-label">极速发货</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">7天</span>
            <span class="stat-label">无忧退换</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 商品网格 -->
    <section class="products-section">
      <div class="section-header">
        <div class="header-left">
          <span class="section-tag">热门推荐</span>
          <h2 class="section-title">全部商品</h2>
        </div>
        <router-link to="/search" class="view-all">
          查看全部
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>

      <div v-if="loading" class="loading-block">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else class="product-grid">
        <Good v-for="good in goodList" :key="good.id" :good="good" />
      </div>
    </section>
  </div>
</template>

<script setup>
import Good from './Good.vue'
import { ref, onMounted } from 'vue'
import { goods, fixImageUrl } from '../api'

const goodList = ref([])
const loading = ref(true)

/** 加载商品列表 */
onMounted(async () => {
  try {
    // 缓存优先
    try {
      const raw = localStorage.getItem('home_goods_cache')
      const prev = raw ? JSON.parse(raw) : []
      if (Array.isArray(prev) && prev.length) { goodList.value = prev; loading.value = false }
    } catch {}
    const body = await goods.list()
    const list = (body.data || []).map(p => ({
      id: p.gid,
      name: p.gname,
      price: p.gprice,
      desc: p.gdetails,
      category: p.gpcategory || '',
      image: fixImageUrl(p.gpic),
      tag: p.gptag || ''
    }))
    goodList.value = list
    try { localStorage.setItem('home_goods_cache', JSON.stringify(list)) } catch {}
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* ===== Hero 区 ===== */
.home-hero {
  background: var(--c-ground);
  padding: var(--s-64) var(--s-16);
  text-align: center;
  position: relative;
  overflow: hidden;
  border-bottom: 1px solid var(--c-hairline);
}
.home-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 50%, rgba(202, 20, 28, 0.04) 0%, transparent 60%),
              radial-gradient(ellipse at 70% 50%, rgba(202, 20, 28, 0.03) 0%, transparent 60%);
  pointer-events: none;
}
.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
}
.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.03em;
  line-height: 1.15;
}
.hero-desc {
  font-size: 18px;
  color: var(--c-muted);
  margin: var(--s-16) 0 0;
}

/* 统计条 */
.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--s-32);
  margin-top: var(--s-32);
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--s-2);
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--c-accent);
  letter-spacing: -0.02em;
}
.stat-label {
  font-size: 13px;
  color: var(--c-muted);
  font-weight: 500;
}
.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--c-hairline);
}

/* ===== 商品区 ===== */
.products-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--s-48) var(--s-16) var(--s-64);
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--s-32);
}
.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
}
.section-tag {
  font-size: 12px;
  font-weight: 700;
  color: var(--c-accent);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.section-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.02em;
}
.view-all {
  display: inline-flex;
  align-items: center;
  gap: var(--s-4);
  font-size: 14px;
  color: var(--c-muted);
  font-weight: 500;
  padding: var(--s-8) var(--s-16);
  border-radius: var(--r-xl);
  transition: all var(--dur-fast) var(--ease-out);
}
.view-all:hover {
  color: var(--c-accent);
  background: var(--c-accent-soft);
}

/* 网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--s-24);
}

/* 加载 */
.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-64) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-12); }

/* 响应式 */
@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
  .hero-title { font-size: 32px; }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: var(--s-16); }
  .home-hero { padding: var(--s-48) var(--s-16); }
  .hero-title { font-size: 26px; }
  .hero-stats { gap: var(--s-20); }
  .stat-num { font-size: 22px; }
}
@media (max-width: 480px) {
  .product-grid { grid-template-columns: 1fr; }
  .hero-stats { flex-direction: column; gap: var(--s-12); }
  .stat-divider { width: 40px; height: 1px; }
}
</style>
