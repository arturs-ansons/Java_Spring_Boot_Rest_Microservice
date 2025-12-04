

import org.banking.account.entity.Account;
import org.banking.account.service.AccountService;
import org.banking.crypto.entity.CryptoAccount;
import org.banking.crypto.entity.CryptoTransaction;
import org.banking.crypto.exception.*;
import org.banking.crypto.repository.CryptoAccountRepository;
import org.banking.crypto.repository.CryptoTransactionRepository;
import org.banking.crypto.service.CoinGeckoService;
import org.banking.crypto.service.CryptoTradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoTradingServiceTest {

    @Mock
    private CryptoTransactionRepository cryptoTransactionRepository;

    @Mock
    private CryptoAccountRepository cryptoAccountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private CoinGeckoService coinGeckoService;

    @InjectMocks
    private CryptoTradingService cryptoTradingService;

    private Account account;
    private CryptoAccount cryptoAccount;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setUserId(100L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(Account.AccountStatus.ACTIVE);

        cryptoAccount = new CryptoAccount();
        cryptoAccount.setId(1L);
        cryptoAccount.setAccount(account);
        cryptoAccount.setCryptoCurrency("BTC");
        cryptoAccount.setBalance(new BigDecimal("1.5"));
        cryptoAccount.setAvailableBalance(new BigDecimal("1.5"));
        cryptoAccount.setAverageBuyPrice(new BigDecimal("45000.00"));
        cryptoAccount.setTotalInvested(new BigDecimal("67500.00"));
    }

    // ========== EXECUTE BUY ORDER TESTS ==========

    @Test
    void executeBuyOrder_SuccessfulBuy_ReturnsTransaction() {
        // Arrange
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(cryptoAccountRepository.findByAccountIdAndCryptoCurrency(1L, "BTC"))
                .thenReturn(Optional.empty());
        when(cryptoAccountRepository.save(any(CryptoAccount.class))).thenReturn(cryptoAccount);
        when(cryptoTransactionRepository.save(any(CryptoTransaction.class))).thenReturn(new CryptoTransaction());

        // Act
        CryptoTransaction result = cryptoTradingService.executeBuyOrder(1L, "BTC",
                new BigDecimal("1000.00"), "USD");

        // Assert
        assertNotNull(result);
        verify(accountService).getAccountById(1L);
        verify(coinGeckoService).getSinglePrice("bitcoin", "usd");

        // When creating a new CryptoAccount, it gets saved twice:
        // 1. In getOrCreateCryptoAccount when creating the account
        // 2. After updating the balance in executeBuyOrder
        verify(cryptoAccountRepository, times(1)).save(any(CryptoAccount.class));
        verify(cryptoTransactionRepository).save(any(CryptoTransaction.class));
    }

    @Test
    void executeBuyOrder_ExistingCryptoAccount_UpdatesExistingAccount() {
        // Arrange
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(cryptoAccountRepository.findByAccountIdAndCryptoCurrency(1L, "BTC"))
                .thenReturn(Optional.of(cryptoAccount));
        when(cryptoAccountRepository.save(any(CryptoAccount.class))).thenReturn(cryptoAccount);
        when(cryptoTransactionRepository.save(any(CryptoTransaction.class))).thenReturn(new CryptoTransaction());

        // Act
        CryptoTransaction result = cryptoTradingService.executeBuyOrder(1L, "BTC",
                new BigDecimal("1000.00"), "USD");

        // Assert
        assertNotNull(result);

        // For existing accounts, it only gets saved once (after updating balance)
        verify(cryptoAccountRepository, times(1)).save(cryptoAccount);
    }

    @Test
    void executeBuyOrder_InsufficientFiatBalance_ThrowsException() {
        // Arrange
        account.setBalance(new BigDecimal("100.00")); // Low balance
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));

        // Act & Assert
        InsufficientFiatBalanceException exception = assertThrows(
                InsufficientFiatBalanceException.class,
                () -> cryptoTradingService.executeBuyOrder(1L, "BTC", new BigDecimal("1000.00"), "USD")
        );

        assertTrue(exception.getMessage().contains("Insufficient fiat balance"));
        verify(cryptoAccountRepository, never()).save(any());
        verify(cryptoTransactionRepository, never()).save(any());
    }

    @Test
    void executeBuyOrder_AccountInactive_ThrowsException() {
        // Arrange
        Account inactiveAccount = new Account();
        inactiveAccount.setId(1L);
        inactiveAccount.setUserId(100L);
        inactiveAccount.setBalance(new BigDecimal("5000.00"));
        inactiveAccount.setStatus(Account.AccountStatus.INACTIVE); // Inactive account

        when(accountService.getAccountById(1L)).thenReturn(inactiveAccount);

        // Act & Assert
        TradingNotAllowedException exception = assertThrows(
                TradingNotAllowedException.class,
                () -> cryptoTradingService.executeBuyOrder(1L, "BTC", new BigDecimal("1000.00"), "USD")
        );

        assertEquals("Account is not active for trading", exception.getMessage());
        // Verify that price was never fetched since validation should fail first
        verify(coinGeckoService, never()).getSinglePrice(anyString(), anyString());
    }

    // ========== EXECUTE SELL ORDER TESTS ==========

    @Test
    void executeSellOrder_SuccessfulSell_ReturnsTransaction() {
        // Arrange
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(cryptoAccountRepository.findByAccountIdAndCryptoCurrency(1L, "BTC"))
                .thenReturn(Optional.of(cryptoAccount));
        when(cryptoAccountRepository.save(any(CryptoAccount.class))).thenReturn(cryptoAccount);
        when(cryptoTransactionRepository.save(any(CryptoTransaction.class))).thenReturn(new CryptoTransaction());

        // Act
        CryptoTransaction result = cryptoTradingService.executeSellOrder(1L, "BTC",
                new BigDecimal("0.5"), "USD");

        // Assert
        assertNotNull(result);
        verify(cryptoAccountRepository, times(1)).save(cryptoAccount);
        verify(cryptoTransactionRepository).save(any(CryptoTransaction.class));
    }

    @Test
    void executeSellOrder_InsufficientCryptoBalance_ThrowsException() {
        // Arrange
        cryptoAccount.setAvailableBalance(new BigDecimal("0.1")); // Low crypto balance
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(cryptoAccountRepository.findByAccountIdAndCryptoCurrency(1L, "BTC"))
                .thenReturn(Optional.of(cryptoAccount));

        // Act & Assert
        InsufficientCryptoBalanceException exception = assertThrows(
                InsufficientCryptoBalanceException.class,
                () -> cryptoTradingService.executeSellOrder(1L, "BTC", new BigDecimal("0.5"), "USD")
        );

        assertTrue(exception.getMessage().contains("Insufficient BTC balance"));
        verify(cryptoAccountRepository, never()).save(any());
        verify(cryptoTransactionRepository, never()).save(any());
    }

    @Test
    void executeSellOrder_CryptoAccountNotFound_ThrowsException() {
        // Arrange
        when(accountService.getAccountById(1L)).thenReturn(account);
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(cryptoAccountRepository.findByAccountIdAndCryptoCurrency(1L, "BTC"))
                .thenReturn(Optional.empty());

        // Act & Assert
        CryptoNotFoundException exception = assertThrows(
                CryptoNotFoundException.class,
                () -> cryptoTradingService.executeSellOrder(1L, "BTC", new BigDecimal("0.5"), "USD")
        );

        assertEquals("No BTC balance found", exception.getMessage());
    }

    // ========== PRICE FETCHING TESTS ==========

    @Test
    void getCurrentCryptoPrice_Successful_ReturnsPrice() {
        // Arrange
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));

        // Act
        BigDecimal price = cryptoTradingService.getCurrentCryptoPrice("BTC", "USD");

        // Assert
        assertEquals(new BigDecimal("50000.00"), price);
        verify(coinGeckoService).getSinglePrice("bitcoin", "usd");
    }

    @Test
    void getCurrentCryptoPrice_ServiceFails_ThrowsException() {
        // Arrange
        when(coinGeckoService.getSinglePrice("bitcoin", "usd"))
                .thenThrow(new RuntimeException("API unavailable"));

        // Act & Assert
        CryptoPriceException exception = assertThrows(
                CryptoPriceException.class,
                () -> cryptoTradingService.getCurrentCryptoPrice("BTC", "USD")
        );

        assertEquals("Unable to fetch current price for BTC", exception.getMessage());
    }

    @Test
    void getMultipleCryptoPrices_Successful_ReturnsPriceMap() {
        // Arrange
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(coinGeckoService.getSinglePrice("ethereum", "usd")).thenReturn(new BigDecimal("3000.00"));

        // Act
        Map<String, BigDecimal> prices = cryptoTradingService.getMultipleCryptoPrices(
                List.of("BTC", "ETH"), "USD");

        // Assert
        assertEquals(2, prices.size());
        assertEquals(new BigDecimal("50000.00"), prices.get("BTC"));
        assertEquals(new BigDecimal("3000.00"), prices.get("ETH"));
    }

    @Test
    void getMultipleCryptoPrices_SomeFailures_ReturnsZeroForFailed() {
        // Arrange
        when(coinGeckoService.getSinglePrice("bitcoin", "usd")).thenReturn(new BigDecimal("50000.00"));
        when(coinGeckoService.getSinglePrice("ethereum", "usd"))
                .thenThrow(new RuntimeException("API error"));

        // Act
        Map<String, BigDecimal> prices = cryptoTradingService.getMultipleCryptoPrices(
                List.of("BTC", "ETH"), "USD");

        // Assert
        assertEquals(2, prices.size());
        assertEquals(new BigDecimal("50000.00"), prices.get("BTC"));
        assertEquals(BigDecimal.ZERO, prices.get("ETH"));
    }

    // ========== PORTFOLIO TESTS ==========

    @Test
    void getCryptoAccounts_UserHasAccounts_ReturnsPortfolio() {
        // Arrange
        when(cryptoAccountRepository.findByAccountId(1L)).thenReturn(List.of(cryptoAccount));

        // Act
        var portfolio = cryptoTradingService.getCryptoAccounts(1L);

        // Assert
        assertNotNull(portfolio);
        assertFalse(portfolio.isEmpty());
        verify(cryptoAccountRepository).findByAccountId(1L);
    }

    @Test
    void getCryptoAccounts_NoAccounts_ReturnsEmptyList() {
        // Arrange
        when(cryptoAccountRepository.findByAccountId(1L)).thenReturn(List.of());

        // Act
        var portfolio = cryptoTradingService.getCryptoAccounts(1L);

        // Assert
        assertNotNull(portfolio);
        assertTrue(portfolio.isEmpty());
        verify(cryptoAccountRepository).findByAccountId(1L);
    }
}