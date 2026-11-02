package br.com.volta.controller;

import br.com.volta.dao.AreaDAO;
import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.IncidentDAO;
import br.com.volta.dao.UserDAO;
import br.com.volta.dao.WasteTypeDAO;
import br.com.volta.models.Area;
import br.com.volta.models.Company;
import br.com.volta.models.ContaminationLevel;
import br.com.volta.models.Incident;
import br.com.volta.models.IncidentStatus;
import br.com.volta.models.Priority;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

//ocorrencias: admin global ve todas; admin de empresa ve apenas a propria empresa
@WebServlet("/ocorrencia")
public class IncidentController extends BaseController {

    private static final String LIST_PATH = "/ocorrencia?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/ocorrencia/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/ocorrencia/formulario.jsp";

    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AreaDAO areaDAO = new AreaDAO();
    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (!SessionUtil.isSystemAdmin(logged) && !SessionUtil.isCompany(logged)) {
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
        if (!SessionUtil.canManage(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteIncident(request, response, logged);
            } else if ("updateStatus".equals(action)) {
                changeStatus(request, response, logged);
            } else if ("insert".equals(action)) {
                saveIncident(request, response, logged, false);
            } else if ("update".equals(action)) {
                saveIncident(request, response, logged, true);
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
        Incident incident = id != null ? incidentDAO.findById(id) : null;
        if (incident == null || !SessionUtil.canManageIncident(logged, incident)) {
            redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
            return;
        }

        request.setAttribute("incident", incident);
        loadSelects(request, incident.getCompany().getId());
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

        if (companyId == null) {
            if (SessionUtil.isSystemAdmin(logged)) {
                showCompanySelection(request, response, null);
                return;
            }
            redirectError(request, response, LIST_PATH, "Selecione uma empresa antes de cadastrar a ocorrencia.");
            return;
        }

        if (companyDAO.findById(companyId) == null) {
            if (SessionUtil.isSystemAdmin(logged)) {
                showCompanySelection(request, response, "Selecione uma empresa valida.");
            } else {
                redirectError(request, response, LIST_PATH, "Empresa do usuario nao encontrada.");
            }
            return;
        }

        loadSelects(request, companyId);
        request.setAttribute("incident", new Incident());
        forward(request, response, FORM_JSP);
    }

    //exibe a selecao de empresa
    private void showCompanySelection(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("companies", companyDAO.findAll());
        request.setAttribute("error", error);
        request.setAttribute("incident", new Incident());
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        Integer companyId = SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("companyId"))
                : SessionUtil.companyId(logged);
        Integer areaId = Validator.parseId(request.getParameter("areaId"));
        Integer userId = Validator.parseId(request.getParameter("userId"));
        Integer wasteTypeId = Validator.parseId(request.getParameter("wasteTypeId"));
        String sectorName = Validator.trimToNull(request.getParameter("sectorName"));
        IncidentStatus status = Validator.parseEnum(IncidentStatus.class, request.getParameter("status"));
        Priority priority = Validator.parseEnum(Priority.class, request.getParameter("priority"));
        ContaminationLevel contamination = Validator.parseEnum(
                ContaminationLevel.class, request.getParameter("contaminationLevel"));
        String dateFromValue = Validator.trimToNull(request.getParameter("dateFrom"));
        String dateToValue = Validator.trimToNull(request.getParameter("dateTo"));
        LocalDate dateFrom = parseDate(dateFromValue);
        LocalDate dateTo = parseDate(dateToValue);

        List<Incident> incidents = incidentDAO.findAllWithFilters(
                companyId, areaId, userId, wasteTypeId, sectorName,
                status, priority, contamination, dateFrom, dateTo);

        request.setAttribute("incidents", incidents);
        request.setAttribute("sectorName", sectorName);
        request.setAttribute("selectedStatus", status);
        request.setAttribute("selectedPriority", priority);
        request.setAttribute("selectedContamination", contamination);
        request.setAttribute("selectedAreaId", areaId);
        request.setAttribute("selectedUserId", userId);
        request.setAttribute("selectedWasteTypeId", wasteTypeId);
        request.setAttribute("dateFrom", dateFromValue);
        request.setAttribute("dateTo", dateToValue);
        request.setAttribute("statuses", IncidentStatus.values());
        request.setAttribute("priorities", Priority.values());
        request.setAttribute("contaminationLevels", ContaminationLevel.values());
        request.setAttribute("wasteTypes", wasteTypeDAO.findAll());
        request.setAttribute("areas", SessionUtil.isSystemAdmin(logged)
                ? areaDAO.findAll()
                : areaDAO.findByCompany(companyId));
        request.setAttribute("users", SessionUtil.isSystemAdmin(logged)
                ? userDAO.findAll()
                : userDAO.findByCompany(companyId));

        if (SessionUtil.isSystemAdmin(logged)) {
            request.setAttribute("companies", companyDAO.findAll());
            request.setAttribute("selectedCompanyId", companyId);
        }

        forward(request, response, LIST_JSP);
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

    //exclui incident
    private void deleteIncident(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Incident incident = id != null ? incidentDAO.findById(id) : null;
        if (incident == null) {
            redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
            return;
        }
        if (!SessionUtil.canManageIncident(logged, incident)) {
            SessionUtil.forbidden(response);
            return;
        }

        try {
            if (!incidentDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Ocorrencia excluida com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir ocorrencia", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a esta ocorrencia."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //altera status
    private void changeStatus(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        IncidentStatus newStatus = Validator.parseEnum(IncidentStatus.class, request.getParameter("status"));
        if (id == null || newStatus == null) {
            redirectError(request, response, LIST_PATH, "Status invalido.");
            return;
        }

        Incident incident = incidentDAO.findById(id);
        if (incident == null) {
            redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
            return;
        }
        if (!SessionUtil.canManageIncident(logged, incident)) {
            SessionUtil.forbidden(response);
            return;
        }
        if (incident.getStatus() == null || !incident.getStatus().canTransitionTo(newStatus)) {
            redirectError(request, response, LIST_PATH, "Transicao de status invalida.");
            return;
        }

        try {
            if (!incidentDAO.updateStatus(id, newStatus)) {
                redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Status atualizado com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao atualizar status da ocorrencia", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //salva incident
    private void saveIncident(HttpServletRequest request, HttpServletResponse response,
                              User logged, boolean update) throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Incident existing = update && id != null ? incidentDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
            return;
        }
        if (update && !SessionUtil.canManageIncident(logged, existing)) {
            SessionUtil.forbidden(response);
            return;
        }

        Integer companyId;
        if (update) {
            if (existing.getCompany() == null || existing.getCompany().getId() == null) {
                redirectError(request, response, LIST_PATH, "Ocorrencia sem empresa vinculada.");
                return;
            }
            companyId = existing.getCompany().getId();
        } else {
            companyId = resolveCompanyId(request, logged);
        }

        Company company = companyId != null ? companyDAO.findById(companyId) : null;
        if (company == null) {
            if (SessionUtil.isSystemAdmin(logged)) {
                showCompanySelection(request, response, "Selecione uma empresa valida.");
            } else {
                redirectError(request, response, LIST_PATH, "Selecione uma empresa antes de cadastrar a ocorrencia.");
            }
            return;
        }

        Incident incident = buildIncident(request, company, update ? id : null);
        String validationError = validateIncident(request, incident, companyId);
        if (validationError != null) {
            showForm(request, response, incident, companyId, validationError);
            return;
        }

        try {
            if (update) {
                if (!incidentDAO.update(incident)) {
                    redirectError(request, response, LIST_PATH, "Ocorrencia nao encontrada.");
                    return;
                }
                redirectSuccess(request, response, LIST_PATH, "Ocorrencia atualizada com sucesso.");
            } else {
                incidentDAO.insert(incident);
                redirectSuccess(request, response, LIST_PATH, "Ocorrencia cadastrada com sucesso.");
            }
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar ocorrencia", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //define a empresa usada no formulario
    private Integer resolveCompanyId(HttpServletRequest request, User logged) {
        return SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("companyId"))
                : SessionUtil.companyId(logged);
    }

    //monta incident
    private Incident buildIncident(HttpServletRequest request, Company company, Integer id) {
        Integer userId = Validator.parseId(request.getParameter("userId"));
        Integer areaId = Validator.parseId(request.getParameter("areaId"));
        Integer wasteTypeId = Validator.parseId(request.getParameter("wasteTypeId"));
        Priority priority = Validator.parseEnum(Priority.class, request.getParameter("priority"));
        ContaminationLevel contamination = Validator.parseEnum(
                ContaminationLevel.class, request.getParameter("contaminationLevel"));

        Incident incident = new Incident();
        incident.setId(id);
        incident.setCompany(company);

        User user = new User();
        user.setId(userId);
        incident.setUser(user);

        Area area = new Area();
        area.setId(areaId);
        incident.setArea(area);

        WasteType wasteType = new WasteType();
        wasteType.setId(wasteTypeId);
        incident.setWasteType(wasteType);

        incident.setPriority(priority);
        incident.setContaminationLevel(contamination != null ? contamination.name() : null);
        incident.setPhotoUrl(Validator.trimToNull(request.getParameter("photoUrl")));
        incident.setEmployeeDescription(Validator.trimToNull(request.getParameter("employeeDescription")));
        return incident;
    }

    //valida incident
    private String validateIncident(HttpServletRequest request, Incident incident, Integer companyId) {
        if (incident.getUser().getId() == null || incident.getArea().getId() == null
                || incident.getWasteType().getId() == null || incident.getPriority() == null) {
            return "Preencha todos os campos obrigatorios.";
        }

        if (companyId == null) {
            return "Selecione uma empresa valida.";
        }

        String contaminationValue = Validator.trimToNull(request.getParameter("contaminationLevel"));
        if (contaminationValue != null && incident.getContaminationLevel() == null) {
            return "Nivel de contaminacao invalido.";
        }

        User user = userDAO.findById(incident.getUser().getId());
        if (user == null || user.getCompany() == null || !Objects.equals(companyId, user.getCompany().getId())) {
            return "Usuario invalido para esta empresa.";
        }

        Area area = areaDAO.findById(incident.getArea().getId());
        if (area == null || area.getCompany() == null || !Objects.equals(companyId, area.getCompany().getId())) {
            return "Area invalida para esta empresa.";
        }

        if (wasteTypeDAO.findById(incident.getWasteType().getId()) == null) {
            return "Tipo de residuo invalido.";
        }

        if (incident.getPhotoUrl() != null) {
            if (!Validator.fits(incident.getPhotoUrl(), Validator.URL_MAX)) {
                return "URL da foto excede o limite de caracteres.";
            }
            if (!Validator.isValidUrl(incident.getPhotoUrl())) {
                return "URL da foto invalida (deve comecar com http ou https).";
            }
        }

        if (!Validator.fits(incident.getEmployeeDescription(), Validator.DESCRIPTION_MAX)) {
            return "Descricao do funcionario excede o limite de caracteres.";
        }

        String weightValue = Validator.trimToNull(request.getParameter("estimatedWeight"));
        Double estimatedWeight = null;
        if (weightValue != null) {
            try {
                estimatedWeight = Double.parseDouble(weightValue.replace(",", "."));
            } catch (NumberFormatException e) {
                return "Peso estimado invalido.";
            }
            if (!Double.isFinite(estimatedWeight) || estimatedWeight < 0) {
                return "Peso estimado invalido.";
            }
            if (estimatedWeight > 1000000) {
                return "Peso estimado acima do limite permitido.";
            }
        }
        incident.setEstimatedWeight(estimatedWeight);
        return null;
    }

    //carrega selects
    private void loadSelects(HttpServletRequest request, Integer companyId) {
        Company company = companyDAO.findById(companyId);
        if (company == null) {
            throw new DaoException("Empresa nao encontrada", null, null);
        }
        request.setAttribute("company", company);
        request.setAttribute("users", userDAO.findByCompany(companyId));
        request.setAttribute("areas", areaDAO.findByCompany(companyId));
        request.setAttribute("wasteTypes", wasteTypeDAO.findAll());
        request.setAttribute("priorities", Priority.values());
        request.setAttribute("contaminationLevels", ContaminationLevel.values());
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Incident incident, Integer companyId, String error)
            throws ServletException, IOException {
        loadSelects(request, companyId);
        request.setAttribute("error", error);
        request.setAttribute("incident", incident);
        forward(request, response, FORM_JSP);
    }
}
