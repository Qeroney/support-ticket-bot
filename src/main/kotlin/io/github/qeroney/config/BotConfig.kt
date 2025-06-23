package io.github.qeroney.config

import io.github.dehuckakpyt.telegrambot.annotation.EnableTelegramBot
import io.github.dehuckakpyt.telegrambot.config.TelegramBotConfig
import io.github.dehuckakpyt.telegrambot.ext.dynamicFreeMarker
import io.github.dehuckakpyt.telegrambot.template.Templater
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.config.properties.ReportProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableTelegramBot
@EnableConfigurationProperties(MessageTemplate::class, ReportProperties::class)
class BotConfig {

    @Bean
    fun telegramBotConfig(): TelegramBotConfig = TelegramBotConfig().apply {
        templater = { Templater.dynamicFreeMarker }
    }
}
