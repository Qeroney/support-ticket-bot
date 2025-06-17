package io.github.qeroney.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource

@Configuration
class PropertySourceConfig {

    @Bean
    fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {
        val configurer = PropertySourcesPlaceholderConfigurer()
        configurer.setLocations(ClassPathResource("messages.properties"))
        configurer.setFileEncoding("UTF-8")
        return configurer
    }
}