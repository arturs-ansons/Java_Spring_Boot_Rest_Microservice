package org.banking.account.repository;

import org.banking.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);
    boolean existsByUserIdAndType(Long userId, Account.AccountType type);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}