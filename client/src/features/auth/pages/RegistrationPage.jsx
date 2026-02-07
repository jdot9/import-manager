import { useState } from "react"
import Card from "../../../shared/components/Card"
import Button from "../../../shared/components/Button"
import Spinner from "../../../shared/components/Spinner"
import { NavLink, useNavigate } from "react-router"
import styles from "./LoginPage.module.css"
import UserService from "../services/AuthService"
import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'

function RegistrationPage() {
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' })
  const navigate = useNavigate()

  const handleSnackbarClose = () => {
    setSnackbar({ ...snackbar, open: false })
  }
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    secretQuestion: '',
    secretAnswer: ''
  })

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
    
    // Validate passwords match
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    
    // Validate password is not empty
    if (!formData.password) {
      setError('Password is required')
      return
    }

    setLoading(true)
    try {
      const response = await UserService.register(formData)
      console.log(response)
      setSnackbar({
        open: true,
        message: response.message,
        severity: 'success'
      })
      // Redirect to login page on successful registration after a short delay
      setTimeout(() => {
        navigate('/login')
      }, 1500)
    } catch (error) {
      console.error(error.message)
      const errorMessage = typeof error.responseBody === 'object' 
        ? JSON.stringify(error.responseBody, null, 2) 
        : (error.responseBody || error.message)
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card title={"Registration"}>
      <form onSubmit={handleSubmit}>
        {error && <div className="form-error" style={{ color: 'red', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}
        <div className="form-group">
            <label htmlFor="firstName" className="form-group__label">First Name</label>
            <input type="text" className="form-group__input" name="firstName" value={formData.firstName} onChange={handleChange} />
        </div>
 
        <div className="form-group">
              <label htmlFor="lastName" className="form-group__label">Last Name</label>
              <input type="text" className="form-group__input" name="lastName" value={formData.lastName} onChange={handleChange} />
        </div>

        <div className="form-group">
            <label htmlFor="email" className="form-group__label">Email</label>
            <input type="email" className="form-group__input" name="email" value={formData.email} onChange={handleChange} />
        </div>
 
        <div className="form-group">
              <label htmlFor="newPassword" className="form-group__label">New Password</label>
              <input type="password" className="form-group__input" name="password" value={formData.password} onChange={handleChange} />
        </div>

        <div className="form-group">
              <label htmlFor="confirmPassword" className="form-group__label">Confirm Password</label>
              <input type="password" className="form-group__input" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} />
        </div>


        <div className="form-group">
              <label htmlFor="question" className="form-group__label">Secret Question</label>
              <input placeholder="Ask a question only you know the answer to." type="text" className="form-group__input" name="secretQuestion" value={formData.secretQuestion} onChange={handleChange} />
        </div>

        <div className="form-group">
              <label htmlFor="answer" className="form-group__label">Secret Answer</label>
              <input type="password" className="form-group__input" name="secretAnswer" value={formData.secretAnswer} onChange={handleChange} />
        </div>

        <div className="form-group">
            <Button type="submit" disabled={loading} style={{width: "100%", fontSize: "larger", fontWeight: '900'}}>
              {loading ? <Spinner text="Registering..." /> : 'Register'}
            </Button>
        </div>
            
        <NavLink to="/login" className={styles.link}>Go back</NavLink>

      </form>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Card>
  )
}

export default RegistrationPage
