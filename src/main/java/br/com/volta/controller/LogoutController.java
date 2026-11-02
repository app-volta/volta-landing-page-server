package br.com.volta.controller;

import br.com.volta.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

//encerra a sessao do usuario logado
@WebServlet("/logout")
public class LogoutController extends HttpServlet {

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //o get tambem encerra a sessao para nao deixar o usuario logado
        String target = targetAfterLogout(request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + target);
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //descobre o perfil antes de invalidar a sessao
        String target = targetAfterLogout(request);

        //invalida a sessao atual para deslogar com seguranca
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + target);
    }

    //retorna cada perfil ao acesso correspondente depois de encerrar a sessao
    private String targetAfterLogout(HttpServletRequest request) {
        if (SessionUtil.isSystemAdmin(request)) {
            return "/acesso-restrito";
        }
        if (SessionUtil.isCompany(request) || SessionUtil.isCooperative(request)) {
            return "/login";
        }
        return "/";
    }
}
