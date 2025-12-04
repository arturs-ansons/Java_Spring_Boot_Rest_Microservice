package com.banking.client.repository;

import com.banking.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUserId(Long userId);
    Optional<Client> findByIdentificationNumber(String identificationNumber);
    Optional<Client> findByPhoneNumber(String phoneNumber);
    boolean existsByUserId(Long userId);
    boolean existsByIdentificationNumber(String identificationNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}