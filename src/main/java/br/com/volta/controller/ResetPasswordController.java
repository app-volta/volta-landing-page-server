package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.dao.UserDAO;
import br.com.volta.util.PasswordResetService;
import br.com.volta.util.PasswordUtil;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;

@WebServlet("/redefinir-senha")
public class ResetPasswordController extends BaseController {

    private static final String FORM_JSP = "/WEB-INF/views/esqueci-senha/redefinir-senha.jsp";

    private final UserDAO userDAO = new UserDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = Validator.trimToNull(request.getParameter("token"));

        if (!PasswordResetService.isValid(token)) {
            request.setAttribute("error", "Link invalido ou expirado.");
        } else {
            request.setAttribute("token", token);
        }

        forward(request, response, FORM_JSP);
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = Validator.trimToNull(request.getParameter("token"));
        Integer userId = PasswordResetService.validate(token);

        if (userId == null) {
            request.setAttribute("error", "Link invalido ou expirado.");
            forward(request, response, FORM_JSP);
            return;
        }

        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            showError(request, response, token, "Preencha a nova senha e a confirmacao.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(request, response, token, "As senhas nao conferem.");
            return;
        }
        if (!Validator.isValidPassword(password)) {
            showError(request, response, token,
                    "A senha deve ter entre 8 e 72 caracteres, com maiuscula e caractere especial.");
            return;
        }

            //consome antes de gravar para impedir que o mesmo link seja usado duas vezes
            if (PasswordResetService.consume(token) == null) {
                request.setAttribute("error", "Link invalido ou expirado.");
                forward(request, response, FORM_JSP);
                return;
            }

            try {
                if (!userDAO.updatePassword(userId, PasswordUtil.hash(password))) {
                    showError(request, response, null,
                            "Nao foi possivel alterar a senha. Solicite um novo link.");
                    return;
                }

                redirect(request, response, "/login?reset=1");

        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Erro de banco ao redefinir senha", e);
            showError(request, response, null,
                    "Nao foi possivel alterar a senha. Solicite um novo link.");
        }
    }

    //exibe error
    private void showError(HttpServletRequest request, HttpServletResponse response,
                           String token, String error) throws ServletException, IOException {
        if (token != null) {
            request.setAttribute("token", token);
        }
        request.setAttribute("error", error);
        forward(request, response, FORM_JSP);
    }
}
