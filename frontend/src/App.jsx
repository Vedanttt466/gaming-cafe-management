import { Routes, Route } from 'react-router-dom'
import Navbar from './components/common/Navbar'
import ProtectedRoute from './components/common/ProtectedRoute'

import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import Pricing from './pages/public/Pricing'

import CustomerDashboard from './pages/customer/CustomerDashboard'
import CreateReservation from './pages/customer/CreateReservation'
import BookingHistory from './pages/customer/BookingHistory'

import StaffDashboard from './pages/staff/StaffDashboard'

import OwnerDashboard from './pages/owner/OwnerDashboard'
import RevenueReports from './pages/owner/RevenueReports'
import StaffManagement from './pages/owner/StaffManagement'
import CustomerHistory from './pages/owner/CustomerHistory'
import AuditLogs from './pages/owner/AuditLogs'

export default function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <Routes>
        <Route path="/" element={<Pricing />} />
        <Route path="/pricing" element={<Pricing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/customer" element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}><CustomerDashboard /></ProtectedRoute>
        } />
        <Route path="/customer/book" element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}><CreateReservation /></ProtectedRoute>
        } />
        <Route path="/customer/bookings" element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}><BookingHistory /></ProtectedRoute>
        } />

        <Route path="/staff" element={
          <ProtectedRoute allowedRoles={['STAFF', 'OWNER']}><StaffDashboard /></ProtectedRoute>
        } />

        <Route path="/owner" element={
          <ProtectedRoute allowedRoles={['OWNER']}><OwnerDashboard /></ProtectedRoute>
        } />
        <Route path="/owner/reports" element={
          <ProtectedRoute allowedRoles={['OWNER']}><RevenueReports /></ProtectedRoute>
        } />
        <Route path="/owner/staff" element={
          <ProtectedRoute allowedRoles={['OWNER']}><StaffManagement /></ProtectedRoute>
        } />
        <Route path="/owner/customers" element={
          <ProtectedRoute allowedRoles={['OWNER']}><CustomerHistory /></ProtectedRoute>
        } />
        <Route path="/owner/audit-logs" element={
          <ProtectedRoute allowedRoles={['OWNER']}><AuditLogs /></ProtectedRoute>
        } />

        <Route path="*" element={<Pricing />} />
      </Routes>
    </div>
  )
}
