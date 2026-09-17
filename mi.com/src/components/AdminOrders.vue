<template>
  <div class="admin-section">
    <div class="section-head">
      <h2 class="head-title">订单管理</h2>
      <span class="head-count" v-if="!loading">{{ orders.length }} 个订单</span>
    </div>
    <div v-if="loading" class="loading-block"><div class="spinner"></div><p>加载中...</p></div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead><tr>
          <th>ID</th><th>用户</th><th>状态</th><th>金额</th><th>优惠</th><th>时间</th><th>操作</th>
        </tr></thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td>{{ o.id }}</td>
            <td>{{ o.uid }}</td>
            <td>
              <select class="status-select" :class="badgeClass(o.status)" :value="o.status" @change="onStatusChange(o, $event)">
                <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
              </select>
            </td>
            <td>¥{{ formatMoney(o.totalPrice) }}</td>
            <td>¥{{ formatMoney(o.discountAmount) }}</td>
            <td>{{ formatTime(o.createdTime) }}</td>
            <td class="actions-cell">
              <button class="btn-sm btn-outline" @click="showDetail(o.id)">详情</button>
              <button class="btn-sm btn-danger-outline" @click="remove(o.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情弹窗 -->
    <div class="modal-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="modal-card wide">
        <h3 class="modal-title">订单详情 #{{ detail?.id }}</h3>
        <div v-if="detailLoading" class="loading-block"><div class="spinner"></div><p>加载中...</p></div>
        <div v-else-if="detail" class="detail-body">
          <div class="detail-row">
            <span class="detail-label">订单编号</span><span>{{ detail.id }}</span>
            <span class="detail-label">用户ID</span><span>{{ detail.uid }}</span>
            <span class="detail-label">状态</span>
            <select class="status-select" :class="badgeClass(detail.status)" :value="detail.status" @change="onDetailStatusChange($event)">
              <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
            </select>
          </div>
          <div class="detail-row">
            <span class="detail-label">总金额</span><span>¥{{ formatMoney(detail.totalPrice) }}</span>
            <span class="detail-label">优惠</span><span>¥{{ formatMoney(detail.discountAmount) }}</span>
            <span class="detail-label">支付方式</span><span>{{ detail.couponId ? '优惠券 #' + detail.couponId : '支付宝' }}</span>
          </div>
          <h4 class="items-title">订单商品</h4>
          <table class="data-table" v-if="detail.orderItems?.length">
            <thead><tr><th>图片</th><th>名称</th><th>单价</th><th>数量</th><th>小计</th></tr></thead>
            <tbody>
              <tr v-for="item in detail.orderItems" :key="item.id">
                <td><img v-if="item.gpic" class="item-thumb" :src="fixUrl(item.gpic)" alt="" /></td>
                <td>{{ item.gname }}</td>
                <td>¥{{ formatMoney(item.price) }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ formatMoney((item.price || 0) * (item.quantity || 0)) }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else class="no-items">暂无商品信息</p>
          <div class="detail-actions">
            <button class="btn-outline" @click="detailVisible = false">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { admin, fixImageUrl } from '../api'

const orders = ref([])
const loading = ref(true)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const statusOptions = [
  { value: 'PAY', label: '待付款' },
  { value: 'PAID', label: '待发货' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

const statusMap = Object.fromEntries(statusOptions.map(s => [s.value, s.label]))

onMounted(async () => { await load() })

async function load() {
  try { const body = await admin.orders.list(); orders.value = body.data || [] }
  finally { loading.value = false }
}

async function onStatusChange(o, event) {
  const newStatus = event.target.value
  if (newStatus === o.status) return
  if (!confirm(`确定将订单 #${o.id} 状态改为「${statusMap[newStatus]}」吗？`)) {
    event.target.value = o.status
    return
  }
  try {
    await admin.orders.updateStatus(o.id, newStatus)
    o.status = newStatus
  } catch (e) {
    alert(e.message)
    event.target.value = o.status
  }
}

async function showDetail(oid) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const body = await admin.orders.detail(oid)
    detail.value = body.data
  } catch (e) { alert(e.message) }
  finally { detailLoading.value = false }
}

async function onDetailStatusChange(event) {
  if (!detail.value) return
  const newStatus = event.target.value
  if (!confirm(`确定将订单 #${detail.value.id} 状态改为「${statusMap[newStatus]}」吗？`)) {
    event.target.value = detail.value.status
    return
  }
  try {
    await admin.orders.updateStatus(detail.value.id, newStatus)
    detail.value.status = newStatus
    // 同步更新列表
    const o = orders.value.find(o => o.id === detail.value.id)
    if (o) o.status = newStatus
  } catch (e) {
    alert(e.message)
    event.target.value = detail.value.status
  }
}

function statusLabel(s) { return statusMap[(s || '').toUpperCase()] || s || '' }

function badgeClass(s) {
  const map = { PAY: 'badge-creating', PAID: 'badge-paid', SHIPPED: 'badge-shipped', COMPLETED: 'badge-done', CANCELLED: 'badge-cancel' }
  return map[(s || '').toUpperCase()] || ''
}

function fixUrl(url) { return fixImageUrl(url) }

function formatMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString()
}

async function remove(id) {
  if (!confirm('确定删除该订单吗？')) return
  try { await admin.orders.deleteOrder(id); await load() }
  catch (e) { alert(e.message) }
}
</script>

<style scoped>
.admin-section { margin: 0; }
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--s-20); padding-bottom: var(--s-16); border-bottom: 1px solid var(--c-hairline); }
.head-title { font-size: 18px; font-weight: 700; color: var(--c-ink); margin: 0; }
.head-count { font-size: 13px; color: var(--c-muted); }

.table-wrap { border: 1px solid var(--c-hairline); border-radius: var(--r-lg); overflow: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.data-table th, .data-table td { padding: var(--s-12) var(--s-16); text-align: left; border-bottom: 1px solid var(--c-hairline); white-space: nowrap; }
.data-table th { background: var(--c-soft-alt); font-weight: 600; color: var(--c-muted); font-size: 12px; }
.data-table td { color: var(--c-ink); }
.actions-cell { display: flex; gap: var(--s-6); }

/* 状态下拉 */
.status-select {
  padding: 2px 8px; border-radius: var(--r-full); font-size: 11px; font-weight: 500; border: none; cursor: pointer; appearance: none;
  -webkit-appearance: none; text-align: center; min-width: 80px; outline: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='5'%3E%3Cpath d='M0 0l4 5 4-5z' fill='%23888'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 6px center; padding-right: 20px;
}
.status-select.badge-creating { background-color: var(--c-warning-bg); color: var(--c-warning); }
.status-select.badge-paid { background-color: var(--c-success-bg); color: var(--c-success); }
.status-select.badge-shipped { background-color: #E8F0FE; color: #1967D2; }
.status-select.badge-done { background-color: #F3E8FD; color: #9334E6; }
.status-select.badge-cancel { background-color: var(--c-danger-bg); color: var(--c-danger); }

.btn-sm { height: 32px; padding: 0 var(--s-12); font-size: 12px; border-radius: var(--r-sm); cursor: pointer; border: 1px solid var(--c-hairline); background: var(--c-ground); color: var(--c-ink); transition: all var(--dur-fast) ease; }
.btn-sm:hover { border-color: var(--c-ink); }
.btn-danger-outline { color: var(--c-danger); border-color: var(--c-danger-bg); background: var(--c-danger-bg); }
.btn-danger-outline:hover { background: var(--c-danger); color: #fff; border-color: var(--c-danger); }

.loading-block { text-align: center; color: var(--c-muted); padding: var(--s-40) 0; }
.loading-block .spinner { margin: 0 auto var(--s-8); }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: var(--c-ground); border-radius: var(--r-xl); padding: var(--s-24); width: 90%; max-width: 700px; max-height: 85vh; overflow-y: auto; box-shadow: 0 8px 32px rgba(0,0,0,.12); }
.modal-card.wide { max-width: 800px; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 var(--s-20); }

.detail-body { font-size: 13px; }
.detail-row { display: grid; grid-template-columns: 80px 1fr 60px 1fr 60px 1fr; gap: var(--s-8) var(--s-12); align-items: center; padding: var(--s-8) 0; border-bottom: 1px solid var(--c-hairline); }
.detail-label { color: var(--c-muted); font-size: 12px; }
.items-title { font-size: 14px; font-weight: 600; margin: var(--s-16) 0 var(--s-8); }
.item-thumb { width: 36px; height: 36px; border-radius: var(--r-sm); object-fit: cover; border: 1px solid var(--c-hairline); }
.no-items { color: var(--c-muted); font-size: 13px; text-align: center; padding: var(--s-16); }
.detail-actions { display: flex; gap: var(--s-8); margin-top: var(--s-16); justify-content: flex-end; }
.detail-actions .btn-outline { height: 36px; padding: 0 var(--s-20); border-radius: var(--r-sm); font-size: 13px; cursor: pointer; border: 1px solid var(--c-hairline); background: var(--c-ground); color: var(--c-ink); transition: all var(--dur-fast) ease; }

@keyframes spin { to { transform: rotate(360deg); } }
.spinner { width: 24px; height: 24px; border: 3px solid var(--c-hairline); border-top-color: var(--c-accent); border-radius: 50%; animation: spin .6s linear infinite; }
</style>
