package br.com.volta.controller;

import br.com.volta.dao.DaoException;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.User;
import br.com.volta.util.EmailService;
import br.com.volta.util.Env;
import br.com.volta.util.Flash;
import br.com.volta.util.PasswordResetService;
import br.com.volta.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

//recuperacao de senha para qualquer usuario cadastrado (admin, empresa ou cooperativa)
@WebServlet("/esqueci-senha")
public class ForgotPasswordController extends BaseController {

    private final UserDAO userDAO = new UserDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        redirect(request, response, "/login");
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean json = wantsJson(request);
        String email = Validator.normalizeEmail(request.getParameter("email"));
        Result result = requestReset(request, email);

        if (json) {
            JSONObject body = new JSONObject();
            body.put("ok", result.ok());
            body.put("message", result.message());
            if (result.devLink() != null) {
                body.put("devLink", result.devLink());
            }

            response.setStatus(result.ok() ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(body.toString());
            return;
        }

        if (result.ok()) {
            Flash.success(request, result.message());
        } else {
            Flash.error(request, result.message());
        }
        redirect(request, response, "/login");
    }

    //executa a operacao requestReset
    private Result requestReset(HttpServletRequest request, String email) {
        if (email == null || !Validator.isValidEmail(email)) {
            return Result.error("Informe um e-mail válido.");
        }

        try {
            User user = userDAO.findByEmail(email);

            //nao revela se um e-mail existe ou nao na base
            if (user == null) {
                LOG.log(Level.INFO,
                        "Pedido de recuperacao para email nao cadastrado: {0}", email);
                return Result.success(
                        "Se este e-mail estiver cadastrado, enviaremos um link de recuperação.");
            }

            int wait = PasswordResetService.secondsUntilResend(user.getId());
            if (wait > 0) {
                return Result.error("Aguarde " + wait
                        + " segundo(s) para solicitar outro link.");
            }

            String token = PasswordResetService.create(user.getId());
            if (token == null) {
                return Result.error("Não foi possível gerar o link. Tente novamente.");
            }

            String link = buildResetLink(request, token);
            if (link == null) {
                PasswordResetService.discard(token);
                return Result.error("Não foi possível montar o link de recuperação.");
            }

            boolean sent = EmailService.sendPasswordReset(email, link);
            if (sent) {
                LOG.log(Level.INFO, "Email de recuperacao enviado para {0}", email);
                return Result.success("Enviamos o link de recuperação para " + email + ".");
            }

            String message = "Não foi possível enviar o e-mail.";
            String problem = EmailService.configurationProblem();
            if (problem != null) {
                message += " " + problem + ".";
            }
            message += " Confira as configurações SMTP no arquivo .env.";

            //em desenvolvimento o link fica disponivel na tela para nao travar o teste
            String devLink = isDev() ? link : null;
            if (devLink == null) {
                //sem email e sem link local, libera novo pedido imediatamente
                PasswordResetService.discard(token);
            }
            LOG.log(Level.WARNING,
                    "Falha ao enviar email de recuperacao para {0}. Link de dev: {1}",
                    new Object[]{email, link});
            return new Result(false, message, devLink);

        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Erro de banco ao solicitar recuperacao de senha", e);
            return Result.error("Erro ao consultar a conta. Tente novamente.");
        }
    }

    //monta o link absoluto usando APP_BASE_URL ou, se ela nao existir, a propria requisicao
    private String buildResetLink(HttpServletRequest request, String token) {
        String base = Env.get("APP_BASE_URL");

        if (base == null || base.isBlank()) {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();

            base = scheme + "://" + host;
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);
            if (!defaultPort) {
                base += ":" + port;
            }
            base += request.getContextPath();
        } else {
            while (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }

            //APP_BASE_URL pode apontar para o dominio; inclui o contexto se faltar
            String contextPath = request.getContextPath();
            if (!contextPath.isEmpty() && !base.endsWith(contextPath)) {
                base += contextPath;
            }
        }

        return base + "/redefinir-senha?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    //executa a operacao wantsJson
    private boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return "1".equals(request.getParameter("ajax"))
                || (accept != null && accept.contains("application/json"));
    }

    //verifica se dev
    private boolean isDev() {
        return "dev".equalsIgnoreCase(Env.get("APP_ENV", "prod"));
    }

    //executa a operacao Result
    private record Result(boolean ok, String message, String devLink) {

        static Result success(String message) {
            return new Result(true, message, null);
        }

        static Result error(String message) {
            return new Result(false, message, null);
        }
    }
}
