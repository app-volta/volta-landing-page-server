<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Empresa - Volta</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<%@ include file="/WEB-INF/jspf/nav.jspf" %>
<%@ include file="/WEB-INF/jspf/flash.jspf" %>

<main class="container formulario">
    <h1>${company.id == null ? 'Nova empresa' : 'Editar empresa'}</h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/empresa">
        <input type="hidden" name="action" value="${company.id == null ? 'insert' : 'update'}">
        <c:if test="${company.id != null}">
            <input type="hidden" name="id" value="${company.id}">
        </c:if>

        <label for="name">Nome</label>
        <input type="text" id="name" name="name" maxlength="150" value="<c:out value='${company.name}'/>" required>

        <label for="cnpj">CNPJ</label>
        <input type="text" id="cnpj" name="cnpj" inputmode="numeric" maxlength="18" value="<c:out value='${company.cnpj}'/>" required>

        <label for="cep">CEP</label>
        <input type="text" id="cep" name="cep" inputmode="numeric" maxlength="9" autocomplete="postal-code" value="<c:out value='${company.cep}'/>" required>

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/empresa?action=list">Cancelar</a>
        </div>
    </form>
</main>
</body>
</html>
