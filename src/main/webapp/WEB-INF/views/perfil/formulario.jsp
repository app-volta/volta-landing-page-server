<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfil - Volta</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Alata&family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
</head>
<body>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>
<%@ include file="/WEB-INF/jspf/flash.jspf" %>

<main class="container formulario">
    <h1>${role.id == null ? 'Novo perfil' : 'Editar perfil'}</h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/perfil">
        <input type="hidden" name="action" value="${role.id == null ? 'insert' : 'update'}">
        <c:if test="${role.id != null}">
            <input type="hidden" name="id" value="${role.id}">
        </c:if>

        <label for="type">Nome do perfil</label>
        <input type="text" id="type" name="type" value="<c:out value='${role.type}'/>" maxlength="50" required>

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/perfil?action=list">Cancelar</a>
        </div>
    </form>
</main>
</body>
</html>
