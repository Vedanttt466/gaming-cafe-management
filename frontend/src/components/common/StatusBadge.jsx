const STYLES = {
  AVAILABLE: 'bg-green-100 text-green-700 border-green-300',
  OCCUPIED: 'bg-red-100 text-red-700 border-red-300',
  RESERVED: 'bg-amber-100 text-amber-700 border-amber-300',
  MAINTENANCE: 'bg-gray-200 text-gray-600 border-gray-300',
  CONFIRMED: 'bg-blue-100 text-blue-700 border-blue-300',
  PENDING_PAYMENT: 'bg-amber-100 text-amber-700 border-amber-300',
  CHECKED_IN: 'bg-green-100 text-green-700 border-green-300',
  EXPIRED: 'bg-gray-200 text-gray-600 border-gray-300',
  CANCELLED: 'bg-gray-200 text-gray-600 border-gray-300',
  NO_SHOW: 'bg-red-100 text-red-700 border-red-300',
  COMPLETED: 'bg-blue-100 text-blue-700 border-blue-300',
  ACTIVE: 'bg-green-100 text-green-700 border-green-300',
}

export default function StatusBadge({ status }) {
  const style = STYLES[status] || 'bg-gray-100 text-gray-600 border-gray-300'
  return (
    <span className={`inline-block text-xs font-semibold px-2.5 py-1 rounded-full border ${style}`}>
      {status?.replace('_', ' ')}
    </span>
  )
}
