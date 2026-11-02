package br.com.volta.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//centraliza a leitura de variaveis do .env para os servicos que nao usam banco
public final class Env {

    private static final Logger LOG = Logger.getLogger(Env.class.getName());
    private static String dotenvDirectory;
    private static final Dotenv DOTENV = loadDotenv();

    //construtor
    private Env() {
        //evita instanciacao da classe utilitaria
    }

    //le uma variavel de ambiente
    public static String get(String key) {
        return get(key, null);
    }

    //le uma variavel de ambiente
    public static String get(String key, String fallback) {
        if (key == null || key.isBlank()) {
            return fallback;
        }
        String value = DOTENV.get(key, System.getenv(key));
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    //le uma variavel de ambiente inteira
    public static int getInt(String key, int fallback) {
        String value = get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    //le uma variavel de ambiente booleana
    public static boolean getBoolean(String key, boolean fallback) {
        String value = get(key);
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    //diretorio onde o .env foi encontrado; usado para marcadores persistentes
    public static File getDotenvDirectory() {
        return dotenvDirectory == null ? null : new File(dotenvDirectory);
    }

    //o diretorio de trabalho do Tomcat nem sempre e a raiz do projeto,
    //entao tenta achar o .env nos lugares mais provaveis (inclusive o catalina.base)
    private static Dotenv loadDotenv() {
        List<String> candidates = new ArrayList<>();
        candidates.add(".");
        candidates.add("..");
        candidates.add("../..");
        candidates.add("../../..");

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            candidates.add(catalinaBase);
            candidates.add(catalinaBase + "/..");
            candidates.add(catalinaBase + "/../..");
        }

        for (String directory : candidates) {
            File file = new File(directory, ".env");
            if (file.exists()) {
                dotenvDirectory = file.getAbsoluteFile().getParent();
                LOG.log(Level.INFO, "Arquivo .env carregado de: {0}", file.getAbsolutePath());
                return Dotenv.configure().directory(directory).load();
            }
        }

        //nao achou em lugar nenhum, segue sem o arquivo e usa so variaveis de ambiente
        dotenvDirectory = new File(".").getAbsolutePath();
        return Dotenv.configure().ignoreIfMissing().load();
    }
}
