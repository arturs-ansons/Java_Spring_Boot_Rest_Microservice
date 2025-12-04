import org.banking.AccountServiceApplication;
import org.banking.account.entity.Account;
import org.banking.account.repository.AccountRepository;
import org.banking.crypto.entity.CryptoAccount;
import org.banking.crypto.repository.CryptoAccountRepository;
import org.banking.crypto.service.CryptoTradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AccountServiceApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.health.rabbit.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
public class CryptoAccountConcurrencyTest {

    @Autowired
    private CryptoTradingService cryptoTradingService;

    @Autowired
    private CryptoAccountRepository cryptoAccountRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Mock
    private com.netflix.discovery.EurekaClient eurekaClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private Account account;

    @BeforeEach
    void setup() {
        // Clean up first
        cryptoAccountRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test account with all required fields
        account = new Account();
        account.setUserId(5L);
        account.setBalance(BigDecimal.valueOf(1000.0));
        account.setAccountNumber("ACC-0005");
        account.setType(Account.AccountType.TRADING);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account); // Save and reassign to get the ID

        System.out.println("Created account with ID: " + account.getId());
    }

    @Test
    void testSimpleCryptoAccountCreation() {
        System.out.println("=== Testing simple creation ===");
        System.out.println("Account ID: " + account.getId());

        // Check if crypto accounts exist before
        List<CryptoAccount> before = cryptoAccountRepository.findByAccountId(account.getId());
        System.out.println("Crypto accounts before: " + before.size());

        // Create crypto account
        CryptoAccount result = cryptoTradingService.getOrCreateCryptoAccount(account, "BTC");
        System.out.println("Result: " + (result != null ? result.getId() : "null"));

        // Check if crypto accounts exist after
        List<CryptoAccount> after = cryptoAccountRepository.findByAccountId(account.getId());
        System.out.println("Crypto accounts after: " + after.size());
        after.forEach(acc -> System.out.println("Crypto Account ID: " + acc.getId() + ", Currency: " + acc.getCryptoCurrency()));

        assertThat(after)
                .hasSize(1)
                .withFailMessage("Expected exactly 1 CryptoAccount, but found " + after.size());
    }

    @Test
    void testConcurrentCryptoAccountCreation() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<CryptoAccount>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        System.out.println("=== Starting concurrent test with " + threadCount + " threads ===");
        System.out.println("Account ID: " + account.getId());

        // Submit all tasks
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // Wait for all threads to be ready
                    System.out.println("Thread " + threadId + " started execution");
                    CryptoAccount result = cryptoTradingService.getOrCreateCryptoAccount(account, "BTC");
                    successCount.incrementAndGet();
                    System.out.println("Thread " + threadId + " completed successfully, got account ID: " +
                            (result != null ? result.getId() : "null"));
                    return result;
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }));
        }

        // Release all threads simultaneously
        System.out.println("=== Releasing all threads simultaneously ===");
        latch.countDown();

        // Wait for all tasks to complete
        for (Future<CryptoAccount> future : futures) {
            try {
                CryptoAccount result = future.get(10, TimeUnit.SECONDS);
                if (result != null) {
                    System.out.println("Future completed with account ID: " + result.getId());
                }
            } catch (TimeoutException e) {
                System.err.println("Future timed out: " + e.getMessage());
            } catch (ExecutionException e) {
                System.err.println("Future execution error: " + e.getMessage());
            }
        }

        // Shutdown executor
        executor.shutdown();
        boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("Executor did not terminate in time, forcing shutdown");
            executor.shutdownNow();
        }

        // Verify results
        List<CryptoAccount> accounts = cryptoAccountRepository.findByAccountId(account.getId());

        System.out.println("=== Test Results ===");
        System.out.println("Success count: " + successCount.get());
        System.out.println("Exception count: " + exceptionCount.get());
        System.out.println("Final crypto accounts count: " + accounts.size());
        System.out.println("Account IDs created: " +
                accounts.stream().map(CryptoAccount::getId).toList());

        // The main assertion: Only ONE crypto account should exist
        assertThat(accounts)
                .hasSize(1)
                .withFailMessage("❌ CONCURRENCY BUG: Expected exactly 1 CryptoAccount, but found " +
                        accounts.size() + ". Multiple accounts were created concurrently!");

        // Additional verification: All threads should have gotten the same account instance
        List<Long> uniqueAccountIds = futures.stream()
                .map(future -> {
                    try {
                        CryptoAccount result = future.get();
                        return result != null ? result.getId() : null;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .distinct()
                .toList();

        System.out.println("Unique account IDs returned to threads: " + uniqueAccountIds);

        assertThat(uniqueAccountIds)
                .hasSize(1)
                .withFailMessage("❌ CONCURRENCY BUG: Threads received different account instances: " + uniqueAccountIds);

        System.out.println("✅ SUCCESS: Concurrency test passed! Only 1 CryptoAccount was created despite " +
                threadCount + " concurrent requests");
    }

    @Test
    void testMultipleCryptocurrenciesCanBeCreated() {
        System.out.println("=== Testing multiple cryptocurrencies for same account ===");

        // Create BTC account
        CryptoAccount btcAccount = cryptoTradingService.getOrCreateCryptoAccount(account, "BTC");
        System.out.println("Created BTC account: " + btcAccount.getId());

        // Create ETH account
        CryptoAccount ethAccount = cryptoTradingService.getOrCreateCryptoAccount(account, "ETH");
        System.out.println("Created ETH account: " + ethAccount.getId());

        // Create ADA account
        CryptoAccount adaAccount = cryptoTradingService.getOrCreateCryptoAccount(account, "ADA");
        System.out.println("Created ADA account: " + adaAccount.getId());

        // Verify all three accounts exist
        List<CryptoAccount> allAccounts = cryptoAccountRepository.findByAccountId(account.getId());
        System.out.println("Total crypto accounts: " + allAccounts.size());
        allAccounts.forEach(acc -> System.out.println("Account: " + acc.getId() + ", Currency: " + acc.getCryptoCurrency()));

        assertThat(allAccounts)
                .hasSize(3)
                .withFailMessage("Expected 3 crypto accounts for different currencies, but found " + allAccounts.size());

        // Verify each currency exists exactly once
        long btcCount = allAccounts.stream().filter(acc -> "BTC".equals(acc.getCryptoCurrency())).count();
        long ethCount = allAccounts.stream().filter(acc -> "ETH".equals(acc.getCryptoCurrency())).count();
        long adaCount = allAccounts.stream().filter(acc -> "ADA".equals(acc.getCryptoCurrency())).count();

        assertThat(btcCount).isEqualTo(1);
        assertThat(ethCount).isEqualTo(1);
        assertThat(adaCount).isEqualTo(1);

        System.out.println("✅ SUCCESS: Multiple cryptocurrencies can be created for the same account");
    }
}