package br.com.volta.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//envia os emails transacionais do sistema por smtp
public final class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    //construtor
    private EmailService() {
        //evita instanciacao da classe utilitaria
    }

    //verifica se o servico de email esta configurado
    public static boolean isConfigured() {
        return configurationProblem() == null;
    }

    //devolve o primeiro problema de configuracao encontrado (ou null se estiver tudo certo)
    public static String configurationProblem() {
        String host = Env.get("SMTP_HOST");
        String from = Env.get("SMTP_FROM", Env.get("SMTP_USER"));
        String username = Env.get("SMTP_USER");
        String password = Env.get("SMTP_PASSWORD");

        if (host == null) {
            return "SMTP_HOST não configurado";
        }
        if (from == null) {
            return "SMTP_FROM (ou SMTP_USER) não configurado";
        }
        //se ha usuario configurado, a senha de app tambem precisa existir
        if (username != null && password == null) {
            return "SMTP_PASSWORD não configurada";
        }
        return null;
    }

    //manda o link de redefinicao em texto puro; devolve false se nao conseguiu
    public static boolean sendPasswordReset(String to, String link) {
        if (to == null || to.isBlank() || link == null || link.isBlank()) {
            return false;
        }
        String problem = configurationProblem();
        if (problem != null) {
            LOG.warning("SMTP nao configurado (" + problem + "): defina as variaveis SMTP_* no .env");
            return false;
        }

        String host = Env.get("SMTP_HOST");
        int port = Env.getInt("SMTP_PORT", 587);
        String username = Env.get("SMTP_USER");
        String password = Env.get("SMTP_PASSWORD");
        String from = Env.get("SMTP_FROM", Env.get("SMTP_USER"));
        boolean startTls = Env.getBoolean("SMTP_STARTTLS", true);

        //senhas de app do Gmail costumam ser copiadas com espacos; o SMTP nao aceita
        if (password != null) {
            password = password.replaceAll("\\s+", "");
        }

        boolean auth = username != null && password != null && !password.isBlank();

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.auth", String.valueOf(auth));
        properties.put("mail.smtp.starttls.enable", String.valueOf(startTls));
        properties.put("mail.smtp.starttls.required", String.valueOf(startTls));
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        properties.put("mail.smtp.connectiontimeout", "15000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");

        Authenticator authenticator = null;
        if (auth) {
            String finalUsername = username;
            String finalPassword = password;
            authenticator = new Authenticator() {
                //retorna password authentication
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername, finalPassword);
                }
            };
        }

        Session session = Session.getInstance(properties, authenticator);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "Volta", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject("Recuperação de senha - Volta", "UTF-8");
            message.setSentDate(new Date());

            String body = "Olá,\n\n"
                    + "Recebemos um pedido para redefinir a senha da sua conta no Volta.\n\n"
                    + "Acesse o link abaixo (válido por 30 minutos):\n"
                    + link + "\n\n"
                    + "Se você não solicitou a troca, ignore este e-mail.\n";

            message.setText(body, "UTF-8");
            message.saveChanges();
            Transport.send(message);
            LOG.log(Level.INFO, "Email enviado para {0} via {1}:{2}",
                    new Object[]{to, host, port});
            return true;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro ao enviar email de recuperacao de senha para "
                    + to + " via " + host + ":" + port
                    + " (usuario=" + username + ")", e);
            return false;
        }
    }
}
