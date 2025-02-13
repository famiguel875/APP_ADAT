package com.es.aplicacion.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AppConfig {
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}