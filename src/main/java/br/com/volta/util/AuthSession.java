package br.com.volta.util;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.CooperativeDAO;
import br.com.volta.dao.RoleDAO;
import br.com.volta.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Centraliza a criacao da sessao autenticada.
 *
 * A area restrita usa este mesmo mecanismo para a conta unica da equipe.
 */
public final class AuthSession {

    private static final int SESSION_MINUTES = 30;

    //construtor
    private AuthSession() {
        //evita instanciacao da classe utilitaria
    }

    /**
     * Recarrega os vinculos do usuario, invalida a sessao antiga e cria a nova.
     * O redirecionamento fica com o chamador.
     */
    public static void start(HttpServletRequest request, HttpServletResponse response,
                             User user, String redirectPath) throws IOException {

        if (user == null) {
            throw new IllegalArgumentException("Usuario autenticado nao informado.");
        }

        //a senha nunca deve permanecer na sessao
        user.setPassword(null);
        reloadRelations(user);

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(SessionUtil.ATTR_USER, user);
        newSession.setMaxInactiveInterval(SESSION_MINUTES * 60);

        response.sendRedirect(request.getContextPath() + redirectPath);
    }

    //completa os objetos usados pelas JSPs e pelas verificacoes de permissao
    private static void reloadRelations(User user) {
        RoleDAO roleDAO = new RoleDAO();
        CompanyDAO companyDAO = new CompanyDAO();
        CooperativeDAO cooperativeDAO = new CooperativeDAO();

        if (user.getRole() != null && user.getRole().getId() != null) {
            user.setRole(roleDAO.findById(user.getRole().getId()));
        }
        if (user.getCompany() != null && user.getCompany().getId() != null) {
            user.setCompany(companyDAO.findById(user.getCompany().getId()));
        }
        if (user.getCooperative() != null && user.getCooperative().getId() != null) {
            user.setCooperative(cooperativeDAO.findById(user.getCooperative().getId()));
        }
    }
}
