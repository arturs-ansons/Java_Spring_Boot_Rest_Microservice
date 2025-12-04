package org.banking;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

@SpringBootApplication
@EntityScan(basePackages = {
        "org.banking.account.entity",
        "org.banking.crypto.entity"
})
@EnableJpaRepositories(basePackages = {
        "org.banking.account.repository",
        "org.banking.crypto.repository"
})

@Slf4j
public class AccountServiceApplication  implements CommandLineRunner {



    private final DataSource dataSource;

    public AccountServiceApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Force Flyway migration
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        log.info("✅ Flyway migrations completed successfully");
    }


}