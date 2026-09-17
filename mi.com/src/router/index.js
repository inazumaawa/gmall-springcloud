import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Center from '../components/Center.vue'
import MyOrder from '../components/MyOrder.vue'
import MyReview from '../components/MyReview.vue'
import OrderDetail from '../components/OrderDetail.vue'
import MyCoupon from '../components/MyCoupon.vue'
import MyAfterSales from '../components/MyAfterSales.vue'
import MyFavorite from '../components/MyFavorite.vue'
import MyProfile from '../components/MyProfile.vue'
import MyAddress from '../components/MyAddress.vue'
import ChatService from '../components/ChatService.vue'
import Search from '../components/Search.vue'
import Detail from '../components/Detail.vue'
import Home from '../components/Home.vue'
import Cart from '../components/Cart.vue'
import Admin from '../components/Admin.vue'
import AdminUsers from '../components/AdminUsers.vue'
import AdminOrders from '../components/AdminOrders.vue'
import AdminProducts from '../components/AdminProducts.vue'
import AdminStats from '../components/AdminStats.vue'
import AdminChat from '../components/AdminChat.vue'

  


const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { name: 'home', path: '/home', component: Home },
    { name: 'login', path: '/login', component: Login },
    { name: 'register', path: '/register', component: Register },
    { name: 'center', path: '/center', component: Center, meta: { requiresAuth: true }, children: [
      { path: 'myorder', component: MyOrder },
      { path: 'myreview', component: MyReview },
      { path: 'mycoupon', component: MyCoupon },
      { path: 'myaftersales', component: MyAfterSales },
      { path: 'myfavorite', component: MyFavorite },
      { path: 'profile', component: MyProfile },
      { path: 'addresses', component: MyAddress },
      { path: 'service', component: ChatService },
      { path: 'orderdetail/:id', component: OrderDetail },
    ] },
    { name: 'search', path: '/search/:keyword?', component: Search },
    { name: 'detail', path: '/detail/:id', component: Detail },
    { name: 'cart', path: '/cart', component: Cart },
    { name: 'admin', path: '/admin', component: Admin, meta: { requiresAuth: true, requiresAdmin: true }, children: [
      { path: 'users', component: AdminUsers },
      { path: 'orders', component: AdminOrders },
      { path: 'products', component: AdminProducts },
      { path: 'stats', component: AdminStats },
      { path: 'chat', component: AdminChat },
    ] },
  ]
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  if (to.matched.some(record => record.meta && record.meta.requiresAuth) && !token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }
  let isAdmin = false
  try {
    const raw = localStorage.getItem('profile')
    const p = raw ? JSON.parse(raw) : null
    const role = String((p && (p.role || p.urole)) || '').toLowerCase()
    isAdmin = !!(p && (role === 'admin' || p.isAdmin === true || String(p.username || p.uname || '').toLowerCase() === 'admin'))
  } catch {}
  if (to.matched.some(record => record.meta && record.meta.requiresAdmin)) {
    if (!isAdmin) {
      return { path: '/home' }
    }
  }
  // 管理员使用客服工作台，不进入用户端客服聊天，避免管理员与自己对话
  if (isAdmin && to.path === '/center/service') {
    return { path: '/admin/chat' }
  }
})

export default router
