<template>
  <div class="center-page">
    <div class="center-container">
      <div class="center-layout">
        <!-- 侧边栏 -->
        <aside class="sidebar">
          <!-- 用户卡片 -->
          <div class="user-card">
            <div class="avatar-wrap">
              <img
                v-if="avatarUrl"
                class="avatar-img"
                :src="avatarUrl"
                alt=""
                @error="onAvatarErr"
              />
              <div v-else class="avatar-circle">{{ avatarLetter }}</div>
            </div>
            <div class="user-info">
              <h3 class="user-name">{{ displayName }}</h3>
              <span class="user-role">{{ userRole }}</span>
            </div>
          </div>

          <!-- 导航 -->
          <nav class="nav-menu">
            <div class="nav-group">
              <div class="nav-label">订单中心</div>
              <router-link class="nav-item" to="/center/myorder">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
                我的订单
              </router-link>
              <router-link class="nav-item" to="/center/myreview">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                我的评价
              </router-link>
            </div>
            <div class="nav-group">
              <div class="nav-label">我的资产</div>
              <router-link class="nav-item" to="/center/mycoupon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 12V8H6a2 2 0 01-2-2c0-1.1.9-2 2-2h12v4"/><path d="M4 6v12c0 1.1.9 2 2 2h14v-4"/><path d="M18 12a2 2 0 000 4h4v-4h-4z"/></svg>
                我的优惠券
              </router-link>
              <router-link class="nav-item" to="/center/myfavorite">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
                我的收藏
              </router-link>
            </div>
            <div class="nav-group">
              <div class="nav-label">设置</div>
              <router-link class="nav-item" to="/center/profile">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                个人信息
              </router-link>
              <router-link class="nav-item" to="/center/addresses">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                收货地址
              </router-link>
              <router-link v-if="!isAdmin" class="nav-item" to="/center/service">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 18v-6a9 9 0 0 1 18 0v6"/><path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/></svg>
                联系客服
              </router-link>
            </div>
            <div class="nav-group" v-if="isAdmin">
              <div class="nav-label">管理</div>
              <router-link class="nav-item" to="/admin/users">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
                管理后台
              </router-link>
            </div>
          </nav>

          <!-- 底部 -->
          <div class="sidebar-footer">
            <router-link to="/" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              返回首页
            </router-link>
          </div>
        </aside>

        <!-- 主内容区 -->
        <section class="main-content">
          <router-view></router-view>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const displayName = ref('')
const avatarUrl = ref('')
const userRole = ref('普通用户')
const isAdmin = ref(false)

/** 头像首字母回退 */
const avatarLetter = computed(() => {
  const name = displayName.value || '用'
  return name.charAt(0)
})

function onAvatarErr() { avatarUrl.value = '' }

/** 读取用户信息 */
onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token) { displayName.value = '用户'; return }
  try {
    const raw = localStorage.getItem('profile')
    const p = raw ? JSON.parse(raw) : {}
    displayName.value = p.nickname || p.username || '用户'
    avatarUrl.value = p.avatar || ''
    const role = String(p.role || '').toUpperCase()
    if (role === 'ADMIN') { userRole.value = '管理员'; isAdmin.value = true }
    else if (role === 'SELLER') userRole.value = '商家'
  } catch {
    displayName.value = '用户'
  }
})
</script>

<style scoped>
/* ===== 页面布局 ===== */
.center-page {
  background: var(--c-soft);
  min-height: calc(100vh - 100px);
  padding: var(--s-32) 0;
}
.center-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--s-16);
}
.center-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: var(--s-24);
  align-items: start;
}

/* ===== 侧边栏 ===== */
.sidebar {
  background: var(--c-ground);
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  min-height: 560px;
  position: sticky;
  top: 80px;
}

/* 用户卡片 */
.user-card {
  padding: var(--s-32) var(--s-24) var(--s-24);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.avatar-wrap { flex-shrink: 0; }
.avatar-img {
  width: 64px; height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--c-hairline);
}
.avatar-circle {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: var(--c-soft);
  color: var(--c-accent);
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-info { text-align: center; }
.user-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--c-ink);
  letter-spacing: -0.01em;
}
.user-role {
  display: inline-block;
  margin-top: var(--s-4);
  font-size: 12px;
  color: var(--c-muted);
}

/* 导航 */
.nav-menu {
  flex: 1;
  padding: var(--s-12);
  overflow-y: auto;
}
.nav-group { margin-bottom: var(--s-8); }
.nav-group:last-child { margin-bottom: 0; }
.nav-label {
  padding: var(--s-12) var(--s-12) var(--s-6);
  font-size: 11px;
  color: var(--c-muted);
  font-weight: 600;
  letter-spacing: 0.05em;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  padding: var(--s-10) var(--s-12);
  font-size: 14px;
  color: var(--c-ink-soft);
  text-decoration: none;
  border-radius: var(--r-md);
  margin-bottom: 2px;
  transition: all var(--dur-fast) ease;
  font-weight: 400;
}
.nav-item svg { color: var(--c-muted); transition: color var(--dur-fast) ease; flex-shrink: 0; }
.nav-item:hover { background: var(--c-soft); color: var(--c-ink); }
.nav-item:hover svg { color: var(--c-accent); }
.nav-item.router-link-active {
  background: var(--c-accent-soft);
  color: var(--c-accent);
  font-weight: 600;
}
.nav-item.router-link-active svg { color: var(--c-accent); }

/* 底部 */
.sidebar-footer {
  padding: var(--s-12);
  border-top: 1px solid var(--c-hairline);
}
.footer-link {
  display: flex;
  align-items: center;
  gap: var(--s-8);
  padding: var(--s-10) var(--s-12);
  font-size: 13px;
  color: var(--c-muted);
  text-decoration: none;
  border-radius: var(--r-md);
  transition: all var(--dur-fast) ease;
}
.footer-link:hover { background: var(--c-soft); color: var(--c-accent); }

/* ===== 主内容区 ===== */
.main-content {
  background: var(--c-ground);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  padding: var(--s-32);
  min-height: 500px;
}

/* 响应式 */
@media (max-width: 900px) {
  .center-layout { grid-template-columns: 1fr; }
  .sidebar { min-height: auto; position: static; }
}
</style>
