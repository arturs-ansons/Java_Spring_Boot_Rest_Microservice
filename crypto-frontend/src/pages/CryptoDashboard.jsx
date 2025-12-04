import React, { useState, useEffect } from 'react';
import { cryptoService } from '../services/cryptoService';
import { TrendingUp, TrendingDown, ArrowUp, ArrowDown } from 'lucide-react';

const CryptoDashboard = () => {
  const [prices, setPrices] = useState({});
  const [buyForm, setBuyForm] = useState({
    cryptoCurrency: 'BTC',
    fiatAmount: '',
    fiatCurrency: 'USD'
  });
  const [sellForm, setSellForm] = useState({
    cryptoCurrency: 'BTC',
    cryptoAmount: '',
    fiatCurrency: 'USD'
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const cryptoOptions = [
    { value: 'BTC', label: 'Bitcoin', geckoId: 'bitcoin' },
    { value: 'ETH', label: 'Ethereum', geckoId: 'ethereum' },
    { value: 'ADA', label: 'Cardano', geckoId: 'cardano' }
  ];

  const loadPrices = async () => {
    try {
      const cryptoIds = cryptoOptions.map(opt => opt.geckoId);
      const priceData = await cryptoService.getPrices(cryptoIds, 'usd');
      setPrices(priceData);
    } catch (error) {
      console.error('Error loading prices:', error);
    }
  };

  useEffect(() => {
    loadPrices();
    const interval = setInterval(loadPrices, 300000); // Refresh every 300 seconds
    return () => clearInterval(interval);
  }, []);

  const handleBuy = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    
    try {
      await cryptoService.buyCrypto(
        buyForm.cryptoCurrency,
        parseFloat(buyForm.fiatAmount),
        buyForm.fiatCurrency
      );
      setMessage('Buy order executed successfully!');
      setBuyForm({ ...buyForm, fiatAmount: '' });
      loadPrices(); // Refresh data
    } catch (error) {
      setMessage(`Error: ${error.response?.data?.message || error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleSell = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    
    try {
      await cryptoService.sellCrypto(
        sellForm.cryptoCurrency,
        parseFloat(sellForm.cryptoAmount),
        sellForm.fiatCurrency
      );
      setMessage('Sell order executed successfully!');
      setSellForm({ ...sellForm, cryptoAmount: '' });
      loadPrices(); // Refresh data
    } catch (error) {
      setMessage(`Error: ${error.response?.data?.message || error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>Crypto Trading</h1>

      {/* Price Ticker */}
      <div className="price-ticker">
        <h2>Current Prices</h2>
        <div className="prices-grid">
          {cryptoOptions.map(crypto => (
            <div key={crypto.value} className="price-card">
              <h3>{crypto.label}</h3>
              <p className="price">${prices[crypto.geckoId] || '0.00'}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Trading Forms */}
      <div className="trading-section">
        <div className="trading-forms">
          {/* Buy Form */}
          <form onSubmit={handleBuy} className="trade-form buy-form">
            <h3>
              <ArrowUp className="icon buy" />
              Buy Crypto
            </h3>
            
            <div className="form-group">
              <label>Crypto Currency</label>
              <select
                value={buyForm.cryptoCurrency}
                onChange={(e) => setBuyForm({ ...buyForm, cryptoCurrency: e.target.value })}
              >
                {cryptoOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Amount ({buyForm.fiatCurrency})</label>
              <input
                type="number"
                step="0.01"
                value={buyForm.fiatAmount}
                onChange={(e) => setBuyForm({ ...buyForm, fiatAmount: e.target.value })}
                placeholder="0.00"
                required
              />
            </div>

            <button type="submit" disabled={loading} className="btn btn-buy">
              {loading ? 'Buying...' : 'Buy Crypto'}
            </button>
          </form>

          {/* Sell Form */}
          <form onSubmit={handleSell} className="trade-form sell-form">
            <h3>
              <ArrowDown className="icon sell" />
              Sell Crypto
            </h3>
            
            <div className="form-group">
              <label>Crypto Currency</label>
              <select
                value={sellForm.cryptoCurrency}
                onChange={(e) => setSellForm({ ...sellForm, cryptoCurrency: e.target.value })}
              >
                {cryptoOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Amount (Crypto)</label>
              <input
                type="number"
                step="0.00000001"
                value={sellForm.cryptoAmount}
                onChange={(e) => setSellForm({ ...sellForm, cryptoAmount: e.target.value })}
                placeholder="0.00000000"
                required
              />
            </div>

            <button type="submit" disabled={loading} className="btn btn-sell">
              {loading ? 'Selling...' : 'Sell Crypto'}
            </button>
          </form>
        </div>

        {/* Message Display */}
        {message && (
          <div className={`message ${message.includes('Error') ? 'error' : 'success'}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
};

export default CryptoDashboard;