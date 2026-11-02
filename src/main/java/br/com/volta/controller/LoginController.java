package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.User;
import br.com.volta.util.AuthSession;
import br.com.volta.util.PasswordUtil;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;

//login publico e login separado para o painel administrativo
@WebServlet(urlPatterns = {"/login", "/acesso-restrito"})
public class LoginController extends BaseController {

    private static final String LOGIN_JSP = "/WEB-INF/views/login/login.jsp";
    private static final String RESTRICTED_LOGIN_JSP = "/WEB-INF/views/login/acesso-restrito.jsp";
    private static final String INVALID_CREDENTIALS = "Email ou senha invalidos";

    private final UserDAO userDAO = new UserDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User logged = SessionUtil.user(request);
        if (logged != null) {
            redirect(request, response, SessionUtil.homePath(logged));
            return;
        }
        forward(request, response, loginPage(request));
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = Validator.normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");
        boolean restrictedAccess = isRestrictedAccess(request);
        String loginPage = loginPage(request);

        if (email == null || password == null || password.isBlank()) {
            request.setAttribute("error", "Preencha email e senha");
            forward(request, response, loginPage);
            return;
        }

        try {
            User user = userDAO.findByEmail(email);
            boolean valid = authenticate(password, user);

            String homePath = valid ? SessionUtil.homePath(user) : null;
            if (!valid || "/login".equals(homePath)
                    || (restrictedAccess && !SessionUtil.isSystemAdmin(user))) {
                request.setAttribute("error", INVALID_CREDENTIALS);
                forward(request, response, loginPage);
                return;
            }

            AuthSession.start(request, response, user, homePath);

        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Falha de banco no login", e);
            request.setAttribute("error", "Servico indisponivel, tente novamente.");
            forward(request, response, loginPage);
        }
    }

    //valida as credenciais do usuario
    private boolean authenticate(String password, User user) {
        if (user == null || user.getPassword() == null) {
            return false;
        }
        return PasswordUtil.verify(password, user.getPassword());
    }

    private boolean isRestrictedAccess(HttpServletRequest request) {
        return "/acesso-restrito".equals(request.getServletPath());
    }

    private String loginPage(HttpServletRequest request) {
        return isRestrictedAccess(request) ? RESTRICTED_LOGIN_JSP : LOGIN_JSP;
    }
}
