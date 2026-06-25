import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { pricingApi } from '../../api/pricingApi'
import Loading from '../../components/common/Loading'
import PcStatusGrid from '../../components/customer/PcStatusGrid'

export default function Pricing() {
  const [pricing, setPricing] = useState(null)

  useEffect(() => {
    pricingApi.get().then((res) => setPricing(res.data))
  }, [])

  if (!pricing) return <Loading />

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-12">
      <div className="text-center mb-10">
        <h1 className="text-3xl font-bold text-gray-900">Simple, transparent pricing</h1>
        <p className="text-gray-500 mt-2">
          We're open {pricing.operatingHours}, with 10 high-performance gaming PCs.
        </p>
      </div>

      <div className="grid sm:grid-cols-2 gap-6 mb-12">
        <div className="bg-gradient-to-br from-brand-600 to-brand-700 text-white rounded-2xl p-8 shadow-md">
          <h2 className="text-lg font-semibold opacity-90">Happy Hour</h2>
          <p className="text-4xl font-bold mt-2">₹{pricing.happyHourPackagePrice}</p>
          <p className="text-sm opacity-80 mt-1">Flat package · {pricing.happyHourWindow}</p>
          <p className="text-sm opacity-80 mt-4">
            Start your session any time in this window and play for as long as it lasts — one flat price, no clock-watching.
          </p>
        </div>

        <div className="bg-white border border-gray-200 rounded-2xl p-8 shadow-sm">
          <h2 className="text-lg font-semibold text-gray-900">Standard Rate</h2>
          <p className="text-4xl font-bold text-gray-900 mt-2">₹{pricing.standardRatePerHour}<span className="text-base font-medium text-gray-400">/hr</span></p>
          <p className="text-sm text-gray-500 mt-1">Billed in {pricing.billingBlockMinutes}-minute blocks</p>
          <p className="text-sm text-gray-500 mt-4">
            Outside happy hour, you're billed for exactly the time you play, rounded up to the nearest {pricing.billingBlockMinutes} minutes.
          </p>
        </div>
      </div>

      <div className="bg-amber-50 border border-amber-200 rounded-xl p-6 mb-12">
        <h3 className="font-semibold text-amber-900">Reserving online?</h3>
        <p className="text-sm text-amber-800 mt-1">
          A ₹{pricing.bookingTokenAmount} booking token holds your PC for {pricing.reservationHoldMinutes} minutes.
          It's adjusted against your final bill when you arrive — and forfeited only if you don't show up in time.
        </p>
      </div>

      <h3 className="text-lg font-semibold text-gray-900 mb-4">Live PC availability</h3>
      <PcStatusGrid />

      <div className="text-center mt-12">
        <Link to="/register" className="bg-brand-600 hover:bg-brand-700 text-white font-medium px-6 py-3 rounded-md inline-block">
          Create an account to book online
        </Link>
      </div>
    </div>
  )
}
