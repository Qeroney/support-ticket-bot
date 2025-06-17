package io.github.qeroney.model

import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Ticket (
    /** Уникальный внутренний идентификатор записи заявки */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    /** Дата и время подачи заявки */
    var submittedAt: LocalDateTime?,

    /** Количество файлов, приложенных к заявке */
    var attachmentCount: Int? = 0,

    /** Вложения */
    @Type(type = "json")
    @Column(columnDefinition = "jsonb not null default '[]'")
    var files: List<Attachment>?,

    /** Краткое описание проблемы */
    var description: String?,

    /** Привязка к пользователю */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: TelegramUser
)