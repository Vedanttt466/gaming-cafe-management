import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { sessionApi } from '../../api/sessionApi'
import { useAuth } from '../../context/AuthContext'
import PcStatusGrid from '../../components/customer/PcStatusGrid'
import StatusBadge from '../../components/common/StatusBadge'
import Loading from '../../components/common/Loading'

export default function CustomerDashboard() {
  const { user } = useAuth()
  const [recentBookings, setRecentBookings] = useState([])
  const [recentSessions, setRecentSessions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      bookingApi.myHistory(0, 5),
      sessionApi.myHistory(0, 5),
    ]).then(([bookingsRes, sessionsRes]) => {
      setRecentBookings(bookingsRes.data.content)
      setRecentSessions(sessionsRes.data.content)
      setLoading(false)
    })
  }, [])

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Hey, {user.name.split(' ')[0]} 👋</h1>
          <p className="text-gray-500 text-sm mt-1">Here's what's happening at the cafe right now.</p>
        </div>
        <Link to="/customer/book" className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-5 py-2.5 rounded-md">
          Book a PC
        </Link>
      </div>

      <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wide">Live PC availability</h2>
      <PcStatusGrid />

      {loading ? <Loading /> : (
        <div className="grid sm:grid-cols-2 gap-6 mt-10">
          <div>
            <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wide">Recent reservations</h2>
            <div className="bg-white rounded-xl border border-gray-100 divide-y">
              {recentBookings.length === 0 && <p className="p-4 text-sm text-gray-400">No reservations yet.</p>}
              {recentBookings.map((b) => (
                <div key={b.id} className="p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">PC #{b.pcNumber ?? '—'}</p>
                    <p className="text-xs text-gray-400">{new Date(b.bookingTime).toLocaleString()}</p>
                  </div>
                  <StatusBadge status={b.status} />
                </div>
              ))}
            </div>
            <Link to="/customer/bookings" className="text-sm text-brand-600 hover:underline mt-2 inline-block">View all →</Link>
          </div>

          <div>
            <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wide">Recent sessions</h2>
            <div className="bg-white rounded-xl border border-gray-100 divide-y">
              {recentSessions.length === 0 && <p className="p-4 text-sm text-gray-400">No sessions yet.</p>}
              {recentSessions.map((s) => (
                <div key={s.id} className="p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">PC #{s.pcNumber} · {s.durationMinutes ? `${s.durationMinutes} min` : 'Ongoing'}</p>
                    <p className="text-xs text-gray-400">{new Date(s.startTime).toLocaleString()}</p>
                  </div>
                  <div className="text-right">
                    <StatusBadge status={s.status} />
                    {s.netAmountDue != null && <p className="text-xs text-gray-500 mt-1">₹{s.netAmountDue}</p>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
