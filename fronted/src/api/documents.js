import http from './http'
import axios from 'axios'

export function listDocuments() {
  return http.get('/api/documents')
}

export function createDocument(payload) {
  return http.post('/api/documents', payload)
}

export function getDocument(id) {
  return http.get(`/api/documents/${id}`)
}

export function updateDocument(id, payload) {
  return http.put(`/api/documents/${id}`, payload)
}

export async function deleteDocument(id) {
  const normalizedId = String(id).trim()
  const attempts = [
    {
      label: 'DELETE /api/documents/{id}',
      run: () => http.delete(`/api/documents/${normalizedId}`)
    },
    {
      label: 'POST /api/documents/{id}/delete',
      run: () => http.post(`/api/documents/${normalizedId}/delete`)
    },
    {
      label: 'POST /api/documents/{id}',
      run: () => http.post(`/api/documents/${normalizedId}`)
    }
  ]

  let lastError = null
  for (const attempt of attempts) {
    try {
      return await attempt.run()
    } catch (error) {
      lastError = error
      console.warn(`[deleteDocument] ${attempt.label} failed:`, error?.message || error)
    }
  }

  throw lastError || new Error('删除文档失败：后端未提供可用删除接口')
}

export function inviteMember(id, payload) {
  return http.post(`/api/documents/${id}/invite`, payload)
}

export async function exportDocument(id, format = 'html') {
  const token = localStorage.getItem('token')
  return axios.get(`http://localhost:8080/api/documents/${id}/export`, {
    params: { format },
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}
