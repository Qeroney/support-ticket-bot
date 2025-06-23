package io.github.qeroney.model

import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "telegram_user_bot")
class TelegramUser(
    /** Уникальный внутренний идентификатор */
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    /** Id пользователя в telegram */
    @Column(nullable = false, unique = true)
    val chatId: Long,

    /** Email пользователя */
    var email: String?,

    /** Номер в формате 79998887766 */
    var phone: String?,

    /** ФИО пользователя */
    var fullName: String?,

    /** Заявки */
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tickets: MutableList<Ticket> = mutableListOf()
)