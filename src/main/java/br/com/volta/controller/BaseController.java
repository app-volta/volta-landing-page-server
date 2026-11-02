package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.models.AdminSection;
import br.com.volta.models.User;
import br.com.volta.util.Flash;
import br.com.volta.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//concentra redirecionamentos e mensagens repetidos pelos controllers
public abstract class BaseController extends HttpServlet {

    protected static final Logger LOG = Logger.getLogger(BaseController.class.getName());

    //atributos usados pelas JSPs (menu, permissoes de exibicao e mensagens)
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = SessionUtil.user(request);
        if (user == null && !isPublicEndpoint(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("systemAdmin", SessionUtil.isSystemAdmin(user));
        request.setAttribute("cooperativeUser", SessionUtil.isCooperative(user));
        request.setAttribute("companyUser", SessionUtil.isCompany(user) && !SessionUtil.isSystemAdmin(user));
        request.setAttribute("homePath", SessionUtil.homePath(user));
        request.setAttribute("canManage", SessionUtil.canManage(user));
        request.setAttribute("adminSections", AdminSection.values());

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            Flash.expose(request);
        }

        super.service(request, response);
    }

    //mantem abertas apenas as rotas necessarias antes da autenticacao
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/login".equals(path)
                || "/acesso-restrito".equals(path)
                || "/cadastro".equals(path)
                || "/esqueci-senha".equals(path)
                || "/redefinir-senha".equals(path);
    }

    //redireciona com mensagem de sucesso
    protected void redirectSuccess(HttpServletRequest request, HttpServletResponse response,
                                   String path, String message) throws IOException {
        Flash.success(request, message);
        redirect(request, response, path);
    }

    //redireciona com mensagem de erro
    protected void redirectError(HttpServletRequest request, HttpServletResponse response,
                                 String path, String message) throws IOException {
        Flash.error(request, message);
        redirect(request, response, path);
    }

    //executa a operacao redirect
    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    //executa a operacao forward
    protected void forward(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    //trata o erro de banco e devolve a mensagem
    protected void handleDatabaseError(HttpServletRequest request, HttpServletResponse response,
                                       String path, DaoException exception) throws IOException {
        LOG.log(Level.SEVERE, "Erro de banco na operacao", exception);
        redirectError(request, response, path, "Erro inesperado, tente novamente.");
    }
}
