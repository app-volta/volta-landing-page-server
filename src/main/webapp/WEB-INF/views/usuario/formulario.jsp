<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuário - Volta</title>
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
            <c:when test="${user != null and user.id != null}">Editar usuário</c:when>
            <c:otherwise>Novo usuário</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/usuario">
        <input type="hidden" name="id" value="${user.id}">
        <c:choose>
            <c:when test="${user != null and user.id != null}">
                <input type="hidden" name="action" value="update">
            </c:when>
            <c:otherwise>
                <input type="hidden" name="action" value="insert">
            </c:otherwise>
        </c:choose>

        <label for="name">Nome</label>
        <input type="text" id="name" name="name" maxlength="150" value="<c:out value='${user.name}'/>" required>

        <label for="email">Email</label>
        <input type="email" id="email" name="email" maxlength="150" value="<c:out value='${user.email}'/>" required>

        <label for="position">Cargo</label>
        <input type="text" id="position" name="position" maxlength="100" value="<c:out value='${user.position}'/>">

        <c:choose>
            <c:when test="${systemAdmin}">
                <label for="companyId">Empresa (perfis de empresa)</label>
                <select id="companyId" name="companyId">
                    <option value="">Nenhuma</option>
                    <c:forEach var="company" items="${companies}">
                        <option value="${company.id}" ${user.company.id == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                    </c:forEach>
                </select>

                <label for="cooperativeId">Cooperativa (para COOPERATIVE)</label>
                <select id="cooperativeId" name="cooperativeId">
                    <option value="">Nenhuma</option>
                    <c:forEach var="cooperative" items="${cooperatives}">
                        <option value="${cooperative.id}" ${user.cooperative.id == cooperative.id ? 'selected' : ''}><c:out value="${cooperative.name}"/></option>
                    </c:forEach>
                </select>
            </c:when>
            <c:otherwise>
                <label for="companyName">Empresa</label>
                <input type="text" id="companyName" value="<c:out value='${sessionScope.loggedUser.company.name}'/>" disabled>
            </c:otherwise>
        </c:choose>

        <label for="roleId">Tipo de usuário</label>
        <select id="roleId" name="roleId" required>
            <option value="">Selecione...</option>
            <c:forEach var="role" items="${roles}">
                <option value="${role.id}" ${user.role.id == role.id ? 'selected' : ''}><c:out value="${role.type}"/></option>
            </c:forEach>
        </select>

        <c:if test="${user == null or user.id == null}">
            <label for="password">Senha</label>
            <input type="password" id="password" name="password" maxlength="72" required>
        </c:if>

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/usuario?action=list">Cancelar</a>
        </div>
    </form>
</main>
</body>
</html>
