import axiosClient from './axiosClient'

export const pcApi = {
  getStatus: () => axiosClient.get('/api/pcs/status').then(r => r.data),
  setMaintenance: (id, underMaintenance) =>
    axiosClient.patch(`/api/pcs/${id}/maintenance`, null, { params: { underMaintenance } }).then(r => r.data),
}
