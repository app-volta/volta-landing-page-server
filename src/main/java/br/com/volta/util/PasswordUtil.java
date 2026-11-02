package br.com.volta.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Centraliza o hash e a verificacao de senhas.
 *
 * Somente BCrypt e aceito.
 */
public final class PasswordUtil {

    //construtor
    private PasswordUtil() {
    }

    //gera o hash do texto informado
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    //executa a operacao verify
    public static boolean verify(String password, String hash) {
        if (password == null || hash == null || !isBCrypt(hash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            //hash malformado nao pode derrubar o login
            return false;
        }
    }

    //um hash BCrypt valido tem 60 caracteres e um dos prefixos conhecidos
    public static boolean isBCrypt(String value) {
        return value != null
                && value.length() == 60
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
