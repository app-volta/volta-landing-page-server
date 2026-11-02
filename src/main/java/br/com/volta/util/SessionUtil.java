package br.com.volta.util;

import br.com.volta.models.Collection;
import br.com.volta.models.Incident;
import br.com.volta.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

//centraliza o acesso a sessao e verificacoes de perfil do usuario logado
public final class SessionUtil {

    public static final String ATTR_USER = "loggedUser";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ROLE_COMPANY_USER = "COMPANY_USER";
    public static final String ROLE_COOPERATIVE = "COOPERATIVE";

    //construtor
    private SessionUtil() {
        //evita instanciacao da classe utilitaria
    }

    //recupera o usuario logado na sessao atual sem criar nova sessao
    public static User user(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_USER);
        return (value instanceof User user) ? user : null;
    }

    //confere se o usuario pertence a uma empresa
    public static boolean isCompany(User user) {
        return user != null && user.getCompany() != null && user.getCompany().getId() != null;
    }

    //verifica se o usuario pertence a uma empresa
    public static boolean isCompany(HttpServletRequest request) {
        return isCompany(user(request));
    }

    //confere se o usuario pertence a uma cooperativa
    public static boolean isCooperative(User user) {
        return user != null && user.getCooperative() != null && user.getCooperative().getId() != null;
    }

    //verifica se o usuario pertence a uma cooperativa
    public static boolean isCooperative(HttpServletRequest request) {
        return isCooperative(user(request));
    }

    //admin global identificado pelo cargo SYSTEM_ADMIN
    public static boolean isSystemAdmin(User user) {
        return hasRole(user, ROLE_SYSTEM_ADMIN);
    }

    //verifica se o usuario e administrador do sistema
    public static boolean isSystemAdmin(HttpServletRequest request) {
        return isSystemAdmin(user(request));
    }

    //admin de empresa: precisa estar vinculado a uma empresa e ter cargo ADMIN
    public static boolean isCompanyAdmin(User user) {
        return isCompany(user) && hasRole(user, ROLE_ADMIN);
    }

    //verifica se o usuario e administrador de empresa
    public static boolean isCompanyAdmin(HttpServletRequest request) {
        return isCompanyAdmin(user(request));
    }

    //perfis que podem gravar dados de empresa
    public static boolean canManage(User user) {
        return isSystemAdmin(user) || isCompanyAdmin(user);
    }

    //verifica se pode manage
    public static boolean canManage(HttpServletRequest request) {
        return canManage(user(request));
    }

    //verifica se o usuario pode gerenciar a empresa
    public static boolean canManageCompany(User user, Integer companyId) {
        if (isSystemAdmin(user)) {
            return true;
        }
        return isCompanyAdmin(user) && companyId != null && companyId.equals(companyId(user));
    }

    //verifica se o usuario pode gerenciar a ocorrencia
    public static boolean canManageIncident(User user, Incident incident) {
        if (incident == null) {
            return false;
        }
        if (isSystemAdmin(user)) {
            return true;
        }
        return isCompanyAdmin(user)
                && incident.getCompany() != null
                && companyId(user) != null
                && companyId(user).equals(incident.getCompany().getId());
    }

    //verifica se o usuario pode usar a ocorrencia
    public static boolean canUseIncident(User user, Incident incident) {
        return canManageIncident(user, incident);
    }

    //verifica se o usuario pode gerenciar a coleta
    public static boolean canManageCollection(User user, Collection collection) {
        if (collection == null) {
            return false;
        }
        if (isSystemAdmin(user)) {
            return true;
        }
        return isCompanyAdmin(user)
                && collection.getIncident() != null
                && collection.getIncident().getCompany() != null
                && companyId(user) != null
                && companyId(user).equals(collection.getIncident().getCompany().getId());
    }

    //a cooperativa pode alterar somente status e data das coletas dela
    public static boolean canOperateCollection(User user, Collection collection) {
        if (canManageCollection(user, collection)) {
            return true;
        }
        return isCooperative(user)
                && collection != null
                && collection.getCooperative() != null
                && cooperativeId(user) != null
                && cooperativeId(user).equals(collection.getCooperative().getId());
    }

    //verifica se o usuario pode gerenciar o usuario alvo
    public static boolean canManageUser(User user, User target) {
        if (target == null) {
            return false;
        }
        if (isSystemAdmin(user)) {
            return true;
        }
        //um admin de empresa nunca pode editar ou excluir um admin global
        if (isSystemAdmin(target)) {
            return false;
        }
        return isCompanyAdmin(user)
                && target.getCompany() != null
                && companyId(user) != null
                && companyId(user).equals(target.getCompany().getId());
    }

    //rota inicial de cada perfil
    public static String homePath(User user) {
        if (isSystemAdmin(user)) {
            return "/admin";
        }
        if (isCooperative(user)) {
            return "/coleta?action=list";
        }
        if (isCompany(user)) {
            return "/empresa?action=dashboard";
        }
        return "/login";
    }

    //devolve a rota inicial do perfil
    public static String homePath(HttpServletRequest request) {
        return homePath(user(request));
    }

    //verifica se possui role
    private static boolean hasRole(User user, String roleType) {
        return user != null
                && user.getRole() != null
                && roleType.equalsIgnoreCase(user.getRole().getType());
    }

    //retorna o id da empresa vinculada ao usuario ou null
    public static Integer companyId(User user) {
        return isCompany(user) ? user.getCompany().getId() : null;
    }

    //devolve o id da empresa vinculada
    public static Integer companyId(HttpServletRequest request) {
        return companyId(user(request));
    }

    //retorna o id da cooperativa vinculada ao usuario ou null
    public static Integer cooperativeId(User user) {
        return isCooperative(user) ? user.getCooperative().getId() : null;
    }

    //devolve o id da cooperativa vinculada
    public static Integer cooperativeId(HttpServletRequest request) {
        return cooperativeId(user(request));
    }

    //devolve codigo 403 de acesso proibido
    public static void forbidden(HttpServletResponse response) throws IOException {
        if (response != null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
