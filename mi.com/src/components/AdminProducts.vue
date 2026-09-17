<template>
  <div class="admin-section">
    <div class="section-head">
      <h2 class="head-title">商品管理</h2>
      <span class="head-count" v-if="!loading">{{ products.length }} 个商品</span>
    </div>
    <div v-if="loading" class="loading-block"><div class="spinner"></div><p>加载中...</p></div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>图片</th><th>名称</th><th>价格</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id || p.gid }}</td>
            <td>
              <img v-if="p.gpic" class="prod-thumb" :src="fixUrl(p.gpic)" alt="" @error="onImgErr($event)" />
              <span v-else class="no-img">—</span>
            </td>
            <td>{{ p.gname }}</td>
            <td>¥{{ formatMoney(p.gprice) }}</td>
            <td class="actions-cell">
              <button class="btn-sm btn-outline" @click="startEdit(p)">编辑</button>
              <button class="btn-sm btn-danger" @click="remove(p.id || p.gid)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <button class="btn-primary btn-add" @click="openAdd">新增商品</button>

    <!-- 编辑/新增弹窗 -->
    <div class="modal-overlay" v-if="editing" @click.self="cancelEdit">
      <div class="modal-card">
        <h3 class="modal-title">{{ editForm.id ? '编辑商品' : '新增商品' }}</h3>
        <div class="form-grid">
          <!-- 商品图片 -->
          <label class="form-label full">商品图片
            <div class="img-upload">
              <img v-if="imagePreview" class="upload-preview" :src="imagePreview" alt="" />
              <div v-else class="upload-placeholder">暂无图片</div>
              <div class="upload-actions">
                <label class="btn-sm btn-outline upload-btn" for="prod-img-input">选择图片</label>
                <input id="prod-img-input" type="file" accept="image/*" style="display:none" @change="onImgChange" />
                <span v-if="uploading" class="upload-tip">上传中...</span>
              </div>
            </div>
          </label>
          <label class="form-label">名称 <input class="form-input" v-model="editForm.gname" /></label>
          <label class="form-label">价格 <input class="form-input" type="number" step="0.01" v-model="editForm.gprice" /></label>
          <label class="form-label">分类
            <select class="form-input" v-model="editForm.types">
              <option v-for="c in categories" :key="c.cid" :value="c.cid">{{ c.cname }}</option>
            </select>
          </label>
          <label class="form-label full">描述 <textarea class="form-textarea" v-model="editForm.gdetails" rows="3"></textarea></label>
        </div>
        <div class="modal-actions">
          <button class="btn-primary" @click="save" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
          <button class="btn-outline" @click="cancelEdit">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { admin, obs, category, fixImageUrl } from '../api'

const products = ref([])
const categories = ref([])
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editForm = ref({ id: '', gid: '', gname: '', gprice: '', gdetails: '', gpic: '', types: 0 })
const imagePreview = ref('')
const selectedFile = ref(null)

/** 加载 */
onMounted(async () => {
  await Promise.all([load(), loadCategories()])
})

async function load() {
  try { const body = await admin.goods.list(); products.value = (body.data || []).map(p => ({ ...p, id: p.gid })) }
  finally { loading.value = false }
}

async function loadCategories() {
  try { const body = await category.list(); categories.value = body.data || [] }
  catch { /* 降级：保持空列表 */ }
}

function fixUrl(url) { return fixImageUrl(url) }
function onImgErr(e) { e.target.style.display = 'none' }

function openAdd() {
  editForm.value = { id: '', gid: '', gname: '', gprice: '', gdetails: '', gpic: '', types: 0 }
  imagePreview.value = ''
  selectedFile.value = null
  editing.value = true
}

function startEdit(p) {
  editForm.value = { id: p.gid, gid: p.gid, gname: p.gname, gprice: p.gprice, gdetails: p.gdetails || '', gpic: p.gpic || '', types: p.types ?? 0 }
  imagePreview.value = p.gpic ? fixUrl(p.gpic) : ''
  selectedFile.value = null
  editing.value = true
}

function cancelEdit() { editing.value = false }

/** 选择图片 */
function onImgChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { alert('请选择图片文件'); return }
  if (file.size > 5 * 1024 * 1024) { alert('图片大小不能超过 5MB'); return }
  selectedFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}

/** 保存 */
async function save() {
  const f = editForm.value
  if (!f.gname || !f.gprice) { alert('请填写名称和价格'); return }
  try {
    saving.value = true
    // 如果有新图片，先上传到 OBS
    let gpic = f.gpic
    if (selectedFile.value) {
      uploading.value = true
      const objectKey = `goods/${f.gname}.jpg`
      const uploadRes = await obs.replace(selectedFile.value, objectKey)
      gpic = uploadRes.data?.url || ''
      uploading.value = false
    }
    // 保存商品信息
    const payload = { gname: f.gname, gprice: Number(f.gprice), gdetails: f.gdetails || '', gpic, types: f.types ?? 0 }
    if (f.id) {
      await admin.goods.updateProduct(f.id, { ...payload, gid: f.id })
    } else {
      await admin.goods.createProduct(payload)
    }
    editing.value = false
    await load()
  } catch (e) { alert(e.message || '保存失败') }
  finally { saving.value = false; uploading.value = false }
}

/** 删除 */
async function remove(id) { if (!confirm('确定删除该商品吗？')) return; await admin.goods.deleteProduct(id); await load() }

function formatMoney(v) { const n = Number(v || 0); return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) }
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

.prod-thumb { width: 40px; height: 40px; border-radius: var(--r-sm); object-fit: cover; border: 1px solid var(--c-hairline); }
.no-img { color: var(--c-muted); font-size: 13px; }

.btn-sm { height: 32px; padding: 0 var(--s-12); font-size: 12px; border-radius: var(--r-sm); cursor: pointer; border: 1px solid var(--c-hairline); background: var(--c-ground); color: var(--c-ink); transition: all var(--dur-fast) ease; }
.btn-sm:hover { border-color: var(--c-ink); }
.btn-danger { color: var(--c-danger); border-color: var(--c-danger-bg); background: var(--c-danger-bg); }
.btn-danger:hover { background: var(--c-danger); color: #fff; border-color: var(--c-danger); }

.btn-add { margin-top: var(--s-16); }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 200; }
.modal-card { background: var(--c-ground); width: 520px; border-radius: var(--r-xl); padding: var(--s-32); box-shadow: var(--shadow-xl); }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 var(--s-20); }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-12); }
.form-label { font-size: 13px; color: var(--c-muted); display: flex; flex-direction: column; gap: var(--s-4); }
.form-label.full { grid-column: 1 / -1; }
.form-input { height: 40px; padding: 0 var(--s-12); border: 1px solid var(--c-hairline); border-radius: var(--r-sm); font-size: 13px; outline: none; color: var(--c-ink); background: var(--c-ground); box-sizing: border-box; }
.form-input:focus { border-color: var(--c-accent); }
.form-textarea { padding: var(--s-12); border: 1px solid var(--c-hairline); border-radius: var(--r-sm); font-size: 13px; color: var(--c-ink); outline: none; resize: vertical; font-family: inherit; background: var(--c-ground); box-sizing: border-box; }
.form-textarea:focus { border-color: var(--c-accent); }
.modal-actions { margin-top: var(--s-20); display: flex; gap: var(--s-12); }

/* 图片上传 */
.img-upload { display: flex; align-items: center; gap: var(--s-12); }
.upload-preview { width: 80px; height: 80px; border-radius: var(--r-md); object-fit: cover; border: 1px solid var(--c-hairline); }
.upload-placeholder { width: 80px; height: 80px; border-radius: var(--r-md); background: var(--c-soft); border: 1px dashed var(--c-hairline); display: flex; align-items: center; justify-content: center; color: var(--c-muted); font-size: 12px; }
.upload-actions { display: flex; flex-direction: column; gap: var(--s-4); }
.upload-btn { cursor: pointer; }
.upload-tip { font-size: 11px; color: var(--c-muted); }

.loading-block { text-align: center; color: var(--c-muted); padding: var(--s-40) 0; }
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
