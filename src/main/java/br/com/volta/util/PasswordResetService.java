package br.com.volta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//guarda os tokens de recuperacao em memoria, com validade e uso unico
public final class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final long CLEANUP_INTERVAL_MILLIS = 60_000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong LAST_CLEANUP = new AtomicLong();

    //chave = hash sha-256 do token; valor = dados do token
    private static final Map<String, ResetToken> TOKENS = new ConcurrentHashMap<>();
    //um token ativo por usuario
    private static final Map<Integer, String> TOKENS_BY_USER = new ConcurrentHashMap<>();
    //evita pedidos repetidos em sequencia
    private static final Map<Integer, Instant> LAST_REQUESTS = new ConcurrentHashMap<>();

    //construtor
    private PasswordResetService() {
        //evita instanciacao da classe utilitaria
    }

    //cria um token novo; devolve null se o usuario pediu outro ha menos de 1 minuto
    public static synchronized String create(Integer userId) {
        if (userId == null) {
            return null;
        }

        cleanup();
        Instant now = Instant.now();
        Instant lastRequest = LAST_REQUESTS.get(userId);
        if (lastRequest != null && lastRequest.plus(REQUEST_COOLDOWN).isAfter(now)) {
            return null;
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hash(rawToken);

        //invalida o token anterior do mesmo usuario
        String oldHash = TOKENS_BY_USER.put(userId, tokenHash);
        if (oldHash != null) {
            TOKENS.remove(oldHash);
        }

        TOKENS.put(tokenHash, new ResetToken(userId, now.plus(TOKEN_TTL)));
        LAST_REQUESTS.put(userId, now);
        return rawToken;
    }

    //confere se o token existe e ainda nao expirou
    public static boolean isValid(String rawToken) {
        cleanup();
        return find(rawToken) != null;
    }

    //devolve o usuario dono do token sem consumir
    public static Integer validate(String rawToken) {
        cleanup();
        ResetToken token = find(rawToken);
        return token == null ? null : token.userId();
    }

    //consome o token de forma atomica; devolve o usuario ou null
    public static Integer consume(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        cleanup();
        String tokenHash = hash(rawToken.trim());
        ResetToken token = TOKENS.remove(tokenHash);
        if (token == null) {
            return null;
        }
        TOKENS_BY_USER.remove(token.userId(), tokenHash);
        if (!token.expiresAt().isAfter(Instant.now())) {
            return null;
        }
        return token.userId();
    }

    //remove um token que nao chegou a ser enviado e libera novo pedido imediatamente
    public static void discard(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String tokenHash = hash(rawToken.trim());
        ResetToken token = TOKENS.remove(tokenHash);
        if (token != null) {
            TOKENS_BY_USER.remove(token.userId(), tokenHash);
            LAST_REQUESTS.remove(token.userId());
        }
    }

    //segundos restantes do cooldown; usado pela tela para desabilitar o reenvio
    public static int secondsUntilResend(Integer userId) {
        if (userId == null) {
            return 0;
        }
        Instant lastRequest = LAST_REQUESTS.get(userId);
        if (lastRequest == null) {
            return 0;
        }
        long millis = Duration.between(Instant.now(), lastRequest.plus(REQUEST_COOLDOWN)).toMillis();
        return (int) Math.max(0, (millis + 999) / 1000);
    }

    //busca os dados necessarios
    private static ResetToken find(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        String tokenHash = hash(rawToken.trim());
        ResetToken token = TOKENS.get(tokenHash);
        if (token == null) {
            return null;
        }
        if (!token.expiresAt().isAfter(Instant.now())) {
            TOKENS.remove(tokenHash);
            TOKENS_BY_USER.remove(token.userId(), tokenHash);
            return null;
        }
        return token;
    }

    //remove tokens expirados e pedidos antigos sem varrer os mapas a cada requisicao
    private static void cleanup() {
        long now = System.currentTimeMillis();
        long last = LAST_CLEANUP.get();
        if (now - last < CLEANUP_INTERVAL_MILLIS || !LAST_CLEANUP.compareAndSet(last, now)) {
            return;
        }

        Instant instant = Instant.now();
        TOKENS.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(instant));
        TOKENS_BY_USER.entrySet().removeIf(entry -> !TOKENS.containsKey(entry.getValue()));
        LAST_REQUESTS.entrySet().removeIf(entry -> entry.getValue().plus(REQUEST_COOLDOWN).isBefore(instant));
    }

    //usado apenas nos testes para deixar o estado previsivel
    static void clearForTests() {
        TOKENS.clear();
        TOKENS_BY_USER.clear();
        LAST_REQUESTS.clear();
        LAST_CLEANUP.set(0);
    }

    //gera o hash do texto informado
    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            //sha-256 sempre existe; se falhar, o token nao pode ser validado
            return "";
        }
    }

    //executa a operacao ResetToken
    private record ResetToken(Integer userId, Instant expiresAt) {
    }
}
