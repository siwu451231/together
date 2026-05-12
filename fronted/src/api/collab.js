import http from './http'

export function getHistory(documentId) {
  return http.get(`/api/history/${documentId}`)
}

export function rollbackHistory(documentId, operationId) {
  return http.post(`/api/history/${documentId}/rollback/${operationId}`)
}
