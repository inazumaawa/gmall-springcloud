<template>
    <div class="header">


        <!-- 主 Header -->
        <div class="site-header">
            <div class="header-bar">
                <!-- Logo -->
                <router-link to="/" class="header-logo">
                    <img src="/images/logo_app.png" alt="VMALL" />
                </router-link>

                <!-- 搜索栏 -->
                <div class="header-search">
                    <form class="search-form" @submit.prevent="goSearch">
                        <input
                            type="text"
                            class="search-text"
                            placeholder="搜索华为商城"
                            v-model="keyword"
                        />
                        <button class="search-btn" type="submit" aria-label="搜索">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                        </button>
                    </form>
                </div>

                <!-- 右侧操作区 -->
                <div class="header-actions">
                    <template v-if="!isLoggedIn">
                        <router-link to="/login" class="action-link">登录</router-link>
                        <router-link to="/register" class="action-link">注册</router-link>
                    </template>
                    <template v-else>
                        <!-- 头像 + 用户名 -->
                        <router-link to="/center" class="user-badge">
                            <img
                                v-if="avatarUrl"
                                class="avatar-img"
                                :src="avatarUrl"
                                alt=""
                                @error="onAvatarErr"
                            />
                            <span v-else class="avatar-fallback">{{ avatarLetter }}</span>
                            <span class="user-label">{{ displayName }}</span>
                        </router-link>
                        <span class="sep">|</span>
                        <a href="javascript:void(0)" @click="doLogout" class="action-link">退出</a>
                    </template>
                    <router-link to="/cart" class="action-link cart-link">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>
                        <span>购物车</span>
                    </router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue"
import { useRouter } from "vue-router"
import { usercenter, auth } from "../api"

const keyword = ref("")
const router = useRouter()

/** 跳转搜索 */
function goSearch() {
  if (keyword.value.trim()) router.push({ path: `/search/${keyword.value.trim()}` })
}

const isLoggedIn = ref(!!localStorage.getItem('uid'))
const displayName = ref('')
const avatarUrl = ref('')

/** 头像首字母回退 */
const avatarLetter = computed(() => {
  const name = displayName.value || '用'
  return name.charAt(0)
})

/** 头像加载失败时清空，回退到字母 */
function onAvatarErr() {
  avatarUrl.value = ''
}

/** 登出 */
async function doLogout() {
  try { await auth.logout() } catch {}
  localStorage.removeItem('token')
  localStorage.removeItem('uid')
  localStorage.removeItem('profile')
  isLoggedIn.value = false
  displayName.value = ''
  avatarUrl.value = ''
  router.push('/login')
}

/** 刷新用户信息 */
async function refreshUser() {
  isLoggedIn.value = !!localStorage.getItem('uid')
  if (!isLoggedIn.value) { displayName.value = ''; avatarUrl.value = ''; return }
  try {
    // 先从缓存读取
    const raw = localStorage.getItem('profile')
    const p = raw ? JSON.parse(raw) : {}
    displayName.value = p.nickname || p.username || '个人中心'
    avatarUrl.value = p.avatar || ''
  } catch {}
  // 再从服务端拉取
  try {
    const body = await usercenter.profile()
    const u = body.data || {}
    displayName.value = u.uname || '个人中心'
    avatarUrl.value = u.uavatar || ''
    localStorage.setItem('profile', JSON.stringify({
      id: u.uaccount,
      username: u.uname,
      nickname: u.uname,
      email: u.uemail,
      role: u.urole,
      avatar: u.uavatar || ''
    }))
  } catch (e) {
    // 仅 401/403 才清除登录态，网络超时/503等不影响
    if (e && (e.response && (e.response.status === 401 || e.response.status === 403))) {
      localStorage.removeItem('token')
      localStorage.removeItem('uid')
      localStorage.removeItem('profile')
      isLoggedIn.value = false
      displayName.value = ''
      avatarUrl.value = ''
    }
    // 其他错误（网络慢、服务熔断）保留登录态，下次再用
  }
}

onMounted(() => {
  refreshUser()
  window.addEventListener('storage', onStorageChange)
  window.addEventListener('focus', refreshUser)
})
onUnmounted(() => {
  window.removeEventListener('storage', onStorageChange)
  window.removeEventListener('focus', refreshUser)
})

function onStorageChange(e) {
  if (e.key === 'uid' || e.key === 'profile') refreshUser()
}
router.afterEach(() => refreshUser())
</script>

<style>
/* ===== 顶栏 ===== */
.site-topbar {
  height: 34px;
  background: var(--grad-header);
  font-size: 12px;
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--s-16);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}
.topbar-inner .sep {
  margin: 0 var(--s-8);
  color: rgba(255, 255, 255, 0.18);
}
.topbar-inner a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  line-height: 34px;
  transition: color var(--dur-fast) ease;
}
.topbar-inner a:hover { color: rgba(255, 255, 255, 0.9); }
.topbar-left { display: flex; align-items: center; }
.topbar-right { display: flex; align-items: center; }

/* ===== 主 Header 栏 ===== */
.site-header {
  background: var(--c-ground);
  border-bottom: 1px solid var(--c-hairline);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--s-16);
  display: flex;
  align-items: center;
  height: 60px;
  gap: var(--s-24);
}
.header-logo {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.header-logo img {
  height: 32px;
  display: block;
}

/* ===== 搜索栏 ===== */
.header-search { flex: 1; max-width: 480px; }
.search-form {
  display: flex;
  align-items: center;
  border: 1px solid var(--c-hairline);
  border-radius: 24px;
  overflow: hidden;
  background: var(--c-soft);
  transition: border-color var(--dur-fast) ease, box-shadow var(--dur-fast) ease;
}
.search-form:focus-within {
  border-color: var(--c-accent);
  box-shadow: 0 0 0 3px rgba(202, 20, 28, 0.1);
  background: var(--c-ground);
}
.search-text {
  flex: 1;
  height: 38px;
  padding: 0 var(--s-16);
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--c-ink);
  background: transparent;
}
.search-text::placeholder { color: var(--c-muted); }
.search-btn {
  width: 44px;
  height: 38px;
  border: none;
  background: transparent;
  color: var(--c-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color var(--dur-fast) ease;
}
.search-btn:hover { color: var(--c-accent); }

/* ===== 右侧操作区 ===== */
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  flex-shrink: 0;
  margin-left: auto;
}
.header-actions .sep {
  color: var(--c-hairline);
  font-size: 13px;
}
.action-link {
  font-size: 14px;
  color: var(--c-ink);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--dur-fast) ease;
  white-space: nowrap;
}
.action-link:hover { color: var(--c-accent); }

/* 用户头像徽章 */
.user-badge {
  display: flex;
  align-items: center;
  gap: var(--s-8);
  text-decoration: none;
  padding: var(--s-4) var(--s-8);
  border-radius: 20px;
  transition: background var(--dur-fast) ease;
}
.user-badge:hover { background: var(--c-soft); }
.avatar-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid var(--c-hairline);
}
.avatar-fallback {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--grad-warm);
  color: var(--c-accent);
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--c-accent-soft);
}
.user-label {
  font-size: 13px;
  color: var(--c-ink);
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 购物车 */
.cart-link {
  display: flex;
  align-items: center;
  gap: var(--s-4);
  padding: var(--s-4) var(--s-12);
  border-radius: 20px;
  transition: background var(--dur-fast) ease;
}
.cart-link:hover { background: var(--c-soft); }

@media (max-width: 768px) {
  .header-bar { height: 52px; gap: var(--s-12); flex-wrap: wrap; }
  .header-search { max-width: none; order: 3; flex-basis: 100%; }
  .header-logo img { height: 28px; }
  .user-label { display: none; }
}
</style>
