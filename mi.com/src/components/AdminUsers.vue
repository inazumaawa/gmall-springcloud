<template>
  <div class="admin-section">
    <div class="section-head">
      <h2 class="head-title">用户管理</h2>
      <span class="head-count" v-if="!loading">{{ users.length }} 个用户</span>
    </div>
    <div v-if="loading" class="loading-block"><div class="spinner"></div><p>加载中...</p></div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>用户名</th><th>邮箱</th><th>性别</th><th>角色</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="u in users" :key="u.id"><td>{{ u.id }}</td><td>{{ u.uname }}</td><td>{{ u.uemail }}</td><td>{{ u.usex }}</td><td>{{ u.urole }}</td>
            <td class="actions-cell">
              <button class="btn-sm btn-outline" @click="startEdit(u)">编辑</button>
              <button class="btn-sm btn-danger" @click="remove(u.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 编辑弹窗 -->
    <div class="modal-overlay" v-if="editing" @click.self="cancelEdit">
      <div class="modal-card">
        <h3 class="modal-title">编辑用户</h3>
        <div class="form-grid">
          <label class="form-label">用户名 <input class="form-input" v-model="editForm.uname" /></label>
          <label class="form-label">邮箱 <input class="form-input" v-model="editForm.uemail" /></label>
          <label class="form-label">性别
            <select class="form-input" v-model="editForm.usex"><option value="男">男</option><option value="女">女</option></select>
          </label>
          <label class="form-label">角色
            <select class="form-input" v-model="editForm.urole"><option value="USER">用户</option><option value="ADMIN">管理员</option><option value="SELLER">商家</option></select>
          </label>
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
import { admin } from '../api'

const users = ref([])
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const editForm = ref({ id: '', uname: '', uemail: '', usex: '', urole: '' })

/** 加载用户列表 */
onMounted(async () => { await load() })

async function load() { try { const body = await admin.users.list(); users.value = (body.data || []).map(u => ({ ...u, id: u.uaccount })) } finally { loading.value = false } }

function startEdit(u) { editForm.value = { ...u }; editing.value = true }
function cancelEdit() { editing.value = false }

/** 保存编辑 */
async function save() {
  try {
    saving.value = true
    await admin.users.update(editForm.value.id, {
      uname: editForm.value.uname, uemail: editForm.value.uemail,
      usex: editForm.value.usex, urole: editForm.value.urole
    })
    editing.value = false
    await load()
    alert('更新成功')
  } catch (e) { alert(e.message || '更新失败') }
  finally { saving.value = false }
}

/** 删除用户 */
async function remove(id) { if (!confirm('确定删除该用户吗？')) return; await admin.users.deleteUser(id); await load() }
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

.btn-sm { height: 32px; padding: 0 var(--s-12); font-size: 12px; border-radius: var(--r-sm); cursor: pointer; border: 1px solid var(--c-hairline); background: var(--c-ground); color: var(--c-ink); transition: all var(--dur-fast) ease; }
.btn-sm:hover { border-color: var(--c-ink); }
.btn-danger { color: var(--c-danger); border-color: var(--c-danger-bg); background: var(--c-danger-bg); }
.btn-danger:hover { background: var(--c-danger); color: #fff; border-color: var(--c-danger); }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 200; }
.modal-card { background: var(--c-ground); width: 440px; border-radius: var(--r-xl); padding: var(--s-32); box-shadow: var(--shadow-xl); }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 var(--s-20); }
.form-grid { display: grid; gap: var(--s-12); }
.form-label { font-size: 13px; color: var(--c-muted); display: flex; flex-direction: column; gap: var(--s-4); }
.form-input { height: 40px; padding: 0 var(--s-12); border: 1px solid var(--c-hairline); border-radius: var(--r-sm); font-size: 13px; outline: none; color: var(--c-ink); background: var(--c-ground); box-sizing: border-box; }
.form-input:focus { border-color: var(--c-accent); }
.modal-actions { margin-top: var(--s-20); display: flex; gap: var(--s-12); }

.loading-block { text-align: center; color: var(--c-muted); padding: var(--s-40) 0; }
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
