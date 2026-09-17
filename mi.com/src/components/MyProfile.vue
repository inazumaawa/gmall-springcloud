<template>
  <div class="profile-section">
    <div class="section-head">
      <h2 class="head-title">个人信息</h2>
    </div>

    <div v-if="loading" class="loading-block">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else>
      <!-- 信息展示 -->
      <div class="profile-info" v-if="!editing">
        <!-- 头像 -->
        <div class="avatar-section">
          <div class="avatar-preview">
            <img
              v-if="profile.uavatar"
              class="avatar-img"
              :src="profile.uavatar"
              alt="头像"
              @error="onAvatarErr"
            />
            <div v-else class="avatar-placeholder">{{ avatarLetter }}</div>
          </div>
          <div class="avatar-actions">
            <label class="btn-outline avatar-btn" for="avatar-input">更换头像</label>
            <input
              id="avatar-input"
              type="file"
              accept="image/*"
              style="display:none"
              @change="onAvatarChange"
            />
            <span v-if="uploadingAvatar" class="upload-tip">上传中...</span>
          </div>
        </div>
        <div class="info-row">
          <span class="info-key">账号</span>
          <span class="info-val">{{ profile.uaccount || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-key">用户名</span>
          <span class="info-val">{{ profile.uname || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-key">邮箱</span>
          <span class="info-val">{{ profile.uemail || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-key">性别</span>
          <span class="info-val">{{ profile.usex || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-key">角色</span>
          <span class="info-val">{{ profile.urole || '—' }}</span>
        </div>
        <div class="profile-actions">
          <button class="btn-primary" @click="startEdit">编辑资料</button>
          <button class="btn-outline" @click="showPwdForm = !showPwdForm">
            {{ showPwdForm ? '取消' : '修改密码' }}
          </button>
        </div>
      </div>

      <!-- 编辑表单 -->
      <div class="edit-form" v-if="editing">
        <div class="form-row">
          <label class="form-key">用户名</label>
          <input class="form-input" v-model="editForm.uname" placeholder="请输入用户名" />
        </div>
        <div class="form-row">
          <label class="form-key">邮箱</label>
          <input class="form-input" v-model="editForm.uemail" type="email" placeholder="请输入邮箱" />
        </div>
        <div class="form-row">
          <label class="form-key">性别</label>
          <select class="form-input" v-model="editForm.usex">
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div class="form-actions">
          <button class="btn-primary" @click="saveEdit" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
          <button class="btn-outline" @click="cancelEdit">取消</button>
        </div>
      </div>

      <!-- 修改密码 -->
      <div class="pwd-form" v-if="showPwdForm">
        <h3 class="sub-title">修改密码</h3>
        <div class="form-row">
          <label class="form-key">旧密码</label>
          <input class="form-input" v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" />
        </div>
        <div class="form-row">
          <label class="form-key">新密码</label>
          <input class="form-input" v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
        </div>
        <div class="form-actions">
          <button class="btn-primary" @click="changePwd" :disabled="changingPwd">
            {{ changingPwd ? '修改中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usercenter, obs } from '../api'

const profile = ref({})
const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const editForm = ref({ uname: '', uemail: '', usex: '' })
const showPwdForm = ref(false)
const changingPwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const uploadingAvatar = ref(false)

/** 头像首字母回退 */
const avatarLetter = computed(() => {
  const name = profile.value.uname || '用'
  return name.charAt(0)
})

function onAvatarErr() { profile.value.uavatar = '' }

/** 加载个人信息（缓存优先） */
onMounted(async () => {
  let hadCache = false
  try {
    const raw = localStorage.getItem('profile')
    if (raw) {
      const p = JSON.parse(raw)
      if (p && typeof p === 'object' && Object.keys(p).length) {
        // 统一字段名：缓存可能使用 role/avatar，内部使用 urole/uavatar
        profile.value = {
          uaccount: p.uaccount || p.id,
          uname: p.uname || p.username || p.nickname,
          uemail: p.uemail || p.email,
          usex: p.usex || '',
          urole: p.urole || p.role || '',
          uavatar: p.uavatar || p.avatar || ''
        }
        hadCache = true
      }
    }
  } catch {}
  if (!hadCache) loading.value = true
  try {
    const body = await usercenter.profile()
    const p = body.data || {}
    profile.value = {
      uaccount: p.uaccount, uname: p.uname,
      uemail: p.uemail, usex: p.usex,
      urole: p.urole, uavatar: p.uavatar
    }
    try {
      localStorage.setItem('profile', JSON.stringify({
        id: p.uaccount,
        username: p.uname,
        nickname: p.uname,
        email: p.uemail,
        role: p.urole,
        avatar: p.uavatar || ''
      }))
    } catch {}
  } catch {}
  loading.value = false
})

function startEdit() {
  editForm.value = {
    uname: profile.value.uname || '',
    uemail: profile.value.uemail || '',
    usex: profile.value.usex || '男'
  }
  editing.value = true
}

function cancelEdit() { editing.value = false }

/** 保存编辑 */
async function saveEdit() {
  try {
    saving.value = true
    await usercenter.updateProfile({
      uaccount: profile.value.uaccount,
      uname: editForm.value.uname,
      uemail: editForm.value.uemail,
      usex: editForm.value.usex
    })
    profile.value.uname = editForm.value.uname
    profile.value.uemail = editForm.value.uemail
    profile.value.usex = editForm.value.usex
    try {
      localStorage.setItem('profile', JSON.stringify({
        id: profile.value.uaccount,
        username: profile.value.uname,
        nickname: profile.value.uname,
        email: profile.value.uemail,
        role: profile.value.urole,
        avatar: profile.value.uavatar || ''
      }))
    } catch {}
    editing.value = false
    alert('更新成功')
  } catch (e) { alert(e.message || '更新失败') }
  finally { saving.value = false }
}

/** 上传头像 */
async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { alert('请选择图片文件'); return }
  if (file.size > 5 * 1024 * 1024) { alert('图片大小不能超过 5MB'); return }
  try {
    uploadingAvatar.value = true
    const objectKey = `avatar/${profile.value.uaccount}.jpg`
    const uploadRes = await obs.replace(file, objectKey)
    const url = uploadRes.data?.url
    if (!url) throw new Error('上传失败：未获取到URL')
    // 更新用户头像
    await usercenter.updateAvatar(url)
    profile.value.uavatar = url
    // 同步 localStorage
    try {
      localStorage.setItem('profile', JSON.stringify({
        id: profile.value.uaccount,
        username: profile.value.uname,
        nickname: profile.value.uname,
        email: profile.value.uemail,
        role: profile.value.urole,
        avatar: url
      }))
    } catch {}
    alert('头像更新成功')
  } catch (err) { alert(err.message || '头像上传失败') }
  finally { uploadingAvatar.value = false; e.target.value = '' }
}

/** 修改密码 */
async function changePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { alert('请填写旧密码和新密码'); return }
  try {
    changingPwd.value = true
    await usercenter.changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    alert('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '' }
    showPwdForm.value = false
  } catch (e) { alert(e.message || '密码修改失败') }
  finally { changingPwd.value = false }
}
</script>

<style scoped>
.profile-section { margin: 0; }

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

/* 信息展示 */
.profile-info {
  max-width: 480px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--s-20);
  padding: var(--s-20) 0;
  border-bottom: 1px solid var(--c-hairline);
}
.avatar-preview { flex-shrink: 0; }
.avatar-img {
  width: 72px; height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--c-hairline);
}
.avatar-placeholder {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: var(--c-soft);
  color: var(--c-accent);
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--c-accent-soft);
}
.avatar-actions {
  display: flex;
  align-items: center;
  gap: var(--s-8);
}
.avatar-btn { cursor: pointer; }
.upload-tip { font-size: 12px; color: var(--c-muted); }

.info-row {
  display: grid;
  grid-template-columns: 100px 1fr;
  align-items: center;
  padding: var(--s-16) 0;
  border-bottom: 1px solid var(--c-hairline);
}
.info-row:last-child { border-bottom: none; }
.info-key { font-size: 14px; color: var(--c-muted); }
.info-val { font-size: 14px; color: var(--c-ink); font-weight: 500; }

.profile-actions {
  margin-top: var(--s-24);
  display: flex;
  gap: var(--s-12);
}

/* 编辑/密码表单 */
.edit-form, .pwd-form {
  max-width: 480px;
  margin-top: var(--s-16);
  padding-top: var(--s-16);
  border-top: 1px solid var(--c-hairline);
}
.form-row {
  display: grid;
  grid-template-columns: 80px 1fr;
  align-items: center;
  gap: var(--s-8);
  margin-bottom: var(--s-12);
}
.form-key { font-size: 13px; color: var(--c-muted); }
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

.form-actions {
  margin-top: var(--s-16);
  display: flex;
  gap: var(--s-12);
}

.loading-block {
  text-align: center;
  color: var(--c-muted);
  padding: var(--s-40) 0;
}
.loading-block .spinner { margin: 0 auto var(--s-8); }
</style>
