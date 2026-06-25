import { useEffect, useState } from 'react'
import { staffApi } from '../../api/staffApi'
import Loading from '../../components/common/Loading'

export default function StaffManagement() {
  const [staff, setStaff] = useState(null)
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '' })
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)

  const load = () => staffApi.list().then((res) => setStaff(res.data.content))

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setError('')
    setCreating(true)
    try {
      await staffApi.create(form)
      setForm({ name: '', email: '', phone: '', password: '' })
      await load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create staff account.')
    } finally {
      setCreating(false)
    }
  }

  const handleToggle = async (id, enabled) => {
    await staffApi.toggleEnabled(id, enabled)
    await load()
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Staff Management</h1>

      <div className="grid lg:grid-cols-3 gap-6">
        <form onSubmit={handleCreate} className="bg-white border border-gray-100 rounded-xl p-5 space-y-3 lg:col-span-1">
          <h3 className="font-semibold text-gray-800">Add new staff member</h3>
          {error && <div className="text-sm text-red-700 bg-red-50 border border-red-200 rounded-md px-3 py-2">{error}</div>}
          <input required placeholder="Full name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm" />
          <input required type="email" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm" />
          <input required pattern="[0-9]{10}" placeholder="Phone (10 digits)" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm" />
          <input required type="password" minLength={6} placeholder="Temporary password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm" />
          <button type="submit" disabled={creating} className="w-full bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white text-sm font-medium py-2 rounded-md">
            {creating ? 'Creating…' : 'Create staff account'}
          </button>
        </form>

        <div className="lg:col-span-2">
          <h3 className="font-semibold text-gray-800 mb-3">Current staff</h3>
          {!staff ? <Loading /> : (
            <div className="bg-white rounded-xl border border-gray-100 divide-y">
              {staff.length === 0 && <p className="p-4 text-sm text-gray-400">No staff added yet.</p>}
              {staff.map((s) => (
                <div key={s.id} className="p-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{s.name}</p>
                    <p className="text-xs text-gray-400">{s.email} · {s.phone}</p>
                  </div>
                  <button
                    onClick={() => handleToggle(s.id, !s.enabled)}
                    className={`text-xs px-3 py-1.5 rounded-md font-medium ${s.enabled ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100'}`}
                  >
                    {s.enabled ? 'Disable' : 'Enable'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
