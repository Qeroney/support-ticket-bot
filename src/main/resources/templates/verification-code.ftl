<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { background: #f3f6fa; font-family: Arial, sans-serif; margin: 0; padding: 0; }
        .container { background: #fff; max-width: 400px; margin: 50px auto; padding: 32px 28px; border-radius: 12px; box-shadow: 0 2px 12px #dde3ee; text-align: center; }
        .title { font-size: 22px; color: #1957d2; margin-bottom: 18px; }
        .code { font-size: 32px; letter-spacing: 6px; color: #184bc2; background: #eaf1ff; border-radius: 8px; padding: 16px 0; font-weight: bold; margin-bottom: 16px; }
        .footer { font-size: 13px; color: #7a7f87; margin-top: 26px; }
    </style>
</head>
<body>
<div class="container">
    <div class="title">Ваш код подтверждения</div>
    <div class="code">${code?html}</div>
    <div>
        <p>Пожалуйста, введите этот код для подтверждения email.</p>
    </div>
    <div class="footer">
        Если вы не запрашивали код — просто проигнорируйте это письмо.
    </div>
</div>
</body>
</html>
