<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cooperativa - Volta</title>
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
    <h1>${cooperative.id == null ? 'Nova cooperativa' : 'Editar cooperativa'}</h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/cooperativa">
        <input type="hidden" name="action" value="${cooperative.id == null ? 'insert' : 'update'}">
        <c:if test="${cooperative.id != null}">
            <input type="hidden" name="id" value="${cooperative.id}">
        </c:if>

        <label for="name">Nome</label>
        <input type="text" id="name" name="name" maxlength="150" value="<c:out value='${cooperative.name}'/>" required>

        <label for="cnpj">CNPJ</label>
        <input type="text" id="cnpj" name="cnpj" inputmode="numeric" maxlength="18" required
               value="<c:out value='${cooperative.cnpj}'/>">

        <label for="cep">CEP</label>
        <input type="text" id="cep" name="cep" inputmode="numeric" maxlength="9" required
               value="<c:out value='${cooperative.cep}'/>">

        <label for="openingTime">Horário de abertura</label>
        <input type="time" id="openingTime" name="openingTime"
               value="<c:out value='${cooperative.openingTimeInput}'/>">

        <label for="closingTime">Horário de fechamento</label>
        <input type="time" id="closingTime" name="closingTime"
               value="<c:out value='${cooperative.closingTimeInput}'/>">

        <label for="monthlyCapacity">Capacidade mensal</label>
        <input type="number" id="monthlyCapacity" name="monthlyCapacity" min="0" step="1"
               value="<c:out value='${cooperative.monthlyCapacity}'/>">

        <fieldset class="checkbox-list">
            <legend>Tipos de resíduo aceitos</legend>
            <c:forEach var="wasteType" items="${wasteTypes}">
                <label>
                    <input type="checkbox" name="wasteTypeIds" value="${wasteType.id}"
                           ${cooperative.wasteTypeIds.contains(wasteType.id) ? 'checked' : ''}>
                    <c:out value="${wasteType.category}"/>
                </label>
            </c:forEach>
        </fieldset>

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/cooperativa?action=list">Cancelar</a>
        </div>
    </form>
</main>
</body>
</html>
