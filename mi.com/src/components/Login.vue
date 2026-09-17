<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>账号登录</h2>
        <p>欢迎回到华为商城</p>
      </div>

      <div class="tabs">
        <button :class="['tab', method==='password' && 'active']" @click="method='password'">账号密码登录</button>
        <button :class="['tab', method==='email' && 'active']" @click="method='email'">邮箱验证码登录</button>
        <div class="tab-indicator" :style="{ transform: method === 'password' ? 'translateX(0)' : 'translateX(100%)' }"></div>
      </div>

      <form v-if="method==='password'" @submit.prevent="login" class="auth-form">
        <input class="input-field" type="text" placeholder="请输入用户名" v-model="username" />
        <input class="input-field" type="password" placeholder="请输入密码" v-model="password" />
        <div class="code-row">
          <input class="input-field" type="text" placeholder="请输入图形验证码" v-model="captchaCode" maxlength="4" />
          <img v-if="captchaImage" class="captcha-img" :src="captchaImage" @click="refreshCaptcha" title="点击刷新" alt="验证码" />
        </div>
        <button type="submit" class="btn-primary">登录</button>
      </form>

      <form v-else @submit.prevent="loginEmail" class="auth-form">
        <input class="input-field" type="email" placeholder="请输入邮箱" v-model="email" />
        <div class="code-row">
          <input class="input-field" type="text" placeholder="请输入验证码" v-model="emailCode" />
          <button type="button" class="btn-code" :disabled="emailCountdown>0" @click="sendEmailCode">
            {{ emailCountdown>0 ? `${emailCountdown}s` : '获取验证码' }}
          </button>
        </div>
        <button type="submit" class="btn-primary">登录</button>
      </form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { auth } from '../api'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const method = ref('password')
const username = ref('')
const password = ref('')
const email = ref('')
const emailCode = ref('')
const emailCountdown = ref(0)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaCode = ref('')

/** 刷新图形验证码 */
async function refreshCaptcha() {
  try {
    const body = await auth.captcha()
    captchaImage.value = body.data.captchaImage
    captchaKey.value = body.data.captchaKey
    captchaCode.value = ''
  } catch (err) { console.error('验证码加载失败', err) }
}

onMounted(() => refreshCaptcha())

async function login() {
  try {
    const body = await auth.login(username.value, password.value, captchaKey.value, captchaCode.value)
    saveAuth(body.data)
    router.push(router.currentRoute.value.query.redirect || '/center')
  } catch (err) { 
    alert(err.message)
    refreshCaptcha()
  }
}

async function loginEmail() {
  try {
    const body = await auth.loginByEmail(email.value, emailCode.value)
    saveAuth(body.data)
    router.push(router.currentRoute.value.query.redirect || '/center')
  } catch (err) { alert(err.message) }
}

/** 保存认证信息到本地 */
function saveAuth(user) {
  if (user) {
    localStorage.setItem('token', user.token || '')
    localStorage.setItem('uid', String(user.uaccount || ''))
    localStorage.setItem('profile', JSON.stringify({
      id: user.uaccount,
      username: user.uname,
      nickname: user.uname,
      email: user.uemail,
      role: user.urole,
      avatar: user.uavatar
    }))
  }
}

/** 发送邮箱验证码 */
function sendEmailCode() {
  if (!email.value) { alert('请输入邮箱'); return }
  if (emailCountdown.value > 0) return
  auth.sendCode(email.value)
    .then(() => {
      emailCountdown.value = 60
      const timer = setInterval(() => {
        emailCountdown.value -= 1
        if (emailCountdown.value <= 0) clearInterval(timer)
      }, 1000)
    })
    .catch(err => alert(err.message))
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 100px - 200px);
  background: var(--c-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--s-40) var(--s-16);
}
.login-card {
  width: 420px;
  background: var(--c-ground);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-md);
  padding: var(--s-40) var(--s-40) var(--s-32);
}
.login-header {
  text-align: center;
  margin-bottom: var(--s-32);
}
.login-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.02em;
}
.login-header p {
  margin: var(--s-8) 0 0;
  color: var(--c-muted);
  font-size: 14px;
}

/* 标签切换 */
.tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  position: relative;
  margin-bottom: var(--s-32);
  border-bottom: 1px solid var(--c-hairline);
}
.tab {
  padding: var(--s-12) 0;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 14px;
  color: var(--c-muted);
  transition: color 200ms ease;
  z-index: 1;
}
.tab.active {
  color: var(--c-ink);
  font-weight: 600;
}
.tab-indicator {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 50%;
  height: 2px;
  background: var(--c-accent);
  transition: transform 250ms cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 表单 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--s-16);
}
.code-row {
  display: flex;
  gap: var(--s-12);
}
.code-row .input-field { flex: 1; }

/* 图形验证码图片 */
.captcha-img {
  flex-shrink: 0;
  width: 110px;
  height: 44px;
  border-radius: var(--r-sm);
  cursor: pointer;
  border: 1px solid var(--c-hairline);
  object-fit: cover;
}

/* 发送验证码按钮 */
.btn-code {
  flex-shrink: 0;
  width: 110px;
  height: 44px;
  border: 1px solid var(--c-ink);
  border-radius: var(--r-sm);
  background: var(--c-ground);
  color: var(--c-ink);
  font-size: 13px;
  cursor: pointer;
  transition: all 150ms ease;
  white-space: nowrap;
}
.btn-code:hover { background: var(--c-ink); color: #fff; }
.btn-code:disabled {
  border-color: var(--c-hairline);
  color: var(--c-muted);
  background: var(--c-soft);
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: var(--s-24);
  font-size: 13px;
  color: var(--c-muted);
}
.login-footer a {
  color: var(--c-accent);
  text-decoration: none;
  margin-left: var(--s-4);
  font-weight: 500;
}
.login-footer a:hover { text-decoration: underline; }
</style>
