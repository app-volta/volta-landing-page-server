<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Coleta - Volta</title>
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
            <c:when test="${collection != null and collection.id != null}">Editar coleta</c:when>
            <c:otherwise>Nova coleta</c:otherwise>
        </c:choose>
    </h1>

    <c:if test="${error != null}">
        <div class="mensagem erro"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/coleta">
        <input type="hidden" name="id" value="${collection.id}">
        <c:choose>
            <c:when test="${collection != null and collection.id != null}">
                <input type="hidden" name="action" value="update">
            </c:when>
            <c:otherwise>
                <input type="hidden" name="action" value="insert">
            </c:otherwise>
        </c:choose>

        <label for="incidentId">Ocorrência</label>
        <select id="incidentId" name="incidentId" required>
            <option value="">Selecione...</option>
            <c:forEach var="incident" items="${incidents}">
                <option value="${incident.id}" data-waste-type-id="${incident.wasteType.id}" ${collection.incident.id == incident.id ? 'selected' : ''}>
                    #${incident.id} - <c:out value="${incident.status}"/>
                    <c:if test="${not empty incident.company.name}"> - <c:out value="${incident.company.name}"/></c:if>
                </option>
            </c:forEach>
        </select>

        <label for="cooperativeId">Cooperativa</label>
        <select id="cooperativeId" name="cooperativeId" required>
            <option value="">Selecione...</option>
            <c:forEach var="cooperative" items="${cooperatives}">
                <option value="${cooperative.id}"
                        data-waste-type-ids="${cooperative.wasteTypeIdsCsv}"
                        ${collection.cooperative.id == cooperative.id ? 'selected' : ''}><c:out value="${cooperative.name}"/></option>
            </c:forEach>
        </select>

        <label for="collectionType">Tipo de coleta</label>
        <select id="collectionType" name="collectionType" required>
            <option value="">Selecione...</option>
            <option value="PADRAO" ${collection.collectionType == 'PADRAO' ? 'selected' : ''}>Padrão</option>
            <option value="RECORRENTE" ${collection.collectionType == 'RECORRENTE' ? 'selected' : ''}>Recorrente</option>
        </select>

        <label for="scheduledDate">Data agendada</label>
        <input type="datetime-local" id="scheduledDate" name="scheduledDate"
               value="<c:out value='${collection.scheduledDateInput}'/>">

        <label for="estimatedTime">Tempo estimado</label>
        <input type="time" id="estimatedTime" name="estimatedTime" value="<c:out value='${collection.estimatedTimeInput}'/>">

        <label>
            <input type="checkbox" name="urgent" value="true" ${collection.urgent ? 'checked' : ''}> Urgente
        </label>

        <div class="botoes">
            <button type="submit" class="btn">Salvar</button>
            <a class="btn btn-secundario" href="${pageContext.request.contextPath}/coleta?action=list">Cancelar</a>
        </div>
    </form>
</main>
<script>
    (function () {
        var incidentSelect = document.getElementById('incidentId');
        var cooperativeSelect = document.getElementById('cooperativeId');
        if (!incidentSelect || !cooperativeSelect) {
            return;
        }

        function updateCooperatives() {
            var selected = incidentSelect.options[incidentSelect.selectedIndex];
            var wasteTypeId = selected ? selected.getAttribute('data-waste-type-id') : '';

            Array.prototype.forEach.call(cooperativeSelect.options, function (option) {
                if (!option.value) {
                    return;
                }
                if (!option.dataset.originalText) {
                    option.dataset.originalText = option.textContent;
                }
                var ids = (option.getAttribute('data-waste-type-ids') || '').split(',');
                var compatible = !wasteTypeId || ids.indexOf(wasteTypeId) !== -1;
                option.disabled = !compatible;
                option.textContent = compatible
                    ? option.dataset.originalText
                    : option.dataset.originalText + ' (não atende)';
            });

            if (cooperativeSelect.selectedOptions[0] && cooperativeSelect.selectedOptions[0].disabled) {
                cooperativeSelect.value = '';
            }
        }

        incidentSelect.addEventListener('change', updateCooperatives);
        updateCooperatives();
    })();
</script>
</body>
</html>
