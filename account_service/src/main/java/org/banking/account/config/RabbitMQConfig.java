package org.banking.account.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public FanoutExchange userRegistrationExchange() {
        return new FanoutExchange("user.registration.exchange");
    }

    @Bean
    public Queue accountRegistrationQueue() {
        return new Queue("user.registration.account.queue", true);
    }

    // Bind account queue to the exchange
    @Bean
    public Binding accountBinding(FanoutExchange userRegistrationExchange,
                                  Queue accountRegistrationQueue) {
        return BindingBuilder.bind(accountRegistrationQueue)
                .to(userRegistrationExchange);
    }
}