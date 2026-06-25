import { useState } from 'react'
import { sessionApi } from '../../api/sessionApi'

export default function WalkInForm({ onStarted }) {
  const [form, setForm] = useState({ name: '', phone: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await sessionApi.startWalkIn(form)
      setForm({ name: '', phone: '' })
      onStarted?.(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not start session. All PCs may be occupied.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="bg-white border border-gray-100 rounded-xl p-5 space-y-3">
      <h3 className="font-semibold text-gray-800">Start a walk-in session</h3>
      {error && <div className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-md px-3 py-2">{error}</div>}
      <input
        required placeholder="Customer name" value={form.name}
        onChange={(e) => setForm({ ...form, name: e.target.value })}
        className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <input
        placeholder="Phone (optional)" value={form.phone}
        onChange={(e) => setForm({ ...form, phone: e.target.value })}
        className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
      />
      <button type="submit" disabled={loading} className="w-full bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white text-sm font-medium py-2 rounded-md">
        {loading ? 'Assigning PC…' : 'Start session (auto-assign PC)'}
      </button>
    </form>
  )
}
