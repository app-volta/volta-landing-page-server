package br.com.volta.controller;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.CooperativeDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.RoleDAO;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.Company;
import br.com.volta.models.Cooperative;
import br.com.volta.models.Role;
import br.com.volta.models.User;
import br.com.volta.util.AdminAccount;
import br.com.volta.util.PasswordUtil;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//usuarios: admin global gerencia todos; admin de empresa gerencia os da propria empresa
@WebServlet("/usuario")
public class UserController extends BaseController {

    private static final String LIST_PATH = "/usuario?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/usuario/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/usuario/formulario.jsp";
    private static final Set<String> ALLOWED_POSITIONS = Set.of("ADMIN", "MANAGER", "EMPLOYEE");

    private final UserDAO userDAO = new UserDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final CooperativeDAO cooperativeDAO = new CooperativeDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (!SessionUtil.canManage(logged)) {
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
                deleteUser(request, response, logged);
            } else if ("insert".equals(action)) {
                saveUser(request, response, logged, false);
            } else if ("update".equals(action)) {
                saveUser(request, response, logged, true);
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

        Integer id = Validator.parseId(request.getParameter("id"));
        User user = id != null ? userDAO.findById(id) : null;
        if (user == null) {
            redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
            return;
        }
        if (!SessionUtil.canManageUser(logged, user)) {
            SessionUtil.forbidden(response);
            return;
        }

        //a senha nunca precisa chegar ao formulario
        user.setPassword(null);
        loadFormData(request, logged, user);
        request.setAttribute("user", user);
        forward(request, response, FORM_JSP);
    }

    //exibe o formulario de cadastro
    private void showNewForm(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {
        loadFormData(request, logged, null);
        request.setAttribute("user", new User());
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        String nameFilter = Validator.trimToNull(request.getParameter("name"));
        String emailFilter = Validator.trimToNull(request.getParameter("email"));
        Integer roleFilter = Validator.parseId(request.getParameter("roleId"));
        Integer companyFilter;
        Integer cooperativeFilter;

        if (SessionUtil.isSystemAdmin(logged)) {
            companyFilter = Validator.parseId(request.getParameter("companyId"));
            cooperativeFilter = Validator.parseId(request.getParameter("cooperativeId"));
            request.setAttribute("companies", companyDAO.findAll());
            request.setAttribute("cooperatives", cooperativeDAO.findAll());
        } else {
            companyFilter = SessionUtil.companyId(logged);
            cooperativeFilter = null;
            Company company = companyDAO.findById(companyFilter);
            request.setAttribute("companies", company != null ? List.of(company) : Collections.emptyList());
            request.setAttribute("cooperatives", Collections.emptyList());
        }

        List<User> users = userDAO.findAllWithFilters(
                nameFilter, emailFilter, roleFilter, companyFilter, cooperativeFilter);
        for (User user : users) {
            user.setPassword(null);
        }

        request.setAttribute("users", users);
        request.setAttribute("nameFilter", nameFilter);
        request.setAttribute("emailFilter", emailFilter);
        request.setAttribute("selectedRoleId", roleFilter);
        request.setAttribute("selectedCompanyId", companyFilter);
        request.setAttribute("selectedCooperativeId", cooperativeFilter);
        request.setAttribute("roles", availableRoles(logged));
        forward(request, response, LIST_JSP);
    }

    //exclui user
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        if (id == null) {
            redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
            return;
        }

        if (logged.getId() != null && id.equals(logged.getId())) {
            redirectError(request, response, LIST_PATH, "Voce nao pode excluir a si mesmo.");
            return;
        }

        User target = userDAO.findById(id);
        if (target == null) {
            redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
            return;
        }
        if (AdminAccount.matches(target.getEmail())) {
            redirectError(request, response, LIST_PATH,
                    "A conta unica da equipe nao pode ser excluida.");
            return;
        }
        if (!SessionUtil.canManageUser(logged, target)) {
            SessionUtil.forbidden(response);
            return;
        }

        if (isLastCompanyAdmin(target)) {
            redirectError(request, response, LIST_PATH,
                    "Nao e possivel excluir o ultimo administrador da empresa.");
            return;
        }
        if (isLastSystemAdmin(target)) {
            redirectError(request, response, LIST_PATH,
                    "Nao e possivel excluir o ultimo administrador do sistema.");
            return;
        }

        try {
            if (!userDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Usuario excluido com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir usuario", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a este usuario."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva user
    private void saveUser(HttpServletRequest request, HttpServletResponse response,
                          User logged, boolean update) throws ServletException, IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        User existing = update && id != null ? userDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
            return;
        }
        if (update && !SessionUtil.canManageUser(logged, existing)) {
            SessionUtil.forbidden(response);
            return;
        }

        User user = buildUser(request, update ? id : null);

        //a conta unica de acesso nao pode trocar de email nem perder o cargo global
        if (update && existing != null && AdminAccount.matches(existing.getEmail())) {
            user.setEmail(AdminAccount.email());
            Role systemAdmin = roleDAO.findByType(SessionUtil.ROLE_SYSTEM_ADMIN);
            if (systemAdmin != null) {
                user.setRole(systemAdmin);
            }

            Integer postedCompanyId = Validator.parseId(request.getParameter("companyId"));
            if (postedCompanyId != null) {
                Company company = new Company();
                company.setId(postedCompanyId);
                user.setCompany(company);
            } else if (existing.getCompany() != null) {
                user.setCompany(existing.getCompany());
            }
            user.setCooperative(null);
        }

        String validationError = validateUser(request, user, logged);
        if (validationError != null) {
            showForm(request, response, logged, user, validationError);
            return;
        }

        if (update && existing.getRole() != null) {
            String demotionError = validateDemotion(existing, user.getRole());
            if (demotionError != null) {
                showForm(request, response, logged, user, demotionError);
                return;
            }
        }

        if (update) {
            try {
                if (!userDAO.update(user)) {
                    redirectError(request, response, LIST_PATH, "Usuario nao encontrado.");
                    return;
                }

                if (logged.getId() != null && logged.getId().equals(id)) {
                    refreshSession(request, id);
                    redirect(request, response, SessionUtil.homePath(user));
                    return;
                }
                //rebaixamento/troca de empresa passa a valer no proximo login
                redirectSuccess(request, response, LIST_PATH, "Usuario atualizado com sucesso.");

            } catch (DaoException e) {
                LOG.log(java.util.logging.Level.SEVERE, "Erro ao atualizar usuario", e);
                String message = e.isUniqueViolation()
                        ? "Email ja cadastrado para outro usuario."
                        : "Erro inesperado, tente novamente.";
                redirectError(request, response, LIST_PATH, message);
            }
            return;
        }

        String password = request.getParameter("password");
        if (password == null || password.isBlank()) {
            showForm(request, response, logged, user, "Informe uma senha.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            showForm(request, response, logged, user,
                    "A senha precisa ter no minimo 8 caracteres, uma letra maiuscula e um caractere especial (., @, #, etc).");
            return;
        }

        user.setPassword(PasswordUtil.hash(password));
        try {
            userDAO.insert(user);
            redirectSuccess(request, response, LIST_PATH, "Usuario cadastrado com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao cadastrar usuario", e);
            String message = e.isUniqueViolation()
                    ? "Email ja cadastrado para outro usuario."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //monta user
    private User buildUser(HttpServletRequest request, Integer id) {
        Integer roleId = Validator.parseId(request.getParameter("roleId"));
        String name = Validator.trimToNull(request.getParameter("name"));
        String email = Validator.normalizeEmail(request.getParameter("email"));
        String position = Validator.trimToNull(request.getParameter("position"));
        Integer postedCompanyId = Validator.parseId(request.getParameter("companyId"));
        Integer postedCooperativeId = Validator.parseId(request.getParameter("cooperativeId"));

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPosition(position);

        Role selectedRole = null;
        if (roleId != null) {
            for (Role role : availableRoles(SessionUtil.user(request))) {
                if (role.getId() != null && role.getId().equals(roleId)) {
                    selectedRole = role;
                    break;
                }
            }
        }
        user.setRole(selectedRole);

        if (selectedRole != null && selectedRole.getType() != null) {
            String roleType = selectedRole.getType();
            if (SessionUtil.ROLE_COOPERATIVE.equalsIgnoreCase(roleType)) {
                if (postedCooperativeId != null) {
                    Cooperative cooperative = new Cooperative();
                    cooperative.setId(postedCooperativeId);
                    user.setCooperative(cooperative);
                }
                user.setCompany(null);
            } else {
                //qualquer perfil que nao seja de cooperativa exige uma empresa
                Integer companyId;
                if (SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(roleType)) {
                    companyId = postedCompanyId != null
                            ? postedCompanyId
                            : SessionUtil.companyId(SessionUtil.user(request));
                } else if (SessionUtil.isSystemAdmin(SessionUtil.user(request))) {
                    companyId = postedCompanyId;
                } else {
                    companyId = SessionUtil.companyId(SessionUtil.user(request));
                }
                if (companyId != null) {
                    Company company = new Company();
                    company.setId(companyId);
                    user.setCompany(company);
                }
                user.setCooperative(null);
            }
        }

        return user;
    }

    //valida user
    private String validateUser(HttpServletRequest request, User user, User logged) {
        if (user.getName() == null || user.getEmail() == null || user.getRole() == null) {
            return "Preencha todos os campos obrigatorios.";
        }
        if (!Validator.fits(user.getName(), Validator.NAME_MAX)) {
            return "Nome excede o limite de caracteres.";
        }
        if (!Validator.fits(user.getEmail(), Validator.EMAIL_MAX)) {
            return "Email excede o limite de caracteres.";
        }
        if (!Validator.fits(user.getPosition(), Validator.POSITION_MAX)) {
            return "Cargo excede o limite de caracteres.";
        }
        if (!ALLOWED_POSITIONS.contains(user.getPosition())) {
            return "Selecione um cargo válido: ADMIN, MANAGER ou EMPLOYEE.";
        }
        if (!Validator.isValidEmail(user.getEmail())) {
            return "Email invalido. Use o formato nome@dominio.com.";
        }

        String roleType = user.getRole().getType();
        if (roleType == null) {
            return "Cargo invalido.";
        }

        if (SessionUtil.ROLE_COOPERATIVE.equalsIgnoreCase(roleType)) {
            if (!SessionUtil.isSystemAdmin(logged)) {
                return "Cargo nao permitido.";
            }
            if (user.getCooperative() == null || user.getCooperative().getId() == null) {
                return "Selecione uma cooperativa valida.";
            }
            Cooperative cooperative = cooperativeDAO.findById(user.getCooperative().getId());
            if (cooperative == null) {
                return "Selecione uma cooperativa valida.";
            }
            user.setCooperative(cooperative);
            user.setCompany(null);
        } else {
            //perfis de empresa e perfis personalizados sempre precisam de uma empresa
            if (user.getCompany() == null || user.getCompany().getId() == null) {
                return SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(roleType)
                        ? "Selecione uma empresa de vinculo para o administrador do sistema."
                        : "Selecione uma empresa valida.";
            }
            Company company = companyDAO.findById(user.getCompany().getId());
            if (company == null || !SessionUtil.canManageCompany(logged, company.getId())) {
                return "Selecione uma empresa valida.";
            }
            user.setCompany(company);
            user.setCooperative(null);
        }

        User sameEmail = userDAO.findByEmail(user.getEmail());
        if (sameEmail != null && !Objects.equals(sameEmail.getId(), user.getId())) {
            return "Email ja cadastrado para outro usuario.";
        }
        return null;
    }

    //valida demotion
    private String validateDemotion(User existing, Role newRole) {
        if (newRole == null) {
            return null;
        }
        String oldType = existing.getRole().getType();
        String newType = newRole.getType();

        if (SessionUtil.ROLE_ADMIN.equalsIgnoreCase(oldType)
                && !SessionUtil.ROLE_ADMIN.equalsIgnoreCase(newType)
                && isLastCompanyAdmin(existing)) {
            return "Nao e possivel rebaixar o ultimo administrador da empresa.";
        }
        if (SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(oldType)
                && !SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(newType)
                && isLastSystemAdmin(existing)) {
            return "Nao e possivel rebaixar o ultimo administrador do sistema.";
        }
        return null;
    }

    //verifica se e o ultimo admin da empresa
    private boolean isLastCompanyAdmin(User target) {
        if (target == null || target.getRole() == null
                || !SessionUtil.ROLE_ADMIN.equalsIgnoreCase(target.getRole().getType())
                || target.getCompany() == null) {
            return false;
        }

        int admins = 0;
        for (User user : userDAO.findByCompany(target.getCompany().getId())) {
            if (user.getRole() != null
                    && SessionUtil.ROLE_ADMIN.equalsIgnoreCase(user.getRole().getType())
                    && !Objects.equals(user.getId(), target.getId())) {
                admins++;
            }
        }
        return admins == 0;
    }

    //verifica se e o ultimo admin do sistema
    private boolean isLastSystemAdmin(User target) {
        if (target == null || target.getRole() == null
                || !SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(target.getRole().getType())) {
            return false;
        }

        int admins = 0;
        for (User user : userDAO.findAll()) {
            if (user.getRole() != null
                    && SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(user.getRole().getType())
                    && !Objects.equals(user.getId(), target.getId())) {
                admins++;
            }
        }
        return admins == 0;
    }

    //recarrega o usuario logado na sessao depois de uma auto-edicao
    private void refreshSession(HttpServletRequest request, Integer id) {
        User updated = userDAO.findById(id);
        if (updated == null) {
            return;
        }

        updated.setPassword(null);
        if (updated.getRole() != null && updated.getRole().getId() != null) {
            updated.setRole(roleDAO.findById(updated.getRole().getId()));
        }
        if (updated.getCompany() != null && updated.getCompany().getId() != null) {
            updated.setCompany(companyDAO.findById(updated.getCompany().getId()));
        }
        if (updated.getCooperative() != null && updated.getCooperative().getId() != null) {
            updated.setCooperative(cooperativeDAO.findById(updated.getCooperative().getId()));
        }

        request.getSession().setAttribute(SessionUtil.ATTR_USER, updated);
    }

    //carrega form data
    private void loadFormData(HttpServletRequest request, User logged, User selectedUser) {
        if (SessionUtil.isSystemAdmin(logged)) {
            request.setAttribute("companies", companyDAO.findAll());
            request.setAttribute("cooperatives", cooperativeDAO.findAll());
        } else {
            Company company = companyDAO.findById(SessionUtil.companyId(logged));
            request.setAttribute("companies", company != null ? List.of(company) : Collections.emptyList());
            request.setAttribute("cooperatives", Collections.emptyList());
        }

        request.setAttribute("roles", availableRoles(logged));
        if (selectedUser != null && selectedUser.getCompany() != null) {
            request.setAttribute("selectedCompanyId", selectedUser.getCompany().getId());
        }
        if (selectedUser != null && selectedUser.getCooperative() != null) {
            request.setAttribute("selectedCooperativeId", selectedUser.getCooperative().getId());
        }
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          User logged, User user, String error)
            throws ServletException, IOException {
        loadFormData(request, logged, user);
        request.setAttribute("error", error);
        request.setAttribute("user", user);
        forward(request, response, FORM_JSP);
    }

    //lista os perfis que o usuario pode atribuir
    private List<Role> availableRoles(User logged) {
        List<Role> roles = new ArrayList<>();
        for (Role role : roleDAO.findAll()) {
            if (SessionUtil.isSystemAdmin(logged)) {
                roles.add(role);
            } else if (!SessionUtil.ROLE_SYSTEM_ADMIN.equalsIgnoreCase(role.getType())
                    && !SessionUtil.ROLE_COOPERATIVE.equalsIgnoreCase(role.getType())) {
                //o admin de empresa pode usar cargos personalizados, mas nao os globais
                roles.add(role);
            }
        }
        return roles;
    }
}
