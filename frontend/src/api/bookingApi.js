import axiosClient from './axiosClient'

export const bookingApi = {
  create: (data) => axiosClient.post('/api/bookings', data).then(r => r.data),
  myHistory: (page = 0, size = 10) =>
    axiosClient.get('/api/bookings/my-history', { params: { page, size } }).then(r => r.data),
  getOne: (id) => axiosClient.get(`/api/bookings/${id}`).then(r => r.data),
  active: () => axiosClient.get('/api/bookings/active').then(r => r.data),
  cancel: (id) => axiosClient.post(`/api/bookings/${id}/cancel`).then(r => r.data),
}
