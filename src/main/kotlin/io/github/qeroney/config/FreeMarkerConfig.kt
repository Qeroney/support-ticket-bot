package io.github.qeroney.config

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration as SpringConfig

@SpringConfig
class FreeMarkerConfig {
    @Bean
    fun freemarkerConfiguration(): Configuration =
        Configuration(Configuration.VERSION_2_3_32).apply {
            setClassLoaderForTemplateLoading(javaClass.classLoader, "templates")
            defaultEncoding = "UTF-8"
            templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        }
}
