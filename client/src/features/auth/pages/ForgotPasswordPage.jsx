import { useState } from 'react'
import Card from '../../../shared/components/Card'
import Button from '../../../shared/components/Button'
import Spinner from '../../../shared/components/Spinner'
import { NavLink } from 'react-router-dom'
import styles from './LoginPage.module.css'
import UserService from '../services/AuthService'

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [securityQuestion, setSecurityQuestion] = useState('')
  const [securityAnswer, setSecurityAnswer] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState(1) // 1: email, 2: security question, 3: reset password

  const handleEmailSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    if (!email.trim()) {
      setError('Please enter your email address')
      return
    }

    setLoading(true)
    try {
      const response = await UserService.getSecurityQuestion(email)
      if (response.question) {
        setSecurityQuestion(response.question)
        setStep(2)
      } else {
        setError('No security question found for this email')
      }
    } catch (err) {
      setError(err.message || 'Email not found')
    } finally {
      setLoading(false)
    }
  }

  const handleSecurityAnswerSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!securityAnswer.trim()) {
      setError('Please enter your security answer')
      return
    }

    setLoading(true)
    try {
      const response = await UserService.verifySecurityAnswer(email, securityAnswer)
      if (response.verified) {
        setStep(3)
      } else {
        setError('Incorrect security answer')
      }
    } catch (err) {
      setError(err.message || 'Verification failed')
    } finally {
      setLoading(false)
    }
  }

  const handlePasswordReset = async (e) => {
    e.preventDefault()
    setError('')

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (!newPassword.trim()) {
      setError('Please enter a new password')
      return
    }

    setLoading(true)
    try {
      await UserService.resetPassword(email, newPassword)
      alert('Password reset successfully!')
      window.location.href = '/login'
    } catch (err) {
      setError(err.message || 'Password reset failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.loginContainer}>
      <Card 
        style={{
          border: '1px solid #e0e0e0',
          maxWidth: '450px',
          width: '100%'
        }} 
        title="Forgot Password"
      >
        {error && (
          <div className={styles.errorMessage}>
            {error}
          </div>
        )}

        {step === 1 && (
          <form onSubmit={handleEmailSubmit}>
            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>
                Email Address
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={styles.input}
                placeholder="Enter your email"
                required
              />
            </div>
            <br />
            <Button 
              type="submit" 
              disabled={loading}
              style={{ width: '100%', fontSize: 'larger', fontWeight: '900' }}
            >
              {loading ? <Spinner text="Checking..." /> : 'Continue'}
            </Button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleSecurityAnswerSubmit}>
            <div className={styles.formGroup}>
              <label className={styles.label}>
                Security Question
              </label>
              <p style={{ 
                padding: '10px', 
                backgroundColor: '#f5f5f5', 
                borderRadius: '4px',
                margin: '0 0 15px 0'
              }}>
                {securityQuestion}
              </p>
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="securityAnswer" className={styles.label}>
                Your Answer
              </label>
              <input
                type="text"
                id="securityAnswer"
                name="securityAnswer"
                value={securityAnswer}
                onChange={(e) => setSecurityAnswer(e.target.value)}
                className={styles.input}
                placeholder="Enter your answer"
                required
              />
            </div>
            <br />
            <Button 
              type="submit" 
              disabled={loading}
              style={{ width: '100%', fontSize: 'larger', fontWeight: '900' }}
            >
              {loading ? <Spinner text="Verifying..." /> : 'Verify Answer'}
            </Button>
          </form>
        )}

        {step === 3 && (
          <form onSubmit={handlePasswordReset}>
            <div className={styles.formGroup}>
              <label htmlFor="newPassword" className={styles.label}>
                New Password
              </label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className={styles.input}
                placeholder="Enter new password"
                required
              />
            </div>
            <br />
            <div className={styles.formGroup}>
              <label htmlFor="confirmPassword" className={styles.label}>
                Confirm Password
              </label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className={styles.input}
                placeholder="Confirm new password"
                required
              />
            </div>
            <br />
            <Button 
              type="submit" 
              disabled={loading}
              style={{ width: '100%', fontSize: 'larger', fontWeight: '900' }}
            >
              {loading ? <Spinner text="Resetting..." /> : 'Reset Password'}
            </Button>
          </form>
        )}

        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <NavLink to="/login" className={styles.link}>
            Back to Login
          </NavLink>
        </div>
      </Card>
    </div>
  )
}

export default ForgotPasswordPage
