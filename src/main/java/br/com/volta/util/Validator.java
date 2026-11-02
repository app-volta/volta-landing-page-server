package br.com.volta.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

//centraliza validacoes e sanitizacao de dados
public final class Validator {

    public static final int NAME_MAX = 150;
    public static final int TEXT_MAX = 255;
    public static final int DESCRIPTION_MAX = 1000;
    public static final int URL_MAX = 500;
    public static final int EMAIL_MAX = 150;
    public static final int POSITION_MAX = 100;
    public static final int CATEGORY_MAX = 100;
    public static final int ROLE_MAX = 50;

    private static final Pattern CNPJ_FORMAT = Pattern.compile("^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$");
    private static final Pattern CPF_FORMAT = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");
    private static final Pattern ZIP_CODE_FORMAT = Pattern.compile("^\\d{5}-?\\d{3}$");
    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern URL_FORMAT = Pattern.compile("^https?://\\S+$");
    private static final Pattern CELLPHONE_FORMAT =
            Pattern.compile("^(?:\\+?55\\s*)?\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$");

    //construtor
    private Validator() {
        //evita instanciacao da classe utilitaria
    }

    //tira tudo que nao for digito numerico
    public static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    //faz trim e devolve null se estiver vazio
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    //confere se o texto respeita o tamanho maximo
    public static boolean fits(String text, int max) {
        return text == null || text.length() <= max;
    }

    //transforma texto em integer ou devolve null se falhar
    public static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //converte id aceitando apenas numeros positivos
    public static Integer parseId(String value) {
        Integer id = parseInteger(value);
        return (id != null && id > 0) ? id : null;
    }

    //converte o texto do formulario no enum correspondente
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        if (type == null || value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //remove espacos e poe em minusculas para nao duplicar login
    public static String normalizeEmail(String email) {
        String clean = trimToNull(email);
        return clean == null ? null : clean.toLowerCase(Locale.ROOT);
    }

    //valida formato de email
    public static boolean isValidEmail(String email) {
        String clean = trimToNull(email);
        return clean != null && fits(clean, EMAIL_MAX) && EMAIL_FORMAT.matcher(clean).matches();
    }

    //confere regex e digitos verificadores do cnpj
    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null || !CNPJ_FORMAT.matcher(cnpj.trim()).matches()) {
            return false;
        }
        String digits = onlyDigits(cnpj);
        if (digits.length() != 14 || digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkCnpjDigit(digits, 12, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2})
                && checkCnpjDigit(digits, 13, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
    }

    //confere formato e os dois digitos verificadores do cpf
    public static boolean isValidCpf(String cpf) {
        if (cpf == null || !CPF_FORMAT.matcher(cpf.trim()).matches()) {
            return false;
        }
        String digits = onlyDigits(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }
        int first = calculateCpfDigit(digits, 9);
        int second = calculateCpfDigit(digits, 10);
        return first == (digits.charAt(9) - '0') && second == (digits.charAt(10) - '0');
    }

    //aceita celular brasileiro com ddd e nono digito, com ou sem formatacao
    public static boolean isValidCellphone(String cellphone) {
        if (cellphone == null || !CELLPHONE_FORMAT.matcher(cellphone.trim()).matches()) {
            return false;
        }
        String digits = onlyDigits(cellphone);
        if (digits.length() == 13 && digits.startsWith("55")) {
            digits = digits.substring(2);
        }
        if (digits.length() != 11 || digits.charAt(2) != '9') {
            return false;
        }
        int areaCode = Integer.parseInt(digits.substring(0, 2));
        return areaCode >= 11 && areaCode <= 99;
    }

    //calcula um digito do cnpj
    private static boolean checkCnpjDigit(String digits, int size, int[] weights) {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (digits.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return expected == (digits.charAt(size) - '0');
    }

    //calcula um digito do cpf
    private static int calculateCpfDigit(String digits, int size) {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (digits.charAt(i) - '0') * (size + 1 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    //valida formato do cep com ou sem hifen
    public static boolean isValidZipCode(String zipCode) {
        if (zipCode == null || !ZIP_CODE_FORMAT.matcher(zipCode.trim()).matches()) {
            return false;
        }
        return onlyDigits(zipCode).length() == 8;
    }

    //confere se o CEP retornado pela consulta pertence a cidade de Sao Paulo
    public static boolean isSaoPaulo(String city, String state) {
        return "sao paulo".equals(normalizeText(city)) && "sp".equals(normalizeText(state));
    }

    //remove acentos e normaliza espacos para comparar textos digitados pelo usuario
    private static String normalizeText(String value) {
        String clean = trimToNull(value);
        if (clean == null) {
            return null;
        }
        String withoutAccents = Normalizer.normalize(clean.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.replaceAll("\\s+", " ");
    }

    //aceita apenas urls com protocolo http ou https
    public static boolean isValidUrl(String url) {
        String clean = trimToNull(url);
        return clean != null && fits(clean, URL_MAX) && URL_FORMAT.matcher(clean).matches();
    }

    //senha entre 8 e 72 com ao menos uma maiuscula e um caractere especial
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            return false;
        }
        boolean hasUpperCase = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                hasSpecial = true;
            }
        }
        return hasUpperCase && hasSpecial;
    }
}
