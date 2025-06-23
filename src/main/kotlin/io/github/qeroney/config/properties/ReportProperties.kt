package io.github.qeroney.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "report")
class ReportProperties {
    var chatIds: Set<Long> = setOf()
}