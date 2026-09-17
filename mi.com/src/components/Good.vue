<template>
  <router-link :to="`/detail/${good.id}`" class="product-card">
    <!-- 图片区 -->
    <div class="card-media">
      <img :src="good.image || '/images/logo_app.png'" alt="" @error="onImgErr" />
      <!-- 热门标签 -->
      <span class="card-tag" v-if="good.tag">{{ good.tag }}</span>

    </div>
    <!-- 信息区 -->
    <div class="card-body">
      <div class="card-category" v-if="good.category">{{ good.category }}</div>
      <h3 class="card-name">{{ good.name }}</h3>
      <div class="card-bottom">
        <div class="card-price">
          <span class="price-symbol">¥</span>{{ format(good.price) }}
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
const { good } = defineProps(['good'])

/** 格式化价格 */
function format(n) { return Number(n || 0).toFixed(2) }

/** 图片加载失败时尝试备选格式 */
function onImgErr(e) {
  const el = e.target
  const tried = Number(el.dataset.altTry || 0)
  const src = String(el.src || '')
  const base = src.replace(/\.[^./?]+$/, '')
  const alts = [base + '.png', base + '.jpg', base + '.webp', '/images/logo_app.png']
  const next = alts[Math.min(tried, alts.length - 1)]
  el.dataset.altTry = String(tried + 1)
  if (src !== next) el.src = next
}
</script>

<style scoped>
.product-card {
  display: block;
  background: var(--c-ground);
  border-radius: var(--r-lg);
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  border: 1px solid transparent;
  transition: transform 350ms var(--ease-out),
              box-shadow 350ms var(--ease-out),
              border-color 350ms ease;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--c-hairline);
}

/* 图片区 */
.card-media {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
  background: var(--c-soft-alt);
}
.card-media img {
  position: absolute;
  inset: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform 600ms var(--ease-out);
}
.product-card:hover .card-media img {
  transform: scale(1.06);
}

/* 标签 */
.card-tag {
  position: absolute;
  top: var(--s-12);
  left: var(--s-12);
  padding: 3px 10px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 3px;
  letter-spacing: 0.04em;
  z-index: 2;
}

/* 信息区 */
.card-body {
  padding: var(--s-16);
}

.card-category {
  font-size: 11px;
  font-weight: 600;
  color: var(--c-accent);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: var(--s-4);
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.01em;
}

/* 底部 */
.card-bottom {
  margin-top: var(--s-12);
}

.card-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--c-ink);
  letter-spacing: -0.02em;
}
.price-symbol {
  font-size: 13px;
  font-weight: 600;
  margin-right: 1px;
}
</style>
