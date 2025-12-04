package org.banking.crypto.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banking.account.dto.AccountResponse;
import org.banking.account.entity.Account;
import org.banking.account.service.AccountService;
import org.banking.crypto.dto.CryptoPortfolioDTO;
import org.banking.crypto.entity.CryptoAccount;
import org.banking.crypto.entity.CryptoTransaction;
import org.banking.crypto.exception.*;
import org.banking.crypto.repository.CryptoAccountRepository;
import org.banking.crypto.repository.CryptoTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoTradingService {

    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final CryptoAccountRepository cryptoAccountRepository;
    private final AccountService accountService;
    private final CoinGeckoService coinGeckoService;

    @Transactional
    public CryptoTransaction executeBuyOrder(Long accountId, String cryptoCurrency,
                                             BigDecimal fiatAmount, String fiatCurrency) {

        log.info("Executing BUY order: account={}, crypto={}, amount={} {}",
                accountId, cryptoCurrency, fiatAmount, fiatCurrency);

        Account account = accountService.getAccountById(accountId);
        validateAccountForTrading(AccountResponse.fromEntity(account));

        BigDecimal currentPrice = getCurrentCryptoPrice(cryptoCurrency, fiatCurrency);
        BigDecimal networkFee = calculateNetworkFee(cryptoCurrency, "BUY");

        BigDecimal totalCost = fiatAmount.add(networkFee);
        BigDecimal cryptoAmount = fiatAmount.divide(currentPrice, 18, RoundingMode.HALF_UP);

        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientFiatBalanceException(
                    String.format("Insufficient fiat balance. Available: %s %s, Required: %s %s",
                            account.getBalance(), fiatCurrency, totalCost, fiatCurrency));
        }

        BigDecimal fiatBalanceBefore = account.getBalance();
        account.setBalance(fiatBalanceBefore.subtract(totalCost));

        CryptoAccount cryptoAccount = getOrCreateCryptoAccount(account, cryptoCurrency);

        BigDecimal cryptoBalanceBefore = cryptoAccount.getBalance();
        cryptoAccount.setBalance(cryptoBalanceBefore.add(cryptoAmount));
        cryptoAccount.setAvailableBalance(cryptoAccount.getAvailableBalance().add(cryptoAmount));

        updateCryptoAccountStatistics(cryptoAccount, cryptoAmount, currentPrice, fiatAmount);

        CryptoTransaction transaction = buildBuyTransaction(account, cryptoCurrency, cryptoAmount,
                currentPrice, fiatAmount, networkFee,
                cryptoBalanceBefore, cryptoAccount.getBalance(),
                fiatBalanceBefore, account.getBalance());


        CryptoTransaction savedTransaction = cryptoTransactionRepository.save(transaction);

        log.info("BUY order completed: {} {} bought for {} {}",
                cryptoAmount, cryptoCurrency, fiatAmount, fiatCurrency);

        return savedTransaction;
    }

    @Transactional
    public CryptoTransaction executeSellOrder(Long accountId, String cryptoCurrency,
                                              BigDecimal cryptoAmount, String fiatCurrency) {

        log.info("Executing SELL order: account={}, crypto={}, amount={}",
                accountId, cryptoCurrency, cryptoAmount);

        Account account = accountService.getAccountById(accountId);
        validateAccountForTrading(AccountResponse.fromEntity(account));

        BigDecimal currentPrice = getCurrentCryptoPrice(cryptoCurrency, fiatCurrency);
        BigDecimal networkFee = calculateNetworkFee(cryptoCurrency, "SELL");

        CryptoAccount cryptoAccount = cryptoAccountRepository
                .findByAccountIdAndCryptoCurrency(accountId, cryptoCurrency)
                .orElseThrow(() -> new CryptoNotFoundException(
                        String.format("No %s balance found", cryptoCurrency)));

        if (cryptoAccount.getAvailableBalance().compareTo(cryptoAmount) < 0) {
            throw new InsufficientCryptoBalanceException(
                    String.format("Insufficient %s balance. Available: %s, Requested: %s",
                            cryptoCurrency, cryptoAccount.getAvailableBalance(), cryptoAmount));
        }

        BigDecimal cryptoBalanceBefore = cryptoAccount.getBalance();
        cryptoAccount.setBalance(cryptoBalanceBefore.subtract(cryptoAmount));
        cryptoAccount.setAvailableBalance(cryptoAccount.getAvailableBalance().subtract(cryptoAmount));

        BigDecimal fiatBalanceBefore = account.getBalance();
        BigDecimal grossProceeds = cryptoAmount.multiply(currentPrice);
        BigDecimal netProceeds = grossProceeds.subtract(networkFee);
        account.setBalance(fiatBalanceBefore.add(netProceeds));

        BigDecimal costBasis = calculateCostBasis(cryptoAccount, cryptoAmount);
        BigDecimal profitLoss = netProceeds.subtract(costBasis);

        CryptoTransaction transaction = buildSellTransaction(
                account,
                cryptoCurrency,
                cryptoAmount,
                currentPrice,
                netProceeds,
                networkFee,
                cryptoBalanceBefore,
                cryptoAccount.getBalance(),
                fiatBalanceBefore,
                account.getBalance(),
                costBasis,
                profitLoss
        );

        cryptoAccountRepository.save(cryptoAccount);
        CryptoTransaction savedTransaction = cryptoTransactionRepository.save(transaction);

        log.info("SELL order completed: {} {} sold for {} {} (P/L: {})",
                cryptoAmount, cryptoCurrency, netProceeds, fiatCurrency, profitLoss);

        return savedTransaction;
    }

    private void validateAccountForTrading(AccountResponse account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new TradingNotAllowedException("Account is not active for trading");
        }

        if (!Boolean.TRUE.equals(account.getCryptoEnabled())) {
            throw new TradingNotAllowedException("Crypto trading is not enabled for this account");
        }
    }

    public BigDecimal getCurrentCryptoPrice(String cryptoCurrency, String fiatCurrency) {
        try {
            String coinGeckoId = convertToCoinGeckoId(cryptoCurrency);
            return coinGeckoService.getSinglePrice(coinGeckoId, fiatCurrency.toLowerCase());
        } catch (Exception e) {
            log.error("Failed to fetch price for {}: {}", cryptoCurrency, e.getMessage());
            throw new CryptoPriceException("Unable to fetch current price for " + cryptoCurrency);
        }
    }

    public Map<String, BigDecimal> getMultipleCryptoPrices(List<String> cryptoCurrencies, String fiatCurrency) {
        Map<String, BigDecimal> prices = new HashMap<>();
        for (String crypto : cryptoCurrencies) {
            try {
                String coinGeckoId = convertToCoinGeckoId(crypto);
                BigDecimal price = coinGeckoService.getSinglePrice(coinGeckoId, fiatCurrency.toLowerCase());
                prices.put(crypto, price);
            } catch (Exception e) {
                log.error("Failed to fetch price for {}: {}", crypto, e.getMessage());
                prices.put(crypto, BigDecimal.ZERO);
            }
        }
        return prices;
    }

    private String convertToCoinGeckoId(String symbol) {
        Map<String, String> mapping = Map.of(
                "BTC", "bitcoin",
                "ETH", "ethereum",
                "ADA", "cardano",
                "USDT", "tether"
        );
        return mapping.getOrDefault(symbol.toUpperCase(), symbol.toLowerCase());
    }

    private BigDecimal calculateNetworkFee(String cryptoCurrency, String transactionType) {
        return BigDecimal.valueOf(0.50);
    }

    public List<CryptoPortfolioDTO> getCryptoAccounts(Long accountId) {
        return cryptoAccountRepository.findByAccountId(accountId).stream()
                .map(CryptoPortfolioDTO::new)
                .collect(Collectors.toList());
    }

    public CryptoAccount getOrCreateCryptoAccount(Account account, String cryptoCurrency) {
        return cryptoAccountRepository
                .findByAccountIdAndCryptoCurrency(account.getId(), cryptoCurrency)
                .orElseGet(() -> {
                    CryptoAccount newAccount = new CryptoAccount();
                    newAccount.setAccount(account);
                    newAccount.setCryptoCurrency(cryptoCurrency);
                    newAccount.setWalletAddress(generateWalletAddress(cryptoCurrency));
                    return     cryptoAccountRepository.save(newAccount);
                });
    }

    private void updateCryptoAccountStatistics(CryptoAccount cryptoAccount, BigDecimal cryptoAmount,
                                               BigDecimal pricePerUnit, BigDecimal fiatAmount) {

        BigDecimal currentBalance = cryptoAccount.getBalance();
        BigDecimal currentAverage = cryptoAccount.getAverageBuyPrice();
        BigDecimal currentTotalInvested = cryptoAccount.getTotalInvested() != null ?
                cryptoAccount.getTotalInvested() : BigDecimal.ZERO;

        if (currentAverage != null && currentBalance.compareTo(cryptoAmount) > 0) {
            BigDecimal existingValue = currentBalance.subtract(cryptoAmount).multiply(currentAverage);
            BigDecimal newValue = cryptoAmount.multiply(pricePerUnit);
            BigDecimal totalValue = existingValue.add(newValue);
            cryptoAccount.setAverageBuyPrice(totalValue.divide(currentBalance, 18, RoundingMode.HALF_UP));
        } else {
            cryptoAccount.setAverageBuyPrice(pricePerUnit);
        }

        cryptoAccount.setTotalInvested(currentTotalInvested.add(fiatAmount));
    }

    private BigDecimal calculateCostBasis(CryptoAccount cryptoAccount, BigDecimal cryptoAmount) {
        if (cryptoAccount.getAverageBuyPrice() == null) {
            return BigDecimal.ZERO;
        }
        return cryptoAmount.multiply(cryptoAccount.getAverageBuyPrice());
    }

    private CryptoTransaction buildBuyTransaction(Account account, String cryptoCurrency,
                                                  BigDecimal cryptoAmount, BigDecimal pricePerUnit,
                                                  BigDecimal fiatAmount, BigDecimal networkFee,
                                                  BigDecimal cryptoBalanceBefore, BigDecimal cryptoBalanceAfter,
                                                  BigDecimal fiatBalanceBefore, BigDecimal fiatBalanceAfter) {

        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(CryptoTransaction.TransactionType.BUY);
        transaction.setCryptoCurrency(cryptoCurrency);
        transaction.setCryptoAmount(cryptoAmount);
        transaction.setFiatAmount(fiatAmount);
        transaction.setPricePerUnit(pricePerUnit);
        transaction.setNetworkFee(networkFee);
        transaction.setNetworkFeeFiat(networkFee);
        transaction.setCryptoBalanceBefore(cryptoBalanceBefore);
        transaction.setCryptoBalanceAfter(cryptoBalanceAfter);
        transaction.setFiatBalanceBefore(fiatBalanceBefore);
        transaction.setFiatBalanceAfter(fiatBalanceAfter);
        transaction.setStatus(CryptoTransaction.TransactionStatus.COMPLETED);
        transaction.setDescription(String.format("Buy %s %s @ %s %s",
                cryptoAmount, cryptoCurrency,
                pricePerUnit, "USD"));
        transaction.setConfirmedAt(LocalDateTime.now());

        return transaction;
    }

    private CryptoTransaction buildSellTransaction(Account account, String cryptoCurrency,
                                                   BigDecimal cryptoAmount, BigDecimal pricePerUnit,
                                                   BigDecimal netProceeds, BigDecimal networkFee,
                                                   BigDecimal cryptoBalanceBefore, BigDecimal cryptoBalanceAfter,
                                                   BigDecimal fiatBalanceBefore, BigDecimal fiatBalanceAfter,
                                                   BigDecimal costBasis, BigDecimal profitLoss) {

        CryptoTransaction transaction = new CryptoTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(CryptoTransaction.TransactionType.SELL);
        transaction.setCryptoCurrency(cryptoCurrency);
        transaction.setCryptoAmount(cryptoAmount);
        transaction.setFiatAmount(netProceeds);
        transaction.setPricePerUnit(pricePerUnit);
        transaction.setNetworkFee(networkFee);
        transaction.setNetworkFeeFiat(networkFee);
        transaction.setCryptoBalanceBefore(cryptoBalanceBefore);
        transaction.setCryptoBalanceAfter(cryptoBalanceAfter);
        transaction.setFiatBalanceBefore(fiatBalanceBefore);
        transaction.setFiatBalanceAfter(fiatBalanceAfter);
        transaction.setStatus(CryptoTransaction.TransactionStatus.COMPLETED);

        String description = String.format("Sell %s %s @ %s %s",
                cryptoAmount, cryptoCurrency,
                pricePerUnit, "USD");
        if (profitLoss.compareTo(BigDecimal.ZERO) >= 0) {
            description += String.format(" (Profit: %s)", profitLoss);
        } else {
            description += String.format(" (Loss: %s)", profitLoss.abs());
        }

        transaction.setDescription(description);
        transaction.setConfirmedAt(LocalDateTime.now());

        return transaction;
    }

    private String generateWalletAddress(String cryptoCurrency) {
        return String.format("%s_wallet_%s", cryptoCurrency.toLowerCase(), System.currentTimeMillis());
    }

}
