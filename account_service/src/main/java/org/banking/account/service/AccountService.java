package org.banking.account.service;

import org.banking.account.dto.AccountRequest;
import org.banking.account.dto.AccountResponse;
import org.banking.account.dto.TransactionRequest;
import org.banking.account.entity.Account;
import org.banking.account.entity.Transaction;
import org.banking.account.exception.*;
import org.banking.account.repository.AccountRepository;
import org.banking.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(Long userId, AccountRequest request) {

        if (accountRepository.existsByUserIdAndType(userId, request.getType())) {
            throw new DuplicateAccountException("User already has a " + request.getType() + " account");
        }

        Account account = new Account();
        account.setUserId(userId);
        account.setAccountNumber(generateAccountNumber());
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setType(request.getType());

        if (request.getInitialDeposit() != null && !request.getInitialDeposit().isEmpty()) {
            try {
                BigDecimal initialDeposit = new BigDecimal(request.getInitialDeposit());
                if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
                    account.setBalance(initialDeposit);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid initial deposit format: {}", request.getInitialDeposit());
            }
        }

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }

    public List<AccountResponse> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponse::fromEntity)
                .toList();
    }

    public AccountResponse getAccountByNumber(String accountNumber, Long userId) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("You are not the owner of this account");
        }

        return AccountResponse.fromEntity(account);
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    public Account getTradingAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .filter(a -> a.getType() == Account.AccountType.TRADING)
                .filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("No active trading account found for user: " + userId));
    }

    @Transactional
    public AccountResponse transfer(TransactionRequest request, Long userId) {

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getFromAccountNumber()));

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getToAccountNumber()));

        if (!fromAccount.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("You are not the owner of this account");
        }

        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Cannot deposit to inactive account");
        }

        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Cannot transfer from inactive account");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        BigDecimal fromBefore = fromAccount.getBalance();
        BigDecimal toBefore = toAccount.getBalance();

        fromAccount.setBalance(fromBefore.subtract(request.getAmount()));
        toAccount.setBalance(toBefore.add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        createTransaction(
                toAccount.getId(),
                Transaction.TransactionType.DEPOSIT,
                request.getAmount(),
                toBefore,
                toBefore.add(request.getAmount()),
                request.getDescription(),
                request.getReference()
        );

        return AccountResponse.fromEntity(fromAccount);
    }

    @Transactional
    public AccountResponse withdraw(TransactionRequest request, Long userId) {

        Account account = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getFromAccountNumber()));

        if (!account.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("You are not the owner of this account");
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Cannot withdraw from inactive account");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        BigDecimal before = account.getBalance();
        BigDecimal after = before.subtract(request.getAmount());

        account.setBalance(after);
        accountRepository.save(account);

        createTransaction(account.getId(), Transaction.TransactionType.WITHDRAWAL,
                request.getAmount(), before, after,
                request.getDescription(), request.getReference());

        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public void deactivateAccount(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("You are not the owner of this account");
        }

        account.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    @Transactional
    public void activateAccount(String accountNumber, Long userId) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new UnauthorizedAccountAccessException("You are not the owner of this account");
        }

        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("ACC%010d", random.nextLong(1_000_000_000L, 10_000_000_000L));
    }

    private void createTransaction(Long accountId, Transaction.TransactionType type,
                                   BigDecimal amount, BigDecimal before,
                                   BigDecimal after, String description, String reference) {

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(after);
        transaction.setDescription(description);
        transaction.setReference(reference);

        transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
