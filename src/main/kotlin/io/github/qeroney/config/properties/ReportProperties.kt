package io.github.qeroney.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "report")
class ReportProperties {
    var adminIds: Set<Long> = setOf()
}