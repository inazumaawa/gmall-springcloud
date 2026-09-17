<template>
  <div>
    <!-- 轮播区 -->
    <div class="banner">
      <!-- 侧边分类导航 -->
      <div class="site-category">
        <ul>
          <li>手机<span>&gt;</span></li>
          <li>穿戴<span>&gt;</span></li>
          <li>平板<span>&gt;</span></li>
          <li>电脑<span>&gt;</span></li>
          <li>耳机音箱<span>&gt;</span></li>
          <li>智慧屏<span>&gt;</span></li>
          <li>路由器<span>&gt;</span></li>
          <li>智能家居<span>&gt;</span></li>
          <li>配件<span>&gt;</span></li>
          <li>更多<span>&gt;</span></li>
        </ul>
      </div>

      <!-- 轮播 -->
      <div class="swiper" @mouseover="stopSwiper" @mouseout="startSwiper">
        <div class="banner-img">
          <img :src="imgList[imgCount]" />
        </div>
        <div class="swiper-button-prev" @click="prev"></div>
        <div class="swiper-button-next" @click="next"></div>
        <div class="swiper-pagination">
          <ul>
            <li
              class="swiper-pagination-bullet"
              v-for="(img, index) in imgList"
              :key="index"
              :class="index == imgCount ? 'swiper-pagination-bullet-active' : ''"
              @click="imgCount = index"
            ></li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 导航网格 -->
    <div class="banner-nav">
      <div class="w">
        <ul class="banner-grid">
          <li v-for="(b, i) in bannerImages" :key="i" class="bn-item">
            <img class="banner-icon" :src="b.src" :alt="b.title || 'banner'" />
            <div class="caption">{{ b.title }}</div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const imgList = ref([])
const imgCount = ref(0)
let timeOut = null

const bannerImages = ref([])
const bannerTitles = [
  '华为手机', '运动健康', '影音娱乐', '智慧办公',
  '鸿蒙智家', '鸿蒙甄选', '鸿蒙智行', '乾高智驾',
  '她之选', '企业商用', '以旧换新', '配件'
]

/** 尝试加载图片，失败则返回 null */
function loadImage(url) {
  return new Promise(resolve => {
    const img = new Image()
    img.onload = () => resolve(url)
    img.onerror = () => resolve(null)
    img.src = url
  })
}

/** 初始化轮播图和导航图 */
async function initImages() {
  const exts = ['webp', 'jpg', 'png', 'jpeg']
  const lunbo = []
  for (let i = 1; i <= 8; i++) {
    let ok = null
    for (const ext of exts) {
      ok = await loadImage(`/images/lunbo${i}.${ext}`)
      if (ok) break
    }
    if (ok) lunbo.push(ok)
  }
  imgList.value = lunbo.length ? lunbo : ['/images/lunbo1.jpg']

  const banners = []
  for (let i = 1; i <= 12; i++) {
    let ok = null
    for (const ext of exts) {
      ok = await loadImage(`/images/banner${i}.${ext}`)
      if (ok) break
    }
    if (ok) banners.push(ok)
  }
  bannerImages.value = banners.map((src, idx) => ({ src, title: bannerTitles[idx] || '' }))
}

const prev = () => {
  imgCount.value--
  if (imgCount.value == -1) imgCount.value = imgList.value.length - 1
}
const next = () => {
  imgCount.value++
  if (imgCount.value == imgList.value.length) imgCount.value = 0
}

const stopSwiper = () => clearInterval(timeOut)
const startSwiper = () => {
  clearInterval(timeOut)
  timeOut = setInterval(next, 3000)
}
onMounted(() => {
  initImages().then(startSwiper)
})
</script>

<style>
/* ===== 轮播区 ===== */
.banner {
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #0A0A0A;
}

/* 侧边分类 */
.site-category {
  position: absolute;
  top: 0;
  left: 0;
  height: 560px;
  width: 234px;
  background: rgba(10, 10, 10, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  z-index: 10;
}
.site-category ul {
  height: 520px;
  width: 234px;
  margin: 20px 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
.site-category li {
  padding-left: 32px;
  height: 44px;
  line-height: 44px;
  color: rgba(255, 255, 255, 0.78);
  cursor: default;
  transition: all 250ms cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.site-category li:hover {
  background: rgba(255, 103, 0, 0.18);
  color: #fff;
}
.site-category li span {
  float: right;
  margin-right: 20px;
  font-weight: 600;
  opacity: 0.5;
  transition: opacity 250ms ease;
}
.site-category li:hover span { opacity: 1; }

/* 轮播图 */
.banner-img img {
  width: 100%;
  height: 560px;
  object-fit: cover;
  display: block;
}

/* 左右箭头 */
.swiper-button-prev,
.swiper-button-next {
  position: absolute;
  top: 50%;
  width: 48px;
  height: 72px;
  margin-top: -36px;
  background: url(../assets/images/icon-slides.png) no-repeat;
  opacity: 0;
  transition: opacity 300ms ease;
  cursor: pointer;
  z-index: 5;
}
.swiper:hover .swiper-button-prev,
.swiper:hover .swiper-button-next { opacity: 1; }
.swiper-button-prev {
  left: 234px;
  background-position: -84px 50%;
}
.swiper-button-prev:hover { background-position: 0 50%; }
.swiper-button-next {
  right: 0;
  background-position: -125px 50%;
}
.swiper-button-next:hover { background-position: -42px 50%; }

/* 指示点 */
.swiper-pagination {
  position: absolute;
  right: 30px;
  bottom: 20px;
  height: 20px;
}
.swiper-pagination ul { display: flex; gap: 8px; }
.swiper-pagination-bullet {
  width: 8px;
  height: 8px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 250ms cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.swiper-pagination-bullet-active,
.swiper-pagination-bullet:hover {
  background: rgba(255, 255, 255, 0.85);
  border-color: rgba(255, 255, 255, 0.85);
  transform: scale(1.2);
}

/* ===== 导航网格 ===== */
.banner-nav {
  background: var(--c-ground);
  padding: var(--s-32) 0 var(--s-40);
  border-bottom: 1px solid var(--c-hairline);
}
.banner-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--s-24) var(--s-16);
}
.bn-item {
  text-align: center;
  padding: var(--s-8);
  border-radius: var(--r-lg);
  transition: background 200ms ease;
}
.bn-item:hover { background: var(--c-soft); }
.banner-icon {
  width: 88px;
  height: 88px;
  object-fit: contain;
  display: inline-block;
  transition: transform 300ms cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.bn-item:hover .banner-icon { transform: translateY(-4px); }
.caption {
  margin-top: var(--s-12);
  font-size: 13px;
  color: var(--c-ink);
  font-weight: 500;
}

/* ===== 响应式 ===== */
@media (max-width: 1280px) {
  .banner-img img,
  .site-category { height: 420px; }
  .site-category ul { height: 380px; }
}
@media (max-width: 900px) {
  .site-category { display: none; }
  .swiper-button-prev { left: 0; }
  .banner-img img { height: 320px; }
}
@media (max-width: 640px) {
  .banner-img img { height: 240px; }
  .banner-grid { grid-template-columns: repeat(3, 1fr); }
  .banner-icon { width: 72px; height: 72px; }
}
@media (max-width: 420px) {
  .banner-img img { height: 200px; }
  .banner-grid { grid-template-columns: repeat(2, 1fr); }
  .banner-icon { width: 64px; height: 64px; }
}
</style>
