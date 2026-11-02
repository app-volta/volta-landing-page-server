<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ocorrência - Volta</title>
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
    <h1>
        <c:choose>
            <c:when test="${incident != null and incident.id != null}">Editar ocorrência</c:when>
            <c:otherwise>Nova ocorrência</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${empty company}">
            <p class="mensagem">Selecione a empresa para carregar os usuários e as áreas disponíveis.</p>
            <form method="get" action="${pageContext.request.contextPath}/ocorrencia">
                <input type="hidden" name="action" value="new">
                <label for="companyId">Empresa</label>
                <select id="companyId" name="companyId" required>
                    <option value="">Selecione...</option>
                    <c:forEach var="item" items="${companies}">
                        <option value="${item.id}"><c:out value="${item.name}"/></option>
                    </c:forEach>
                </select>
                <div class="botoes">
                    <button type="submit" class="btn">Continuar</button>
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/ocorrencia?action=list">Cancelar</a>
                </div>
            </form>
        </c:when>
        <c:otherwise>
            <form method="post" action="${pageContext.request.contextPath}/ocorrencia">
                <input type="hidden" name="id" value="${incident.id}">
                <input type="hidden" name="companyId" value="${company.id}">
                <c:choose>
                    <c:when test="${incident != null and incident.id != null}">
                        <input type="hidden" name="action" value="update">
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="action" value="insert">
                    </c:otherwise>
                </c:choose>

                <label for="companyName">Empresa</label>
                <input type="text" id="companyName" value="<c:out value='${company.name}'/>" disabled>

                <label for="userId">Usuário</label>
                <select id="userId" name="userId" required>
                    <option value="">Selecione...</option>
                    <c:forEach var="user" items="${users}">
                        <option value="${user.id}" ${incident.user.id == user.id ? 'selected' : ''}><c:out value="${user.name}"/></option>
                    </c:forEach>
                </select>

                <label for="areaId">Área</label>
                <select id="areaId" name="areaId" required>
                    <option value="">Selecione...</option>
                    <c:forEach var="area" items="${areas}">
                        <option value="${area.id}" ${incident.area.id == area.id ? 'selected' : ''}><c:out value="${area.sectorName}"/></option>
                    </c:forEach>
                </select>

                <label for="wasteTypeId">Tipo de resíduo</label>
                <select id="wasteTypeId" name="wasteTypeId" required>
                    <option value="">Selecione...</option>
                    <c:forEach var="wasteType" items="${wasteTypes}">
                        <option value="${wasteType.id}" ${incident.wasteType.id == wasteType.id ? 'selected' : ''}><c:out value="${wasteType.category}"/></option>
                    </c:forEach>
                </select>

                <label for="priority">Prioridade</label>
                <select id="priority" name="priority" required>
                    <c:forEach var="priority" items="${priorities}">
                        <option value="${priority}" ${incident.priority == priority ? 'selected' : ''}><c:out value="${priority}"/></option>
                    </c:forEach>
                </select>

                <label for="contaminationLevel">Nível de contaminação</label>
                <select id="contaminationLevel" name="contaminationLevel">
                    <option value="">Não informado</option>
                    <c:forEach var="level" items="${contaminationLevels}">
                        <option value="${level}" ${incident.contaminationLevel == level ? 'selected' : ''}><c:out value="${level}"/></option>
                    </c:forEach>
                </select>

                <label for="photoUrl">URL da foto</label>
                <input type="url" id="photoUrl" name="photoUrl" maxlength="500" placeholder="https://..."
                       value="<c:out value='${incident.photoUrl}'/>">

                <label for="employeeDescription">Descrição do funcionário</label>
                <textarea id="employeeDescription" name="employeeDescription" maxlength="1000" rows="3"><c:out value="${incident.employeeDescription}"/></textarea>

                <label for="estimatedWeight">Peso estimado (kg)</label>
                <input type="number" step="0.01" min="0" max="1000000" id="estimatedWeight" name="estimatedWeight"
                       value="<c:out value='${incident.estimatedWeight}'/>">

                <div class="botoes">
                    <button type="submit" class="btn">Salvar</button>
                    <a class="btn btn-secundario" href="${pageContext.request.contextPath}/ocorrencia?action=list">Cancelar</a>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
