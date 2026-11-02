<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel do sistema - Volta</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
</head>
<body class="painel-admin">
<%@ include file="/WEB-INF/jspf/nav.jspf" %>

<main class="painel">
    <%@ include file="/WEB-INF/jspf/flash.jspf" %>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <section class="painel-secao">
        <h2 class="painel-titulo"><span>Operação</span></h2>
        <div class="grade">
            <c:forEach var="section" items="${operationSections}">
                <a class="card" href="${pageContext.request.contextPath}${section.path}">
                    <span class="nome"><c:out value="${section.label}"/></span>
                    <span class="num"><c:out value="${sectionCounts[section.key]}"/><span>Cadastradas</span></span>
                </a>
            </c:forEach>
        </div>
    </section>

    <section class="painel-secao">
        <h2 class="painel-titulo"><span>Acesso</span></h2>
        <div class="grade">
            <c:forEach var="section" items="${accessSections}">
                <a class="card" href="${pageContext.request.contextPath}${section.path}">
                    <span class="nome"><c:out value="${section.label}"/></span>
                    <span class="num"><c:out value="${sectionCounts[section.key]}"/><span>Cadastrados</span></span>
                </a>
            </c:forEach>
        </div>
    </section>
</main>
</body>
</html>
