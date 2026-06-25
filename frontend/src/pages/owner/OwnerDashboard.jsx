import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { dashboardApi } from '../../api/dashboardApi'
import Card from '../../components/common/Card'
import PcStatusGrid from '../../components/customer/PcStatusGrid'
import Loading from '../../components/common/Loading'

export default function OwnerDashboard() {
  const [summary, setSummary] = useState(null)

  const load = () => dashboardApi.summary().then((res) => setSummary(res.data))

  useEffect(() => {
    load()
    const interval = setInterval(load, 30000)
    return () => clearInterval(interval)
  }, [])

  if (!summary) return <Loading />

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Owner Dashboard</h1>
        <div className="flex gap-3">
          <Link to="/owner/reports" className="text-sm bg-gray-800 hover:bg-black text-white px-4 py-2 rounded-md">Revenue reports</Link>
          <Link to="/owner/customers" className="text-sm bg-gray-800 hover:bg-black text-white px-4 py-2 rounded-md">Customers</Link>
          <Link to="/owner/audit-logs" className="text-sm bg-gray-800 hover:bg-black text-white px-4 py-2 rounded-md">Audit logs</Link>
          <Link to="/owner/staff" className="text-sm bg-brand-600 hover:bg-brand-700 text-white px-4 py-2 rounded-md">Manage staff</Link>
        </div>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <Card title="Today's revenue" value={`₹${summary.todayRevenue}`} />
        <Card title="This week" value={`₹${summary.weekRevenue}`} />
        <Card title="This month" value={`₹${summary.monthRevenue}`} />
        <Card title="Forfeited tokens today" value={`₹${summary.todayForfeitedTokens}`} sublabel={`${summary.todayNoShowCount} no-shows`} accent="text-red-600" />
        <Card title="PCs available" value={`${summary.availablePcs} / ${summary.totalPcs}`} />
        <Card title="Active sessions" value={summary.activeSessions} />
        <Card title="Pending reservations" value={summary.pendingReservations} />
        <Card title="Sessions today" value={summary.todaySessionsCount} />
      </div>

      <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wide">Live PC status</h2>
      <PcStatusGrid />
    </div>
  )
}
