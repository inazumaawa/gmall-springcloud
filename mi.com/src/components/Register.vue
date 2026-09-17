<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h2>注册账号</h2>
        <p>创建您的华为商城账号</p>
      </div>

      <form @submit.prevent="register" class="auth-form">
        <input class="input-field" type="text" placeholder="请输入用户名" v-model="username" />
        <input class="input-field" type="password" placeholder="请输入密码" v-model="password" />
        <select v-model="gender" class="input-field gender-select">
          <option value="" disabled>请选择性别</option>
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
        <input class="input-field" type="email" placeholder="请输入邮箱" v-model="email" />
        <button type="submit" class="btn-primary">注册</button>
      </form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { auth } from '../api'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')
const gender = ref('')
const email = ref('')

/** 注册 */
function register() {
  auth.register({
    uname: username.value,
    upassword: password.value,
    usex: gender.value,
    uemail: email.value
  })
    .then(() => {
      alert('注册成功')
      router.push('/login')
    })
    .catch(err => alert(err.message))
}
</script>

<style scoped>
.register-page {
  min-height: calc(100vh - 100px - 200px);
  background: var(--c-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--s-40) var(--s-16);
}
.register-card {
  width: 420px;
  background: var(--c-ground);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-md);
  padding: var(--s-40) var(--s-40) var(--s-32);
}
.register-header {
  text-align: center;
  margin-bottom: var(--s-32);
}
.register-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
  letter-spacing: -0.02em;
}
.register-header p {
  margin: var(--s-8) 0 0;
  color: var(--c-muted);
  font-size: 14px;
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--s-16);
}
.gender-select {
  appearance: none;
  cursor: pointer;
}

.register-footer {
  text-align: center;
  margin-top: var(--s-24);
  font-size: 13px;
  color: var(--c-muted);
}
.register-footer a {
  color: var(--c-accent);
  text-decoration: none;
  margin-left: var(--s-4);
  font-weight: 500;
}
.register-footer a:hover { text-decoration: underline; }
</style>
