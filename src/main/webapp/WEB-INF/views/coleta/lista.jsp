<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coletas - Volta</title>
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
    <h1>Coletas cadastradas</h1>


    <c:if test="${canManage}">
        <a class="btn" href="${pageContext.request.contextPath}/coleta?action=new">+ Nova coleta</a>
    </c:if>

    <form class="filtros-painel" method="get" action="${pageContext.request.contextPath}/coleta"
          aria-label="Filtros de coletas">
        <input type="hidden" name="action" value="list">

        <div class="filtros-cabecalho">
            <div class="filtros-titulo">
                <span class="filtros-icone" aria-hidden="true">
                    <span class="material-symbols-outlined">filter_alt</span>
                </span>
                <div>
                    <h2>Filtrar coletas</h2>
                    <p>Refine por empresa, cooperativa, status, urgência e data da solicitação.</p>
                </div>
            </div>
        </div>

        <div class="filtros-grid">
            <c:if test="${systemAdmin}">
                <div class="filtro-campo">
                    <label for="coleta-empresa">Empresa</label>
                    <select id="coleta-empresa" name="companyId">
                        <option value="">Todas as empresas</option>
                        <c:forEach var="company" items="${companies}">
                            <option value="${company.id}" ${selectedCompanyId == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <c:if test="${not cooperativeUser}">
                <div class="filtro-campo">
                    <label for="coleta-cooperativa">Cooperativa</label>
                    <select id="coleta-cooperativa" name="cooperativeId">
                        <option value="">Todas as cooperativas</option>
                        <c:forEach var="cooperative" items="${cooperatives}">
                            <option value="${cooperative.id}" ${selectedCooperativeId == cooperative.id ? 'selected' : ''}><c:out value="${cooperative.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filtro-campo">
                    <label for="coleta-ocorrencia">Ocorrência</label>
                    <select id="coleta-ocorrencia" name="incidentId">
                        <option value="">Todas as ocorrências</option>
                        <c:forEach var="incident" items="${incidents}">
                            <option value="${incident.id}" ${selectedIncidentId == incident.id ? 'selected' : ''}>#${incident.id} - <c:out value="${incident.status}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <div class="filtro-campo">
                <label for="coleta-status">Status</label>
                <select id="coleta-status" name="status">
                    <option value="">Todos os status</option>
                    <c:forEach var="status" items="${collectionStatuses}">
                        <option value="${status}" ${selectedStatus == status ? 'selected' : ''}><c:out value="${status}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="coleta-tipo">Tipo de coleta</label>
                <select id="coleta-tipo" name="collectionType">
                    <option value="">Todos os tipos</option>
                    <c:forEach var="type" items="${collectionTypes}">
                        <option value="${type}" ${selectedCollectionType == type ? 'selected' : ''}>
                            <c:choose>
                                <c:when test="${type == 'PADRAO'}">Padrão</c:when>
                                <c:otherwise>Recorrente</c:otherwise>
                            </c:choose>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="coleta-urgencia">Urgência</label>
                <select id="coleta-urgencia" name="urgent">
                    <option value="">Todas</option>
                    <option value="true" ${selectedUrgent == true ? 'selected' : ''}>Urgentes</option>
                    <option value="false" ${selectedUrgent == false ? 'selected' : ''}>Não urgentes</option>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="coleta-data-inicio">Solicitada de</label>
                <input type="date" id="coleta-data-inicio" name="dateFrom"
                       value="<c:out value='${dateFrom}'/>">
            </div>

            <div class="filtro-campo">
                <label for="coleta-data-fim">Solicitada até</label>
                <input type="date" id="coleta-data-fim" name="dateTo"
                       value="<c:out value='${dateTo}'/>">
            </div>
        </div>

        <div class="filtros-acoes">
            <a class="filtros-limpar" href="${pageContext.request.contextPath}/coleta?action=list">
                <span class="material-symbols-outlined" aria-hidden="true">restart_alt</span>
                Limpar filtros
            </a>
            <button type="submit" class="filtros-aplicar">
                <span class="material-symbols-outlined" aria-hidden="true">search</span>
                Aplicar filtros
            </button>
        </div>
    </form>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>
    <c:if test="${empty collections}">
        <p class="vazio">Nenhuma coleta encontrada.</p>
    </c:if>

    <c:if test="${not empty collections}">
        <div class="tabela-responsiva">
            <table>
                <thead>
                <tr>
                    <c:if test="${systemAdmin}">
                        <th>Empresa</th>
                    </c:if>
                    <th>Ocorrência</th>
                    <th>Cooperativa</th>
                    <th>Status</th>
                    <th>Tipo</th>
                    <th>Urgente</th>
                    <th>Solicitada em</th>
                    <th>Data agendada</th>
                    <c:if test="${canManage or cooperativeUser}">
                        <th>Alterar status</th>
                        <th>Agendar data</th>
                    </c:if>
                    <c:if test="${canManage}">
                        <th>Ações</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="collection" items="${collections}">
                    <c:set var="canOperate" value="${canManage or (cooperativeUser and collection.cooperative.id == sessionScope.loggedUser.cooperative.id)}"/>
                    <tr>
                        <c:if test="${systemAdmin}">
                            <td><c:out value="${collection.incident.company.name}"/></td>
                        </c:if>
                        <td>#<c:out value="${collection.incident.id}"/></td>
                        <td><c:out value="${collection.cooperative.name}"/></td>
                        <td><c:out value="${collection.currentStatus}"/></td>
                        <td><c:out value="${collection.collectionType}"/></td>
                        <td>${collection.urgent ? 'Sim' : 'Não'}</td>
                        <td><c:out value="${collection.requestDate}"/></td>
                        <td><c:out value="${collection.scheduledDate}"/></td>
                        <c:if test="${canOperate}">
                            <td>
                                <form class="form-inline" method="post" action="${pageContext.request.contextPath}/coleta">
                                    <input type="hidden" name="action" value="updateStatus">
                                    <input type="hidden" name="id" value="${collection.id}">
                                    <select name="status" required>
                                        <c:forEach var="status" items="${collectionStatuses}">
                                            <option value="${status}" ${collection.currentStatus == status ? 'selected' : ''}><c:out value="${status}"/></option>
                                        </c:forEach>
                                    </select>
                                    <button type="submit">Alterar</button>
                                </form>
                            </td>
                            <td>
                                <form class="form-inline" method="post" action="${pageContext.request.contextPath}/coleta">
                                    <input type="hidden" name="action" value="updateScheduledDate">
                                    <input type="hidden" name="id" value="${collection.id}">
                                    <input type="datetime-local" name="scheduledDate" required
                                           value="<c:out value='${collection.scheduledDateInput}'/>">
                                    <button type="submit">Agendar</button>
                                </form>
                            </td>
                        </c:if>
                        <c:if test="${canManage}">
                            <td class="acoes">
                                <a class="btn-editar" href="${pageContext.request.contextPath}/coleta?action=edit&id=${collection.id}">Editar</a>
                                <form method="post" action="${pageContext.request.contextPath}/coleta"
                                      data-confirm="Excluir esta coleta também apagará o histórico, avaliações e conversas vinculadas. Continuar?">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${collection.id}">
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
