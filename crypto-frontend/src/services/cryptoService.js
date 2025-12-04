import { accountApi } from './authService';

export const cryptoService = {
  async getPrices(cryptoIds = ['bitcoin', 'ethereum','cardano'], currency = 'usd') {
    const response = await accountApi.get('/crypto/trading/prices', {
      params: { cryptoIds: cryptoIds.join(','), currency }
    });
    return response.data;
  },

  async buyCrypto(cryptoCurrency, fiatAmount, fiatCurrency = 'USD') {
    const response = await accountApi.post('/crypto/trading/buy', {
      cryptoCurrency,
      fiatAmount,
      fiatCurrency
    });
    return response.data;
  },

  async sellCrypto(cryptoCurrency, cryptoAmount, fiatCurrency = 'USD') {
    const response = await accountApi.post('/crypto/trading/sell', {
      cryptoCurrency,
      cryptoAmount,
      fiatCurrency
    });
    return response.data;
  },

async getPortfolio() {
  try {
    console.log('🔄 Fetching portfolio...');
    const response = await accountApi.get('/crypto/trading/portfolio');
    console.log('📊 Portfolio response:', response.data);
    
    // Handle both array and object responses
    return response.data;
  } catch (error) {
    console.error('❌ Portfolio fetch error:', error);
    throw error;
  }
},


async getAaccount() {
  try {
    console.log('🔄 Fetching account...');
    const response = await accountApi.get('/accounts/my-accounts');
    console.log('📊 Account response:', response.data);
    
    // Handle both array and object responses
    return response.data;
  } catch (error) {
    console.error('❌ Account fetch error:', error);
    throw error;
  }
},

async getTransactionHistory(page = 0, size = 10) {
  const response = await accountApi.get('/crypto/trading/transactions', {
    params: { page, size }
  });

  console.log('📊 Transactions response:', response.data);

  // Backend returns a plain array → NOT paginated
  return response.data; 
}

};