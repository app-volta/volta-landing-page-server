<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cooperativas - Volta</title>
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
    <h1>Cooperativas cadastradas</h1>


    <c:if test="${systemAdmin}">
        <a class="btn" href="${pageContext.request.contextPath}/cooperativa?action=new">+ Nova cooperativa</a>
    </c:if>

    <c:if test="${systemAdmin}">
        <form class="form-inline" method="get" action="${pageContext.request.contextPath}/cooperativa">
            <input type="hidden" name="action" value="list">
            <input type="text" name="name" maxlength="150" placeholder="Nome..." value="<c:out value='${nameFilter}'/>">
            <input type="text" name="cnpj" maxlength="18" placeholder="CNPJ..." value="<c:out value='${cnpjFilter}'/>">
            <input type="text" name="cep" maxlength="9" placeholder="CEP..." value="<c:out value='${cepFilter}'/>">
            <select name="wasteTypeId">
                <option value="">Todos os resíduos</option>
                <c:forEach var="wasteType" items="${wasteTypes}">
                    <option value="${wasteType.id}" ${selectedWasteTypeId == wasteType.id ? 'selected' : ''}><c:out value="${wasteType.category}"/></option>
                </c:forEach>
            </select>
            <input type="number" min="0" step="1" name="minCapacity" placeholder="Capacidade mínima" value="<c:out value='${minCapacityFilter}'/>">
            <button type="submit">Filtrar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/cooperativa?action=list">Limpar</a>
        </form>
    </c:if>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${empty cooperatives}">
        <p class="vazio">Nenhuma cooperativa encontrada.</p>
    </c:if>

    <c:if test="${not empty cooperatives}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>CNPJ</th>
                    <th>CEP</th>
                    <th>Horário de funcionamento</th>
                    <th>Avaliação média</th>
                    <th>Capacidade mensal</th>
                    <c:if test="${systemAdmin}">
                        <th>Ações</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="cooperative" items="${cooperatives}">
                    <tr>
                        <td><c:out value="${cooperative.name}"/></td>
                        <td><c:out value="${cooperative.cnpj}"/></td>
                        <td><c:out value="${cooperative.cep}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${cooperative.openingTime != null and cooperative.closingTime != null}">
                                    <c:out value="${cooperative.openingTime}"/> - <c:out value="${cooperative.closingTime}"/>
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${cooperative.averageRating}"/></td>
                        <td><c:out value="${cooperative.monthlyCapacity}"/></td>
                        <c:if test="${systemAdmin}">
                            <td class="acoes">
                                <a class="btn-editar"
                                   href="${pageContext.request.contextPath}/cooperativa?action=edit&id=${cooperative.id}">Editar</a>
                                <form method="post" action="${pageContext.request.contextPath}/cooperativa"
                                      data-confirm="Excluir esta cooperativa também apagará coletas, usuários e demais registros vinculados. Continuar?">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${cooperative.id}">
                                    <button type="submit" class="btn-perigo">Excluir</button>
                                </form>
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
