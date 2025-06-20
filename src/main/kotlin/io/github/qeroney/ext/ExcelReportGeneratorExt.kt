package io.github.qeroney.ext

import io.github.qeroney.model.Ticket
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun List<Ticket>.generateExcelReport(): ByteArray = XSSFWorkbook().use { workbook ->
    val sheet = workbook.createSheet("Заявки")

    val headerFont = workbook.createFont().apply { bold = true }
    val headerStyle = workbook.createCellStyle().apply {
        setFont(headerFont)
        alignment = HorizontalAlignment.CENTER
        fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        setAllBorders(BorderStyle.THIN)
    }

    val baseTextStyle = workbook.createCellStyle().apply {
        wrapText = true
        verticalAlignment = VerticalAlignment.TOP
        setAllBorders(BorderStyle.THIN)
    }

    val zebraTextStyle = workbook.createCellStyle().apply {
        cloneStyleFrom(baseTextStyle)
        fillForegroundColor = IndexedColors.LIGHT_TURQUOISE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
    }

    val dateStyle = workbook.createCellStyle().apply {
        cloneStyleFrom(baseTextStyle)
        dataFormat = workbook.creationHelper.createDataFormat().getFormat("dd.MM.yyyy HH:mm")
    }

    val numberStyle = workbook.createCellStyle().apply {
        cloneStyleFrom(baseTextStyle)
        dataFormat = workbook.creationHelper.createDataFormat().getFormat("0")
    }

    val headers = listOf("ID", "Дата создания", "ФИО", "Email", "Телефон", "Описание", "Кол-во вложений")
    sheet.createRow(0).apply {
        headers.forEachIndexed { i, title ->
            createCell(i).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }
    }

    this.forEachIndexed { rowIndex, ticket ->
        val row = sheet.createRow(rowIndex + 1)
        val textStyle = if (rowIndex % 2 == 0) baseTextStyle else zebraTextStyle

        row.createCell(0).apply {
            setCellValue(ticket.id!!.toDouble())
            cellStyle = numberStyle
        }
        row.createCell(1).apply {
            setCellValue(formatter.format(ticket.submittedAt!!))
            cellStyle = dateStyle
        }
        row.createCell(2).apply {
            setCellValue(ticket.owner.fullName!!)
            cellStyle = textStyle
        }
        row.createCell(3).apply {
            setCellValue(ticket.owner.email!!)
            cellStyle = textStyle
        }
        row.createCell(4).apply {
            setCellValue(ticket.owner.phone!!)
            cellStyle = textStyle
        }
        row.createCell(5).apply {
            setCellValue(ticket.description!!)
            cellStyle = textStyle
        }
        row.createCell(6).apply {
            setCellValue(ticket.attachmentCount!!.toDouble())
            cellStyle = numberStyle
        }
    }

    headers.indices.forEach(sheet::autoSizeColumn)

    return ByteArrayOutputStream().use { out ->
        workbook.write(out)
        out.toByteArray()
    }
}

private fun CellStyle.setAllBorders(style: BorderStyle) {
    borderTop = style
    borderBottom = style
    borderLeft = style
    borderRight = style
}