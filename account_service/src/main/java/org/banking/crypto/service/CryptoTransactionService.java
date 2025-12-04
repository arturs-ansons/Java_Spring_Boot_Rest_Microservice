package org.banking.crypto.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banking.account.entity.*;
import org.banking.account.repository.AccountRepository;
import org.banking.account.service.AccountService;
import org.banking.crypto.dto.CryptoTransactionDTO;
import org.banking.crypto.entity.CryptoAccount;
import org.banking.crypto.entity.CryptoTransaction;
import org.banking.crypto.repository.CryptoAccountRepository;
import org.banking.crypto.repository.CryptoTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoTransactionService {

    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final CryptoAccountRepository cryptoAccountRepository;
    private final AccountRepository accountRepository;


    public List<CryptoTransactionDTO> getTransactions(Long accountId) {
        return cryptoTransactionRepository.findByAccountId(accountId)
                .stream()
                .map(CryptoTransactionDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CryptoTransaction createBuyTransaction(Long accountId, String cryptoCurrency,
                                                  BigDecimal cryptoAmount, BigDecimal pricePerUnit,
                                                  BigDecimal totalCost) {

        Account account = (Account) accountRepository.findByUserId(accountId);

        // Validate sufficient balance
        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient fiat balance");
        }

        // Update fiat balance
        BigDecimal fiatBalanceBefore = account.getBalance();
        account.setBalance(fiatBalanceBefore.subtract(totalCost));

        // Get or create crypto account
        CryptoAccount cryptoAccount = cryptoAccountRepository
                .findByAccountIdAndCryptoCurrency(accountId, cryptoCurrency)
                .orElseGet(() -> {
                    CryptoAccount newCryptoAccount = new CryptoAccount();
                    newCryptoAccount.setAccount(account);
                    newCryptoAccount.setCryptoCurrency(cryptoCurrency);
                    return newCryptoAccount;
                });

        // Update crypto balance
        BigDecimal cryptoBalanceBefore = cryptoAccount.getBalance();
        cryptoAccount.setBalance(cryptoBalanceBefore.add(cryptoAmount));
        cryptoAccount.setAvailableBalance(cryptoAccount.getAvailableBalance().add(cryptoAmount));

        // Calculate average buy price
        if (cryptoBalanceBefore.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalValue = cryptoBalanceBefore.multiply(cryptoAccount.getAverageBuyPrice())
                    .add(cryptoAmount.multiply(pricePerUnit));
            BigDecimal totalCoins = cryptoBalanceBefore.add(cryptoAmount);
            cryptoAccount.setAverageBuyPrice(totalValue.divide(totalCoins, 18, RoundingMode.HALF_UP));
        } else {
            cryptoAccount.setAverageBuyPrice(pricePerUnit);
        }

        // Create transaction record
        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(CryptoTransaction.TransactionType.BUY);
        transaction.setCryptoCurrency(cryptoCurrency);
        transaction.setCryptoAmount(cryptoAmount);
        transaction.setFiatAmount(totalCost);
        transaction.setPricePerUnit(pricePerUnit);
        transaction.setCryptoBalanceBefore(cryptoBalanceBefore);
        transaction.setCryptoBalanceAfter(cryptoAccount.getBalance());
        transaction.setFiatBalanceBefore(fiatBalanceBefore);
        transaction.setFiatBalanceAfter(account.getBalance());
        transaction.setStatus(CryptoTransaction.TransactionStatus.COMPLETED);
        transaction.setDescription(String.format("Buy %s %s", cryptoAmount, cryptoCurrency));
        transaction.setConfirmedAt(LocalDateTime.now());

        // Save everything
        cryptoAccountRepository.save(cryptoAccount);
        return cryptoTransactionRepository.save(transaction);
    }

    @Transactional
    public CryptoTransaction createDepositTransaction(Long accountId, String cryptoCurrency,
                                                      BigDecimal cryptoAmount, String fromAddress,
                                                      String transactionHash, String network) {

        Account account = (Account) accountRepository.findByUserId(accountId);

        // Get or create crypto account
        CryptoAccount cryptoAccount = cryptoAccountRepository
                .findByAccountIdAndCryptoCurrency(accountId, cryptoCurrency)
                .orElseGet(() -> {
                    CryptoAccount newCryptoAccount = new CryptoAccount();
                    newCryptoAccount.setAccount(account);
                    newCryptoAccount.setCryptoCurrency(cryptoCurrency);
                    return newCryptoAccount;
                });

        // Update crypto balance
        BigDecimal cryptoBalanceBefore = cryptoAccount.getBalance();
        cryptoAccount.setBalance(cryptoBalanceBefore.add(cryptoAmount));
        cryptoAccount.setAvailableBalance(cryptoAccount.getAvailableBalance().add(cryptoAmount));

        // Create transaction record
        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(CryptoTransaction.TransactionType.DEPOSIT);
        transaction.setCryptoCurrency(cryptoCurrency);
        transaction.setCryptoAmount(cryptoAmount);
        transaction.setFromAddress(fromAddress);
        transaction.setToAddress(cryptoAccount.getWalletAddress());
        transaction.setTransactionHash(transactionHash);
        transaction.setNetwork(network);
        transaction.setCryptoBalanceBefore(cryptoBalanceBefore);
        transaction.setCryptoBalanceAfter(cryptoAccount.getBalance());
        transaction.setFiatBalanceBefore(account.getBalance());
        transaction.setFiatBalanceAfter(account.getBalance());
        transaction.setStatus(CryptoTransaction.TransactionStatus.PENDING);
        transaction.setDescription(String.format("Deposit %s %s", cryptoAmount, cryptoCurrency));

        cryptoAccountRepository.save(cryptoAccount);
        return cryptoTransactionRepository.save(transaction);
    }

    public void updateTransactionStatus(Long transactionId, CryptoTransaction.TransactionStatus status) {
        CryptoTransaction transaction = cryptoTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(status);
        if (status == CryptoTransaction.TransactionStatus.COMPLETED) {
            transaction.setConfirmedAt(LocalDateTime.now());
        }

        cryptoTransactionRepository.save(transaction);
    }
}