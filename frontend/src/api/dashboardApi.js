import axiosClient from './axiosClient'

export const dashboardApi = {
  summary: () => axiosClient.get('/api/dashboard/summary').then(r => r.data),
  revenueTrend: (days = 14) => axiosClient.get('/api/analytics/revenue-trend', { params: { days } }).then(r => r.data),
  peakHours: () => axiosClient.get('/api/analytics/peak-hours').then(r => r.data),
  customerHistory: () => axiosClient.get('/api/analytics/customer-history').then(r => r.data),
}
