const API_BASE_URL = 'http://localhost:8080/api';

class UserService {
  /**
   * Helper method to handle fetch responses
   * @param {Response} response - Fetch response
   * @returns {Promise} Parsed JSON data
   */
  async handleResponse(response) {
    const data = await response.json().catch(() => ({}));
    
    if (!response.ok) {
      // Create user-friendly error messages based on status code
      let errorMessage = data.message || data.error;
      
      if (!errorMessage) {
        switch (response.status) {
          case 400:
            errorMessage = 'Invalid request. Please check your input and try again.';
            break;
          case 401:
            errorMessage = 'Invalid credentials. Please check your email and password.';
            break;
          case 403:
            errorMessage = 'Access denied. You do not have permission to perform this action.';
            break;
          case 404:
            errorMessage = 'The requested resource was not found.';
            break;
          case 409:
            errorMessage = 'This email is already registered. Please use a different email or login.';
            break;
          case 500:
            errorMessage = 'Server error. Please try again later.';
            break;
          default:
            errorMessage = 'Something went wrong. Please try again.';
        }
      }
      
      const error = new Error(errorMessage);
      error.response = { data, status: response.status };
      throw error;
    }
    
    return data;
  }

  /**
   * Login user with email and password
   * @param {string} email - User's email
   * @param {string} password - User's password
   * @returns {Promise} Response with user data and token
   */
  async login(email, password) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password })
      });
      
      const data = await this.handleResponse(response);
      
      // Store user data and token in localStorage
      if (data.token) {
        // Store the complete user object including UUID
        const userData = {
          uuid: data.uuid,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
        };
        
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('token', data.token);
        
        console.log('User logged in successfully:', userData);
      }
      
      return data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  /**
   * Register a new user
   * @param {Object} userData - User registration data
   * @returns {Promise} Response with created user
   */
  async register(userData) {
    const response = await fetch(`${API_BASE_URL}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    });
    
    // Get response body as text first
    const responseText = await response.text();
    
    // Try to parse as JSON, fallback to raw text
    let responseBody;
    try {
      responseBody = JSON.parse(responseText);
    } catch {
      responseBody = responseText;
    }
    
    if (!response.ok) {
      const error = new Error(responseBody?.message || 'Registration failed');
      error.responseBody = responseBody;
      throw error;
    }
    
    // Display success response body
    alert(typeof responseBody === 'object' 
      ? JSON.stringify(responseBody, null, 2) 
      : responseBody
    );
    
    return responseBody;
  }

  /**
   * Logout user
   */
  logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }

  /**
   * Get current logged in user
   * @returns {Object|null} Current user or null
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  /**
   * Check if user is authenticated
   * @returns {boolean} True if user is logged in
   */
  isAuthenticated() {
    return localStorage.getItem('token');
  }

  /**
   * Get user by UUID
   * @param {string} uuid - User UUID
   * @returns {Promise} User data
   */
  async getUserByUUID(uuid) {
    try {
      const response = await fetch(`${API_BASE_URL}/users/${uuid}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Get user error:', error);
      throw error;
    }
  }

  /**
   * Update user by UUID
   * @param {string} uuid - User UUID
   * @param {Object} userData - Updated user data
   * @returns {Promise} Updated user data
   */
  async updateUser(uuid, userData) {
    try {
      const response = await fetch(`${API_BASE_URL}/users/${uuid}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Update user error:', error);
      throw error;
    }
  }

  /**
   * Get all users
   * @returns {Promise} List of all users
   */
  async getAllUsers() {
    try {
      const response = await fetch(`${API_BASE_URL}/users`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Get all users error:', error);
      throw error;
    }
  }

  /**
   * Get security question for a user by email
   * @param {string} email - User's email
   * @returns {Promise} Security question
   */
  async getSecurityQuestion(email) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/security-question?email=${encodeURIComponent(email)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Get security question error:', error);
      throw error;
    }
  }

  /**
   * Verify security answer
   * @param {string} email - User's email
   * @param {string} answer - Security answer
   * @returns {Promise} Verification result
   */
  async verifySecurityAnswer(email, answer) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/verify-security-answer`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, answer })
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Verify security answer error:', error);
      throw error;
    }
  }

  /**
   * Reset password
   * @param {string} email - User's email
   * @param {string} newPassword - New password
   * @returns {Promise} Reset result
   */
  async resetPassword(email, newPassword) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, newPassword })
      });
      
      return await this.handleResponse(response);
    } catch (error) {
      console.error('Reset password error:', error);
      throw error;
    }
  }
}

export default new UserService();

