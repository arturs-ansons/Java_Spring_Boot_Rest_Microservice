import React, { createContext, useState, useContext, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

 useEffect(() => {
  const verifyUser = async () => {
    const token = localStorage.getItem('token');

    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const isValid = await authService.verifyToken(token);
      console.log('Token valid?', isValid);
      
      if (isValid) {
        setUser({ username: 'User' });
      } else {
        console.log('Invalid token, logging out');
        localStorage.removeItem('token');
        setUser(null);
      }
    } catch (error) {
      console.error('Token validation failed', error);
      localStorage.removeItem('token');
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  verifyUser();
}, []);



  const login = async (username, password) => {
    try {
      const response = await authService.login(username, password);
      setUser({ username: response.user?.username || username });
      localStorage.setItem('token', response.token);
      localStorage.setItem('username', response.user?.username || username);
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || 'Login failed' 
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await authService.register(userData);
      setUser({ username: response.user?.username || userData.username });
      localStorage.setItem('token', response.token);
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || 'Registration failed' 
      };
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('token');
  };

  const getUser = () => {
  const username = localStorage.getItem("username");

  return { username};
};

  const value = {
    user,
    getUser,
    login,
    register,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
