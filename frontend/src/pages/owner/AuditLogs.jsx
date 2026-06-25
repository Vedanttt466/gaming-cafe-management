import { useEffect, useState } from 'react'
import axiosClient from '../../api/axiosClient'
import Loading from '../../components/common/Loading'

export default function AuditLogs() {
  const [logs, setLogs] = useState(null)
  const [page, setPage] = useState(0)

  const load = (p) => axiosClient.get('/api/owner/audit-logs', { params: { page: p, size: 30 } })
    .then((res) => setLogs(res.data.data))

  useEffect(() => { load(page) }, [page])

  if (!logs) return <Loading />

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Audit Logs</h1>

      <div className="bg-white rounded-xl border border-gray-100 divide-y">
        {logs.content.length === 0 && <p className="p-4 text-sm text-gray-400">No log entries yet.</p>}
        {logs.content.map((log) => (
          <div key={log.id} className="p-3 text-sm">
            <span className="font-medium text-gray-800">{log.action}</span>
            <span className="text-gray-400"> · {log.entityType} #{log.entityId} · {new Date(log.createdAt).toLocaleString()}</span>
            {log.details && <p className="text-xs text-gray-500 mt-0.5">{log.details}</p>}
          </div>
        ))}
      </div>

      {logs.totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          {Array.from({ length: logs.totalPages }).map((_, i) => (
            <button key={i} onClick={() => setPage(i)}
              className={`px-3 py-1 rounded-md text-sm ${i === page ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600'}`}>
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
