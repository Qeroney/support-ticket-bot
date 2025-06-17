package io.github.qeroney.service.notification

import io.github.dehuckakpyt.telegrambot.transaction.action.TransactionAction
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class SendNotificationService(
    private val transactional: TransactionAction,
    private val mailSender: JavaMailSender) {

    @Value("\${spring.mail.username}")
    private lateinit var fromEmail: String

    suspend fun sendVerificationCodeToEmail(email: String, code: String) = transactional {
        val msg = SimpleMailMessage().apply {
            setFrom(fromEmail)
            setTo(email)
            setSubject("Ваш код подтверждения")
            setText("Здравствуйте! Ваш код подтверждения: $code")
        }
        mailSender.send(msg)
    }
}