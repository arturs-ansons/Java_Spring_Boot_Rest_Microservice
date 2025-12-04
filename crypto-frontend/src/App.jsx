import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Account from './pages/Account';
import CryptoDashboard from './pages/CryptoDashboard';
import './App.css';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  return loading ? (
    <div className="spinner">⏳</div>
  ) : user ? (
    children
  ) : (
    <Navigate to="/login" />
  );
};


function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app">
          <Navbar />
          <main className="main-content">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route 
                path="/account" 
                element={
                  <ProtectedRoute>
                    <Account />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/crypto" 
                element={
                  <ProtectedRoute>
                    <CryptoDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route path="/" element={<Navigate to="/account" />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;