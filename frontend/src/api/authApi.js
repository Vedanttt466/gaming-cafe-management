import axiosClient from './axiosClient'

export const authApi = {
  login: (data) => axiosClient.post('/api/auth/login', data).then(r => r.data),
  register: (data) => axiosClient.post('/api/auth/register', data).then(r => r.data),
}
