import { useEffect, useState } from 'react'
import { pcApi } from '../../api/pcApi'
import { usePcStatusSocket } from '../../hooks/usePcStatusSocket'
import StatusBadge from '../common/StatusBadge'

export default function PcStatusGrid() {
  const [pcs, setPcs] = useState([])
  const [loading, setLoading] = useState(true)

  const load = async () => {
    const res = await pcApi.getStatus()
    setPcs(res.data)
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  usePcStatusSocket((data) => setPcs(data))

  if (loading) return null

  return (
    <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
      {pcs.map((pc) => (
        <div key={pc.id} className="bg-white rounded-lg border border-gray-200 p-3 text-center">
          <div className="text-sm font-semibold text-gray-700">PC #{pc.pcNumber}</div>
          <div className="mt-2"><StatusBadge status={pc.status} /></div>
        </div>
      ))}
    </div>
  )
}
