import axiosClient from './axiosClient'

export const pricingApi = {
  get: () => axiosClient.get('/api/pricing').then(r => r.data),
}
