<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ocorrências - Volta</title>
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
    <h1>Ocorrências cadastradas</h1>


    <c:if test="${canManage}">
        <c:url var="novaOcorrenciaUrl" value="/ocorrencia">
            <c:param name="action" value="new"/>
            <c:if test="${systemAdmin and not empty selectedCompanyId}">
                <c:param name="companyId" value="${selectedCompanyId}"/>
            </c:if>
        </c:url>
        <a class="btn" href="${novaOcorrenciaUrl}">+ Nova ocorrência</a>
    </c:if>

    <form class="form-inline" method="get" action="${pageContext.request.contextPath}/ocorrencia">
        <input type="hidden" name="action" value="list">
        <c:if test="${systemAdmin}">
            <select name="companyId">
                <option value="">Todas as empresas</option>
                <c:forEach var="company" items="${companies}">
                    <option value="${company.id}" ${selectedCompanyId == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                </c:forEach>
            </select>
        </c:if>
        <input type="text" name="sectorName" placeholder="Filtrar por setor..." value="<c:out value='${sectorName}'/>">
        <select name="status">
            <option value="">Todos os status</option>
            <c:forEach var="status" items="${statuses}">
                <option value="${status}" ${selectedStatus == status ? 'selected' : ''}><c:out value="${status}"/></option>
            </c:forEach>
        </select>
        <button type="submit">Filtrar</button>
    </form>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <c:if test="${empty incidents}">
        <p class="vazio sem-resultado">Nenhuma ocorrência encontrada.</p>
    </c:if>

    <c:if test="${not empty incidents}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <c:if test="${systemAdmin}">
                        <th>Empresa</th>
                    </c:if>
                    <th>Responsável</th>
                    <th>Área</th>
                    <th>Resíduo</th>
                    <th>Status</th>
                    <th>Prioridade</th>
                    <th>Contaminação</th>
                    <th>Data de registro</th>
                    <c:if test="${canManage}">
                        <th>Alterar status</th>
                        <th>Ações</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="incident" items="${incidents}">
                    <tr>
                        <c:if test="${systemAdmin}">
                            <td><c:out value="${incident.company.name}"/></td>
                        </c:if>
                        <td><c:out value="${incident.user.name}"/></td>
                        <td><c:out value="${incident.area.sectorName}"/></td>
                        <td><c:out value="${incident.wasteType.category}"/></td>
                        <td><c:out value="${incident.status}"/></td>
                        <td><c:out value="${incident.priority}"/></td>
                        <td><c:out value="${incident.contaminationLevel}"/></td>
                        <td><c:out value="${incident.registeredAt}"/></td>
                        <c:if test="${canManage}">
                            <td>
                                <form class="form-inline" method="post" action="${pageContext.request.contextPath}/ocorrencia">
                                    <input type="hidden" name="action" value="updateStatus">
                                    <input type="hidden" name="id" value="${incident.id}">
                                    <select name="status" required>
                                        <c:forEach var="status" items="${statuses}">
                                            <option value="${status}" ${incident.status == status ? 'selected' : ''}><c:out value="${status}"/></option>
                                        </c:forEach>
                                    </select>
                                    <button type="submit">Alterar</button>
                                </form>
                            </td>
                            <td class="acoes">
                                <a class="btn-editar" href="${pageContext.request.contextPath}/ocorrencia?action=edit&id=${incident.id}">Editar</a>
                                <form method="post" action="${pageContext.request.contextPath}/ocorrencia"
                                      data-confirm="Excluir esta ocorrência também apagará coletas e demais registros vinculados. Continuar?">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${incident.id}">
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
