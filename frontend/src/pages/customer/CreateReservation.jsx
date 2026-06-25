import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { paymentApi } from '../../api/paymentApi'
import { pricingApi } from '../../api/pricingApi'
import { useAuth } from '../../context/AuthContext'

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) return resolve(true)
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export default function CreateReservation() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [pricing, setPricing] = useState(null)
  const [bookingTime, setBookingTime] = useState('')
  const [step, setStep] = useState('form') // form -> paying -> done
  const [error, setError] = useState('')
  const [booking, setBooking] = useState(null)

  useEffect(() => {
    pricingApi.get().then((res) => setPricing(res.data))
  }, [])

  const handleCreateBooking = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const res = await bookingApi.create({ bookingTime })
      setBooking(res.data)
      await startPayment(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create reservation. Please try a different time.')
    }
  }

  const startPayment = async (bookingData) => {
    setStep('paying')
    const scriptLoaded = await loadRazorpayScript()
    if (!scriptLoaded) {
      setError('Could not load the payment widget. Please check your connection and try again.')
      setStep('form')
      return
    }

    const orderRes = await paymentApi.createOrder(bookingData.id)
    const order = orderRes.data

    const options = {
      key: order.razorpayKeyId,
      amount: Math.round(order.amount * 100),
      currency: order.currency,
      name: 'GameZone Cafe',
      description: `Booking token for PC #${bookingData.pcNumber}`,
      order_id: order.razorpayOrderId,
      prefill: { name: user.name, email: user.email },
      handler: async (response) => {
        try {
          await paymentApi.verify({
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature,
          })
          setStep('done')
        } catch (err) {
          setError('Payment verification failed. Please contact staff if money was deducted.')
          setStep('form')
        }
      },
      modal: {
        ondismiss: () => setStep('form'),
      },
      theme: { color: '#4f46e5' },
    }

    const rzp = new window.Razorpay(options)
    rzp.open()
  }

  if (step === 'done') {
    return (
      <div className="max-w-md mx-auto px-4 py-16 text-center">
        <div className="text-5xl mb-4">✅</div>
        <h1 className="text-2xl font-bold text-gray-900">You're booked!</h1>
        <p className="text-gray-500 mt-2">
          PC #{booking?.pcNumber} is reserved for you. Arrive within {pricing?.reservationHoldMinutes} minutes
          of your booking time or the reservation (and token) will be forfeited.
        </p>
        <button onClick={() => navigate('/customer')} className="mt-6 bg-brand-600 hover:bg-brand-700 text-white font-medium px-6 py-2.5 rounded-md">
          Go to dashboard
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-md mx-auto px-4 py-12">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Reserve a PC</h1>
      <p className="text-gray-500 text-sm mb-6">
        We'll auto-assign you any available PC. {pricing && `A ₹${pricing.bookingTokenAmount} token holds it for ${pricing.reservationHoldMinutes} minutes.`}
      </p>

      {error && <div className="mb-4 text-sm text-red-700 bg-red-50 border border-red-200 rounded-md px-3 py-2">{error}</div>}

      <form onSubmit={handleCreateBooking} className="bg-white border border-gray-100 rounded-xl p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Date & time</label>
          <input
            type="datetime-local" required value={bookingTime}
            onChange={(e) => setBookingTime(e.target.value)}
            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
          {pricing && (
            <p className="text-xs text-gray-400 mt-1">Open daily {pricing.operatingHours}</p>
          )}
        </div>
        <button
          type="submit" disabled={step === 'paying'}
          className="w-full bg-brand-600 hover:bg-brand-700 disabled:opacity-60 text-white font-medium py-2.5 rounded-md"
        >
          {step === 'paying' ? 'Opening payment…' : `Reserve & pay token`}
        </button>
      </form>
    </div>
  )
}
