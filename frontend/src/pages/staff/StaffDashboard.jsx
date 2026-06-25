import { useEffect, useState } from 'react'
import { bookingApi } from '../../api/bookingApi'
import { sessionApi } from '../../api/sessionApi'
import PcStatusGrid from '../../components/customer/PcStatusGrid'
import StatusBadge from '../../components/common/StatusBadge'
import WalkInForm from './WalkInForm'
import ActiveSessions from './ActiveSessions'
import Loading from '../../components/common/Loading'

export default function StaffDashboard() {
  const [pendingBookings, setPendingBookings] = useState([])
  const [activeSessions, setActiveSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const [checkingInId, setCheckingInId] = useState(null)
  const [error, setError] = useState('')

  const loadAll = async () => {
    const [bookingsRes, sessionsRes] = await Promise.all([
      bookingApi.active(),
      sessionApi.active(),
    ])
    setPendingBookings(bookingsRes.data)
    setActiveSessions(sessionsRes.data)
    setLoading(false)
  }

  useEffect(() => {
    loadAll()
    const interval = setInterval(loadAll, 30000) // periodic refresh as a fallback to WebSocket pings
    return () => clearInterval(interval)
  }, [])

  const handleCheckIn = async (bookingId) => {
    setCheckingInId(bookingId)
    setError('')
    try {
      await sessionApi.checkIn(bookingId)
      await loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not check in this booking.')
    } finally {
      setCheckingInId(null)
    }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Staff Dashboard</h1>

      <h2 className="text-sm font-semibold text-gray-700 mb-3 uppercase tracking-wide">PC status</h2>
      <PcStatusGrid />

      {loading ? <Loading /> : (
        <div className="grid lg:grid-cols-3 gap-6 mt-8">
          <div className="lg:col-span-1 space-y-6">
            <WalkInForm onStarted={loadAll} />

            <div>
              <h3 className="font-semibold text-gray-800 mb-3">Pending reservations</h3>
              {error && <div className="mb-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-md px-3 py-2">{error}</div>}
              <div className="bg-white rounded-xl border border-gray-100 divide-y">
                {pendingBookings.length === 0 && <p className="p-4 text-sm text-gray-400">None right now.</p>}
                {pendingBookings.map((b) => (
                  <div key={b.id} className="p-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-800">{b.customerName}</p>
                        <p className="text-xs text-gray-400">PC #{b.pcNumber} · {new Date(b.bookingTime).toLocaleTimeString()}</p>
                      </div>
                      <StatusBadge status={b.status} />
                    </div>
                    {b.status === 'CONFIRMED' && (
                      <button
                        onClick={() => handleCheckIn(b.id)} disabled={checkingInId === b.id}
                        className="mt-2 w-full text-xs bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white py-1.5 rounded-md"
                      >
                        {checkingInId === b.id ? 'Checking in…' : 'Check in'}
                      </button>
                    )}
                    {b.status === 'PENDING_PAYMENT' && (
                      <p className="text-xs text-amber-600 mt-1">Awaiting token payment</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="lg:col-span-2">
            <ActiveSessions sessions={activeSessions} onChanged={loadAll} />
          </div>
        </div>
      )}
    </div>
  )
}
