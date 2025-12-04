import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Wallet, LogOut, User } from 'lucide-react';

const Navbar = () => {
  const { user, logout, getUser } = useAuth();
  const location = useLocation();
  const userData = getUser();

  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <Wallet className="nav-icon" />
        <span>CryptoBank</span>
      </div>
      
      {user && (
        <div className="nav-links">
                <div>
        </div>
          <Link 
            to="/account" 
            className={`nav-link ${location.pathname === '/account' ? 'active' : ''}`}
          >
            <User size={18} />
            {userData.username}
          </Link>
          <Link 
            to="/crypto" 
            className={`nav-link ${location.pathname === '/crypto' ? 'active' : ''}`}
          >
            💰 Crypto
          </Link>
          <button onClick={handleLogout} className="nav-link logout-btn">
            <LogOut size={18} />
            Logout
          </button>
        </div>
      )}
    </nav>
  );
};

export default Navbar;