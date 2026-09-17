import axios from 'axios'

const api = axios.create({
  baseURL: '/'
})

// 修复后端拼接图片URL的问题：后端会将 http://127.0.0.1:88 拼接到外部URL前
export function fixImageUrl(url) {
  if (!url) return '/images/logo_app.png'
  const str = String(url)
  // 修复重复的 http 前缀：http://127.0.0.1:88https://xxx -> https://xxx
  const cleaned = str.replace(/^http:\/\/[^/]+https?:\/\//, 'https://')
  // 如果以 / 开头则是本地相对路径，直接返回
  if (cleaned.startsWith('/')) return cleaned
  // 如果是完整外部URL直接返回
  if (/^https?:\/\//.test(cleaned)) return cleaned
  return cleaned
}

// 请求拦截器：后端网关通过 Authorization: Bearer <token> 鉴权
api.interceptors.request.use(cfg => {
  const uid = localStorage.getItem('uid')
  const token = localStorage.getItem('token')
  if (uid) cfg.headers.uid = uid
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})

// 响应拦截器：后端返回 {success, code: 200/520, data, message}
// 401 全局处理：清除登录态并重定向到登录页
api.interceptors.response.use(res => {
  if (res.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('uid')
    localStorage.removeItem('profile')
    window.location.href = '/#/login'
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
  const body = res.data
  if (body && body.code !== 200) {
    const err = new Error(body.message || '请求失败')
    err.responseBody = body
    throw err
  }
  return body
})

// ========== 认证模块 ==========
export const auth = {
  login: (uname, upassword, captchaKey, captchaCode) => api.post('/auth/login', { uname, upassword, captchaKey, captchaCode }),
  register: (payload) => api.post('/auth/register', payload),
  loginByEmail: (email, code) => api.post('/auth/emaillogin', { email, code }),
  sendCode: (email) => api.post('/auth/sendcode', { email }),
  logout: () => api.post('/auth/logout'),
  captcha: () => api.get('/auth/captcha')
}

// ========== 商品模块 ==========
export const goods = {
  list: () => api.get('/goods/list'),
  detail: (id) => api.get('/goods/detail', { params: { id } }),
  search: (keyword) => api.get('/goods/search', { params: { keyword } })
}

// ========== 商品分类模块 ==========
export const category = {
  list: () => api.get('/goods/category/list')
}

// ========== 购物车模块 ==========
export const cart = {
  add: (gid, number = 1) => api.post('/cart/add', null, { params: { gid, number } }),
  list: () => api.get('/cart/list'),
  remove: (id) => api.delete('/cart/delete', { params: { id } }),
  updateNumber: (id, number) => api.post('/cart/update', null, { params: { id, number } }),
  subList: (cartIds) => api.post('/cart/sublist', cartIds),
  deleteList: (cartIds) => api.post('/cart/deletelist', cartIds)
}

// ========== 订单模块 ==========
export const orders = {
  list: () => api.get('/order/list'),
  create: (selectedCarts, aid, userCouponId) => api.post('/order/create', {
    selectedCarts, aid, userCouponId
  }),
  remove: (oid) => api.delete('/order/delete', { params: { oid } }),
  updateAddress: (oid, aid) => api.put('/order/update-address', null, { params: { oid, aid } })
}

// ========== 支付模块 ==========
export const alipay = {
  pay: ({ traceNo, totalAmount, subject }) =>
    api.post('/alipay/pay', null, { params: { traceNo, totalAmount, subject } })
}

// ========== 收货地址模块 ==========
export const addresses = {
  list: () => api.get('/address/list'),
  add: (payload) => api.post('/address/add', payload),
  update: (payload) => api.put('/address/update', payload),
  remove: (id) => api.delete('/address/delete', { params: { id } }),
  setDefault: (id) => api.put('/address/setdefault', null, { params: { id } }),
  detail: (id) => api.get('/address/detail', { params: { id } })
}

// ========== 优惠券模块 ==========
export const coupons = {
  available: () => api.get('/coupon/available'),
  all: () => api.get('/coupon/list'),
  my: () => api.get('/coupon/my'),
  detail: (id) => api.get('/coupon/detail', { params: { id } }),
  receive: (cid) => api.post('/coupon/receive', null, { params: { cid } }),
  use: (ucId, orderAmount) => api.post('/coupon/use', { ucId, orderAmount }),
  create: (payload) => api.post('/coupon/create', payload),
  update: (payload) => api.put('/coupon/update', payload),
  remove: (id) => api.delete('/coupon/delete', { params: { id } })
}

// ========== 评价模块 ==========
export const reviews = {
  listByGoods: (gid) => api.get('/review/goods', { params: { gid } }),
  my: () => api.get('/review/user'),
  add: (payload) => api.post('/review/add', payload),
  remove: (id) => api.delete('/review/delete', { params: { id } })
}

// ========== 收藏模块 ==========
export const favorites = {
  list: () => api.get('/favorite/list'),
  add: (gid) => api.post('/favorite/add', null, { params: { gid } }),
  remove: (gid) => api.delete('/favorite/delete', { params: { gid } }),
  removeById: (id) => api.delete('/favorite/deleteById', { params: { id } }),
  check: (gid) => api.get('/favorite/check', { params: { gid } })
}

// ========== 客服模块 ==========
export const chat = {
  createSession: () => api.post('/chat/session/create'),
  history: (sessionId) => api.get('/chat/history', { params: { sessionId } }),
  sessionList: () => api.get('/chat/session/list'),
  close: (sessionId) => api.post('/chat/session/close', null, { params: { sessionId } })
}

// ========== 物流模块（并入 address） ==========
export const logistics = {
  track: (orderId) => api.get('/address/logistics/track', { params: { orderId } }),
  add: (track) => api.post('/address/logistics/add', track)
}

// ========== OBS 文件上传 ==========
export const obs = {
  upload: (file, objectKey) => {
    const form = new FormData()
    form.append('file', file)
    form.append('objectKey', objectKey)
    return api.post('/obs/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  replace: (file, objectKey) => {
    const form = new FormData()
    form.append('file', file)
    form.append('objectKey', objectKey)
    return api.put('/obs/replace', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  remove: (objectKey) => api.delete('/obs/delete', { params: { objectKey } })
}

// ========== 个人中心模块 ==========
export const usercenter = {
  profile: () => api.get('/usercenter/profile'),
  updateProfile: (payload) => api.put('/usercenter/profile', payload),
  changePassword: (payload) => api.put('/usercenter/password', payload),
  updateAvatar: (uavatar) => api.put('/usercenter/avatar', { uavatar })
}

// ========== 管理端模块 ==========
export const admin = {
  goods: {
    list: () => api.get('/admin/goods/list'),
    add: (payload) => api.post('/admin/goods/add', payload),
    update: (payload) => api.put('/admin/goods/update', payload),
    remove: (gid) => api.delete('/admin/goods/delete', { params: { gid } }),
    // 便捷别名
    products: () => api.get('/admin/goods/list'),
    createProduct: (payload) => api.post('/admin/goods/add', payload),
    updateProduct: (id, payload) => api.put('/admin/goods/update', { ...payload, gid: id }),
    deleteProduct: (gid) => api.delete('/admin/goods/delete', { params: { gid } })
  },
  orders: {
    list: () => api.get('/admin/order/list'),
    listByStatus: (status) => api.get('/admin/order/listByStatus', { params: { status } }),
    detail: (oid) => api.get('/admin/order/detail', { params: { oid } }),
    updateStatus: (id, status) => api.put('/admin/order/status', null, { params: { id, status } }),
    remove: (oid) => api.delete('/admin/order/delete', { params: { oid } }),
    // 便捷别名
    orders: () => api.get('/admin/order/list'),
    deleteOrder: (oid) => api.delete('/admin/order/delete', { params: { oid } })
  },
  users: {
    list: () => api.get('/admin/user/list'),
    remove: (user) => api.delete('/admin/user/delete', { data: user }),
    update: (id, payload) => api.put('/admin/user/update', { ...payload, uaccount: String(id) }),
    // 便捷别名
    users: () => api.get('/admin/user/list'),
    deleteUser: (id) => api.delete('/admin/user/delete', { data: { uaccount: String(id) } })
  },
  stats: () => api.get('/admin/stats')
}

export default api
