import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, NavLink } from 'react-router-dom'
import Card from '../../../shared/components/Card'
import UserService from '../services/AuthService'
import styles from './LoginPage.module.css'


function LoginPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Handle OAuth2 callback
  useEffect(() => {
    const oauth = searchParams.get('oauth')
    const uuid = searchParams.get('uuid')
    const email = searchParams.get('email')
    const errorParam = searchParams.get('error')

    if (errorParam === 'oauth_failed') {
      setError('OAuth login failed. Please try again.')
      return
    }

    if (oauth === 'success' && uuid && email) {
      // OAuth2 login successful - fetch user data and store in localStorage
      const fetchOAuthUser = async () => {
        try {
          const userData = await UserService.getUserByUUID(uuid)
          
          // Store user data in localStorage (simulating a login session)
          const userSession = {
            uuid: userData.uuid,
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            userRole: userData.userRole?.role || null
          }
          
          localStorage.setItem('user', JSON.stringify(userSession))
          localStorage.setItem('token', uuid) // Using UUID as token for OAuth users
          
          console.log('OAuth2 user logged in successfully:', userSession)
          navigate('/connections')
        } catch (err) {
          console.error('Error fetching OAuth user:', err)
          setError('Failed to complete OAuth login. Please try again.')
        }
      }

      fetchOAuthUser()
    }
  }, [searchParams, navigate])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // Clear error when user starts typing
    if (error) setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Basic validation
    if (!formData.email || !formData.password) {
      setError('Please enter both email and password')
      setLoading(false)
      return
    }

    try {
      await UserService.login(formData.email, formData.password)
      // Redirect to connections page on successful login
      navigate('/connections')
    } catch (err) {
      console.error('Login failed:', err)
      setError(
        err.response?.data?.message || 
        'Login failed. Please check your credentials and try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleOAuth2Login = (provider) => {
    // Redirect to Spring Security OAuth2 authorization endpoint
    // Spring Security default: /oauth2/authorization/{registrationId}
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`
  }

  return (
    <div className={styles.loginContainer}>
      <Card 
        style={{
          border: '1px solid #e0e0e0',
          maxWidth: '450px',
          width: '100%'
        }} 
        title="Login"
      >
        <form className={styles.loginForm} onSubmit={handleSubmit}>
          {error && (
            <div className={styles.errorMessage}>
              {error}
            </div>
          )}

          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.label}>
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={styles.input}
              placeholder="Enter your email"
              required
              autoComplete="email"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.label}>
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={styles.input}
              placeholder="Enter your password"
              required
              autoComplete="current-password"
            />
          </div>

          <button 
            type="submit" 
            className={styles.submitButton}
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>

          <div className={styles.dividerContainer}>
            <span className={styles.dividerLine}></span>
            <span className={styles.dividerText}>OR</span>
            <span className={styles.dividerLine}></span>
          </div>

          <button 
            type="button"
            onClick={() => handleOAuth2Login('google')}
            className={styles.googleButton}
            disabled={loading}
          >
            <svg className={styles.googleIcon} viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Continue with Google
          </button>

          <div className={styles.footer}>
            <NavLink to="/forgot-password" className={styles.link}>
              Forgot Password?
            </NavLink>
            <span className={styles.divider}>|</span>
            <NavLink to="/registration" className={styles.link}>
              Create Account
            </NavLink>
           
          </div>
        </form>
      </Card>
    </div>
  )
}

export default LoginPage
