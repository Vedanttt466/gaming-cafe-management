import { useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * Subscribes to /topic/pc-status over STOMP/WebSocket for real-time PC availability,
 * and falls back to the provided polling function if the socket cannot connect
 * (e.g. corporate proxies that block WebSocket upgrades).
 */
export function usePcStatusSocket(onUpdate) {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  useEffect(() => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true)
        client.subscribe('/topic/pc-status', (message) => {
          try {
            const data = JSON.parse(message.body)
            onUpdate?.(data)
          } catch (e) {
            console.error('Failed to parse pc-status message', e)
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return { connected }
}
