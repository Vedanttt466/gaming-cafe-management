import { useEffect, useState } from 'react'
import { bookingApi } from '../../api/bookingApi'
import StatusBadge from '../../components/common/StatusBadge'
import Loading from '../../components/common/Loading'

export default function BookingHistory() {
  const [bookings, setBookings] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [cancellingId, setCancellingId] = useState(null)

  const load = async (p = 0) => {
    setLoading(true)
    const res = await bookingApi.myHistory(p, 10)
    setBookings(res.data.content)
    setTotalPages(res.data.totalPages)
    setPage(p)
    setLoading(false)
  }

  useEffect(() => { load(0) }, [])

  const handleCancel = async (id) => {
    setCancellingId(id)
    try {
      await bookingApi.cancel(id)
      await load(page)
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My reservations</h1>

      {loading ? <Loading /> : (
        <div className="bg-white rounded-xl border border-gray-100 divide-y">
          {bookings.length === 0 && <p className="p-6 text-sm text-gray-400">No reservations found.</p>}
          {bookings.map((b) => (
            <div key={b.id} className="p-4 flex items-center justify-between">
              <div>
                <p className="font-medium text-gray-800">PC #{b.pcNumber ?? '—'}</p>
                <p className="text-xs text-gray-400">{new Date(b.bookingTime).toLocaleString()}</p>
                <p className="text-xs text-gray-400 mt-0.5">
                  Token: ₹{b.tokenAmount} {b.tokenPaid ? '(paid)' : '(unpaid)'} {b.tokenForfeited && '· forfeited'}
                </p>
              </div>
              <div className="text-right flex flex-col items-end gap-2">
                <StatusBadge status={b.status} />
                {(b.status === 'PENDING_PAYMENT' || b.status === 'CONFIRMED') && (
                  <button
                    onClick={() => handleCancel(b.id)}
                    disabled={cancellingId === b.id}
                    className="text-xs text-red-600 hover:underline disabled:opacity-50"
                  >
                    {cancellingId === b.id ? 'Cancelling…' : 'Cancel'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          {Array.from({ length: totalPages }).map((_, i) => (
            <button
              key={i} onClick={() => load(i)}
              className={`px-3 py-1 rounded-md text-sm ${i === page ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600'}`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
