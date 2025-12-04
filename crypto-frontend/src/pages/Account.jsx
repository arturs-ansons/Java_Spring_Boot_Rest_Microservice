import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { cryptoService } from '../services/cryptoService';
import { authService } from '../services/authService';
import { Wallet, TrendingUp, DollarSign, TrendingDown, RefreshCw } from 'lucide-react';

const Account = () => {
  const { user } = useAuth();
  const [portfolio, setPortfolio] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [myAccount, setMyAccount] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadData = async () => {
    try {
      setRefreshing(true);
      const [portfolioData, transactionsData, myAccountData] = await Promise.all([
        cryptoService.getPortfolio(),
        cryptoService.getTransactionHistory(),
        cryptoService.getAaccount()
      ]);
      setPortfolio(portfolioData);
      setTransactions(transactionsData);
      setMyAccount(myAccountData)
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  return (

    <div className="page-container">
      <div className="page-header">
        <h1>Account Overview</h1>
        <button 
          onClick={loadData} 
          className="refresh-btn"
          disabled={refreshing}
        >
          <RefreshCw size={16} className={refreshing ? 'spinning' : ''} />
          Refresh
        </button>
      </div>

 {/* Account Summary */}
{myAccount && (
  
  <div className="portfolio-summary">
    <div className="summary-card">
      <div className="summary-icon">
        <DollarSign />
      </div>
      <div className="summary-content">
        <h3>Cash available</h3>
        <p className="summary-value">
          ${myAccount.reduce((total, item) => 
            total + (parseFloat(item?.balance) || 0), 0
          ).toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
          })}
        </p>
      </div>
    </div>
    </div>
      )}


      {/* Portfolio Summary */}
{portfolio && (
  <div className="portfolio-summary">
    <div className="summary-card">
      <div className="summary-icon">
        <Wallet />
      </div>
      <div className="summary-content">
        <h3>Total Portfolio Value</h3>
        <p className="summary-value">
          BTC/ {portfolio.reduce((total, item) => 
            total + (parseFloat(item?.balance) || 0), 0
          ).toLocaleString(undefined, {
             minimumFractionDigits: 8,
             maximumFractionDigits: 8
          })}
        </p>
      </div>
    </div>

    <div className="summary-card">
      <div className="summary-icon profit">
        <TrendingUp />
      </div>
      <div className="summary-content">
        <h3>Total Invested</h3>
        <p className="summary-value">
          ${portfolio.reduce((total, item) => 
            total + (parseFloat(item?.totalInvested) || 0), 0
          ).toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
          })}
        </p>
      </div>
    </div>
        </div>
      )}

      {/* Holdings */}
      {portfolio?.holdings && Object.keys(portfolio.holdings).length > 0 && (
        <div className="section">
          <h2>Your Holdings</h2>
          <div className="holdings-grid">
            {Object.entries(portfolio.holdings).map(([currency, holding]) => (
              <div key={currency} className="holding-card">
                <div className="holding-header">
                  <h3>{currency}</h3>
                  <span className={`price-change ${holding.profitLoss >= 0 ? 'positive' : 'negative'}`}>
                    {holding.profitLoss >= 0 ? '+' : ''}{holding.profitLossPercentage?.toFixed(2)}%
                  </span>
                </div>
                <div className="holding-details">
                  <p>Balance: <strong>{holding.balance?.toFixed(8)}</strong></p>
                  <p>Current Value: <strong>${holding.currentValue?.toFixed(2)}</strong></p>
                  <p>Avg Buy Price: <strong>${holding.averageBuyPrice?.toFixed(2)}</strong></p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}


{/* Recent Transactions */}
<div className="section">
  <h2>Recent Transactions</h2>

  <div className="transactions-list">
    {Array.isArray(transactions) && transactions.length > 0 ? (
      transactions.map((tx) => (
        <div key={tx.id} className="transaction-item">

          <div className="transaction-main">
            <div className="transaction-type">
              <span className={`type-badge ${tx.transactionType?.toLowerCase() || 'unknown'}`}>
                {tx.transactionType}
              </span>

              <span className="crypto-currency">{tx.cryptoCurrency}</span>
            </div>

            <div className="transaction-amount">
              <span className="crypto-amount">
                {Number(tx.cryptoAmount).toFixed(8)} {tx.cryptoCurrency}
              </span>

              <span className="fiat-amount">
                ${Number(tx.fiatAmount).toLocaleString(undefined, { minimumFractionDigits: 2 })} {tx.fiatCurrency}
              </span>
            </div>
          </div>

          <div className="transaction-meta">
            <span className="transaction-date">
              {new Date(tx.createdAt).toLocaleString()}
            </span>

            <span className={`status ${tx.status?.toLowerCase() || 'unknown'}`}>
              {tx.status}
            </span>
          </div>

        </div>
      ))
    ) : (
      <p className="no-data">No transactions yet</p>
    )}
  </div>
</div>

    </div>
    
  );
  
};


export default Account;