import axiosClient from './axiosClient'

export const sessionApi = {
  startWalkIn: (data) => axiosClient.post('/api/sessions/walk-in', data).then(r => r.data),
  checkIn: (bookingId) => axiosClient.post('/api/sessions/check-in', { bookingId }).then(r => r.data),
  end: (id) => axiosClient.post(`/api/sessions/${id}/end`).then(r => r.data),
  active: () => axiosClient.get('/api/sessions/active').then(r => r.data),
  myHistory: (page = 0, size = 10) =>
    axiosClient.get('/api/sessions/my-history', { params: { page, size } }).then(r => r.data),
}
