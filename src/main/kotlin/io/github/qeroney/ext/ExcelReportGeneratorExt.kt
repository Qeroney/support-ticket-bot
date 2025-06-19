package io.github.qeroney.ext

import io.github.qeroney.model.Ticket
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

fun List<Ticket>.generateExcelReport(): ByteArray =
    XSSFWorkbook().use { wb ->
        val sheet = wb.createSheet("Заявки")
        val headers = listOf("ID", "Дата создания", "ФИО", "Email", "Телефон", "Описание", "Кол-во вложений")

        sheet.createRow(0).apply {
            headers.forEachIndexed { i, h -> createCell(i).setCellValue(h) }
        }

        this.forEachIndexed { i, t ->
            sheet.createRow(i + 1).apply {
                createCell(0).setCellValue(t.id?.toDouble() ?: 0.0)
                createCell(1).setCellValue(t.submittedAt?.toString() ?: "")
                createCell(2).setCellValue(t.owner.fullName.orEmpty())
                createCell(3).setCellValue(t.owner.email.orEmpty())
                createCell(4).setCellValue(t.owner.phone.orEmpty())
                createCell(5).setCellValue(t.description.orEmpty())
                createCell(6).setCellValue(t.attachmentCount?.toDouble() ?: 0.0)
            }
        }

        ByteArrayOutputStream().use { out ->
            wb.write(out)
            out.toByteArray()
        }
    }
