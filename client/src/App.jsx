import './App.css'
import ConnectionPage from './features/connections/pages/ConnectionPage'
import { BrowserRouter, Routes, Route, Navigate } from "react-router";
import './index.css'
import MainLayout from './shared/components/MainLayout'
import ImportPage from './features/imports/pages/ImportPage'
import LoginPage from './features/auth/pages/LoginPage'
import ProtectedRoute from './shared/components/ProtectedRoute'
import RegistrationPage from './features/auth/pages/RegistrationPage'
import ForgotPasswordPage from './features/auth/pages/ForgotPasswordPage'

function App() {
 
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registration" element={<RegistrationPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        
        {/* Protected routes - require authentication */}
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <MainLayout>
                <Routes>
                  <Route path="/" element={<Navigate to="/connections" replace />} />
                  <Route path="/connections" element={<ConnectionPage />} />
                  <Route path="/imports" element={<ImportPage />} />
                </Routes>
              </MainLayout>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App
