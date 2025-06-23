package io.github.qeroney.ext

import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.ext.downloadByPath
import io.ktor.client.statement.*
import java.nio.file.Files
import java.nio.file.Paths

suspend fun TelegramBot.save(fileId: String) {
    val info = getFile(fileId)
    val filePath = info.filePath ?: throw ChatException("Не удалось получить путь к файлу в Telegram")

    val bytes = downloadByPath(filePath).readBytes()

    val dir = Paths.get("files", "attachments")
    if (Files.notExists(dir)) Files.createDirectories(dir)
    val extension = filePath.substringAfterLast('.', "")
    val fileName = if (extension.isNotBlank()) "$fileId.$extension" else fileId

    Files.write(dir.resolve(fileName), bytes)
}