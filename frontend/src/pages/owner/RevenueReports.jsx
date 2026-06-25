import { useEffect, useState } from 'react'
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { dashboardApi } from '../../api/dashboardApi'
import Loading from '../../components/common/Loading'

export default function RevenueReports() {
  const [trend, setTrend] = useState(null)
  const [peakHours, setPeakHours] = useState(null)

  useEffect(() => {
    dashboardApi.revenueTrend(14).then((res) => setTrend(res.data))
    dashboardApi.peakHours().then((res) => setPeakHours(res.data))
  }, [])

  if (!trend || !peakHours) return <Loading />

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Revenue Reports</h1>

      <div className="bg-white border border-gray-100 rounded-xl p-5 mb-8">
        <h2 className="font-semibold text-gray-800 mb-4">Revenue — last 14 days</h2>
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={trend}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f1f1" />
            <XAxis dataKey="label" fontSize={12} />
            <YAxis fontSize={12} />
            <Tooltip formatter={(v) => `₹${v}`} />
            <Line type="monotone" dataKey="revenue" stroke="#4f46e5" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-white border border-gray-100 rounded-xl p-5">
        <h2 className="font-semibold text-gray-800 mb-4">Peak hours (last 30 days)</h2>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={peakHours}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f1f1" />
            <XAxis dataKey="hourOfDay" fontSize={12} tickFormatter={(h) => `${h}:00`} />
            <YAxis fontSize={12} />
            <Tooltip labelFormatter={(h) => `${h}:00`} />
            <Bar dataKey="sessionCount" fill="#6366f1" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
