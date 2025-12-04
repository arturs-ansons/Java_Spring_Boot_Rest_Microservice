import axios from 'axios';

// Use gateway base URL instead of direct service URLs
const gatewayApi = axios.create({
  baseURL: '/api', // This will hit your Express gateway at port 5000
});

// Create instances for different services that go through the gateway
const authApi = axios.create({
  baseURL: '/api/auth', // Gateway will proxy to auth-service:8081
});

const accountApi = axios.create({
  baseURL: '/api/account', // Gateway will proxy to account-service:8082
});

// Add token to account API requests
accountApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration for account API
accountApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);



export const authService = {

  async login(username, password) {
    // This will hit: /api/auth/login -> gateway -> auth-service:8081/api/auth/login
    const response = await authApi.post('/login', { username, password });
    return response.data;
  },


  async register(userData) {
    // This will hit: /api/auth/register -> gateway -> auth-service:8081/api/auth/register
    const response = await authApi.post('/register', userData);
    return response.data;
  },



  async verifyToken(token) {
    if (!token) return false;

    try {
      // This will hit: /api/auth/validate -> gateway -> auth-service:8081/api/auth/validate
      const response = await authApi.post(
        '/validate', 
        {}, // empty body
        { headers: { Authorization: `Bearer ${token}` } }
      );

      
      
      // Handle different response formats
      if (typeof response.data === 'boolean') return response.data;
      if (response.data.valid !== undefined) return response.data.valid;
      if (response.data.success !== undefined) return response.data.success;
      return true;
    } catch (error) {
      console.error('Token validation error:', error);
      return false;
    }
  }
};

// Export accountApi for crypto services
export { accountApi };