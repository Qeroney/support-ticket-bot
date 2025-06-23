package io.github.qeroney.config

import com.antkorwin.xsync.XSync
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class XSyncConfig {

    @Bean("chatIdSync")
    fun chatIdSync(): XSync<Long> = XSync()
}
