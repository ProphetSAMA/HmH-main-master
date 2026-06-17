import { ElNotification } from 'element-plus'

class WebSocketClient {
  constructor() {
    if (WebSocketClient.instance) {
      return WebSocketClient.instance
    }
    WebSocketClient.instance = this
    
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.connected = false
    this.connecting = false
    this.reconnectTimer = null
  }

  connect() {
    // 如果正在连接或已经连接，则不再创建新连接
    if (this.connecting || this.connected) {
      console.log('WebSocket 已经连接或正在连接中')
      return
    }

    try {
      this.connecting = true
      console.log('正在建立 WebSocket 连接...')
      
      // 使用 WebSocket 连接后端
      this.ws = new WebSocket('ws://localhost:8080/ws')

      this.ws.onopen = () => {
        console.log('WebSocket 连接成功')
        this.reconnectAttempts = 0
        this.connected = true
        this.connecting = false
        
        // 连接成功后发送用户信息
        const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}')
        if (currentUser.roleName && currentUser.id) {
          this.ws.send(JSON.stringify({
            type: 'register',
            role: currentUser.roleName,
            userId: currentUser.id
          }))
        }
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'notification') {
            ElNotification({
              title: data.title || '系统通知',
              message: data.message,
              type: data.notificationType || 'info',
              duration: 5000,
              position: 'top-right'
            })
          }
        } catch (error) {
          console.error('处理 WebSocket 消息时出错:', error)
        }
      }

      this.ws.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.connected = false
        this.connecting = false
        this.reconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.connected = false
        this.connecting = false
      }
    } catch (error) {
      console.error('创建 WebSocket 连接时出错:', error)
      this.connected = false
      this.connecting = false
      this.reconnect()
    }
  }

  reconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重新连接... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      this.reconnectTimer = setTimeout(() => {
        this.connect()
      }, 3000)
    } else {
      console.log('达到最大重连次数，停止重连')
    }
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws && (this.connected || this.connecting)) {
      this.ws.close()
      this.connected = false
      this.connecting = false
    }
  }
}

export const wsClient = new WebSocketClient() 