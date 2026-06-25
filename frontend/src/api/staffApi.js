import axiosClient from './axiosClient'

export const staffApi = {
  create: (data) => axiosClient.post('/api/owner/staff', data).then(r => r.data),
  list: (page = 0, size = 20) => axiosClient.get('/api/owner/staff', { params: { page, size } }).then(r => r.data),
  toggleEnabled: (id, enabled) =>
    axiosClient.patch(`/api/owner/staff/${id}/enabled`, null, { params: { enabled } }).then(r => r.data),
}
