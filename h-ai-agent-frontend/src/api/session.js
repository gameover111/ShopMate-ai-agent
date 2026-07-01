import http from './config'

export function fetchSessions(type = 'shop_mate') {
  return http.get('/chat/sessions', { params: { type } })
}

export function createSession(type = 'shop_mate') {
  return http.post('/chat/sessions', null, { params: { type } })
}

export function renameSession(id, title) {
  return http.put(`/chat/sessions/${id}`, { title })
}

export function deleteSession(id) {
  return http.delete(`/chat/sessions/${id}`)
}

export function fetchSessionMessages(id) {
  return http.get(`/chat/sessions/${id}/messages`)
}
