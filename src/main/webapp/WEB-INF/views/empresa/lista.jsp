<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Empresas - Volta</title>
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
    <h1>Empresas cadastradas</h1>


    <c:if test="${systemAdmin}">
        <a class="btn" href="${pageContext.request.contextPath}/empresa?action=new">+ Nova empresa</a>
    </c:if>

    <c:if test="${systemAdmin}">
        <form class="form-inline" method="get" action="${pageContext.request.contextPath}/empresa">
            <input type="hidden" name="action" value="list">
            <input type="text" name="name" maxlength="150" placeholder="Nome..." value="<c:out value='${nameFilter}'/>">
            <input type="text" name="cnpj" maxlength="18" placeholder="CNPJ..." value="<c:out value='${cnpjFilter}'/>">
            <input type="text" name="address" maxlength="255" placeholder="Endereço..." value="<c:out value='${addressFilter}'/>">
            <button type="submit">Filtrar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/empresa?action=list">Limpar</a>
        </form>
    </c:if>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${empty companies}">
        <p class="vazio">Nenhuma empresa encontrada.</p>
    </c:if>

    <c:if test="${not empty companies}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>CNPJ</th>
                    <th>CEP</th>
                    <th>Endereço</th>
                    <c:if test="${canManage}">
                        <th>Ações</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="company" items="${companies}">
                    <tr>
                        <td><c:out value="${company.name}"/></td>
                        <td><c:out value="${company.cnpj}"/></td>
                        <td><c:out value="${company.cep}"/></td>
                        <td><c:out value="${company.address}"/></td>
                        <c:if test="${canManage}">
                            <td class="acoes">
                                <a class="btn-editar" href="${pageContext.request.contextPath}/empresa?action=edit&id=${company.id}">Editar</a>
                                <c:if test="${systemAdmin}">
                                    <form method="post" action="${pageContext.request.contextPath}/empresa"
                                          data-confirm="Excluir esta empresa também apagará áreas, usuários, ocorrências, coletas e demais registros vinculados. Continuar?">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${company.id}">
                                        <button type="submit" class="btn-perigo">Excluir</button>
                                    </form>
                                </c:if>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</main>
</body>
</html>
