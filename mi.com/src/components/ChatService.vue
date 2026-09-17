<template>
  <div class="service-page">
    <div class="section-head">
      <h2 class="head-title">联系客服</h2>
      <span class="conn-status" :class="connected ? 'on' : 'off'">
        <i class="dot"></i>{{ connected ? '已连接' : '连接中...' }}
      </span>
    </div>

    <div class="chat-box">
      <!-- 客服信息头 -->
      <div class="chat-header">
        <div class="agent-avatar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 18v-6a9 9 0 0 1 18 0v6"/>
            <path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/>
          </svg>
        </div>
        <div class="agent-info">
          <div class="agent-name">在线客服</div>
          <div class="agent-sub">客服实时在线，随问随答</div>
        </div>
      </div>

      <!-- 消息区 -->
      <div class="chat-body" ref="bodyRef">
        <div v-if="!messages.length" class="empty-tip">
          <div class="empty-avatar">客</div>
          <p>您好，请问有什么可以帮您？</p>
        </div>
        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.self ? 'self' : 'other'">
          <span class="avatar">{{ m.self ? '我' : '客服' }}</span>
          <div class="bubble">{{ m.content }}</div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input">
        <input v-model="text" @keyup.enter="send" placeholder="请输入消息..." />
        <button @click="send" :disabled="!connected">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { chat } from '../api'

const text = ref('')
const messages = ref([])
const bodyRef = ref(null)
const connected = ref(false)
let ws = null
let sessionId = null

function wsUrl() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const uid = localStorage.getItem('uid') || '0'
  return `${proto}://${location.host}/ws/chat?uid=${uid}&role=user`
}

function connect() {
  ws = new WebSocket(wsUrl())
  ws.onopen = () => { connected.value = true }
  ws.onmessage = (e) => {
    try {
      const m = JSON.parse(e.data)
      messages.value.push({ content: m.content, self: false })
      scroll()
    } catch {}
  }
  ws.onerror = () => { connected.value = false }
  ws.onclose = () => { connected.value = false }
}

async function loadHistory() {
  try {
    const r = await chat.createSession()
    sessionId = r.data
    const h = await chat.history(sessionId)
    messages.value = (h.data || []).map(m => ({ content: m.content, self: m.senderRole === 'user' }))
    scroll()
  } catch {}
}

function send() {
  const c = text.value.trim()
  if (!c || !ws || ws.readyState !== 1) return
  ws.send(JSON.stringify({ content: c }))
  messages.value.push({ content: c, self: true })
  text.value = ''
  scroll()
}

function scroll() {
  nextTick(() => { if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight })
}

onMounted(() => { connect(); loadHistory() })
onUnmounted(() => { if (ws) ws.close() })
</script>

<style scoped>
.service-page { margin: 0; }

/* 页头 */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
.conn-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: var(--r-full);
}
.conn-status .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.conn-status.on { background: var(--c-success-bg); color: var(--c-success); }
.conn-status.off { background: var(--c-warning-bg); color: var(--c-warning); }

/* 聊天卡片 */
.chat-box {
  display: flex;
  flex-direction: column;
  height: 540px;
  border: 1px solid var(--c-hairline);
  border-radius: var(--r-lg);
  overflow: hidden;
  background: var(--c-ground);
  box-shadow: var(--shadow-card);
}

/* 客服信息头 */
.chat-header {
  display: flex;
  align-items: center;
  gap: var(--s-12);
  padding: var(--s-14) var(--s-20);
  background: var(--grad-accent);
  color: #fff;
  flex-shrink: 0;
}
.agent-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.agent-name { font-size: 14px; font-weight: 600; }
.agent-sub { font-size: 12px; opacity: 0.85; margin-top: 2px; }

/* 消息区 */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--s-20);
  background: var(--c-soft);
}
.empty-tip {
  text-align: center;
  color: var(--c-muted);
  font-size: 13px;
  padding: var(--s-40) 0;
}
.empty-avatar {
  width: 52px;
  height: 52px;
  margin: 0 auto var(--s-12);
  border-radius: 50%;
  background: var(--grad-accent);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
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
.msg.other .avatar { background: var(--grad-accent); color: #fff; }
.msg.self .avatar { background: var(--c-hairline); color: var(--c-ink-soft); }

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

/* 输入区 */
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
  border: none;
  border-radius: var(--r-full);
  background: var(--grad-accent);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity var(--dur-fast) ease;
}
.chat-input button:hover:not(:disabled) { opacity: 0.9; }
.chat-input button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
