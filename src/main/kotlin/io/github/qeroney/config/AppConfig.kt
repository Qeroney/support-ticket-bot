package io.github.qeroney.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["io.github.qeroney.repository"])
@EntityScan("io.github.qeroney.model")
class AppConfig