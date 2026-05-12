import http from './http'

export function listPendingInvitations() {
  return http.get('/api/invitations/pending')
}

export function acceptInvitation(id) {
  return http.post(`/api/invitations/${id}/accept`)
}

export function rejectInvitation(id) {
  return http.post(`/api/invitations/${id}/reject`)
}
