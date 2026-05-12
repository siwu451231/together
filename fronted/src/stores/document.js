import { defineStore } from 'pinia'
import { createDocument, deleteDocument, getDocument, listDocuments, updateDocument } from '@/api/documents'

export const useDocumentStore = defineStore('document', {
  state: () => ({
    documents: [],
    current: null,
    loading: false
  }),
  actions: {
    async fetchDocuments() {
      this.loading = true
      try {
        const res = await listDocuments()
        this.documents = res.data || []
      } finally {
        this.loading = false
      }
    },
    async create(payload) {
      const res = await createDocument(payload)
      try {
        await this.fetchDocuments()
      } catch (error) {
        console.warn('[document.create] 列表刷新失败，不影响新建结果:', error)
      }
      return res.data
    },
    async fetchById(id) {
      const res = await getDocument(id)
      this.current = res.data
      return this.current
    },
    async save(id, payload) {
      const res = await updateDocument(id, payload)
      this.current = res.data
      return this.current
    },
    async remove(id) {
      await deleteDocument(id)
      if (this.current?.id === id) {
        this.current = null
      }
      await this.fetchDocuments()
    }
  }
})
