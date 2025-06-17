package io.github.qeroney.model

import javax.persistence.Embeddable

@Embeddable
data class Attachment(
    val fileId: String,

    val fileName: String?,

    val type: FileType,

    val fileSize: Long?
)