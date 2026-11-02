<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel da empresa - VOLTA</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Alata&family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<main class="container painel-estatico">
    <%@ include file="/WEB-INF/jspf/flash.jspf" %>
    <section>
        <h1 class="painel-titulo"><span>Minha empresa</span></h1>
        <div class="cards">
            <a class="card" href="${pageContext.request.contextPath}/area?action=list">
                <span class="nome">Unidades</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/usuario?action=list">
                <span class="nome">Funcionários</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/ocorrencia?action=list">
                <span class="nome">Ocorrências</span>
            </a>
            <a class="card" href="${pageContext.request.contextPath}/coleta?action=list">
                <span class="nome">Coletas</span>
            </a>
        </div>
    </section>
</main>
</body>
</html>
