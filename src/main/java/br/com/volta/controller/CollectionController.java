package br.com.volta.controller;

import br.com.volta.dao.CollectionDAO;
import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.CooperativeDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.IncidentDAO;
import br.com.volta.dao.WasteTypeDAO;
import br.com.volta.models.Collection;
import br.com.volta.models.CollectionStatus;
import br.com.volta.models.Cooperative;
import br.com.volta.models.Incident;
import br.com.volta.models.User;
import br.com.volta.models.WasteType;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//coletas: admin global gerencia todas; admin de empresa gerencia as da propria empresa;
//a cooperativa altera somente status e data das coletas dela
@WebServlet("/coleta")
public class CollectionController extends BaseController {

    private static final String LIST_PATH = "/coleta?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/coleta/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/coleta/formulario.jsp";

    private final CollectionDAO collectionDAO = new CollectionDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final CooperativeDAO cooperativeDAO = new CooperativeDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (!SessionUtil.isSystemAdmin(logged)
                && !SessionUtil.isCompany(logged)
                && !SessionUtil.isCooperative(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("edit".equals(action)) {
                showEditForm(request, response, logged);
            } else if ("new".equals(action)) {
                showNewForm(request, response, logged);
            } else {
                showList(request, response, logged);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        String action = request.getParameter("action");

        if ("updateStatus".equals(action) || "updateScheduledDate".equals(action)) {
            if (!SessionUtil.canManage(logged) && !SessionUtil.isCooperative(logged)) {
                SessionUtil.forbidden(response);
                return;
            }
        } else if (!SessionUtil.canManage(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        try {
            if ("delete".equals(action)) {
                deleteCollection(request, response, logged);
            } else if ("updateStatus".equals(action)) {
                changeStatus(request, response, logged);
            } else if ("updateScheduledDate".equals(action)) {
                changeSchedule(request, response, logged);
            } else if ("insert".equals(action)) {
                saveCollection(request, response, logged, false);
            } else if ("update".equals(action)) {
                saveCollection(request, response, logged, true);
            } else {
                redirect(request, response, LIST_PATH);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //exibe o formulario de edicao
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        if (!SessionUtil.canManage(logged)) {
            redirect(request, response, LIST_PATH);
            return;
        }

        Integer id = Validator.parseId(request.getParameter("id"));
        Collection collection = id != null ? collectionDAO.findById(id) : null;
        if (collection == null) {
            redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
            return;
        }
        if (!SessionUtil.canManageCollection(logged, collection)) {
            SessionUtil.forbidden(response);
            return;
        }

        request.setAttribute("collection", collection);
        loadSelects(request, logged, companyId(collection), wasteTypeId(collection), cooperativeId(collection));
        forward(request, response, FORM_JSP);
    }

    //exibe o formulario de cadastro
    private void showNewForm(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        if (!SessionUtil.canManage(logged)) {
            redirect(request, response, LIST_PATH);
            return;
        }

        Integer companyId = SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("companyId"))
                : SessionUtil.companyId(logged);
        if (!SessionUtil.isSystemAdmin(logged) && companyId == null) {
            redirectError(request, response, LIST_PATH, "Selecione uma empresa antes de cadastrar a coleta.");
            return;
        }

        loadSelects(request, logged, companyId, null, null);
        request.setAttribute("collection", new Collection());
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        boolean systemAdmin = SessionUtil.isSystemAdmin(logged);
        boolean companyUser = SessionUtil.isCompany(logged);
        boolean cooperativeUser = SessionUtil.isCooperative(logged);

        Integer companyFilter = systemAdmin
                ? Validator.parseId(request.getParameter("companyId"))
                : (companyUser ? SessionUtil.companyId(logged) : null);
        Integer cooperativeFilter = cooperativeUser
                ? SessionUtil.cooperativeId(logged)
                : Validator.parseId(request.getParameter("cooperativeId"));
        Integer incidentFilter = Validator.parseId(request.getParameter("incidentId"));
        CollectionStatus statusFilter = Validator.parseEnum(
                CollectionStatus.class, request.getParameter("status"));
        String urgentValue = Validator.trimToNull(request.getParameter("urgent"));
        Boolean urgentFilter = urgentValue == null ? null : Boolean.valueOf(urgentValue);
        String typeFilter = Validator.trimToNull(request.getParameter("collectionType"));
        String dateFromValue = Validator.trimToNull(request.getParameter("dateFrom"));
        String dateToValue = Validator.trimToNull(request.getParameter("dateTo"));
        LocalDate dateFrom = parseDate(dateFromValue);
        LocalDate dateTo = parseDate(dateToValue);

        List<Collection> collections = collectionDAO.findAllWithFilters(
                companyFilter, cooperativeFilter, incidentFilter, statusFilter,
                urgentFilter, typeFilter, dateFrom, dateTo);

        request.setAttribute("collections", collections);
        request.setAttribute("selectedCompanyId", companyFilter);
        request.setAttribute("selectedCooperativeId", cooperativeFilter);
        request.setAttribute("selectedIncidentId", incidentFilter);
        request.setAttribute("selectedStatus", statusFilter);
        request.setAttribute("selectedUrgent", urgentFilter);
        request.setAttribute("selectedCollectionType", typeFilter);
        request.setAttribute("dateFrom", dateFromValue);
        request.setAttribute("dateTo", dateToValue);
        loadListFilters(request, logged, companyFilter);
        forward(request, response, LIST_JSP);
    }

    //carrega list filters
    private void loadListFilters(HttpServletRequest request, User logged, Integer companyFilter) {
        request.setAttribute("collectionStatuses", CollectionStatus.values());
        request.setAttribute("collectionTypes", List.of("PADRAO", "RECORRENTE"));

        if (SessionUtil.isSystemAdmin(logged)) {
            request.setAttribute("companies", companyDAO.findAll());
            request.setAttribute("cooperatives", cooperativeDAO.findAll());
            request.setAttribute("incidents", incidentDAO.findAll());
        } else if (SessionUtil.isCompany(logged)) {
            request.setAttribute("cooperatives", cooperativeDAO.findAll());
            request.setAttribute("incidents", incidentDAO.findByCompany(
                    companyFilter != null ? companyFilter : SessionUtil.companyId(logged)));
        }
    }

    //converte date
    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    //exclui collection
    private void deleteCollection(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Collection collection = id != null ? collectionDAO.findById(id) : null;
        if (collection == null) {
            redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
            return;
        }
        if (!SessionUtil.canManageCollection(logged, collection)) {
            SessionUtil.forbidden(response);
            return;
        }

        try {
            if (!collectionDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Coleta excluida com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir coleta", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a esta coleta."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //altera status
    private void changeStatus(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        CollectionStatus newStatus = Validator.parseEnum(CollectionStatus.class, request.getParameter("status"));
        if (id == null || newStatus == null) {
            redirectError(request, response, LIST_PATH, "Status invalido.");
            return;
        }

        Collection collection = collectionDAO.findById(id);
        if (collection == null) {
            redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
            return;
        }
        if (!SessionUtil.canOperateCollection(logged, collection)) {
            SessionUtil.forbidden(response);
            return;
        }
        if (collection.getCurrentStatus() == null
                || !collection.getCurrentStatus().canTransitionTo(newStatus)) {
            redirectError(request, response, LIST_PATH, "Transicao de status invalida.");
            return;
        }

        try {
            if (!collectionDAO.updateStatus(id, newStatus)) {
                redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Status atualizado com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao atualizar status da coleta", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //altera schedule
    private void changeSchedule(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Collection collection = id != null ? collectionDAO.findById(id) : null;
        if (collection == null) {
            redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
            return;
        }
        if (!SessionUtil.canOperateCollection(logged, collection)) {
            SessionUtil.forbidden(response);
            return;
        }

        LocalDateTime scheduledDate = parseDateTime(Validator.trimToNull(request.getParameter("scheduledDate")));
        if (scheduledDate == null) {
            redirectError(request, response, LIST_PATH, "Data agendada invalida.");
            return;
        }

        boolean changed = !Objects.equals(scheduledDate, withoutSeconds(collection.getScheduledDate()));
        if (changed && scheduledDate.isBefore(withoutSeconds(LocalDateTime.now()))) {
            redirectError(request, response, LIST_PATH, "A data agendada nao pode ser no passado.");
            return;
        }

        try {
            if (!collectionDAO.updateScheduledDate(id, scheduledDate)) {
                redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Data agendada atualizada com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao atualizar data da coleta", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //salva collection
    private void saveCollection(HttpServletRequest request, HttpServletResponse response,
                                User logged, boolean update) throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Collection existing = update && id != null ? collectionDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
            return;
        }
        if (update && !SessionUtil.canManageCollection(logged, existing)) {
            SessionUtil.forbidden(response);
            return;
        }

        Integer incidentId = Validator.parseId(request.getParameter("incidentId"));
        Integer cooperativeId = Validator.parseId(request.getParameter("cooperativeId"));
        Collection collection = buildCollection(request, update ? id : null, incidentId, cooperativeId);

        String validationError = validateCollection(request, collection, existing, logged);
        if (validationError != null) {
            showForm(request, response, logged, collection, validationError);
            return;
        }

        try {
            if (update) {
                if (!collectionDAO.update(collection)) {
                    redirectError(request, response, LIST_PATH, "Coleta nao encontrada.");
                    return;
                }
                redirectSuccess(request, response, LIST_PATH, "Coleta atualizada com sucesso.");
            } else {
                collectionDAO.insert(collection);
                redirectSuccess(request, response, LIST_PATH, "Coleta cadastrada com sucesso.");
            }
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar coleta", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //monta collection
    private Collection buildCollection(HttpServletRequest request, Integer id,
                                       Integer incidentId, Integer cooperativeId) {
        Collection collection = new Collection();
        collection.setId(id);

        Incident incident = new Incident();
        incident.setId(incidentId);
        collection.setIncident(incident);

        Cooperative cooperative = new Cooperative();
        cooperative.setId(cooperativeId);
        collection.setCooperative(cooperative);

        String collectionType = Validator.trimToNull(request.getParameter("collectionType"));
        collection.setCollectionType(collectionType == null ? null : collectionType.toUpperCase(Locale.ROOT));
        collection.setUrgent("on".equals(request.getParameter("urgent")) || "true".equals(request.getParameter("urgent")));
        return collection;
    }

    //valida collection
    private String validateCollection(HttpServletRequest request, Collection collection,
                                      Collection existing, User logged) {
        if (collection.getIncident() == null || collection.getIncident().getId() == null
                || collection.getCooperative() == null || collection.getCooperative().getId() == null) {
            return "Preencha todos os campos obrigatorios.";
        }

        Incident incident = incidentDAO.findById(collection.getIncident().getId());
        if (incident == null) {
            return "Ocorrencia nao encontrada.";
        }
        if (!SessionUtil.canUseIncident(logged, incident)) {
            return "Ocorrencia invalida para o seu perfil.";
        }
        if (incident.getCompany() == null) {
            return "Ocorrencia sem empresa vinculada.";
        }
        collection.getIncident().setCompany(incident.getCompany());
        collection.getIncident().setWasteType(incident.getWasteType());

        Cooperative cooperative = cooperativeDAO.findById(collection.getCooperative().getId());
        if (cooperative == null) {
            return "Cooperativa nao encontrada.";
        }
        collection.getCooperative().setName(cooperative.getName());

        if (incident.getWasteType() == null) {
            return "Ocorrencia sem tipo de residuo.";
        }
        boolean servesWasteType = wasteTypeDAO.findByCooperative(cooperative.getId()).stream()
                .anyMatch(type -> type.getId().equals(incident.getWasteType().getId()));
        if (!servesWasteType) {
            return "A cooperativa selecionada nao atende ao tipo de residuo da ocorrencia.";
        }

        if (collection.getCollectionType() == null
                || (!"PADRAO".equals(collection.getCollectionType())
                && !"RECORRENTE".equals(collection.getCollectionType()))) {
            return "Selecione um tipo de coleta valido.";
        }

        String scheduledValue = Validator.trimToNull(request.getParameter("scheduledDate"));
        LocalDateTime scheduledDate = parseDateTime(scheduledValue);
        if (scheduledValue != null && scheduledDate == null) {
            return "Data agendada invalida.";
        }
        if (scheduledDate != null) {
            boolean changed = existing == null
                    || !Objects.equals(scheduledDate, withoutSeconds(existing.getScheduledDate()));
            if (changed && scheduledDate.isBefore(withoutSeconds(LocalDateTime.now()))) {
                return "A data agendada nao pode ser no passado.";
            }
        }
        collection.setScheduledDate(scheduledDate);

        String estimatedValue = Validator.trimToNull(request.getParameter("estimatedTime"));
        LocalTime estimatedTime = null;
        if (estimatedValue != null) {
            try {
                estimatedTime = LocalTime.parse(estimatedValue);
            } catch (DateTimeParseException e) {
                return "Horario estimado invalido.";
            }
        }
        collection.setEstimatedTime(estimatedTime);
        return null;
    }

    //carrega selects
    private void loadSelects(HttpServletRequest request, User logged, Integer companyId,
                             Integer wasteTypeId, Integer selectedCooperativeId) {

        List<Incident> incidents;
        if (companyId != null) {
            incidents = incidentDAO.findByCompany(companyId);
        } else if (SessionUtil.isSystemAdmin(logged)) {
            incidents = incidentDAO.findAll();
        } else {
            incidents = incidentDAO.findByCompany(SessionUtil.companyId(logged));
        }
        request.setAttribute("incidents", incidents);

        List<Cooperative> cooperatives = wasteTypeId != null
                ? cooperativeDAO.findByWasteType(wasteTypeId)
                : cooperativeDAO.findAll();

        if (selectedCooperativeId != null
                && cooperatives.stream().noneMatch(item -> selectedCooperativeId.equals(item.getId()))) {
            Cooperative selected = cooperativeDAO.findById(selectedCooperativeId);
            if (selected != null) {
                cooperatives.add(selected);
            }
        }

        for (Cooperative cooperative : cooperatives) {
            cooperative.setWasteTypeIds(loadWasteTypeIds(cooperative.getId()));
        }

        request.setAttribute("cooperatives", cooperatives);
        request.setAttribute("collectionStatuses", CollectionStatus.values());
        if (companyId != null) {
            request.setAttribute("companyId", companyId);
        }
    }

    //carrega os tipos de residuo vinculados
    private List<Integer> loadWasteTypeIds(Integer cooperativeId) {
        return wasteTypeDAO.findByCooperative(cooperativeId).stream()
                .map(WasteType::getId)
                .toList();
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          User logged, Collection collection, String error)
            throws ServletException, IOException {
        loadSelects(request, logged, companyId(collection), wasteTypeId(collection), cooperativeId(collection));
        request.setAttribute("error", error);
        request.setAttribute("collection", collection);
        forward(request, response, FORM_JSP);
    }

    //devolve o id da empresa vinculada
    private Integer companyId(Collection collection) {
        return collection.getIncident() != null && collection.getIncident().getCompany() != null
                ? collection.getIncident().getCompany().getId()
                : null;
    }

    //devolve o id do tipo de residuo
    private Integer wasteTypeId(Collection collection) {
        return collection.getIncident() != null && collection.getIncident().getWasteType() != null
                ? collection.getIncident().getWasteType().getId()
                : null;
    }

    //devolve o id da cooperativa vinculada
    private Integer cooperativeId(Collection collection) {
        return collection.getCooperative() != null ? collection.getCooperative().getId() : null;
    }

    //remove os segundos de um horario
    private LocalDateTime withoutSeconds(LocalDateTime value) {
        return value == null ? null : value.withSecond(0).withNano(0);
    }

    //converte date time
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
