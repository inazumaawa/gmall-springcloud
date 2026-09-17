/**
 * 构建通用请求头（token + uid）
 */
function buildHeaders() {
  const headers = {}
  const token = localStorage.getItem('token')
  const uid = localStorage.getItem('uid')
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  if (uid) {
    headers['uid'] = uid
  }
  return headers
}

/**
 * AI 聊天 SSE 流式请求
 * @param {string} message - 用户消息
 * @param {AbortController} controller - 用于取消请求的控制器
 * @returns {ReadableStreamDefaultReader} - 返回读取器用于读取流式响应
 */
export function aiChatStream(message, controller) {
  return fetch(`/ai/chat?message=${encodeURIComponent(message)}`, {
    method: 'GET',
    headers: buildHeaders(),
    signal: controller.signal
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.body.getReader()
  })
}

/**
 * AI 图片聊天 SSE 流式请求（多模态：图片 + 可选文本）
 * @param {File} imageFile - 图片文件
 * @param {string} [message] - 可选的文字描述
 * @param {AbortController} controller - 用于取消请求的控制器
 * @returns {ReadableStreamDefaultReader} - 返回读取器用于读取流式响应
 */
export function aiChatImageStream(imageFile, message, controller) {
  const formData = new FormData()
  formData.append('image', imageFile)
  if (message) {
    formData.append('message', message)
  }
  return fetch('/ai/chat-image', {
    method: 'POST',
    headers: buildHeaders(),
    body: formData,
    signal: controller.signal
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.body.getReader()
  })
}

/**
 * AI 图片聊天 SSE 流式请求（通过公开 URL）
 * @param {string} imageUrl - 图片的公开 URL
 * @param {string} [message] - 可选的文字描述
 * @param {AbortController} controller - 用于取消请求的控制器
 * @returns {ReadableStreamDefaultReader} - 返回读取器用于读取流式响应
 */
export function aiChatImageUrlStream(imageUrl, message, controller) {
  const params = new URLSearchParams()
  params.append('url', imageUrl)
  if (message) {
    params.append('message', message)
  }
  return fetch('/ai/chat-image-url?' + params.toString(), {
    method: 'POST',
    headers: buildHeaders(),
    signal: controller.signal
  }).then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.body.getReader()
  })
}

/**
 * 普通 AI 聊天请求（非流式）
 * @param {string} message - 用户消息
 * @returns {Promise} - 返回 Promise 对象
 */
export function aiChat(message) {
  // Token is handled by request interceptor
  return request.get('/ai/chat', {
    params: { message }
  })
}
