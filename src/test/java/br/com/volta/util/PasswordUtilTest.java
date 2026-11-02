package br.com.volta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashesAndVerifiesBcryptPassword() {
        String hash = PasswordUtil.hash("SenhaForte1!");
        assertNotEquals("SenhaForte1!", hash);
        assertTrue(PasswordUtil.isBCrypt(hash));
        assertTrue(PasswordUtil.verify("SenhaForte1!", hash));
        assertFalse(PasswordUtil.verify("SenhaErrada1!", hash));
    }

    @Test
    void rejectsLegacyOrMalformedHashes() {
        assertFalse(PasswordUtil.verify("SenhaForte1!", "SenhaForte1!"));
        assertFalse(PasswordUtil.verify("SenhaForte1!", "5f4dcc3b5aa765d61d8327deb882cf99"));
        assertFalse(PasswordUtil.verify("SenhaForte1!", null));
        assertFalse(PasswordUtil.verify(null, PasswordUtil.hash("SenhaForte1!")));
        assertFalse(PasswordUtil.isBCrypt("$2"));
        assertFalse(PasswordUtil.isBCrypt(null));
    }
}
