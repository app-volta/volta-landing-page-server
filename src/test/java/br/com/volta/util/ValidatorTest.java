package br.com.volta.util;

import br.com.volta.models.IncidentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    @Test
    void validatesCnpjWithAndWithoutFormatting() {
        String cnpj = cnpjWithCheckDigits("112223330001");
        assertTrue(Validator.isValidCnpj(cnpj));
        assertTrue(Validator.isValidCnpj(formatCnpj(cnpj)));
        assertFalse(Validator.isValidCnpj("11.111.111/1111-11"));
        assertFalse(Validator.isValidCnpj("11222333000100"));
        assertFalse(Validator.isValidCnpj(""));
        assertFalse(Validator.isValidCnpj(null));
    }

    @Test
    void validatesCpfWithAndWithoutFormatting() {
        String cpf = cpfWithCheckDigits("529982247");
        assertTrue(Validator.isValidCpf(cpf));
        assertTrue(Validator.isValidCpf(formatCpf(cpf)));
        assertFalse(Validator.isValidCpf("111.111.111-11"));
        assertFalse(Validator.isValidCpf("52998224724"));
        assertFalse(Validator.isValidCpf(null));
    }

    @Test
    void validatesBrazilianCellphone() {
        assertTrue(Validator.isValidCellphone("(11) 91234-5678"));
        assertTrue(Validator.isValidCellphone("11912345678"));
        assertTrue(Validator.isValidCellphone("+55 11 91234-5678"));
        assertFalse(Validator.isValidCellphone("(11) 81234-5678"));
        assertFalse(Validator.isValidCellphone("1191234567"));
        assertFalse(Validator.isValidCellphone("119123456789"));
        assertFalse(Validator.isValidCellphone(null));
    }

    @Test
    void validatesZipCodeEmailAndUrl() {
        assertTrue(Validator.isValidZipCode("01310-100"));
        assertTrue(Validator.isValidZipCode("01310100"));
        assertFalse(Validator.isValidZipCode("0131010"));
        assertFalse(Validator.isValidZipCode("013101000"));

        assertTrue(Validator.isValidEmail("usuario@empresa.com"));
        assertFalse(Validator.isValidEmail("usuario@empresa"));
        assertFalse(Validator.isValidEmail("usuario empresa.com"));

        assertTrue(Validator.isValidUrl("https://exemplo.com/foto.jpg"));
        assertTrue(Validator.isValidUrl("http://exemplo.com"));
        assertFalse(Validator.isValidUrl("ftp://exemplo.com"));
        assertFalse(Validator.isValidUrl("exemplo.com"));
    }

    @Test
    void validatesSaoPauloCityAndState() {
        assertTrue(Validator.isSaoPaulo("São Paulo", "SP"));
        assertTrue(Validator.isSaoPaulo("Sao Paulo", "sp"));
        assertTrue(Validator.isSaoPaulo("  SÃO   PAULO  ", "SP"));
        assertFalse(Validator.isSaoPaulo("Guarulhos", "SP"));
        assertFalse(Validator.isSaoPaulo("São Paulo", "RJ"));
        assertFalse(Validator.isSaoPaulo(null, "SP"));
    }

    @Test
    void validatesPasswordStrength() {
        assertTrue(Validator.isValidPassword("Abcdef1!"));
        assertFalse(Validator.isValidPassword("abcdef1!"));
        assertFalse(Validator.isValidPassword("Abcdef12"));
        assertFalse(Validator.isValidPassword("Abc1!"));
        assertFalse(Validator.isValidPassword("A".repeat(71) + "1!"));
    }

    @Test
    void parsesEnumsAndNumbers() {
        assertEquals(IncidentStatus.EM_ANALISE,
                Validator.parseEnum(IncidentStatus.class, " em_analise "));
        assertNull(Validator.parseEnum(IncidentStatus.class, "INVALIDO"));
        assertNull(Validator.parseEnum(null, "PENDENTE"));

        assertEquals(42, Validator.parseInteger(" 42 "));
        assertNull(Validator.parseInteger("4,2"));
        assertEquals(7, Validator.parseId("7"));
        assertNull(Validator.parseId("0"));
        assertNull(Validator.parseId("-3"));
    }

    @Test
    void normalizesEmail() {
        assertEquals("usuario@empresa.com", Validator.normalizeEmail("  USUARIO@EMPRESA.COM "));
        assertNull(Validator.normalizeEmail("   "));
    }

    private static String cnpjWithCheckDigits(String base) {
        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int first = checkDigit(base, firstWeights);
        String withFirst = base + first;
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int second = checkDigit(withFirst, secondWeights);
        return withFirst + second;
    }

    private static int checkDigit(String digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            sum += (digits.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static String cpfWithCheckDigits(String base) {
        int first = cpfDigit(base, 9);
        String withFirst = base + first;
        int second = cpfDigit(withFirst, 10);
        return withFirst + second;
    }

    private static int cpfDigit(String digits, int size) {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (digits.charAt(i) - '0') * (size + 1 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static String formatCnpj(String cnpj) {
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "."
                + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    private static String formatCpf(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
