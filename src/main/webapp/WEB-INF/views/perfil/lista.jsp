<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfis de acesso - Volta</title>
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

<main class="container lista">
    <h1>Perfis de acesso</h1>


    <a class="btn" href="${pageContext.request.contextPath}/perfil?action=new">+ Novo perfil</a>

    <form class="form-inline" method="get" action="${pageContext.request.contextPath}/perfil">
        <input type="hidden" name="action" value="list">
        <input type="text" name="type" maxlength="50" placeholder="Perfil..." value="<c:out value='${typeFilter}'/>">
        <select name="withUsers">
            <option value="">Todos os vínculos</option>
            <option value="true" ${selectedWithUsers == true ? 'selected' : ''}>Com usuários</option>
            <option value="false" ${selectedWithUsers == false ? 'selected' : ''}>Sem usuários</option>
        </select>
        <button type="submit">Filtrar</button>
        <a class="btn btn-secundario" href="${pageContext.request.contextPath}/perfil?action=list">Limpar</a>
    </form>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${empty roles}">
        <p class="vazio sem-resultado">Nenhum perfil encontrado.</p>
    </c:if>

    <c:if test="${not empty roles}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <th>Perfil</th>
                    <th>Usuários vinculados</th>
                    <th>Ações</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="role" items="${roles}">
                    <tr>
                        <td><c:out value="${role.type}"/></td>
                        <td><c:out value="${role.userCount}"/></td>
                        <td class="acoes">
                            <a class="btn-editar" href="${pageContext.request.contextPath}/perfil?action=edit&id=${role.id}">Editar</a>
                            <form method="post" action="${pageContext.request.contextPath}/perfil"
                                  data-confirm="Excluir este perfil também apagará os usuários vinculados e os registros deles. Continuar?">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${role.id}">
                                <button type="submit" class="btn-perigo">Excluir</button>
                            </form>
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
