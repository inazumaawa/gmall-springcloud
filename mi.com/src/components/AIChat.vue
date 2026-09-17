<template>
  <!-- 客服按钮 -->
  <button class="ai-service-btn" @click="toggleChat" :title="showChat ? '关闭对话' : 'AI 客服'">
    <span v-if="!showChat" class="btn-content">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
    </span>
    <span v-else class="btn-content">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
    </span>
  </button>

  <!-- 对话窗口 -->
  <div v-if="showChat" class="chat-window">
    <div class="chat-header">
      <span>AI 智能客服</span>
      <button class="close-btn" @click="toggleChat">&times;</button>
    </div>
    <div class="chat-body" ref="chatBodyRef">
      <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.sender === 'user' ? 'msg-user' : 'msg-ai'">
        <div v-if="msg.sender === 'ai'" class="avatar ai-avatar">AI</div>
        <div class="bubble">
          <div v-if="msg.images && msg.images.length > 0" class="msg-images">
            <img v-for="(img, i) in msg.images" :key="i" :src="img" alt="uploaded" />
          </div>
          <div v-if="msg.text" v-html="parseMarkdown(msg.text)"></div>
        </div>
        <div v-if="msg.sender === 'user'" class="avatar user-avatar">我</div>
      </div>
      <div v-if="isThinking" class="thinking">AI 正在思考...</div>
    </div>
    <!-- 已选图片预览 -->
    <div v-if="selectedFiles.length > 0" class="img-preview-bar">
      <div v-for="(file, idx) in selectedFiles" :key="idx" class="preview-item">
        <img :src="file.preview" alt="preview" />
        <span class="remove-preview" @click="removeImage(idx)">&times;</span>
      </div>
    </div>
    <div class="chat-input">
      <!-- 图片上传按钮 -->
      <label class="upload-btn" title="上传图片">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <input type="file" accept="image/*" @change="onImageSelected" hidden />
      </label>
      <input
        type="text"
        v-model="inputText"
        @keyup.enter="handleSendOrStop"
        placeholder="输入你的消息..."
        :disabled="isThinking && !isGenerating"
      />
      <button @click="handleSendOrStop" :class="{ 'stop-btn': isGenerating }">
        {{ isGenerating ? '停止' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { aiChatStream, aiChatImageStream, aiChatImageUrlStream } from '@/api/ai.js'
import { marked } from 'marked'

const showChat = ref(false)
const inputText = ref('')
const messages = ref([])
const isThinking = ref(false)
const isGenerating = ref(false)
const chatBodyRef = ref(null)
/** 当前已选择的图片文件列表（含预览 URL） */
const selectedFiles = ref([])
/** 匹配以 http 开头的图片 URL */
const IMG_URL_RE = /^(https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp)(\?[^\s]*)?)/i
let controller = null

function toggleChat() {
  showChat.value = !showChat.value
}

// 自动滚动到底部
async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

watch(messages, () => scrollToBottom(), { deep: true })

// Markdown 解析（breaks: true 使单 \n 渲染为换行，兼容图片识别结果中替换 \n\n → \n 后的段落显示）
function parseMarkdown(text) {
  if (!text) return ''
  try {
    return marked.parse(text, { breaks: true })
  } catch {
    return text
  }
}

/** 图片选择回调：生成预览 URL 并存入 selectedFiles */
function onImageSelected(e) {
  const files = e.target.files
  if (!files) return
  for (const file of files) {
    selectedFiles.value.push({
      file,
      preview: URL.createObjectURL(file)
    })
  }
  // 重置 input 以支持重复选择同一文件
  e.target.value = ''
}

/** 移除已选图片 */
function removeImage(idx) {
  const item = selectedFiles.value[idx]
  if (item && item.preview) {
    URL.revokeObjectURL(item.preview)
  }
  selectedFiles.value.splice(idx, 1)
}

function handleSendOrStop() {
  if (isGenerating.value) {
    if (controller) controller.abort()
    isGenerating.value = false
    isThinking.value = false
    return
  }
  const text = inputText.value.trim()
  const hasImages = selectedFiles.value.length > 0
  // 检测图片 URL（仅当无上传文件时生效）
  const imgMatch = text.match(IMG_URL_RE)
  const isImageUrl = !hasImages && imgMatch
  // 既无文本也无图片则不发送
  if (!text && !hasImages) return

  // 构建用户消息
  const userMsg = { sender: 'user', text }
  if (hasImages) {
    userMsg.images = selectedFiles.value.map(f => f.preview)
  } else if (isImageUrl) {
    userMsg.images = [imgMatch[1]]
  }
  messages.value.push(userMsg)
  inputText.value = ''
  isThinking.value = true

  // 快捷回复（纯文本，无图片时生效）
  if (!hasImages && !isImageUrl) {
    const lower = text.toLowerCase()
    if (lower === '你好') {
      messages.value.push({ sender: 'ai', text: '你好，请问有什么可以帮您？' })
      isThinking.value = false
      return
    }
    if (lower === '你是谁') {
      messages.value.push({ sender: 'ai', text: '我是华为商城的 AI 智能客服，可以帮您解答产品问题、查询购物车、添加商品等。' })
      isThinking.value = false
      return
    }
  }

  // 提取待发送图片 File 对象
  const files = hasImages ? selectedFiles.value.map(f => f.file) : []
  selectedFiles.value = []
  sendToServer(text, files)
}

async function sendToServer(message, files = []) {
  try {
    controller = new AbortController()
    // 检测图片 URL
    const imgMatch = message.match(IMG_URL_RE)
    const isImageUrl = files.length === 0 && imgMatch

    // 有文件上传时走文件接口，有图片 URL 时走 URL 接口，否则纯文本
    let reader
    if (files.length > 0) {
      reader = await aiChatImageStream(files[0], message, controller)
    } else if (isImageUrl) {
      const imgUrl = imgMatch[1]
      const restText = message.substring(imgUrl.length).trim() || null
      reader = await aiChatImageUrlStream(imgUrl, restText, controller)
    } else {
      reader = await aiChatStream(message, controller)
    }
    const decoder = new TextDecoder('utf-8')

    const aiIdx = messages.value.length
    messages.value.push({ sender: 'ai', text: '' })
    isGenerating.value = true

    let buffer = ''
    let isSSE = null

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk

      if (isSSE === null) {
        isSSE = buffer.includes('data:')
      }

      let newContent = ''
      if (isSSE) {
        const events = buffer.split('\n\n')
        buffer = events.pop() || ''
        for (const event of events) {
          if (!event.trim()) continue
          // 逐行解析同一 SSE 事件中的 data: 行及后续续行
          const lines = event.split('\n')
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const content = line.substring(5)
              if (content === '[DONE]') {
                reader.cancel()
                return
              }
              newContent += content
            } else if (newContent) {
              // 同一事件中 data: 行后的续行，追加 \n 连接（图片识别多段落场景）
              newContent += '\n' + line
            }
          }
        }
      } else {
        newContent = buffer
        buffer = ''
      }
      messages.value[aiIdx].text += newContent
      await new Promise(r => setTimeout(r, 5))
      isThinking.value = false
    }
  } catch (e) {
    if (e.name !== 'AbortError') {
      messages.value.push({ sender: 'ai', text: '抱歉，我暂时无法回答这个问题，请稍后再试。' })
    }
  } finally {
    isThinking.value = false
    isGenerating.value = false
  }
}
</script>

<style scoped>
.ai-service-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9998;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6700, #f56c6c);
  color: #fff;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 103, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s, box-shadow 0.3s;
}
.ai-service-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(255, 103, 0, 0.45);
}
.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-window {
  position: fixed;
  bottom: 100px;
  right: 30px;
  z-index: 9999;
  width: 400px;
  height: 560px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-header {
  background: linear-gradient(135deg, #ff6700, #f56c6c);
  color: #fff;
  padding: 14px 16px;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
  padding: 0 4px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f8f9fa;
}
.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 8px;
}
.msg-user {
  justify-content: flex-end;
}
.msg-ai {
  justify-content: flex-start;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  color: #fff;
}
.ai-avatar {
  background: #ff6700;
}
.user-avatar {
  background: #409eff;
}

.bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  overflow: hidden;
}

/* 用户上传的图片限制尺寸，避免占满对话框 */
.msg-images {
  margin-bottom: 6px;
}
.msg-images img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  object-fit: contain;
  display: block;
}
.msg-ai .bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 4px;
}
.msg-user .bubble {
  background: #ff6700;
  color: #fff;
  border-top-right-radius: 4px;
}

.thinking {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 8px;
}

.chat-input {
  display: flex;
  padding: 10px 12px;
  gap: 8px;
  border-top: 1px solid #eee;
  background: #fff;
}
.chat-input input {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.3s;
}
.chat-input input:focus {
  border-color: #ff6700;
}
.chat-input button {
  background: #ff6700;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 18px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
  white-space: nowrap;
}
.chat-input button:hover {
  background: #e55f00;
}
.chat-input .stop-btn {
  background: #f56c6c;
}
.chat-input .stop-btn:hover {
  background: #e55e5e;
}

/* 图片上传按钮 */
.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #999;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s;
}
.upload-btn:hover {
  background: #ff6700;
  color: #fff;
}

/* 图片预览栏 */
.img-preview-bar {
  display: flex;
  gap: 8px;
  padding: 8px 12px 0;
  background: #fff;
  overflow-x: auto;
}
.preview-item {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #eee;
}
.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.remove-preview {
  position: absolute;
  top: 0;
  right: 0;
  width: 18px;
  height: 18px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  border-radius: 0 8px 0 4px;
  cursor: pointer;
}
.remove-preview:hover {
  background: rgba(255, 0, 0, 0.7);
}

</style>
