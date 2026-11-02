package br.com.volta.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consulta CNPJs na BrasilAPI para confirmar se o numero existe na Receita
 * Federal.
 *
 * A validacao local do digito verificador continua sendo feita pelo
 * {@link Validator}. Esta classe apenas evita aceitar um CNPJ matematicamente
 * valido, mas que nao existe.
 */
public final class CnpjService {

    private static final Logger LOG = Logger.getLogger(CnpjService.class.getName());
    private static final String BASE_URL = "https://brasilapi.com.br/api/cnpj/v1/";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration ERROR_CACHE_TTL = Duration.ofSeconds(30);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static final Map<String, CachedResult> CACHE = new ConcurrentHashMap<>();

    private CnpjService() {
    }

    public enum Status {
        EXISTS,
        NOT_FOUND,
        UNAVAILABLE
    }

    public record Result(Status status) {
    }

    private record CachedResult(Result result, Instant expiresAt) {
    }

    /**
     * Devolve null quando o CNPJ existe. Caso contrario, devolve a mensagem que
     * deve ser exibida ao usuario.
     */
    public static String validateExistence(String cnpj) {
        return switch (lookup(cnpj).status()) {
            case EXISTS -> null;
            case NOT_FOUND -> "CNPJ não encontrado na Receita Federal.";
            case UNAVAILABLE -> "Não foi possível validar o CNPJ agora. Tente novamente.";
        };
    }

    public static Result lookup(String cnpj) {
        String digits = Validator.onlyDigits(cnpj);
        if (digits.length() != 14) {
            return new Result(Status.NOT_FOUND);
        }

        Instant now = Instant.now();
        CachedResult cached = CACHE.get(digits);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.result();
        }

        Result result = fetch(digits);
        Duration ttl = result.status() == Status.UNAVAILABLE ? ERROR_CACHE_TTL : CACHE_TTL;
        CACHE.put(digits, new CachedResult(result, now.plus(ttl)));
        return result;
    }

    private static Result fetch(String digits) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + digits))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Volta/1.0")
                    .GET()
                    .build();

            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            int status = response.statusCode();

            if (status == 200) {
                return new Result(Status.EXISTS);
            }
            if (status == 404) {
                return new Result(Status.NOT_FOUND);
            }

            LOG.log(Level.WARNING, "Consulta de CNPJ {0} retornou status {1}",
                    new Object[]{digits, status});
            return new Result(Status.UNAVAILABLE);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Consulta de CNPJ interrompida", e);
            return new Result(Status.UNAVAILABLE);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Não foi possível consultar o CNPJ " + digits, e);
            return new Result(Status.UNAVAILABLE);
        }
    }
}
