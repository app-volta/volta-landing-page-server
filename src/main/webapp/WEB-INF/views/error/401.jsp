<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>401 - Volta</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="sem-nav">
<main class="container erro-pagina">
    <h1>Autenticação necessária</h1>
    <p>Sua sessão expirou ou você não está autenticado.</p>
    <a class="btn" href="${pageContext.request.contextPath}/login">Ir para o login</a>
</main>
</body>
</html>
