import axiosClient from './axiosClient'

export const paymentApi = {
  createOrder: (bookingId) => axiosClient.post(`/api/payments/booking/${bookingId}/create-order`).then(r => r.data),
  verify: (data) => axiosClient.post('/api/payments/verify', data).then(r => r.data),
  recordCash: (sessionId) => axiosClient.post('/api/payments/cash', { sessionId }).then(r => r.data),
}
