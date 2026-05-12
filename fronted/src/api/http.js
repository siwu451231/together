import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data

    // Backend uses a unified Result wrapper: { code, message, data }.
    // Business errors can still be HTTP 200, so we must check code here.
    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code !== 0) {
        return Promise.reject(new Error(payload.message || '请求失败'))
      }
      return payload
    }

    // Fallback for endpoints that return raw data.
    return payload
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default http
