<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuários - Volta</title>
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

<main class="container lista">
    <h1>Usuários cadastrados</h1>


    <a class="btn" href="${pageContext.request.contextPath}/usuario?action=new">+ Novo usuário</a>

    <form class="form-inline" method="get" action="${pageContext.request.contextPath}/usuario">
        <input type="hidden" name="action" value="list">
        <input type="text" name="name" maxlength="150" placeholder="Nome..." value="<c:out value='${nameFilter}'/>">
        <input type="text" name="email" maxlength="150" placeholder="Email..." value="<c:out value='${emailFilter}'/>">
        <select name="roleId">
            <option value="">Todos os perfis</option>
            <c:forEach var="role" items="${roles}">
                <option value="${role.id}" ${selectedRoleId == role.id ? 'selected' : ''}><c:out value="${role.type}"/></option>
            </c:forEach>
        </select>
        <c:if test="${systemAdmin}">
            <select name="companyId">
                <option value="">Todas as empresas</option>
                <c:forEach var="company" items="${companies}">
                    <option value="${company.id}" ${selectedCompanyId == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                </c:forEach>
            </select>
            <select name="cooperativeId">
                <option value="">Todas as cooperativas</option>
                <c:forEach var="cooperative" items="${cooperatives}">
                    <option value="${cooperative.id}" ${selectedCooperativeId == cooperative.id ? 'selected' : ''}><c:out value="${cooperative.name}"/></option>
                </c:forEach>
            </select>
        </c:if>
        <button type="submit">Filtrar</button>
        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/usuario?action=list">Limpar</a>
    </form>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${empty users}">
        <p class="vazio">Nenhum usuário encontrado.</p>
    </c:if>

    <c:if test="${not empty users}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>Email</th>
                    <th>Perfil</th>
                    <th>Cargo</th>
                    <c:if test="${systemAdmin}">
                        <th>Empresa / Cooperativa</th>
                    </c:if>
                    <th>Ações</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}">
                    <tr>
                        <td><c:out value="${user.name}"/></td>
                        <td><c:out value="${user.email}"/></td>
                        <td><c:out value="${user.role.type}"/></td>
                        <td><c:out value="${user.position}"/></td>
                        <c:if test="${systemAdmin}">
                            <td>
                                <c:choose>
                                    <c:when test="${not empty user.company.name}"><c:out value="${user.company.name}"/></c:when>
                                    <c:when test="${not empty user.cooperative.name}"><c:out value="${user.cooperative.name}"/></c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                        <c:set var="rowManageable" value="${systemAdmin or (user.role.type ne 'SYSTEM_ADMIN' and not empty user.company.id and user.company.id eq sessionScope.loggedUser.company.id)}"/>
                        <td class="acoes">
                            <c:choose>
                                <c:when test="${rowManageable}">
                                    <a class="btn-editar" href="${pageContext.request.contextPath}/usuario?action=edit&id=${user.id}">Editar</a>
                                    <c:if test="${user.id != sessionScope.loggedUser.id}">
                                        <form method="post" action="${pageContext.request.contextPath}/usuario"
                                              data-confirm="Excluir este usuário também apagará ocorrências, coletas e demais registros vinculados. Continuar?">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <button type="submit" class="btn-perigo">Excluir</button>
                                        </form>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <span class="vazio">Sem permissão</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</main>
</body>
</html>
