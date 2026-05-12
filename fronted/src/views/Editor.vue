<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="editor-page">
    <header class="editor-top">
      <div class="top-left">
        <button class="icon-btn" @click="back" title="返回工作台">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
          </svg>
        </button>
        <div class="doc-meta">
          <h2>{{ doc?.title || '加载文档中...' }}</h2>
          <div class="status">
            <span class="dot" :class="isDirty ? 'dirty' : 'synced'"></span>
            <small>v{{ version }} &bull; {{ syncingText }}</small>
          </div>
        </div>
        <div class="presence-chip" @click="showOnlineUsers = !showOnlineUsers" title="点击查看在线用户">
          <span class="presence-dot"></span>
          <small>在线 {{ onlineCount }}</small>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="caret-icon" :class="{ 'rotate': showOnlineUsers }">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
          
          <div v-if="showOnlineUsers" class="presence-dropdown" @click.stop>
            <div class="dropdown-header">在线用户 ({{ onlineCount }})</div>
            <ul v-if="onlineUsers.length" class="dropdown-list">
              <li v-for="(user, idx) in onlineUsers" :key="idx" class="dropdown-item">
                <div class="user-avatar" :style="{ backgroundColor: user.color }">{{ user.name.charAt(0).toUpperCase() }}</div>
                <span :style="{ color: user.color, fontWeight: 'bold' }">{{ user.name }}</span>
              </li>
            </ul>
            <div v-else class="dropdown-empty">暂无其他用户</div>
          </div>
        </div>
      </div>
      
      <div class="actions">
        <div class="author-color-control" @click.stop>
          <button class="author-color-btn" @click="showAuthorColorPicker = !showAuthorColorPicker" title="设置我的高光颜色">
            <span class="author-color-dot" :style="{ backgroundColor: authorColor }"></span>
            <span>我的高光</span>
          </button>

          <div v-if="showAuthorColorPicker" class="author-color-panel">
            <div ref="colorWheelRef" class="author-color-wheel" @mousedown.prevent="pickAuthorColorFromWheel">
              <div class="author-color-wheel-center"></div>
            </div>
            <label class="author-color-slider-label">
              亮度
              <input
                type="range"
                min="35"
                max="88"
                step="1"
                v-model.number="authorLightness"
                @input="updateAuthorColorFromHsl"
              />
            </label>
            <div class="author-color-preview">
              <span class="preview-chip" :style="{ backgroundColor: authorColor }">示例高光</span>
            </div>
          </div>
        </div>

        <div class="invite-wrapper">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="invite-icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zM4 19.235v-.11a6.375 6.375 0 0112.75 0v.109A12.318 12.318 0 0110.374 21c-2.331 0-4.512-.645-6.374-1.766z" />
          </svg>
          <input v-model="inviteUser" placeholder="输入用户名邀请" @keyup.enter="invite" />
          <button class="small-btn" @click="invite">邀 请</button>
        </div>
        <button class="primary-btn" @click="saveNow" :disabled="saving">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h9l5 5v9a2 2 0 01-2 2H6a2 2 0 01-2-2V6z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 4v6h8V8" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 20v-6h8v6" />
          </svg>
          <span>{{ saving ? '保存中...' : '保 存' }}</span>
        </button>
        <div class="export-dropdown-wrap" @click.stop>
          <button class="secondary-btn" @click="showExportMenu = !showExportMenu" :disabled="saving" title="导出文档">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v12m0 0l-4-4m4 4l4-4M5 21h14" />
            </svg>
            <span>导 出</span>
            <svg class="export-caret" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 9l6 6 6-6" />
            </svg>
          </button>
          <div v-if="showExportMenu" class="export-menu">
            <button class="export-item" @click="exportNow('html')">导出为 HTML</button>
            <button class="export-item" @click="exportNow('txt')">导出为 TXT</button>
            <button class="export-item" @click="exportNow('md')">导出为 Markdown</button>
            <button class="export-item" @click="exportNow('doc')">导出为 Word (.doc)</button>
          </div>
        </div>
      </div>
    </header>

    <div class="main-content">
      <div class="editor-area">
        <!-- Zine Style Formatting Toolbar (Moved to Editor Area) -->
        <div class="zine-toolbar">
          <!-- Font Selection -->
          <div class="tool-group">
            <select class="tool-select" title="字体" @mousedown="restoreSelection" @change="execCmd('fontName', $event.target.value)">
              <option value="Inter, -apple-system, sans-serif">系统默认</option>
              <option value="SimSun, STSong">宋体</option>
              <option value="SimHei, STHeiti">黑体</option>
              <option value="KaiTi, STKaiti">楷体</option>
              <option value="Times New Roman">Times New Roman</option>
            </select>
            <select class="tool-select" title="字号" @mousedown="restoreSelection" @change="execCmd('fontSize', $event.target.value)">
              <option value="1">10px</option>
              <option value="2">13px</option>
              <option value="3">16px</option>
              <option value="4">18px</option>
              <option value="5">24px</option>
              <option value="6">32px</option>
            </select>
          </div>

          <div class="divider"></div>

          <!-- Text Formatting -->
          <div class="tool-group">
            <button class="tool-btn" title="加粗 (Ctrl+B)" @click="execCmd('bold')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z" />
              </svg>
            </button>
            <button class="tool-btn" title="斜体 (Ctrl+I)" @click="execCmd('italic')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="19" y1="4" x2="10" y2="4"></line>
                <line x1="14" y1="20" x2="5" y2="20"></line>
                <line x1="15" y1="4" x2="9" y2="20"></line>
              </svg>
            </button>
            <button class="tool-btn" title="下划线 (Ctrl+U)" @click="execCmd('underline')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 3v7a6 6 0 006 6 6 6 0 006-6V3"></path>
                <line x1="4" y1="21" x2="20" y2="21"></line>
              </svg>
            </button>
            <div class="color-picker-wrapper">
              <button class="tool-btn" title="字体颜色" @click="showColorPicker = !showColorPicker" :class="{ active: showColorPicker }">
                <span class="color-indicator" :style="{ backgroundColor: currentColor }"></span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 14px; height: 14px; margin-left: 2px;">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              <div v-if="showColorPicker" class="color-dropdown" @click.stop>
                <div class="color-grid">
                  <button 
                    v-for="c in predefinedColors" 
                    :key="c" 
                    class="color-swatch" 
                    :style="{ backgroundColor: c }" 
                    @click="applyColor(c)"
                    :title="c"
                  ></button>
                </div>
              </div>
              
              <div v-if="showColorPicker" class="picker-overlay" @click="showColorPicker = false"></div>
            </div>
          </div>

          <div class="divider"></div>

          <!-- Alignment -->
          <div class="tool-group">
            <button class="tool-btn" title="左对齐" @click="execCmd('justifyLeft')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="12" x2="15" y2="12"></line>
                <line x1="3" y1="18" x2="19" y2="18"></line>
              </svg>
            </button>
            <button class="tool-btn" title="居中对齐" @click="execCmd('justifyCenter')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="6" y1="12" x2="18" y2="12"></line>
                <line x1="4" y1="18" x2="20" y2="18"></line>
              </svg>
            </button>
            <button class="tool-btn" title="右对齐" @click="execCmd('justifyRight')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="9" y1="12" x2="21" y2="12"></line>
                <line x1="5" y1="18" x2="21" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>

        <div
          ref="editorRef"
          class="rich-textarea"
          contenteditable
          @beforeinput="handleBeforeInput"
          @compositionstart="handleCompositionStart"
          @compositionend="handleCompositionEnd"
          @input="onRichInput"
          @keydown="handleKeydown"
          placeholder="开始输入内容..."
        ></div>
      </div>

      <aside class="history">
        <div class="history-head">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3>修订历史</h3>
        </div>
        <ul v-if="history.length">
          <li v-for="item in history" :key="item.id">
            <div class="history-row">
              <div class="rev-info">
                <strong>v{{ item.newVersion }}</strong>
                <span>由用户 #{{ item.userId }} 修改</span>
              </div>
              <button
                class="rollback-btn"
                @click="rollback(item.id)"
                title="回退至此版本"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 15L3 9m0 0l6-6M3 9h12a6 6 0 010 12h-3" />
                </svg>
              </button>
            </div>
          </li>
        </ul>
        <p v-else class="empty-history">暂无历史记录。</p>
      </aside>
    </div>

    <div class="chat-fab-wrap" @click.stop>
      <button class="chat-fab" @click="toggleChat" :title="showChat ? '收起聊天' : '展开聊天'">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M7 8h10M7 12h6m-8 8l-3 1 1-3a8 8 0 1115.5-2.5A8 8 0 014 20z" />
        </svg>
        <span>聊天</span>
        <span v-if="unreadChatCount > 0" class="chat-badge">{{ unreadChatCount > 99 ? '99+' : unreadChatCount }}</span>
      </button>

      <section v-if="showChat" class="chat-panel">
        <header class="chat-header">
          <strong>协作聊天</strong>
          <button class="chat-close" @click="showChat = false" title="关闭聊天">×</button>
        </header>

        <div class="chat-ephemeral-note">提示：聊天记录不会保存，刷新页面后会清空。</div>

        <div ref="chatListRef" class="chat-list">
          <div v-if="!chatMessages.length" class="chat-empty">发送第一条消息开始沟通</div>
          <article
            v-for="(msg, idx) in chatMessages"
            :key="`${msg.timestamp || ''}-${msg.senderId || ''}-${idx}`"
            class="chat-item"
            :class="{ mine: msg.senderId === auth.user?.id }"
          >
            <div class="chat-meta">
              <span class="chat-name" :style="{ color: msg.color || '#2f6fed' }">{{ msg.senderName || `用户#${msg.senderId}` }}</span>
              <time>{{ formatChatTime(msg.timestamp) }}</time>
            </div>
            <p class="chat-text">{{ msg.content }}</p>
          </article>
        </div>

        <footer class="chat-input-row">
          <input
            v-model="chatInput"
            maxlength="500"
            placeholder="输入消息，回车发送"
            @keydown.enter.prevent="sendChat"
          />
          <button class="chat-send" @click="sendChat">发送</button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDocumentStore } from '@/stores/document'
import { getHistory, rollbackHistory } from '@/api/collab'
import { exportDocument, inviteMember } from '@/api/documents'
import { useWebSocket } from '@/composables/useWebSocket'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const docs = useDocumentStore()
const id = Number(route.params.id)

const doc = ref(null)
const content = ref('')
const version = ref(0)
const history = ref([])
const inviteUser = ref('')
const dirty = ref(false)
const saving = ref(false)
const editorRef = ref(null)
const onlineCount = ref(0)
const onlineUsers = ref([])
const showOnlineUsers = ref(false)
const showChat = ref(false)
const unreadChatCount = ref(0)
const showExportMenu = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const chatListRef = ref(null)
const colorWheelRef = ref(null)
const showAuthorColorPicker = ref(false)
const authorHue = ref(0)
const authorSaturation = ref(90)
const authorLightness = ref(70)
const authorColor = ref('hsla(0, 90%, 70%, 0.4)')
const isComposing = ref(false)
const lastCompositionText = ref('')
let compositionHandled = false
let wsTimer = null
let ws = null
let subscription = null
let presenceSubscription = null
let chatSubscription = null
const presenceClientId = `editor_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`

let currentRange = null
const handleSelectionChange = () => {
  if (document.activeElement === editorRef.value) {
    const sel = window.getSelection()
    if (sel.rangeCount > 0) {
      currentRange = sel.getRangeAt(0)
    }
  }
}

const restoreSelection = () => {
  if (currentRange) {
    const sel = window.getSelection()
    sel.removeAllRanges()
    sel.addRange(currentRange)
  }
}

const authorColorStorageKey = computed(() => `author-highlight-color-${auth.user?.id || 'guest'}`)

const updateAuthorColorFromHsl = () => {
  authorColor.value = `hsla(${authorHue.value}, ${authorSaturation.value}%, ${authorLightness.value}%, 0.4)`
  localStorage.setItem(
    authorColorStorageKey.value,
    JSON.stringify({ h: authorHue.value, s: authorSaturation.value, l: authorLightness.value })
  )
  
  if (ws && ws.client && ws.client.connected) {
    sendPresence('update_color')
  }
}

const initAuthorColor = () => {
  const saved = localStorage.getItem(authorColorStorageKey.value)
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      if (Number.isFinite(parsed.h) && Number.isFinite(parsed.s) && Number.isFinite(parsed.l)) {
        authorHue.value = Math.max(0, Math.min(359, Math.round(parsed.h)))
        authorSaturation.value = Math.max(35, Math.min(100, Math.round(parsed.s)))
        authorLightness.value = Math.max(35, Math.min(88, Math.round(parsed.l)))
        updateAuthorColorFromHsl()
        return
      }
    } catch {
      // ignore invalid local value
    }
  }

  const uid = Number(auth.user?.id || 1)
  authorHue.value = ((uid * 47) % 360 + 360) % 360
  authorSaturation.value = 90
  authorLightness.value = 70
  updateAuthorColorFromHsl()
}

const pickAuthorColorFromWheel = (event) => {
  const wheel = colorWheelRef.value
  if (!wheel) return

  const rect = wheel.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = event.clientX - cx
  const dy = event.clientY - cy

  const angle = (Math.atan2(dy, dx) * 180) / Math.PI
  const hue = (angle + 360) % 360
  const radius = Math.sqrt(dx * dx + dy * dy)
  const ratio = Math.min(1, Math.max(0, radius / (rect.width / 2)))

  authorHue.value = Math.round(hue)
  authorSaturation.value = Math.round(38 + ratio * 62)
  updateAuthorColorFromHsl()
}

const escapeHtml = (raw) =>
  raw
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')

const textToHighlightHtml = (text) => {
  const safeText = escapeHtml(text).replaceAll(' ', '&nbsp;').replaceAll('\n', '<br>')
  let colorStyle = ''
  try {
    let currentForeColor = document.queryCommandValue('foreColor')
    if (currentForeColor && currentForeColor !== 'rgb(17, 17, 17)' && currentForeColor !== 'rgba(0, 0, 0, 0)' && currentForeColor !== 'false') {
      colorStyle = `color:${currentForeColor};`
    } else if (currentColor.value !== '#111111') {
      colorStyle = `color:${currentColor.value};`
    }
  } catch (e) {
    // ignore exception
  }
  return `<span class="author-highlight" data-author-id="${auth.user?.id || ''}" data-author-name="${escapeHtml(auth.user?.username || '')}" style="background-color:${authorColor.value};${colorStyle}">${safeText}</span>`
}

const insertHighlightedText = (text) => {
  if (!text) return
  editorRef.value?.focus()
  restoreSelection()

  let inOwnHighlight = false
  const sel = window.getSelection()
  if (sel && sel.rangeCount > 0) {
    let node = sel.getRangeAt(0).startContainer
    while (node && node !== editorRef.value && node !== document.body) {
      if (node.nodeType === 1 && node.classList.contains('author-highlight')) {
        if (node.getAttribute('data-author-id') === String(auth.user?.id || '')) {
          inOwnHighlight = true
        }
        break
      }
      node = node.parentNode
    }
  }

  if (inOwnHighlight) {
    // 如果已经属于自己的高光标签内部，比如换行后浏览器自动继承了上一行的span，
    // 直接插入纯文本即可，不需要再包裹一层造成透明度叠加。
    document.execCommand('insertText', false, text)
  } else {
    document.execCommand('insertHTML', false, textToHighlightHtml(text))
  }

  if (editorRef.value) {
    content.value = editorRef.value.innerHTML
    onLocalInput()
  }
}

const handleBeforeInput = (e) => {
  // Chrome/Edge在中文输入确认时可能发出insertFromComposition
  if (e.inputType === 'insertFromComposition' && e.data) {
    e.preventDefault()
    compositionHandled = true
    insertHighlightedText(e.data)
    lastCompositionText.value = ''
    return
  }

  if (isComposing.value || e.isComposing) {
    return
  }

  if (e.inputType === 'insertText' && e.data) {
    e.preventDefault()
    insertHighlightedText(e.data)
  }
}

const handleCompositionStart = () => {
  isComposing.value = true
  lastCompositionText.value = ''
  compositionHandled = false
}

const handleCompositionEnd = (e) => {
  isComposing.value = false
  if (compositionHandled) {
    compositionHandled = false
    return
  }
  
  lastCompositionText.value = e?.data || ''

  // 兜底：部分浏览器首次中文输入不会触发可拦截的提交事件，这里手动包一层高光
  queueMicrotask(() => {
    if (!lastCompositionText.value) return

    const sel = window.getSelection()
    if (!sel || sel.rangeCount === 0) return

    const caret = sel.getRangeAt(0)
    if (!caret.collapsed) return

    const node = caret.startContainer
    const offset = caret.startOffset
    if (!node || node.nodeType !== Node.TEXT_NODE) return

    const raw = node.textContent || ''
    const text = lastCompositionText.value
    const textLen = text.length
    if (textLen === 0 || offset < textLen) return

    const start = offset - textLen
    if (raw.slice(start, offset) !== text) return

    const range = document.createRange()
    range.setStart(node, start)
    range.setEnd(node, offset)

    // 检查是否已经在自己的高光里，避免兜底再次嵌套
    let parent = node.parentNode
    let inOwnHighlight = false
    while (parent && parent !== editorRef.value && parent !== document.body) {
      if (parent.nodeType === 1 && parent.classList.contains('author-highlight')) {
        if (parent.getAttribute('data-author-id') === String(auth.user?.id || '')) {
          inOwnHighlight = true
        }
        break
      }
      parent = parent.parentNode
    }

    if (inOwnHighlight) {
      // 已经在自己的高光里，无需再次包裹
      lastCompositionText.value = ''
      return
    }

    const span = document.createElement('span')
    span.className = 'author-highlight'
    span.setAttribute('data-author-id', String(auth.user?.id || ''))
    span.setAttribute('data-author-name', String(auth.user?.username || ''))
    span.style.backgroundColor = authorColor.value
    try {
      let currentForeColor = document.queryCommandValue('foreColor')
      if (currentForeColor && currentForeColor !== 'rgb(17, 17, 17)' && currentForeColor !== 'rgba(0, 0, 0, 0)' && currentForeColor !== 'false') {
        span.style.color = currentForeColor
      } else if (currentColor.value !== '#111111') {
        span.style.color = currentColor.value
      }
    } catch (e) {
      // ignore exception
    }
    span.textContent = text

    range.deleteContents()
    range.insertNode(span)

    const next = document.createRange()
    next.setStartAfter(span)
    next.collapse(true)
    sel.removeAllRanges()
    sel.addRange(next)

    if (editorRef.value) {
      content.value = editorRef.value.innerHTML
      onLocalInput()
    }

    lastCompositionText.value = ''
  })
}

// Color picker state
const showColorPicker = ref(false)
const currentColor = ref('#111111')
const predefinedColors = [
  '#111111', '#4B5563', '#9CA3AF', '#EF4444', 
  '#F97316', '#F59E0B', '#10B981', '#3B82F6', 
  '#6366F1', '#8B5CF6', '#A855F7', '#EC4899'
]

const applyColor = (color) => {
  currentColor.value = color
  showColorPicker.value = false
  execCmd('foreColor', color)
}

const execCmd = (command, value = null) => {
  editorRef.value?.focus()
  restoreSelection()
  try { 
    document.execCommand('styleWithCSS', false, true) 
  } catch (e) {
    // ignore exception
  }
  document.execCommand(command, false, value)
  
  if (editorRef.value) {
    content.value = editorRef.value.innerHTML
    onLocalInput()
  }
}

const onRichInput = (e) => {
  content.value = e.target.innerHTML
  onLocalInput()
}

const handleKeydown = (e) => {
  if (e.key === 'Tab') {
    e.preventDefault()
    execCmd('insertHTML', '&nbsp;&nbsp;&nbsp;&nbsp;')
  }
}

const isDirty = computed(() => dirty.value || saving.value)

const syncingText = computed(() => {
  if (saving.value) return '保存中...'
  if (dirty.value) return '未保存更改'
  return '已保存至云端'
})

const load = async () => {
  doc.value = await docs.fetchById(id)
  content.value = doc.value.content || ''
  if (editorRef.value) {
    editorRef.value.innerHTML = content.value
  }
  version.value = doc.value.version || 0
  dirty.value = false
  await loadHistory()
}

const loadHistory = async () => {
  const historyRes = await getHistory(id)
  history.value = historyRes.data || []
}

const scrollChatToBottom = () => {
  setTimeout(() => {
    if (!chatListRef.value) return
    chatListRef.value.scrollTop = chatListRef.value.scrollHeight
  }, 0)
}

const formatChatTime = (timestamp) => {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  if (Number.isNaN(d.getTime())) return ''
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const toggleChat = () => {
  showChat.value = !showChat.value
  if (showChat.value) {
    unreadChatCount.value = 0
    scrollChatToBottom()
  }
}

const connectWs = () => {
  ws = useWebSocket(auth.token)
  ws.connect(() => {
    subscription = ws.client.subscribe(`/topic/document.${id}`, (message) => {
      const payload = JSON.parse(message.body)
      if (!payload || payload.documentId !== id) return

      if (payload.senderId === auth.user?.id) return

      content.value = payload.content || ''
      if (editorRef.value && editorRef.value.innerHTML !== content.value) {
        editorRef.value.innerHTML = content.value
      }
    })

    presenceSubscription = ws.client.subscribe(`/topic/document.${id}.presence`, (message) => {
      const payload = JSON.parse(message.body)
      if (!payload || payload.documentId !== id) return

      onlineCount.value = payload.onlineCount || 0
      onlineUsers.value = (payload.users || []).map((u) => ({
        name: u.displayName || `用户#${u.userId}`,
        color: u.color || '#cccccc'
      }))
    })

    chatSubscription = ws.client.subscribe(`/topic/document.${id}.chat`, (message) => {
      const payload = JSON.parse(message.body)
      if (!payload || payload.documentId !== id) return
      chatMessages.value.push(payload)
      if (chatMessages.value.length > 200) {
        chatMessages.value.shift()
      }
      if (showChat.value) {
        scrollChatToBottom()
      } else if (payload.senderId !== auth.user?.id) {
        unreadChatCount.value += 1
      }
    })

    setTimeout(() => {
      sendPresence('join')
    }, 300)
  })
}

const sendPatch = () => {
  if (!ws || !ws.client.connected) return
  ws.client.publish({
    destination: '/app/collab.edit',
    body: JSON.stringify({
      documentId: id,
      baseVersion: version.value,
      content: content.value,
      token: auth.token
    })
  })
}

const sendPresence = (action) => {
  if (!ws || !ws.client.connected) return
  ws.client.publish({
    destination: '/app/collab.presence',
    body: JSON.stringify({
      documentId: id,
      token: auth.token,
      clientId: presenceClientId,
      action,
      color: authorColor.value
    })
  })
}

const sendChat = () => {
  const message = chatInput.value.trim()
  if (!message) return
  if (!ws || !ws.client.connected) return

  ws.client.publish({
    destination: '/app/collab.chat',
    body: JSON.stringify({
      documentId: id,
      token: auth.token,
      content: message,
      color: authorColor.value
    })
  })

  chatInput.value = ''
}

const exportNow = async (format = 'html') => {
  const res = await exportDocument(id, format)
  const blob = new Blob([res.data], { type: res.headers['content-type'] || 'text/html;charset=utf-8' })

  const disposition = res.headers['content-disposition'] || ''
  const match = disposition.match(/filename="?([^";]+)"?/i)
  const fileName = match?.[1] || `document-${id}.${format}`

  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = decodeURIComponent(fileName)
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
  showExportMenu.value = false
}

const saveNow = async () => {
  if (editorRef.value) {
    content.value = editorRef.value.innerHTML
  }
  saving.value = true
  try {
    const saved = await docs.save(id, { content: content.value, manualSave: true })
    doc.value = { ...doc.value, ...saved }
    content.value = saved.content || ''
    version.value = saved.version || version.value
    dirty.value = false
    await loadHistory()
  } finally {
    saving.value = false
  }
}

const onLocalInput = () => {
  dirty.value = true
  clearTimeout(wsTimer)
  wsTimer = setTimeout(sendPatch, 300)
}

const invite = async () => {
  if (!inviteUser.value.trim()) return
  await inviteMember(id, { username: inviteUser.value, role: 'EDITOR' })
  inviteUser.value = ''
}

const rollback = async (operationId) => {
  saving.value = true
  try {
    const res = await rollbackHistory(id, operationId)
    const reverted = res.data
    doc.value = { ...doc.value, ...reverted }
    content.value = reverted.content || ''
    if (editorRef.value) {
      editorRef.value.innerHTML = content.value
    }
    version.value = reverted.version || version.value
    dirty.value = false
    await loadHistory()
  } finally {
    saving.value = false
  }
}

const back = () => router.push('/dashboard')

onMounted(async () => {
  initAuthorColor()
  await load()
  connectWs()
  document.addEventListener('selectionchange', handleSelectionChange)
  document.addEventListener('click', handleDocumentClick)
})

const handleDocumentClick = (e) => {
  if (!e.target.closest('.presence-chip')) {
    showOnlineUsers.value = false
  }
  if (!e.target.closest('.author-color-control')) {
    showAuthorColorPicker.value = false
  }
  if (!e.target.closest('.export-dropdown-wrap')) {
    showExportMenu.value = false
  }
}

onBeforeUnmount(() => {
  document.removeEventListener('selectionchange', handleSelectionChange)
  document.removeEventListener('click', handleDocumentClick)
  sendPresence('leave')
  if (subscription) subscription.unsubscribe()
  if (presenceSubscription) presenceSubscription.unsubscribe()
  if (chatSubscription) chatSubscription.unsubscribe()
  if (wsTimer) clearTimeout(wsTimer)
  
  setTimeout(() => {
    if (ws) ws.disconnect()
  }, 100)
})
</script>

<style scoped>
.editor-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
}

.editor-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #ebebeb;
  gap: 12px;
}

/* Zine Toolbar Styles */
.zine-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 12px 24px;
  border-bottom: 1px solid #ebebeb;
  margin: 0;
  width: 100%;
}

.tool-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.divider {
  width: 1px;
  height: 20px;
  background-color: #d1d5db;
  margin: 0 4px;
}

.tool-select {
  background: transparent;
  border: 1px solid transparent;
  padding: 4px;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
}

.tool-select:hover {
  background: rgba(0,0,0,0.05);
}

.tool-select option {
  color: #111;
  background: #fff;
  padding: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;
  padding: 0;
}

.tool-btn svg {
  width: 18px;
  height: 18px;
}

.tool-btn:hover {
  background: rgba(0,0,0,0.05);
  color: #111;
}

.tool-btn.active {
  background: #fff;
  color: #111;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.color-picker-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.color-dropdown {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 8px;
  background: #fff;
  border: 1px solid #ebebeb;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border-radius: 8px;
  padding: 8px;
  z-index: 101;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.color-swatch {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid rgba(0,0,0,0.1);
  cursor: pointer;
  padding: 0;
  transition: transform 0.1s;
}

.color-swatch:hover {
  transform: scale(1.1);
  border-color: rgba(0,0,0,0.3);
}

.picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
}

.color-indicator {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 3px;
  border: 1px solid rgba(0,0,0,0.1);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.presence-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f7faf7;
  border: 1px solid #d7ead7;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.presence-chip:hover {
  background: #eef7ee;
}

.caret-icon {
  width: 14px;
  height: 14px;
  color: #226b3a;
  transition: transform 0.2s;
}
.caret-icon.rotate {
  transform: rotate(180deg);
}

.presence-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background: #ffffff;
  border: 1px solid #ebebeb;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  border-radius: 12px;
  width: 200px;
  z-index: 100;
  overflow: hidden;
  cursor: default;
}

.dropdown-header {
  padding: 10px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #555;
  background: #fafafa;
  border-bottom: 1px solid #ebebeb;
}

.dropdown-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 240px;
  overflow-y: auto;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid #f5f5f5;
}
.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-empty {
  padding: 16px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: #111;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.presence-chip small {
  color: #226b3a;
  font-size: 12px;
  white-space: nowrap;
}

.presence-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.15);
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  color: #111;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.icon-btn:hover {
  background: #f0f0f0;
}
.icon-btn svg { width: 22px; height: 22px; }

.doc-meta h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111;
  line-height: 1.2;
}

.status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}
.status small {
  color: #777;
  font-size: 12px;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.dot.synced { background: #10b981; }
.dot.dirty { background: #f59e0b; }

.actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.author-color-control {
  position: relative;
}

.author-color-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #e2e2e2;
  background: #fff;
  color: #222;
  cursor: pointer;
  font-size: 13px;
}

.author-color-btn:hover {
  background: #fafafa;
}

.author-color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.15);
}

.author-color-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 220px;
  background: #fff;
  border: 1px solid #ebebeb;
  border-radius: 12px;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.12);
  padding: 12px;
  z-index: 120;
}

.author-color-wheel {
  width: 132px;
  height: 132px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: conic-gradient(
    #ff0000,
    #ff7a00,
    #ffee00,
    #55ff00,
    #00ffd5,
    #0077ff,
    #6a00ff,
    #ff00c8,
    #ff0000
  );
  position: relative;
  cursor: crosshair;
}

.author-color-wheel-center {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #fff;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 1px solid #ececec;
}

.author-color-slider-label {
  display: block;
  font-size: 12px;
  color: #666;
}

.author-color-slider-label input {
  width: 100%;
  margin-top: 6px;
}

.author-color-preview {
  margin-top: 10px;
}

.preview-chip {
  display: inline-block;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
  color: #222;
}

.invite-wrapper {
  display: flex;
  align-items: center;
  background: #f2f2f2;
  border-radius: 10px;
  padding: 4px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}
.invite-wrapper:focus-within {
  background: #fff;
  border-color: #111;
}
.invite-icon {
  width: 18px;
  height: 18px;
  color: #555;
  margin-left: 8px;
}
.invite-wrapper input {
  border: none;
  background: transparent;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
  width: 150px;
}
.small-btn {
  background: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.small-btn:hover { background: #f9f9f9; }

.primary-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #111;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.primary-btn:hover:not(:disabled) {
  background: #333;
}
.primary-btn svg { width: 16px; height: 16px; }

.secondary-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  color: #111;
  border: 1px solid #d9d9d9;
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.secondary-btn:hover:not(:disabled) {
  background: #f7f7f7;
}

.secondary-btn svg {
  width: 16px;
  height: 16px;
}

.export-dropdown-wrap {
  position: relative;
}

.export-caret {
  width: 14px;
  height: 14px;
  transition: transform 0.2s;
}

.export-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  min-width: 150px;
  overflow: hidden;
  z-index: 130;
}

.export-item {
  width: 100%;
  text-align: left;
  border: none;
  background: #fff;
  padding: 10px 12px;
  font-size: 13px;
  color: #222;
  cursor: pointer;
}

.export-item:hover {
  background: #f5f7fb;
}

.main-content {
  flex: 1;
  display: flex;
  padding: 24px;
  gap: 24px;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
}

.editor-area {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #ebebeb;
  box-shadow: 0 4px 20px rgba(0,0,0,0.01);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.rich-textarea {
  flex: 1;
  border: none;
  padding: 32px 40px;
  font-size: 16px;
  line-height: 1.6;
  background: transparent;
  color: #111;
  outline: none;
  font-family: 'Inter', -apple-system, sans-serif;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
.rich-textarea[contenteditable="true"]:empty:before {
  content: attr(placeholder);
  color: #ccc;
  pointer-events: none;
  display: block;
}

.history {
  width: 300px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #ebebeb;
  box-shadow: 0 4px 20px rgba(0,0,0,0.01);
}
.history-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  color: #111;
}
.history-head svg { width: 20px; height: 20px; }
.history-head h3 { margin: 0; font-size: 16px; font-weight: 600; }

.history ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.history-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
}
.rev-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.rev-info strong { font-size: 14px; font-weight: 600; }
.rev-info span { font-size: 12px; color: #777; }

.rollback-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: #fff;
  color: #111;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  border: 1px solid #ebebeb;
}
.rollback-btn:hover { background: #f5f5f5; }
.rollback-btn svg { width: 14px; height: 14px; }

.empty-history {
  color: #999;
  font-size: 14px;
  text-align: center;
  padding: 20px 0;
}

.chat-fab-wrap {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 160;
}

.chat-fab {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  border: none;
  border-radius: 999px;
  padding: 10px 14px;
  background: #111;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.22);
}

.chat-fab svg {
  width: 16px;
  height: 16px;
}

.chat-badge {
  min-width: 20px;
  height: 20px;
  border-radius: 999px;
  background: #ff3b30;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  line-height: 1;
}

.chat-panel {
  width: min(360px, calc(100vw - 24px));
  height: min(500px, calc(100vh - 110px));
  margin-top: 10px;
  border-radius: 14px;
  border: 1px solid #e7e7e7;
  background: #fff;
  box-shadow: 0 16px 44px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  height: 48px;
  padding: 0 12px;
  border-bottom: 1px solid #efefef;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-close {
  width: 28px;
  height: 28px;
  border: 1px solid #ececec;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  color: #666;
}

.chat-ephemeral-note {
  padding: 8px 12px;
  font-size: 12px;
  color: #8a5a00;
  background: #fff8e6;
  border-bottom: 1px solid #f2e5bf;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: linear-gradient(180deg, #fdfdfd 0%, #f8f8f8 100%);
}

.chat-empty {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-top: 20px;
}

.chat-item {
  margin-bottom: 10px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid #efefef;
  border-radius: 10px;
}

.chat-item.mine {
  border-color: #d8e5ff;
  background: #f7faff;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.chat-name {
  font-size: 12px;
  font-weight: 600;
}

.chat-meta time {
  font-size: 11px;
  color: #909090;
}

.chat-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-input-row {
  border-top: 1px solid #efefef;
  padding: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-input-row input {
  flex: 1;
  border: 1px solid #e3e3e3;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 13px;
  outline: none;
}

.chat-input-row input:focus {
  border-color: #9fb6ff;
}

.chat-send {
  border: none;
  border-radius: 10px;
  background: #1f4dff;
  color: #fff;
  font-size: 12px;
  padding: 8px 12px;
  cursor: pointer;
}

.chat-send:hover {
  background: #1a40d8;
}

@media (max-width: 900px) {
  .main-content { flex-direction: column; padding: 16px; }
  .history { width: 100%; }
  .actions .invite-wrapper { display: none; }
  .chat-fab-wrap {
    right: 12px;
    bottom: 12px;
  }
}
</style>