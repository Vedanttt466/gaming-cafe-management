import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const homeLink =
    user?.role === 'OWNER' ? '/owner' : user?.role === 'STAFF' ? '/staff' : '/customer'

  return (
    <nav className="bg-brand-700 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 flex items-center justify-between h-16">
        <Link to={user ? homeLink : '/'} className="font-bold text-lg tracking-tight">
          🎮 GameZone Cafe
        </Link>

        <div className="flex items-center gap-4 text-sm">
          {!user && (
            <>
              <Link to="/pricing" className="hover:text-brand-100">Pricing</Link>
              <Link to="/login" className="hover:text-brand-100">Login</Link>
              <Link to="/register" className="bg-white text-brand-700 px-3 py-1.5 rounded-md font-medium hover:bg-brand-50">
                Sign up
              </Link>
            </>
          )}

          {user && (
            <>
              <span className="hidden sm:inline text-brand-100">
                {user.name} · {user.role}
              </span>
              <button
                onClick={handleLogout}
                className="bg-brand-900/40 hover:bg-brand-900/60 px-3 py-1.5 rounded-md font-medium"
              >
                Logout
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}
