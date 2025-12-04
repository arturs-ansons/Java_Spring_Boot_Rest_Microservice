package org.banking.crypto.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.banking.account.entity.Account;
import org.banking.account.service.AccountService;
import org.banking.crypto.dto.CryptoPortfolioDTO;
import org.banking.crypto.dto.CryptoRequest;
import org.banking.crypto.dto.CryptoTransactionDTO;
import org.banking.crypto.dto.CryptoTransactionResponse;
import org.banking.crypto.entity.CryptoTransaction;
import org.banking.crypto.entity.OrderSide;
import org.banking.crypto.service.CryptoTradingService;
import org.banking.crypto.service.CryptoTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account/crypto/trading")
@RequiredArgsConstructor
public class CryptoTradingController {

    private final CryptoTradingService cryptoTradingService;
    private final AccountService accountService;
    private final CryptoTransactionService cryptoTransactionService;



    @PostMapping("/buy")
    public ResponseEntity<CryptoTransactionResponse> buyCrypto(
            HttpServletRequest httpRequest,
            @RequestBody CryptoRequest request) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        Account account = accountService.getTradingAccountByUserId(userId);

        CryptoTransaction transaction = cryptoTradingService.executeBuyOrder(
                account.getId(), request.getCryptoCurrency(), request.getFiatAmount(), request.getFiatCurrency());

        return ResponseEntity.ok(CryptoTransactionResponse.toResponse(transaction));
    }
    @GetMapping("/transactions")
    public List<CryptoTransactionDTO> transactions(HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        Account account = accountService.getTradingAccountByUserId(userId);

        return cryptoTransactionService.getTransactions(account.getId());
    }
    @GetMapping("/portfolio")
    public List<CryptoPortfolioDTO> getPortfolio(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        Account account = accountService.getTradingAccountByUserId(userId);

        return cryptoTradingService.getCryptoAccounts(account.getId());

    }

    @GetMapping("/price")
    public BigDecimal getCoinGeckoService() {
        return cryptoTradingService.getCurrentCryptoPrice("btc","usd");
    }

    @GetMapping("/prices")
    public Map<String, BigDecimal> getMultipleCryptoPrices(
            @RequestParam List<String> cryptoIds,
            @RequestParam(defaultValue = "usd") String currency) {
        return cryptoTradingService.getMultipleCryptoPrices(cryptoIds, currency);
    }

    @PostMapping("/sell")
    public ResponseEntity<CryptoTransactionResponse> sellCrypto(
            HttpServletRequest httpRequest,
            @RequestBody CryptoRequest request) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        Account account = accountService.getTradingAccountByUserId(userId);

        CryptoTransaction transaction = cryptoTradingService.executeSellOrder(
                account.getId(), request.getCryptoCurrency(), request.getCryptoAmount(), request.getFiatCurrency());

        return ResponseEntity.ok(CryptoTransactionResponse.toResponse(transaction));
    }

}