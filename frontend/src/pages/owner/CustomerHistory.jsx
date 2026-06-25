import { useEffect, useState } from 'react'
import { dashboardApi } from '../../api/dashboardApi'
import Loading from '../../components/common/Loading'

export default function CustomerHistory() {
  const [customers, setCustomers] = useState(null)
  const [sortKey, setSortKey] = useState('totalSpend')

  useEffect(() => {
    dashboardApi.customerHistory().then((res) => setCustomers(res.data))
  }, [])

  if (!customers) return <Loading />

  const sorted = [...customers].sort((a, b) => (b[sortKey] ?? 0) - (a[sortKey] ?? 0))

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Customer History</h1>
        <select value={sortKey} onChange={(e) => setSortKey(e.target.value)} className="text-sm border border-gray-300 rounded-md px-3 py-1.5">
          <option value="totalSpend">Sort by total spend</option>
          <option value="totalVisits">Sort by total visits</option>
          <option value="noShowCount">Sort by no-shows</option>
        </select>
      </div>

      <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-gray-500 border-b border-gray-100">
              <th className="p-3 font-medium">Name</th>
              <th className="p-3 font-medium">Contact</th>
              <th className="p-3 font-medium">Visits</th>
              <th className="p-3 font-medium">No-shows</th>
              <th className="p-3 font-medium">Total spend</th>
              <th className="p-3 font-medium">Last visit</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {sorted.map((c) => (
              <tr key={c.customerId}>
                <td className="p-3 font-medium text-gray-800">{c.name}</td>
                <td className="p-3 text-gray-500">{c.email}<br/><span className="text-xs">{c.phone}</span></td>
                <td className="p-3">{c.totalVisits}</td>
                <td className="p-3">{c.noShowCount > 0 ? <span className="text-red-600 font-medium">{c.noShowCount}</span> : 0}</td>
                <td className="p-3 font-medium">₹{c.totalSpend}</td>
                <td className="p-3 text-gray-400">{c.lastVisit ? new Date(c.lastVisit).toLocaleDateString() : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
