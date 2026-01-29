import { Navigate, useLocation } from 'react-router-dom'
import UserService from '../../features/auth/services/AuthService'
import PropTypes from 'prop-types'

/**
 * ProtectedRoute component that checks authentication before rendering children
 * Redirects to login page if user is not authenticated
 * Allows OAuth2 callbacks to pass through
 */
function ProtectedRoute({ children }) {
  const location = useLocation()
  const isAuthenticated = UserService.isAuthenticated()
  
  // Check if this is an OAuth2 callback (allow it to pass through)
  const isOAuthCallback = new URLSearchParams(location.search).get('oauth') === 'success'

  if (!isAuthenticated && !isOAuthCallback) {
    // Redirect to login page if not authenticated and not an OAuth callback
    return <Navigate to="/login" replace />
  }

  // Render the protected component if authenticated or OAuth callback
  return children
}

ProtectedRoute.propTypes = {
  children: PropTypes.node.isRequired
}

export default ProtectedRoute

