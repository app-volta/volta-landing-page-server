package br.com.volta.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

//guarda mensagens de uma requisicao para a proxima, evitando repetir o post
public final class Flash {

    private static final String SUCCESS = "flashSuccess";
    private static final String ERROR = "flashError";
    private static final String WARNING = "flashWarning";

    //construtor
    private Flash() {
        //evita instanciacao da classe utilitaria
    }

    //executa a operacao success
    public static void success(HttpServletRequest request, String message) {
        put(request, SUCCESS, message);
    }

    //executa a operacao error
    public static void error(HttpServletRequest request, String message) {
        put(request, ERROR, message);
    }

    //executa a operacao warning
    public static void warning(HttpServletRequest request, String message) {
        put(request, WARNING, message);
    }

    //transporta as mensagens da sessao para a requisicao e limpa a sessao
    public static void expose(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        move(session, request, SUCCESS);
        move(session, request, ERROR);
        move(session, request, WARNING);
    }

    //executa a operacao put
    private static void put(HttpServletRequest request, String key, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        request.getSession(true).setAttribute(key, message);
    }

    //executa a operacao move
    private static void move(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            session.removeAttribute(key);
            request.setAttribute(key, value);
        }
    }
}
