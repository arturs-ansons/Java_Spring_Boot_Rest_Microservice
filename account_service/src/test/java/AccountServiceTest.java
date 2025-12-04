
import org.banking.account.dto.AccountRequest;
import org.banking.account.dto.AccountResponse;
import org.banking.account.dto.TransactionRequest;
import org.banking.account.entity.Account;
import org.banking.account.entity.Transaction;
import org.banking.account.exception.*;
import org.banking.account.repository.AccountRepository;
import org.banking.account.repository.TransactionRepository;
import org.banking.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountRequest accountRequest;
    private TransactionRequest transactionRequest;
    private Account account;
    private Account tradingAccount;

    @BeforeEach
    void setUp() {
        accountRequest = new AccountRequest();
        accountRequest.setType(Account.AccountType.SAVINGS);
        accountRequest.setInitialDeposit("1000.00");

        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("500.00"));
        transactionRequest.setDescription("Test transaction");
        transactionRequest.setReference("REF123");

        account = new Account();
        account.setId(1L);
        account.setUserId(100L);
        account.setAccountNumber("ACC12345678");
        account.setType(Account.AccountType.SAVINGS);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("1000.00"));

        tradingAccount = new Account();
        tradingAccount.setId(2L);
        tradingAccount.setUserId(100L);
        tradingAccount.setAccountNumber("ACC87654321");
        tradingAccount.setType(Account.AccountType.TRADING);
        tradingAccount.setStatus(Account.AccountStatus.ACTIVE);
        tradingAccount.setBalance(new BigDecimal("5000.00"));
    }

    // ========== CREATE ACCOUNT TESTS ==========

    @Test
    void createAccount_SuccessfulCreation_ReturnsAccountResponse() {
        // Arrange
        when(accountRepository.existsByUserIdAndType(100L, Account.AccountType.SAVINGS)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        AccountResponse response = accountService.createAccount(100L, accountRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ACC12345678", response.getAccountNumber());
        assertEquals(Account.AccountType.SAVINGS, response.getType());
        assertEquals(Account.AccountStatus.ACTIVE, response.getStatus());

        verify(accountRepository).existsByUserIdAndType(100L, Account.AccountType.SAVINGS);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_DuplicateAccountType_ThrowsDuplicateAccountException() {
        // Arrange
        when(accountRepository.existsByUserIdAndType(100L, Account.AccountType.SAVINGS)).thenReturn(true);

        // Act & Assert
        DuplicateAccountException exception = assertThrows(DuplicateAccountException.class,
                () -> accountService.createAccount(100L, accountRequest));

        assertEquals("User already has a SAVINGS account", exception.getMessage());
        verify(accountRepository).existsByUserIdAndType(100L, Account.AccountType.SAVINGS);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_WithInvalidInitialDeposit_IgnoresInvalidDeposit() {
        // Arrange
        accountRequest.setInitialDeposit("invalid");
        when(accountRepository.existsByUserIdAndType(100L, Account.AccountType.SAVINGS)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        AccountResponse response = accountService.createAccount(100L, accountRequest);

        // Assert
        assertNotNull(response);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_WithValidInitialDeposit_SetsBalance() {
        // Arrange
        accountRequest.setInitialDeposit("1500.50");
        when(accountRepository.existsByUserIdAndType(100L, Account.AccountType.SAVINGS)).thenReturn(false);

        Account accountWithBalance = new Account();
        accountWithBalance.setBalance(new BigDecimal("1500.50"));
        accountWithBalance.setAccountNumber("ACC12345678");
        accountWithBalance.setType(Account.AccountType.SAVINGS);
        accountWithBalance.setStatus(Account.AccountStatus.ACTIVE);

        when(accountRepository.save(any(Account.class))).thenReturn(accountWithBalance);

        // Act
        AccountResponse response = accountService.createAccount(100L, accountRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1500.50"), response.getBalance());
    }

    // ========== GET USER ACCOUNTS TESTS ==========

    @Test
    void getUserAccounts_UserHasAccounts_ReturnsAccountList() {
        // Arrange
        List<Account> accounts = List.of(account, tradingAccount);
        when(accountRepository.findByUserId(100L)).thenReturn(accounts);

        // Act
        List<AccountResponse> responses = accountService.getUserAccounts(100L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(accountRepository).findByUserId(100L);
    }

    @Test
    void getUserAccounts_UserHasNoAccounts_ReturnsEmptyList() {
        // Arrange
        when(accountRepository.findByUserId(100L)).thenReturn(List.of());

        // Act
        List<AccountResponse> responses = accountService.getUserAccounts(100L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(accountRepository).findByUserId(100L);
    }

    // ========== GET ACCOUNT BY NUMBER TESTS ==========

    @Test
    void getAccountByNumber_ValidAccountAndOwner_ReturnsAccountResponse() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));

        // Act
        AccountResponse response = accountService.getAccountByNumber("ACC12345678", 100L);

        // Assert
        assertNotNull(response);
        assertEquals("ACC12345678", response.getAccountNumber());
        verify(accountRepository).findByAccountNumber("ACC12345678");
    }

    @Test
    void getAccountByNumber_AccountNotFound_ThrowsAccountNotFoundException() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC99999999")).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountByNumber("ACC99999999", 100L));

        assertEquals("Account not found: ACC99999999", exception.getMessage());
        verify(accountRepository).findByAccountNumber("ACC99999999");
    }

    @Test
    void getAccountByNumber_UnauthorizedAccess_ThrowsUnauthorizedAccountAccessException() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));

        // Act & Assert
        UnauthorizedAccountAccessException exception = assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.getAccountByNumber("ACC12345678", 999L)); // Different user ID

        assertEquals("You are not the owner of this account", exception.getMessage());
        verify(accountRepository).findByAccountNumber("ACC12345678");
    }

    // ========== GET ACCOUNT BY ID TESTS ==========

    @Test
    void getAccountById_AccountExists_ReturnsAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        Account result = accountService.getAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_AccountNotFound_ThrowsAccountNotFoundException() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccountById(999L));

        assertEquals("Account not found with id: 999", exception.getMessage());
        verify(accountRepository).findById(999L);
    }

    // ========== GET TRADING ACCOUNT TESTS ==========

    @Test
    void getTradingAccountByUserId_ActiveTradingAccountExists_ReturnsAccount() {
        // Arrange
        List<Account> accounts = List.of(account, tradingAccount);
        when(accountRepository.findByUserId(100L)).thenReturn(accounts);

        // Act
        Account result = accountService.getTradingAccountByUserId(100L);

        // Assert
        assertNotNull(result);
        assertEquals(Account.AccountType.TRADING, result.getType());
        assertEquals(Account.AccountStatus.ACTIVE, result.getStatus());
        verify(accountRepository).findByUserId(100L);
    }

    @Test
    void getTradingAccountByUserId_NoTradingAccount_ThrowsAccountNotFoundException() {
        // Arrange
        when(accountRepository.findByUserId(100L)).thenReturn(List.of(account)); // Only savings account

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getTradingAccountByUserId(100L));

        assertEquals("No active trading account found for user: 100", exception.getMessage());
        verify(accountRepository).findByUserId(100L);
    }

    @Test
    void getTradingAccountByUserId_TradingAccountInactive_ThrowsAccountNotFoundException() {
        // Arrange
        tradingAccount.setStatus(Account.AccountStatus.INACTIVE);
        List<Account> accounts = List.of(account, tradingAccount);
        when(accountRepository.findByUserId(100L)).thenReturn(accounts);

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> accountService.getTradingAccountByUserId(100L));

        assertEquals("No active trading account found for user: 100", exception.getMessage());
    }

    // ========== TRANSFER TESTS ==========

    @Test
    void transfer_SuccessfulTransfer_ReturnsFromAccountResponse() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setUserId(100L);
        fromAccount.setAccountNumber("ACC11111111");
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setStatus(Account.AccountStatus.ACTIVE);

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setUserId(200L);
        toAccount.setAccountNumber("ACC22222222");
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setStatus(Account.AccountStatus.ACTIVE);

        transactionRequest.setFromAccountNumber("ACC11111111");
        transactionRequest.setToAccountNumber("ACC22222222");
        transactionRequest.setAmount(new BigDecimal("300.00"));

        when(accountRepository.findByAccountNumber("ACC11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC22222222")).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Act
        AccountResponse response = accountService.transfer(transactionRequest, 100L);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("700.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("800.00"), toAccount.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_InsufficientFunds_ThrowsInsufficientFundsException() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setUserId(100L);
        fromAccount.setBalance(new BigDecimal("100.00"));
        fromAccount.setStatus(Account.AccountStatus.ACTIVE);

        Account toAccount = new Account();
        toAccount.setStatus(Account.AccountStatus.ACTIVE);

        transactionRequest.setFromAccountNumber("ACC11111111");
        transactionRequest.setToAccountNumber("ACC22222222");
        transactionRequest.setAmount(new BigDecimal("300.00"));

        when(accountRepository.findByAccountNumber("ACC11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC22222222")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer(transactionRequest, 100L));

        assertEquals("Insufficient funds", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transfer_FromAccountInactive_ThrowsAccountInactiveException() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setUserId(100L);
        fromAccount.setStatus(Account.AccountStatus.INACTIVE);

        Account toAccount = new Account();
        toAccount.setStatus(Account.AccountStatus.ACTIVE);

        transactionRequest.setFromAccountNumber("ACC11111111");
        transactionRequest.setToAccountNumber("ACC22222222");

        when(accountRepository.findByAccountNumber("ACC11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC22222222")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        AccountInactiveException exception = assertThrows(AccountInactiveException.class,
                () -> accountService.transfer(transactionRequest, 100L));

        assertEquals("Cannot transfer from inactive account", exception.getMessage());
    }

    @Test
    void transfer_ToAccountInactive_ThrowsAccountInactiveException() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setUserId(100L);
        fromAccount.setStatus(Account.AccountStatus.ACTIVE);

        Account toAccount = new Account();
        toAccount.setStatus(Account.AccountStatus.INACTIVE);

        transactionRequest.setFromAccountNumber("ACC11111111");
        transactionRequest.setToAccountNumber("ACC22222222");

        when(accountRepository.findByAccountNumber("ACC11111111")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("ACC22222222")).thenReturn(Optional.of(toAccount));

        // Act & Assert
        AccountInactiveException exception = assertThrows(AccountInactiveException.class,
                () -> accountService.transfer(transactionRequest, 100L));

        assertEquals("Cannot deposit to inactive account", exception.getMessage());
    }

    // ========== WITHDRAW TESTS ==========

    @Test
    void withdraw_SuccessfulWithdraw_ReturnsAccountResponse() {
        // Arrange
        Account account = new Account();
        account.setId(1L);
        account.setUserId(100L);
        account.setAccountNumber("ACC12345678");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(Account.AccountStatus.ACTIVE);

        transactionRequest.setFromAccountNumber("ACC12345678");
        transactionRequest.setAmount(new BigDecimal("300.00"));

        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Act
        AccountResponse response = accountService.withdraw(transactionRequest, 100L);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("700.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void withdraw_UnauthorizedUser_ThrowsUnauthorizedAccountAccessException() {
        // Arrange
        transactionRequest.setFromAccountNumber("ACC12345678");
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));

        // Act & Assert
        UnauthorizedAccountAccessException exception = assertThrows(UnauthorizedAccountAccessException.class,
                () -> accountService.withdraw(transactionRequest, 999L)); // Different user ID

        assertEquals("You are not the owner of this account", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ========== ACCOUNT ACTIVATION/DEACTIVATION TESTS ==========

    @Test
    void deactivateAccount_SuccessfulDeactivation_UpdatesAccountStatus() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        accountService.deactivateAccount("ACC12345678", 100L);

        // Assert
        assertEquals(Account.AccountStatus.INACTIVE, account.getStatus());
        verify(accountRepository).save(account);
    }

    @Test
    void activateAccount_SuccessfulActivation_UpdatesAccountStatus() {
        // Arrange
        account.setStatus(Account.AccountStatus.INACTIVE);
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        accountService.activateAccount("ACC12345678", 100L);

        // Assert
        assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
        verify(accountRepository).save(account);
    }

    // ========== GET ACCOUNT TRANSACTIONS TESTS ==========

    @Test
    void getAccountTransactions_AccountHasTransactions_ReturnsTransactionList() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountId(1L);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(transaction));

        // Act
        List<Transaction> transactions = accountService.getAccountTransactions(1L);

        // Assert
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        verify(transactionRepository).findByAccountIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getAccountTransactions_NoTransactions_ReturnsEmptyList() {
        // Arrange
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        // Act
        List<Transaction> transactions = accountService.getAccountTransactions(1L);

        // Assert
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
        verify(transactionRepository).findByAccountIdOrderByCreatedAtDesc(1L);
    }
}