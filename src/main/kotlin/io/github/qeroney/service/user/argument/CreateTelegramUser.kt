package io.github.qeroney.service.user.argument

data class CreateTelegramUser(
    val chatId: Long,
    val email: String? = null,
    val phone: String? = null,
    val fullName: String? = null,
)
