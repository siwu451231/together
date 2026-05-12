import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

export function useWebSocket(token) {
  const client = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    connectHeaders: {
      Authorization: `Bearer ${token || ''}`
    }
  })

  return {
    client,
    connect(onConnect) {
      client.onConnect = onConnect
      client.activate()
    },
    disconnect() {
      if (client.active) {
        client.deactivate()
      }
    }
  }
}
