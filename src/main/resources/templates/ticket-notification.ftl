<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; background: #f9f9fb; margin: 0; padding: 0; }
        .container { background: #fff; max-width: 600px; margin: 30px auto; box-shadow: 0 2px 8px #e0e0e0; border-radius: 10px; padding: 32px; }
        .header { border-bottom: 2px solid #0077ff22; margin-bottom: 24px; }
        .header h2 { color: #1957d2; margin: 0; font-size: 26px; }
        .details { margin: 24px 0; }
        .detail-item { margin-bottom: 14px; }
        .detail-title { font-weight: bold; color: #333; }
        .description { background: #f2f7ff; border-left: 4px solid #1957d2; padding: 12px; border-radius: 6px; margin-top: 6px; }
        .footer { color: #7a7f87; font-size: 13px; margin-top: 40px; }
        .attachments { color: #222; margin-top: 12px; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>Инцидент &laquo;${fio?html}&raquo;</h2>
    </div>
    <div>
        <p>Добрый день!</p>
        <p>Пользователь отправил новую заявку через Telegram-бот.</p>
    </div>
    <div class="details">
        <div class="detail-item">
            <span class="detail-title">ФИО:</span> ${fio?html}
        </div>
        <div class="detail-item">
            <span class="detail-title">Email:</span> ${email?html}
        </div>
        <div class="detail-item">
            <span class="detail-title">Телефон:</span> ${phone?html}
        </div>
        <div class="detail-item">
            <span class="detail-title">Дата и время создания:</span> ${createdAt?html}
        </div>
        <div class="detail-item">
            <span class="detail-title">Краткое описание проблемы:</span>
            <div class="description">${description?html}</div>
        </div>
        <div class="detail-item attachments">
            <span class="detail-title">Кол-во вложений:</span> ${attachmentsCount}
            <#if attachmentsCount?number gt 0>
            <em>(Вложения прикреплены к письму)</em>
            <#else>
            <em>(Без вложений)</em>
        </#if>
    </div>
</div>
<div class="footer">
    <p>Просим зарегистрировать инцидент и уведомить пользователя о статусе заявки, если это предусмотрено.</p>
    <p>---</p>
    <p>С уважением,<br>Telegram-бот технической поддержки</p>
</div>
</div>
</body>
</html>
