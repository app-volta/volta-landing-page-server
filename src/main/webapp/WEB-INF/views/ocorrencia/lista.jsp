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
    <link href="https://fonts.googleapis.com/css2?family=Belanosima:wght@400;600;700&display=swap" rel="stylesheet">
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

    <form class="filtros-painel" method="get" action="${pageContext.request.contextPath}/ocorrencia"
          aria-label="Filtros de ocorrências">
        <input type="hidden" name="action" value="list">

        <div class="filtros-cabecalho">
            <div class="filtros-titulo">
                <span class="filtros-icone" aria-hidden="true">
                    <span class="material-symbols-outlined">filter_alt</span>
                </span>
                <div>
                    <h2>Filtrar ocorrências</h2>
                    <p>Combine os critérios para encontrar rapidamente o que precisa.</p>
                </div>
            </div>
        </div>

        <div class="filtros-grid">
            <c:if test="${systemAdmin}">
                <div class="filtro-campo">
                    <label for="ocorrencia-empresa">Empresa</label>
                    <select id="ocorrencia-empresa" name="companyId">
                        <option value="">Todas as empresas</option>
                        <c:forEach var="company" items="${companies}">
                            <option value="${company.id}" ${selectedCompanyId == company.id ? 'selected' : ''}><c:out value="${company.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <div class="filtro-campo">
                <label for="ocorrencia-area">Área</label>
                <select id="ocorrencia-area" name="areaId">
                    <option value="">Todas as áreas</option>
                    <c:forEach var="area" items="${areas}">
                        <option value="${area.id}" ${selectedAreaId == area.id ? 'selected' : ''}><c:out value="${area.sectorName}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-responsavel">Responsável</label>
                <select id="ocorrencia-responsavel" name="userId">
                    <option value="">Todos os responsáveis</option>
                    <c:forEach var="user" items="${users}">
                        <option value="${user.id}" ${selectedUserId == user.id ? 'selected' : ''}><c:out value="${user.name}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-residuo">Tipo de resíduo</label>
                <select id="ocorrencia-residuo" name="wasteTypeId">
                    <option value="">Todos os resíduos</option>
                    <c:forEach var="wasteType" items="${wasteTypes}">
                        <option value="${wasteType.id}" ${selectedWasteTypeId == wasteType.id ? 'selected' : ''}><c:out value="${wasteType.category}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-prioridade">Prioridade</label>
                <select id="ocorrencia-prioridade" name="priority">
                    <option value="">Todas as prioridades</option>
                    <c:forEach var="priority" items="${priorities}">
                        <option value="${priority}" ${selectedPriority == priority ? 'selected' : ''}><c:out value="${priority}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-status">Status</label>
                <select id="ocorrencia-status" name="status">
                    <option value="">Todos os status</option>
                    <c:forEach var="status" items="${statuses}">
                        <option value="${status}" ${selectedStatus == status ? 'selected' : ''}><c:out value="${status}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-contaminacao">Contaminação</label>
                <select id="ocorrencia-contaminacao" name="contaminationLevel">
                    <option value="">Toda contaminação</option>
                    <c:forEach var="level" items="${contaminationLevels}">
                        <option value="${level}" ${selectedContamination == level ? 'selected' : ''}><c:out value="${level}"/></option>
                    </c:forEach>
                </select>
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-setor">Setor</label>
                <input type="text" id="ocorrencia-setor" name="sectorName" maxlength="150"
                       placeholder="Buscar por setor..." value="<c:out value='${sectorName}'/>">
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-data-inicio">Registrada de</label>
                <input type="date" id="ocorrencia-data-inicio" name="dateFrom"
                       value="<c:out value='${dateFrom}'/>">
            </div>

            <div class="filtro-campo">
                <label for="ocorrencia-data-fim">Registrada até</label>
                <input type="date" id="ocorrencia-data-fim" name="dateTo"
                       value="<c:out value='${dateTo}'/>">
            </div>
        </div>

        <div class="filtros-acoes">
            <a class="filtros-limpar" href="${pageContext.request.contextPath}/ocorrencia?action=list">
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

    <c:if test="${empty incidents}">
        <p class="vazio">Nenhuma ocorrência encontrada.</p>
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
