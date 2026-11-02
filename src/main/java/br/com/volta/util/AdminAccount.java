package br.com.volta.util;

//centraliza a conta administrativa configurada no ambiente
public final class AdminAccount {

    //construtor
    private AdminAccount() {
        //evita instanciacao da classe utilitaria
    }

    //email usado para login e como destino das instrucoes de recuperacao
    public static String email() {
        return Validator.normalizeEmail(Env.get("ADMIN_EMAIL"));
    }

    //executa a operacao matches
    public static boolean matches(String email) {
        String configured = email();
        return configured != null
                && configured.equalsIgnoreCase(Validator.normalizeEmail(email));
    }
}
