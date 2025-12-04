package org.banking.account.controller;

import org.banking.account.dto.AccountRequest;
import org.banking.account.dto.AccountResponse;
import org.banking.account.dto.TransactionRequest;
import org.banking.account.entity.Transaction;
import org.banking.account.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create-account")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        AccountResponse response = accountService.createAccount(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<AccountResponse> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getMyAccounts(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AccountResponse account = accountService.getAccountByNumber(accountNumber,userId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<AccountResponse> transfer(
            @Valid @RequestBody TransactionRequest request, HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        AccountResponse account = accountService.transfer(request, userId);
        return ResponseEntity.ok(account);
    }
    @PostMapping("/{accountNumber}/activateAccount")
    public ResponseEntity<Void> activateAccount(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        accountService.activateAccount(accountNumber, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @Valid @RequestBody TransactionRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AccountResponse account = accountService.withdraw(request, userId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable String accountNumber, Long userId) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber, userId);
        List<Transaction> transactions = accountService.getAccountTransactions(account.getId());
        return ResponseEntity.ok(transactions);
    }

    @DeleteMapping("/{accountNumber}/deactivateAccount")
    public ResponseEntity<Void> deactivateAccount(@PathVariable String accountNumber, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        accountService.deactivateAccount(accountNumber, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Account Service is healthy");
    }
}