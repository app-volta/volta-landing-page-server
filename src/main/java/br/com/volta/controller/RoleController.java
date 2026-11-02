package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.dao.RoleDAO;
import br.com.volta.models.Role;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

//perfis de acesso usados pelas regras de autorizacao do sistema
@WebServlet("/perfil")
public class RoleController extends BaseController {

    private static final String LIST_PATH = "/perfil?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/perfil/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/perfil/formulario.jsp";
    //perfis usados pelo proprio controle de acesso nao podem ser renomeados nem excluidos
    private static final Set<String> SYSTEM_ROLES = Set.of(
            SessionUtil.ROLE_SYSTEM_ADMIN,
            SessionUtil.ROLE_ADMIN,
            SessionUtil.ROLE_COMPANY_USER,
            SessionUtil.ROLE_COOPERATIVE);

    private final RoleDAO roleDAO = new RoleDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireSystemAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("edit".equals(action)) {
                showEditForm(request, response);
            } else if ("new".equals(action)) {
                request.setAttribute("role", new Role());
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

        if (!requireSystemAdmin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteRole(request, response);
            } else if ("insert".equals(action)) {
                saveRole(request, response, false);
            } else if ("update".equals(action)) {
                saveRole(request, response, true);
            } else {
                redirect(request, response, LIST_PATH);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //retorna false depois de enviar 403 para interromper o fluxo do servlet
    private boolean requireSystemAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!SessionUtil.isSystemAdmin(request)) {
            SessionUtil.forbidden(response);
            return false;
        }
        return true;
    }

    //exibe o formulario de edicao
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        Role role = id != null ? roleDAO.findById(id) : null;
        if (role == null) {
            redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
            return;
        }

        request.setAttribute("role", role);
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String typeFilter = Validator.trimToNull(request.getParameter("type"));
        String withUsersValue = Validator.trimToNull(request.getParameter("withUsers"));
        Boolean withUsers = withUsersValue == null ? null : Boolean.valueOf(withUsersValue);
        List<Role> roles = roleDAO.findAllWithFilters(typeFilter, withUsers);

        request.setAttribute("roles", roles);
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("selectedWithUsers", withUsers);
        forward(request, response, LIST_JSP);
    }

    //exclui role
    private void deleteRole(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        if (id == null) {
            redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
            return;
        }

        Role existing = roleDAO.findById(id);
        if (existing == null) {
            redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
            return;
        }
        if (isSystemRole(existing.getType())) {
            redirectError(request, response, LIST_PATH,
                    "Perfis do sistema nao podem ser excluidos.");
            return;
        }

        try {
            if (!roleDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Perfil excluido com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir perfil", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem usuarios vinculados a este perfil."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva role
    private void saveRole(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {

        Integer id = update ? Validator.parseId(request.getParameter("id")) : null;
        if (update && id == null) {
            redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
            return;
        }
        Role existing = update ? roleDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
            return;
        }

        Role role = new Role();
        role.setId(id);
        String type = Validator.trimToNull(request.getParameter("type"));
        role.setType(type == null ? null : type.toUpperCase(Locale.ROOT));

        String validationError = validateRole(role);
        if (validationError != null) {
            showForm(request, response, role, validationError);
            return;
        }

        if (update && isSystemRole(existing.getType())
                && !existing.getType().equalsIgnoreCase(role.getType())) {
            showForm(request, response, role, "Perfis do sistema nao podem ser renomeados.");
            return;
        }

        try {
            if (update) {
                if (!roleDAO.update(role)) {
                    redirectError(request, response, LIST_PATH, "Perfil nao encontrado.");
                    return;
                }
                redirectSuccess(request, response, LIST_PATH, "Perfil atualizado com sucesso.");
                return;
            }

            roleDAO.insert(role.getType());
            redirectSuccess(request, response, LIST_PATH, "Perfil cadastrado com sucesso.");

        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar perfil", e);
            String message = e.isUniqueViolation()
                    ? "Perfil ja cadastrado."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //valida role
    private String validateRole(Role role) {
        if (role.getType() == null) {
            return "Informe o nome do perfil.";
        }
        if (!Validator.fits(role.getType(), Validator.ROLE_MAX)) {
            return "Nome do perfil excede o limite de caracteres.";
        }

        for (Role existing : roleDAO.findAll()) {
            if (existing.getType() != null
                    && existing.getType().equalsIgnoreCase(role.getType())
                    && !Objects.equals(existing.getId(), role.getId())) {
                return "Perfil ja cadastrado.";
            }
        }
        return null;
    }

    //verifica se o perfil e do sistema
    private boolean isSystemRole(String type) {
        return type != null && SYSTEM_ROLES.contains(type.toUpperCase(Locale.ROOT));
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Role role, String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("role", role);
        forward(request, response, FORM_JSP);
    }
}
