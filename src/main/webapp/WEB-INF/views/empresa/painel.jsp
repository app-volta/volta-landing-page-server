<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Empresa - VOLTA</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Alata&family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<main class="container painel-estatico painel-empresa">
    <%@ include file="/WEB-INF/jspf/flash.jspf" %>
    <section class="empresa-acessos" aria-label="Acessos da empresa">
        <h2>O que você deseja gerenciar?</h2>
        <div class="cards">
            <a class="card empresa-card" href="${pageContext.request.contextPath}/area?action=list">
                <span class="nome">Áreas</span>
                <span class="empresa-card-descricao">Organize os setores e unidades da empresa.</span>
                <span class="empresa-card-acao">Acessar áreas <span aria-hidden="true">→</span></span>
            </a>
            <a class="card empresa-card" href="${pageContext.request.contextPath}/usuario?action=list">
                <span class="nome">Funcionários</span>
                <span class="empresa-card-descricao">Mantenha os usuários da sua empresa atualizados.</span>
                <span class="empresa-card-acao">Acessar funcionários <span aria-hidden="true">→</span></span>
            </a>
            <a class="card empresa-card" href="${pageContext.request.contextPath}/ocorrencia?action=list">
                <span class="nome">Ocorrências</span>
                <span class="empresa-card-descricao">Registre e acompanhe as ocorrências da operação.</span>
                <span class="empresa-card-acao">Acessar ocorrências <span aria-hidden="true">→</span></span>
            </a>
            <a class="card empresa-card" href="${pageContext.request.contextPath}/coleta?action=list">
                <span class="nome">Coletas</span>
                <span class="empresa-card-descricao">Consulte e acompanhe as coletas relacionadas.</span>
                <span class="empresa-card-acao">Acessar coletas <span aria-hidden="true">→</span></span>
            </a>
        </div>
    </section>
</main>
</body>
</html>
