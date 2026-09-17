<template>
  <div class="admin-section">
    <div class="section-head">
      <h2 class="head-title">客服工作台</h2>
      <span class="sess-count">共 {{ sessions.length }} 个会话</span>
    </div>

    <div class="chat-layout">
      <!-- 会话列表 -->
      <aside class="sessions">
        <div v-for="s in sessions" :key="s.id" class="sess-item" :class="{ active: s.id === currentId }" @click="select(s)">
          <span class="sess-avatar">{{ String(s.uid).slice(-2) }}</span>
          <div class="sess-info">
            <span class="sess-name">用户 #{{ s.uid }}</span>
            <span class="sess-status" :class="s.status === 'OPEN' ? 'open' : 'closed'">
              {{ s.status === 'OPEN' ? '进行中' : '已结束' }}
            </span>
          </div>
        </div>
        <p v-if="!sessions.length" class="empty">暂无会话</p>
      </aside>

      <!-- 聊天区 -->
      <section class="chat-main">
        <div class="chat-header">
          <span class="head-avatar">{{ current ? String(current.uid).slice(-2) : '客' }}</span>
          <div class="head-info">
            <div class="head-name">{{ current ? '用户 #' + current.uid : '客服工作台' }}</div>
            <div class="head-sub">{{ current ? (current.status === 'OPEN' ? '会话进行中' : '会话已结束') : '请选择左侧会话开始接待' }}</div>
          </div>
        </div>

        <div class="chat-body" ref="bodyRef">
          <div v-if="!current" class="tip">请选择左侧会话开始接待</div>
          <div v-for="(m, i) in msgs" :key="i" class="msg" :class="m.senderRole === 'admin' ? 'self' : 'other'">
            <span class="avatar">{{ m.senderRole === 'admin' ? '客服' : '用户' }}</span>
            <div class="bubble">{{ m.content }}</div>
          </div>
        </div>

        <div class="chat-input">
          <input v-model="text" :disabled="!current" @keyup.enter="send" placeholder="回复用户..." />
          <button @click="send" :disabled="!current">发送</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { chat } from '../api'

const sessions = ref([])
const msgs = ref([])
const current = ref(null)
const currentId = ref(null)
const text = ref('')
const bodyRef = ref(null)
let ws = null

function wsUrl() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const uid = localStorage.getItem('uid') || '0'
  return `${proto}://${location.host}/ws/chat?uid=${uid}&role=admin`
}

function connect() {
  ws = new WebSocket(wsUrl())
  ws.onmessage = (e) => {
    try {
      const m = JSON.parse(e.data)
      // 用户新消息：若属于当前会话则追加显示
      if (current.value && m.sessionId === current.value.id) {
        msgs.value.push(m)
        scroll()
      }
    } catch {}
  }
  ws.onerror = () => {}
  ws.onclose = () => {}
}

async function loadSessions() {
  try {
    const r = await chat.sessionList()
    const myUid = String(localStorage.getItem('uid') || '')
    // 过滤掉管理员自己发起的会话，避免“和自己对话”
    sessions.value = (r.data || []).filter(s => String(s.uid) !== myUid)
  } catch {}
}

async function select(s) {
  current.value = s
  currentId.value = s.id
  try {
    const r = await chat.history(s.id)
    msgs.value = r.data || []
    scroll()
  } catch { msgs.value = [] }
}

function send() {
  const c = text.value.trim()
  if (!c || !current.value || !ws || ws.readyState !== 1) return
  ws.send(JSON.stringify({ toUid: current.value.uid, sessionId: current.value.id, content: c }))
  msgs.value.push({ senderRole: 'admin', content: c })
  text.value = ''
  scroll()
}

function scroll() {
  nextTick(() => { if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight })
}

onMounted(() => { connect(); loadSessions() })
onUnmounted(() => { if (ws) ws.close() })
</script>

<style scoped>
.admin-section { margin: 0; }

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-20);
  padding-bottom: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.head-title { font-size: 18px; font-weight: 700; color: var(--c-ink); margin: 0; }
.sess-count {
  font-size: 12px;
  color: var(--c-accent);
  background: var(--c-accent-soft);
  padding: 4px 12px;
  border-radius: var(--r-full);
}

.chat-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: var(--s-16);
  height: calc(100vh - 200px);
  min-height: 420px;
}

/* 会话列表 */
.sessions {
  background: var(--c-ground);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  padding: var(--s-8);
  overflow-y: auto;
}
.sess-item {
  display: flex;
  align-items: center;
  gap: var(--s-10);
  padding: var(--s-10);
  border-radius: var(--r-md);
  cursor: pointer;
  margin-bottom: 4px;
  transition: background var(--dur-fast) ease;
}
.sess-item:hover { background: var(--c-soft); }
.sess-item.active { background: var(--c-accent-soft); }
.sess-avatar {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--c-soft);
  color: var(--c-ink-soft);
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sess-item.active .sess-avatar { background: var(--grad-accent); color: #fff; }
.sess-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.sess-name {
  font-size: 13px;
  color: var(--c-ink);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sess-item.active .sess-name { color: var(--c-accent); font-weight: 600; }
.sess-status { font-size: 11px; }
.sess-status.open { color: var(--c-success); }
.sess-status.closed { color: var(--c-muted); }
.empty, .tip { color: var(--c-muted); font-size: 13px; text-align: center; padding: var(--s-20) 0; }

/* 聊天区 */
.chat-main {
  background: var(--c-ground);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  padding: var(--s-14) var(--s-20);
  background: var(--grad-accent);
  color: #fff;
  flex-shrink: 0;
}
.head-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.head-name { font-size: 14px; font-weight: 600; }
.head-sub { font-size: 12px; opacity: 0.85; margin-top: 2px; }

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--s-20);
  background: var(--c-soft);
}
.msg {
  display: flex;
  align-items: flex-start;
  gap: var(--s-8);
  margin-bottom: var(--s-16);
}
.msg.self { flex-direction: row-reverse; }

.avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.msg.other .avatar { background: var(--c-soft); color: var(--c-ink-soft); }
.msg.self .avatar { background: var(--grad-accent); color: #fff; }

.bubble {
  max-width: 70%;
  padding: var(--s-10) var(--s-14);
  border-radius: var(--r-md);
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}
.msg.other .bubble {
  background: var(--c-ground);
  color: var(--c-ink);
  border: 1px solid var(--c-hairline);
  border-top-left-radius: 4px;
}
.msg.self .bubble {
  background: var(--grad-accent);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(202, 20, 28, 0.18);
}

/* 输入区：单一框体包裹文字与按钮 */
.chat-input {
  display: flex;
  align-items: center;
  gap: var(--s-8);
  flex-shrink: 0;
  margin: var(--s-12);
  padding: 6px 6px 6px var(--s-16);
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-full);
  background: var(--c-ground);
  transition: border-color var(--dur-fast) ease, box-shadow var(--dur-fast) ease;
}
.chat-input:focus-within {
  border-color: var(--c-accent);
  box-shadow: 0 0 0 3px var(--c-accent-soft);
}
.chat-input input {
  flex: 1;
  min-width: 0;
  height: 36px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: var(--c-ink);
}
.chat-input input::placeholder { color: var(--c-muted-light); }
.chat-input button {
  height: 36px;
  padding: 0 var(--s-20);
  background: var(--grad-accent);
  color: #fff;
  border: none;
  border-radius: var(--r-full);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity var(--dur-fast) ease;
}
.chat-input button:hover:not(:disabled) { opacity: 0.9; }
.chat-input button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
