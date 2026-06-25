import { useEffect, useState } from 'react'
import { sessionApi } from '../../api/sessionApi'
import { paymentApi } from '../../api/paymentApi'
import StatusBadge from '../../components/common/StatusBadge'

function ElapsedTimer({ startTime }) {
  const [now, setNow] = useState(Date.now())
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 30000)
    return () => clearInterval(t)
  }, [])
  const minutes = Math.floor((now - new Date(startTime).getTime()) / 60000)
  return <span>{minutes} min elapsed</span>
}

export default function ActiveSessions({ sessions, onChanged }) {
  const [endingId, setEndingId] = useState(null)
  const [billedSession, setBilledSession] = useState(null)

  const handleEnd = async (id) => {
    setEndingId(id)
    try {
      const res = await sessionApi.end(id)
      setBilledSession(res.data)
      onChanged?.()
    } finally {
      setEndingId(null)
    }
  }

  const handleCash = async (sessionId) => {
    await paymentApi.recordCash(sessionId)
    setBilledSession(null)
  }

  return (
    <div>
      <h3 className="font-semibold text-gray-800 mb-3">Active sessions ({sessions.length})</h3>

      {billedSession && (
        <div className="mb-4 bg-blue-50 border border-blue-200 rounded-xl p-4">
          <p className="text-sm font-medium text-blue-900">
            Session ended — PC #{billedSession.pcNumber} · {billedSession.billingType} billing
          </p>
          <p className="text-sm text-blue-800 mt-1">
            Gross ₹{billedSession.grossAmount} − token ₹{billedSession.tokenAdjusted} = <strong>Net due ₹{billedSession.netAmountDue}</strong>
          </p>
          <button onClick={() => handleCash(billedSession.id)} className="mt-2 text-xs bg-blue-600 text-white px-3 py-1.5 rounded-md hover:bg-blue-700">
            Mark as paid (cash)
          </button>
        </div>
      )}

      <div className="bg-white rounded-xl border border-gray-100 divide-y">
        {sessions.length === 0 && <p className="p-4 text-sm text-gray-400">No active sessions right now.</p>}
        {sessions.map((s) => (
          <div key={s.id} className="p-4 flex items-center justify-between">
            <div>
              <p className="font-medium text-gray-800">PC #{s.pcNumber} · {s.walkIn ? s.customerName : s.customerName}</p>
              <p className="text-xs text-gray-400"><ElapsedTimer startTime={s.startTime} /></p>
            </div>
            <div className="flex items-center gap-3">
              <StatusBadge status={s.status} />
              <button
                onClick={() => handleEnd(s.id)} disabled={endingId === s.id}
                className="text-xs bg-gray-800 hover:bg-black text-white px-3 py-1.5 rounded-md disabled:opacity-50"
              >
                {endingId === s.id ? 'Ending…' : 'End session'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
