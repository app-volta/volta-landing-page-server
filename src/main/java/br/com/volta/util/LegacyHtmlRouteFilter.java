package br.com.volta.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Encaminha URLs das antigas telas HTML para os servlets com sessao e autorizacao.
 * Mantem a landing publica em /index.html.
 */
@WebFilter("*.html")
public class LegacyHtmlRouteFilter implements Filter {

    private static final Map<String, String> ROUTES = Map.of(
            "area", "/area",
            "empresa", "/empresa",
            "cooperativa", "/cooperativa",
            "usuario", "/usuario",
            "funcionario", "/usuario",
            "ocorrencia", "/ocorrencia",
            "coleta", "/coleta",
            "unidade", "/area",
            "perfil", "/perfil",
            "tipo-residuo", "/tipo-residuo"
    );

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                         jakarta.servlet.ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();

        if ("/index.html".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String target = resolveTarget(path, request.getParameter("id"));
        if (SessionUtil.user(request) == null && isAdminOnly(target)) {
            target = "/acesso-restrito";
        }
        response.sendRedirect(request.getContextPath() + target);
    }

    private boolean isAdminOnly(String target) {
        return target.startsWith("/admin")
                || target.startsWith("/cooperativa")
                || target.startsWith("/perfil")
                || target.startsWith("/tipo-residuo")
                || target.startsWith("/empresa?action=new");
    }

    private String resolveTarget(String path, String id) {
        if ("/login.html".equals(path)) return "/login";
        if ("/login-crud.html".equals(path) || "/area-restrita.html".equals(path)) {
            return "/acesso-restrito";
        }
        if ("/cadastrar.html".equals(path)) return "/cadastro";
        if ("/esqueci-senha.html".equals(path)) return "/esqueci-senha";
        if ("/painel.html".equals(path)) return "/admin";
        if ("/minha-empresa/painel.html".equals(path)) return "/empresa?action=dashboard";

        String[] parts = path.substring(1).split("/");
        if (parts.length == 0) return "/login";

        String resource;
        String page;
        if ("minha-empresa".equals(parts[0]) && parts.length >= 3) {
            resource = parts[1];
            page = parts[2];
        } else if (parts.length >= 2) {
            resource = parts[0];
            page = parts[1];
        } else {
            return "/login";
        }

        String route = ROUTES.get(resource);
        if (route == null) return "/login";
        if (page.startsWith("lista")) return route + "?action=list";
        if (page.startsWith("formulario")) {
            if (id != null && id.matches("\\d+")) return route + "?action=edit&id=" + id;
            return route + "?action=new";
        }
        return "/login";
    }
}
