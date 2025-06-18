package io.github.qeroney.model

import io.hypersistence.utils.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
class Ticket (
    /** Уникальный внутренний идентификатор записи заявки */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    val id: Long? = null,

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