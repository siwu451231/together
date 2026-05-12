<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="dashboard">
    <header class="topbar">
      <div class="brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="brand-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.058 3.41c-4.074-.74-8.077.6-10.245 4.288-2.167 3.688-1.51 8.274 1.766 11.228l1.373 1.233-1.514 1.15a.75.75 0 0 0 .524 1.332l5.125-.33a.75.75 0 0 0 .618-.466l1.838-4.881a.75.75 0 0 0-1.428-.506l-.805 2.138-1.464-1.112c-2.434-2.193-2.92-5.59-1.31-8.324 1.611-2.734 4.582-3.73 7.608-3.18 3.027.55 5.513 3.084 5.952 6.064l1.492-.224c-.588-3.992-3.92-7.387-7.97-8.121z"/>
        </svg>
        <div class="user-info">
          <h1>Together 工作台</h1>
          <p>{{ auth.user?.displayName || auth.user?.username }}</p>
        </div>
      </div>
      <button class="icon-btn" @click="logout" title="退出登录">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
        </svg>
      </button>
    </header>

    <main class="content">
      <section class="create-panel">
        <div class="input-wrapper">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="input-icon">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m3.75 9v6m3-3H9m1.5-12H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
          </svg>
          <input v-model="title" placeholder="新文档标题..." @keyup.enter="createDoc" />
        </div>
        <button class="primary-btn" @click="createDoc" :disabled="creating">
          <svg v-if="!creating" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          <span>{{ creating ? '创建中...' : '新 建' }}</span>
        </button>
      </section>

      <section class="invite-panel" v-if="invitations.length">
        <div class="panel-head">
          <h2>待处理邀请</h2>
          <button class="icon-btn xs" @click="loadInvitations" :disabled="loadingInvites" title="刷新">
            <svg viewBox="0 0 24 24" fill="none" class="spin-hover" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
            </svg>
          </button>
        </div>
        <div class="invite-list">
          <article v-for="item in invitations" :key="item.id" class="invite-card">
            <div class="invite-info">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="invite-icon">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
              </svg>
              <div>
                <h3>{{ item.documentTitle }}</h3>
                <p><strong>{{ item.inviterName }}</strong> 邀请您作为 {{ item.role }}</p>
              </div>
            </div>
            <div class="invite-actions">
              <button class="action-btn accept" @click="accept(item.id)" title="接受">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                </svg>
              </button>
              <button class="action-btn reject" @click="reject(item.id)" title="拒绝">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </article>
        </div>
      </section>

      <section class="list-panel">
        <div class="panel-head">
          <h2>我的文档</h2>
        </div>
        <div v-if="docs.documents.length" class="doc-grid">
          <article v-for="doc in docs.documents" :key="doc.id" class="doc-card" @click="goEditor(doc.id)">
            <button
              v-if="doc.role === 'OWNER'"
              class="delete-doc-btn"
              @click.stop="removeDoc(doc)"
              title="删除文档"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 3h6m-7 4h8m-9 0l1 13h6l1-13M10 11v6m4-6v6" />
              </svg>
            </button>
            <div class="doc-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m3.75 9v6m3-3H9m1.5-12H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
            </div>
            <div class="doc-content">
              <h3>{{ doc.title }}</h3>
              <p>v{{ doc.version }} • {{ doc.role }}</p>
            </div>
          </article>
        </div>
        <div class="empty-state" v-else>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p>暂无文档，请在上方创建您的第一份协作文档。</p>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDocumentStore } from '@/stores/document'
import { acceptInvitation, listPendingInvitations, rejectInvitation } from '@/api/invitations'

const router = useRouter()
const auth = useAuthStore()
const docs = useDocumentStore()
const title = ref('')
const creating = ref(false)
const deleting = ref(false)
const invitations = ref([])
const loadingInvites = ref(false)
let inviteTimer = null

onMounted(async () => {
  await Promise.all([docs.fetchDocuments(), loadInvitations()])
  inviteTimer = setInterval(loadInvitations, 10000)
})

onBeforeUnmount(() => {
  if (inviteTimer) {
    clearInterval(inviteTimer)
    inviteTimer = null
  }
})

const loadInvitations = async () => {
  loadingInvites.value = true
  try {
    const res = await listPendingInvitations()
    invitations.value = res.data || []
  } finally {
    loadingInvites.value = false
  }
}

const createDoc = async () => {
  const normalizedTitle = title.value.trim()
  if (!normalizedTitle) {
    window.alert('请输入文档标题')
    return
  }
  creating.value = true
  try {
    const doc = await docs.create({ title: normalizedTitle, content: '' })
    title.value = ''
    router.push(`/editor/${doc.id}`)
  } catch (error) {
    console.error('[createDoc] 创建文档失败:', error)
    window.alert(error?.message || '创建失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

const goEditor = (id) => router.push(`/editor/${id}`)

const removeDoc = async (doc) => {
  if (deleting.value) return
  const ok = window.confirm(`确认删除文档「${doc.title}」吗？此操作不可撤销。`)
  if (!ok) return

  deleting.value = true
  try {
    await docs.remove(doc.id)
  } catch (error) {
    console.error('[removeDoc] 删除失败:', error)
    window.alert(error?.message || '删除失败，请稍后重试')
  } finally {
    deleting.value = false
  }
}

const accept = async (invitationId) => {
  const res = await acceptInvitation(invitationId)
  await Promise.all([docs.fetchDocuments(), loadInvitations()])
  const doc = res.data
  if (doc?.id) {
    router.push(`/editor/${doc.id}`)
  }
}

const reject = async (invitationId) => {
  await rejectInvitation(invitationId)
  await loadInvitations()
}

const logout = () => {
  if (inviteTimer) {
    clearInterval(inviteTimer)
    inviteTimer = null
  }
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: #fafafa;
}
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: #fff;
  border-bottom: 1px solid #ebebeb;
}
.brand {
  display: flex;
  align-items: center;
  gap: 16px;
}
.brand-icon {
  width: 28px;
  height: 28px;
  color: #111;
}
.user-info h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111;
}
.user-info p {
  margin: 2px 0 0;
  font-size: 13px;
  color: #777;
}

.content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 24px;
}

.create-panel {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}
.input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  color: #999;
}
.input-wrapper input {
  width: 100%;
  padding: 16px 16px 16px 48px;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  font-size: 15px;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.input-wrapper input:focus {
  border-color: #111;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 24px;
  border-radius: 12px;
  border: none;
  background: #111;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.primary-btn:hover:not(:disabled) {
  background: #333;
}
.primary-btn svg {
  width: 18px;
  height: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.panel-head h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111;
}

.icon-btn {
  background: transparent;
  border: none;
  color: #555;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn:hover {
  background: #f0f0f0;
  color: #111;
}
.icon-btn svg { width: 20px; height: 20px; }
.icon-btn.xs svg { width: 18px; height: 18px; }

.invite-panel {
  margin-bottom: 48px;
}
.invite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.invite-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border: 1px solid #ebebeb;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.invite-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.invite-icon {
  width: 24px;
  height: 24px;
  color: #666;
}
.invite-info h3 {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 500;
}
.invite-info p {
  margin: 0;
  font-size: 13px;
  color: #777;
}

.invite-actions {
  display: flex;
  gap: 8px;
}
.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
}
.action-btn svg {
  width: 18px;
  height: 18px;
}
.action-btn.accept {
  background: #f0fdf4;
  color: #166534;
}
.action-btn.accept:hover {
  background: #dcfce7;
}
.action-btn.reject {
  background: #fef2f2;
  color: #991b1b;
}
.action-btn.reject:hover {
  background: #fee2e2;
}

.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.doc-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: #fff;
  border: 1px solid #ebebeb;
  border-radius: 16px;
  padding: 20px;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.doc-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border-color: #e0e0e0;
}
.doc-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #fafafa;
  border-radius: 10px;
  color: #555;
}
.doc-icon svg {
  width: 20px;
  height: 20px;
}
.doc-content h3 {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 500;
  color: #111;
}
.doc-content p {
  margin: 0;
  font-size: 13px;
  color: #888;
}

.delete-doc-btn {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px solid #f0d6d6;
  background: #fff6f6;
  color: #b42318;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.delete-doc-btn:hover {
  background: #ffecec;
}

.delete-doc-btn svg {
  width: 14px;
  height: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 20px;
  color: #999;
  text-align: center;
  background: #fff;
  border: 1px dashed #ebebeb;
  border-radius: 16px;
}
.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  color: #ccc;
}
.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>
