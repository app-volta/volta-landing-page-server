package br.com.volta.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;

// Evita que o navegador continue exibindo HTML/CSS/JS antigos durante o desenvolvimento.
public class NoCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // sem configuracao
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpResponse) {
            if (request instanceof HttpServletRequest httpRequest
                    && isTextResource(httpRequest.getRequestURI())) {
                httpResponse.setCharacterEncoding("UTF-8");
            }
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
        }

        chain.doFilter(request, response);
    }

    //aplica UTF-8 apenas em respostas textuais, sem interferir em imagens e fontes
    private boolean isTextResource(String uri) {
        String path = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        String file = path.substring(path.lastIndexOf('/') + 1);
        if (!file.contains(".")) {
            return true;
        }
        return file.endsWith(".html")
                || file.endsWith(".jsp")
                || file.endsWith(".css")
                || file.endsWith(".js")
                || file.endsWith(".json")
                || file.endsWith(".txt")
                || file.endsWith(".xml")
                || file.endsWith(".svg");
    }

    @Override
    public void destroy() {
        // sem recursos para liberar
    }
}
