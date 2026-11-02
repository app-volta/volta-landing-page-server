package br.com.volta.controller;

import br.com.volta.dao.AreaDAO;
import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.models.Area;
import br.com.volta.models.Company;
import br.com.volta.models.User;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//areas: admin global gerencia todas; admin de empresa gerencia apenas as da propria empresa
@WebServlet("/area")
public class AreaController extends BaseController {

    private static final String LIST_PATH = "/area?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/area/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/area/formulario.jsp";

    private final AreaDAO areaDAO = new AreaDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();

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
                deleteArea(request, response, logged);
            } else if ("insert".equals(action)) {
                saveArea(request, response, logged, false);
            } else if ("update".equals(action)) {
                saveArea(request, response, logged, true);
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
        Area area = id != null ? areaDAO.findById(id) : null;
        if (area == null || !canAccess(logged, area)) {
            redirectError(request, response, LIST_PATH, "Area nao encontrada.");
            return;
        }

        prepareForm(request, logged, area.getCompany());
        request.setAttribute("area", area);
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
        Company company = companyId != null ? companyDAO.findById(companyId) : null;

        prepareForm(request, logged, company);
        request.setAttribute("area", new Area());
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        Integer companyFilter = SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("companyId"))
                : SessionUtil.companyId(logged);
        String sectorFilter = Validator.trimToNull(request.getParameter("sectorName"));
        String locationFilter = Validator.trimToNull(request.getParameter("locationDescription"));

        request.setAttribute("areas",
                areaDAO.findAllWithFilters(companyFilter, sectorFilter, locationFilter));
        request.setAttribute("sectorFilter", sectorFilter);
        request.setAttribute("locationFilter", locationFilter);
        request.setAttribute("selectedCompanyId", companyFilter);
        if (SessionUtil.isSystemAdmin(logged)) {
            request.setAttribute("companies", companyDAO.findAll());
        }

        forward(request, response, LIST_JSP);
    }

    //exclui area
    private void deleteArea(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Area area = id != null ? areaDAO.findById(id) : null;
        if (area == null || !canAccess(logged, area)) {
            redirectError(request, response, LIST_PATH, "Area nao encontrada.");
            return;
        }

        try {
            if (!areaDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Area nao encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Area excluida com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir area", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a esta area."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva area
    private void saveArea(HttpServletRequest request, HttpServletResponse response, User logged, boolean update)
            throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        if (update && id == null) {
            redirectError(request, response, LIST_PATH, "Area nao encontrada.");
            return;
        }

        Area existing = update ? areaDAO.findById(id) : null;
        if (update && (existing == null || !canAccess(logged, existing))) {
            redirectError(request, response, LIST_PATH, "Area nao encontrada.");
            return;
        }

        Integer companyId = update
                ? existing.getCompany().getId()
                : (SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("companyId"))
                : SessionUtil.companyId(logged));

        Company company = update ? existing.getCompany() : companyId != null ? companyDAO.findById(companyId) : null;
        if (company == null) {
            showForm(request, response, logged, new Area(), "Selecione uma empresa valida.");
            return;
        }

        Area area = new Area();
        area.setId(update ? id : null);
        area.setCompany(company);
        area.setSectorName(Validator.trimToNull(request.getParameter("sectorName")));
        area.setLocationDescription(Validator.trimToNull(request.getParameter("locationDescription")));

        String validationError = validateArea(area);
        if (validationError != null) {
            showForm(request, response, logged, area, validationError);
            return;
        }

        try {
            if (update) {
                if (!areaDAO.update(area)) {
                    redirectError(request, response, LIST_PATH, "Area nao encontrada.");
                    return;
                }
                redirectSuccess(request, response, LIST_PATH, "Area atualizada com sucesso.");
            } else {
                areaDAO.insert(area);
                redirectSuccess(request, response, LIST_PATH, "Area cadastrada com sucesso.");
            }
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar area", e);
            redirectError(request, response, LIST_PATH, "Erro inesperado, tente novamente.");
        }
    }

    //valida area
    private String validateArea(Area area) {
        if (area.getSectorName() == null) {
            return "Preencha todos os campos obrigatorios.";
        }
        if (!Validator.fits(area.getSectorName(), Validator.NAME_MAX)) {
            return "Nome do setor excede o limite de caracteres.";
        }
        if (!Validator.fits(area.getLocationDescription(), Validator.TEXT_MAX)) {
            return "Descricao do local excede o limite de caracteres.";
        }
        return null;
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          User logged, Area area, String error)
            throws ServletException, IOException {
        prepareForm(request, logged, area.getCompany());
        request.setAttribute("error", error);
        request.setAttribute("area", area);
        forward(request, response, FORM_JSP);
    }

    //prepara os dados do formulario
    private void prepareForm(HttpServletRequest request, User logged, Company selectedCompany) {
        if (SessionUtil.isSystemAdmin(logged)) {
            request.setAttribute("companies", companyDAO.findAll());
        } else {
            Company company = companyDAO.findById(SessionUtil.companyId(logged));
            request.setAttribute("companies", company != null ? List.of(company) : Collections.emptyList());
        }

        if (selectedCompany != null) {
            request.setAttribute("selectedCompanyId", selectedCompany.getId());
        }
    }

    //verifica se o usuario pode acessar o registro
    private boolean canAccess(User logged, Area area) {
        return SessionUtil.isSystemAdmin(logged)
                || (area.getCompany() != null
                && SessionUtil.companyId(logged) != null
                && SessionUtil.companyId(logged).equals(area.getCompany().getId()));
    }
}
