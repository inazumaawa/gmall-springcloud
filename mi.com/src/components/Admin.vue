<template>
  <div class="admin-dashboard" v-if="isAdmin">
    <div class="dashboard-layout">
      <!-- 侧边栏 -->
      <aside class="admin-sidebar">
        <div class="sidebar-brand">
          <h2 class="brand-title">管理后台</h2>
        </div>
        <nav class="sidebar-nav">
          <router-link to="/admin/users" class="nav-item" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
            用户管理
          </router-link>
          <router-link to="/admin/orders" class="nav-item" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>
            订单管理
          </router-link>
          <router-link to="/admin/products" class="nav-item" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
            商品管理
          </router-link>
          <router-link to="/admin/stats" class="nav-item" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            数据统计
          </router-link>
          <router-link to="/admin/chat" class="nav-item" active-class="active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            客服工作台
          </router-link>
        </nav>
        <div class="sidebar-footer">
          <router-link to="/" class="back-link">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            返回首页
          </router-link>
        </div>
      </aside>

      <!-- 主内容 -->
      <section class="admin-content">
        <router-view></router-view>
      </section>
    </div>
  </div>
  <div v-else class="unauthorized">
    <h2>无权限访问</h2>
    <p>您没有管理员权限</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const isAdmin = ref(false)

/** 检查管理员权限 */
onMounted(() => {
  try {
    const raw = localStorage.getItem('profile')
    const p = raw ? JSON.parse(raw) : {}
    isAdmin.value = String(p.role || '').toUpperCase() === 'ADMIN'
  } catch { isAdmin.value = false }
})
</script>

<style scoped>
.admin-dashboard {
  background: var(--c-soft);
  min-height: calc(100vh - 100px);
}

.dashboard-layout {
  max-width: 1300px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 220px 1fr;
  min-height: calc(100vh - 100px);
}

/* 侧边栏 */
.admin-sidebar {
  background: var(--c-ink);
  color: #fff;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 60px;
  height: calc(100vh - 100px);
}
.sidebar-brand {
  padding: var(--s-24) var(--s-20);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: -0.01em;
}

.sidebar-nav {
  flex: 1;
  padding: var(--s-16) var(--s-12);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  padding: var(--s-12) var(--s-16);
  font-size: 14px;
  color: rgba(255,255,255,0.55);
  text-decoration: none;
  border-radius: var(--r-md);
  transition: all var(--dur-fast) ease;
}
.nav-item:hover { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.9); }
.nav-item.active {
  background: var(--c-accent);
  color: #fff;
  font-weight: 600;
}
.nav-item svg { opacity: 0.6; flex-shrink: 0; }
.nav-item.active svg { opacity: 1; }

.sidebar-footer {
  padding: var(--s-12);
  border-top: 1px solid rgba(255,255,255,0.08);
}
.back-link {
  display: flex;
  align-items: center;
  gap: var(--s-8);
  padding: var(--s-10) var(--s-16);
  font-size: 13px;
  color: rgba(255,255,255,0.4);
  text-decoration: none;
  border-radius: var(--r-md);
  transition: all var(--dur-fast) ease;
}
.back-link:hover { background: rgba(255,255,255,0.06); color: rgba(255,255,255,0.7); }

/* 主内容 */
.admin-content {
  padding: var(--s-24);
  overflow-y: auto;
}

.unauthorized {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 150px);
  color: var(--c-muted);
}
.unauthorized h2 { font-size: 22px; color: var(--c-ink); margin: 0 0 var(--s-8); }

@media (max-width: 768px) {
  .dashboard-layout { grid-template-columns: 1fr; }
  .admin-sidebar { height: auto; position: static; }
}
</style>
