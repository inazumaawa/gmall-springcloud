<template>
  <div class="address-section">
    <div class="section-head">
      <h2 class="head-title">收货地址</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="list.length" class="addr-list">
      <div class="addr-card" v-for="a in list" :key="a.id" :class="{ default: a.isDefault === 1 }">
        <div class="addr-main">
          <div class="addr-top">
            <span class="addr-name">{{ a.receiverName }}</span>
            <span class="addr-phone">{{ a.phone }}</span>
            <span class="addr-badge" v-if="a.isDefault === 1">默认</span>
          </div>
          <div class="addr-detail">{{ [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ') }}</div>
        </div>
        <div class="addr-actions">
          <button class="action-link" @click="startEdit(a)">编辑</button>
          <button class="action-link" @click="setDefault(a)">设为默认</button>
          <button class="action-link danger" @click="remove(a.id)">删除</button>
        </div>
      </div>
    </div>

    <p v-else class="empty-hint">暂无收货地址</p>

    <!-- 编辑/新增表单 -->
    <div class="form-block" v-if="editing">
      <h3 class="sub-title">{{ editForm.id ? '编辑地址' : '新增地址' }}</h3>
      <div class="form-grid">
        <input class="form-input" v-model="editForm.receiverName" placeholder="收货人姓名" />
        <input class="form-input" v-model="editForm.phone" placeholder="手机号" />
        <input class="form-input" v-model="editForm.province" placeholder="省份" />
        <input class="form-input" v-model="editForm.city" placeholder="城市" />
        <input class="form-input" v-model="editForm.district" placeholder="区/县" />
        <input class="form-input full" v-model="editForm.detail" placeholder="详细地址" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" @click="save" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
        <button class="btn-outline" @click="cancelEdit">取消</button>
      </div>
    </div>

    <button class="btn-outline-accent btn-add" @click="openAdd" v-if="!editing">新增地址</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addresses } from '../api'

const list = ref([])
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const editForm = ref({ id: '', receiverName: '', phone: '', province: '', city: '', district: '', detail: '' })

/** 加载地址 */
async function load() {
  try {
    loading.value = true
    const body = await addresses.list()
    list.value = body.data || []
  } finally { loading.value = false }
}

onMounted(async () => { await load() })

/** 设为默认 */
async function setDefault(a) {
  try {
    await addresses.setDefault(a.id)
    await load()
  } catch (e) { alert(e.message) }
}

/** 删除 */
async function remove(id) {
  if (!confirm('确定删除该地址吗？')) return
  try { await addresses.delete(id); await load() } catch (e) { alert(e.message) }
}

function openAdd() {
  editForm.value = { id: '', receiverName: '', phone: '', province: '', city: '', district: '', detail: '' }
  editing.value = true
}

function startEdit(a) {
  editForm.value = { ...a }
  editing.value = true
}

function cancelEdit() { editing.value = false }

/** 保存 */
async function save() {
  const f = editForm.value
  if (!f.receiverName || !f.phone || !f.detail) { alert('请填写完整信息'); return }
  try {
    saving.value = true
    const payload = { receiverName: f.receiverName, phone: f.phone, province: f.province, city: f.city, district: f.district, detail: f.detail }
    if (f.id) { await addresses.update(payload) }
    else { await addresses.add(payload) }
    editing.value = false
    await load()
  } catch (e) { alert(e.message || '保存失败') }
  finally { saving.value = false }
}
</script>

<style scoped>
.address-section { margin: 0; }

.section-head {
  margin-bottom: var(--s-20);
  padding-bottom: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.head-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
}

.sub-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--c-ink);
  margin: 0 0 var(--s-12);
}

/* 地址列表 */
.addr-list {
  display: grid;
  gap: var(--s-12);
}
.addr-card {
  padding: var(--s-20);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  background: var(--c-ground);
  transition: box-shadow var(--dur-fast) ease;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--s-16);
}
.addr-card:hover { box-shadow: var(--shadow-md); }
.addr-card.default { border-color: var(--c-accent); background: var(--c-accent-soft); }

.addr-main { flex: 1; min-width: 0; }
.addr-top { display: flex; align-items: center; gap: var(--s-12); margin-bottom: var(--s-6); flex-wrap: wrap; }
.addr-name { font-size: 15px; font-weight: 600; color: var(--c-ink); }
.addr-phone { font-size: 14px; color: var(--c-muted); }
.addr-badge {
  font-size: 11px;
  padding: 2px 10px;
  background: var(--c-accent);
  color: #fff;
  border-radius: var(--r-sm);
  font-weight: 500;
}
.addr-detail { font-size: 13px; color: var(--c-muted); line-height: 1.6; }

.addr-actions {
  display: flex;
  gap: var(--s-8);
  flex-shrink: 0;
  flex-wrap: wrap;
}
.action-link {
  background: none;
  border: none;
  font-size: 13px;
  color: var(--c-muted);
  cursor: pointer;
  padding: var(--s-4) var(--s-8);
  transition: color var(--dur-fast) ease;
}
.action-link:hover { color: var(--c-accent); }
.action-link.danger:hover { color: var(--c-danger); }

/* 表单 */
.form-block {
  margin-top: var(--s-24);
  padding-top: var(--s-24);
  border-top: 1px solid var(--c-hairline);
  max-width: 560px;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--s-12);
}
.form-input {
  height: 40px;
  padding: 0 var(--s-12);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-sm);
  font-size: 13px;
  color: var(--c-ink);
  outline: none;
  transition: border-color var(--dur-fast) ease;
  background: var(--c-ground);
  box-sizing: border-box;
}
.form-input:focus { border-color: var(--c-accent); }
.form-input.full { grid-column: 1 / -1; }

.form-actions {
  margin-top: var(--s-16);
  display: flex;
  gap: var(--s-12);
}

.btn-add {
  margin-top: var(--s-16);
  height: 40px;
}

.empty-hint { color: var(--c-muted); font-size: 14px; text-align: center; padding: var(--s-24) 0; }

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }

@media (max-width: 640px) {
  .add-card { flex-direction: column; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
