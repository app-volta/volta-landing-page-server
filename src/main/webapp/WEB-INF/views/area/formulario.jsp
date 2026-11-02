<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Área - Volta</title>
    <link rel="icon" href="${pageContext.request.contextPath}/assets/img/content/logo.png" type="image/x-icon">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200">
</head>
<body>
<%@ include file="/WEB-INF/jspf/nav.jspf" %>
<%@ include file="/WEB-INF/jspf/flash.jspf" %>

<main class="container formulario">
    <h1>
        <c:choose>
            <c:when test="${area != null and area.id != null}">Editar área</c:when>
            <c:otherwise>Nova área</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/area">
        <input type="hidden" name="id" value="${area.id}">
        <c:choose>
            <c:when test="${area != null and area.id != null}">
                <input type="hidden" name="action" value="update">
            </c:when>
            <c:otherwise>
                <input type="hidden" name="action" value="insert">
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${systemAdmin and (area == null or area.id == null)}">
                <label for="companyId">Empresa</label>
                <select id="companyId" name="companyId" required>
                    <option value="">Selecione...</option>
                    <c:forEach var="company" items="${companies}">
                        <option value="${company.id}" ${selectedCompanyId == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                    </c:forEach>
                </select>
            </c:when>
            <c:otherwise>
                <label for="companyName">Empresa</label>
                <input type="text" id="companyName"
                       value="<c:out value='${not empty area.company.name ? area.company.name : sessionScope.loggedUser.company.name}'/>"
                       disabled>
            </c:otherwise>
        </c:choose>

        <label for="sectorName">Nome do setor</label>
        <input type="text" id="sectorName" name="sectorName" value="<c:out value='${area.sectorName}'/>" required>

        <label for="locationDescription">Descrição do local</label>
        <input type="text" id="locationDescription" name="locationDescription" maxlength="255" value="<c:out value='${area.locationDescription}'/>">

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/area?action=list">Cancelar</a>
        </div>
    </form>
</main>
</body>
</html>
