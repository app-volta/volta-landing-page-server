package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.dao.WasteTypeDAO;
import br.com.volta.models.RiskLevel;
import br.com.volta.models.WasteType;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//tipos de residuo aceitos pelas cooperativas e usados nas ocorrencias
@WebServlet("/tipo-residuo")
public class WasteTypeController extends BaseController {

    private static final String LIST_PATH = "/tipo-residuo?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/tipo-residuo/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/tipo-residuo/formulario.jsp";

    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("edit".equals(action)) {
                showEditForm(request, response);
            } else if ("new".equals(action)) {
                prepareForm(request);
                request.setAttribute("wasteType", new WasteType());
                forward(request, response, FORM_JSP);
            } else {
                showList(request, response);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteWasteType(request, response);
            } else if ("insert".equals(action)) {
                saveWasteType(request, response, false);
            } else if ("update".equals(action)) {
                saveWasteType(request, response, true);
            } else {
                redirect(request, response, LIST_PATH);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //exibe o formulario de edicao
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        WasteType wasteType = id != null ? wasteTypeDAO.findById(id) : null;
        if (wasteType == null) {
            redirectError(request, response, LIST_PATH, "Tipo de residuo nao encontrado.");
            return;
        }

        prepareForm(request);
        request.setAttribute("wasteType", wasteType);
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String categoryFilter = Validator.trimToNull(request.getParameter("category"));
        String descriptionFilter = Validator.trimToNull(request.getParameter("description"));
        RiskLevel riskFilter = Validator.parseEnum(RiskLevel.class, request.getParameter("defaultRisk"));
        List<WasteType> wasteTypes = wasteTypeDAO.findAllWithFilters(
                categoryFilter, riskFilter, descriptionFilter);

        request.setAttribute("wasteTypes", wasteTypes);
        request.setAttribute("categoryFilter", categoryFilter);
        request.setAttribute("descriptionFilter", descriptionFilter);
        request.setAttribute("selectedRisk", riskFilter);
        request.setAttribute("riskLevels", RiskLevel.values());
        forward(request, response, LIST_JSP);
    }

    //exclui waste type
    private void deleteWasteType(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        if (id == null) {
            redirectError(request, response, LIST_PATH, "Tipo de residuo nao encontrado.");
            return;
        }

        try {
            if (!wasteTypeDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Tipo de residuo nao encontrado.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Tipo de residuo excluido com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir tipo de residuo", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a este tipo de residuo."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva waste type
    private void saveWasteType(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {

        Integer id = update ? Validator.parseId(request.getParameter("id")) : null;
        if (update && (id == null || wasteTypeDAO.findById(id) == null)) {
            redirectError(request, response, LIST_PATH, "Tipo de residuo nao encontrado.");
            return;
        }

        WasteType wasteType = new WasteType();
        wasteType.setId(id);
        wasteType.setCategory(Validator.trimToNull(request.getParameter("category")));
        wasteType.setDescription(Validator.trimToNull(request.getParameter("description")));
        wasteType.setDefaultRisk(Validator.parseEnum(RiskLevel.class, request.getParameter("defaultRisk")));

        String validationError = validateWasteType(request, wasteType);
        if (validationError != null) {
            showForm(request, response, wasteType, validationError);
            return;
        }

        try {
            if (update) {
                if (!wasteTypeDAO.update(wasteType)) {
                    redirectError(request, response, LIST_PATH, "Tipo de residuo nao encontrado.");
                    return;
                }
                redirectSuccess(request, response, LIST_PATH, "Tipo de residuo atualizado com sucesso.");
                return;
            }

            wasteTypeDAO.insert(wasteType);
            redirectSuccess(request, response, LIST_PATH, "Tipo de residuo cadastrado com sucesso.");

        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar tipo de residuo", e);
            String message = e.isUniqueViolation()
                    ? "Categoria ja cadastrada."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //valida waste type
    private String validateWasteType(HttpServletRequest request, WasteType wasteType) {
        if (wasteType.getCategory() == null) {
            return "Informe a categoria do residuo.";
        }
        if (!Validator.fits(wasteType.getCategory(), Validator.CATEGORY_MAX)) {
            return "Categoria excede o limite de caracteres.";
        }
        if (!Validator.fits(wasteType.getDescription(), Validator.TEXT_MAX)) {
            return "Descricao excede o limite de caracteres.";
        }

        WasteType sameCategory = wasteTypeDAO.findByCategory(wasteType.getCategory());
        if (sameCategory != null && !Objects.equals(sameCategory.getId(), wasteType.getId())) {
            return "Categoria ja cadastrada.";
        }

        String riskValue = Validator.trimToNull(request.getParameter("defaultRisk"));
        if (riskValue == null) {
            return "Informe o risco padrao.";
        }
        if (wasteType.getDefaultRisk() == null) {
            return "Risco padrao invalido.";
        }
        return null;
    }

    //prepara os dados do formulario
    private void prepareForm(HttpServletRequest request) {
        request.setAttribute("riskLevels", RiskLevel.values());
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          WasteType wasteType, String error) throws ServletException, IOException {
        prepareForm(request);
        request.setAttribute("error", error);
        request.setAttribute("wasteType", wasteType);
        forward(request, response, FORM_JSP);
    }
}
