package com.banking.client.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
    public Queue clientRegistrationQueue() {
        return new Queue("user.registration.client.queue", true);
    }

    @Bean
    public Binding clientBinding(FanoutExchange userRegistrationExchange,
                                 Queue clientRegistrationQueue) {
        return BindingBuilder.bind(clientRegistrationQueue).to(userRegistrationExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
