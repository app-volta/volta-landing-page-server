package br.com.volta.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consulta CEPs para preencher enderecos e obter coordenadas aproximadas.
 *
 * A BrasilAPI nao exige chave. Quando o CEP nao tem coordenada conhecida a
 * resposta vem sem o bloco "location", entao o resultado sai com lat/long nulos.
 */
public final class ZipCodeService {

    private static final Logger LOG = Logger.getLogger(ZipCodeService.class.getName());
    private static final String BASE_URL = "https://brasilapi.com.br/api/cep/v2/";
    private static final String ADDRESS_URL = "https://cep.awesomeapi.com.br/json/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    //construtor
    private ZipCodeService() {
    }

    //resultado da consulta: o cep normalizado, as coordenadas (podem ser nulas) e a cidade/UF
    public record ZipCodeResult(String zipCode, Double latitude, Double longitude, String city, String state) {
    }

    public record AddressResult(String zipCode, String address, String district,
                                String city, String state, Double latitude, Double longitude) {
    }

    //consulta o endereco usando a AwesomeAPI, que recebe os oito digitos do CEP
    public static AddressResult findAddress(String zipCode) {
        String digits = Validator.onlyDigits(zipCode);
        if (digits.length() != 8) {
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ADDRESS_URL + digits))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.log(Level.WARNING, "Consulta de endereco para CEP retornou status {0}",
                        response.statusCode());
                return null;
            }

            JSONObject body = new JSONObject(response.body());
            String address = body.optString("address", "").trim();
            if (address.isEmpty()) {
                String type = body.optString("address_type", "").trim();
                String name = body.optString("address_name", "").trim();
                address = (type + " " + name).trim();
            }

            String district = body.optString("district", "").trim();
            String city = body.optString("city", "").trim();
            String state = body.optString("state", "").trim();
            Double latitude = parseCoordinate(body.optString("lat", ""));
            Double longitude = parseCoordinate(body.optString("lng", ""));
            String fullAddress = java.util.stream.Stream.of(address, district, city, state)
                    .filter(part -> !part.isBlank())
                    .collect(java.util.stream.Collectors.joining(", "));
            if (fullAddress.isBlank()) {
                return null;
            }

            return new AddressResult(digits, fullAddress, district, city, state, latitude, longitude);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Consulta de endereco por CEP interrompida", e);
            return null;
        } catch (IOException | JSONException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Nao foi possivel consultar endereco por CEP", e);
            return null;
        }
    }

    //consulta o cep; devolve null se o cep for invalido ou a consulta falhar
    public static ZipCodeResult find(String zipCode) {
        String digits = Validator.onlyDigits(zipCode);
        if (digits.length() != 8) {
            LOG.log(Level.WARNING, "CEP invalido para consulta de coordenadas: {0}", zipCode);
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + digits))
                    .timeout(Duration.ofSeconds(3))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            //a API responde 404 quando o cep nao existe
            if (response.statusCode() != 200) {
                LOG.log(Level.WARNING, "Consulta de CEP {0} retornou status {1}",
                        new Object[]{digits, response.statusCode()});
                return null;
            }

            JSONObject body = new JSONObject(response.body());
            JSONObject location = body.optJSONObject("location");
            JSONObject coordinates = location != null ? location.optJSONObject("coordinates") : null;

            Double latitude = readCoordinate(coordinates, location, "latitude");
            Double longitude = readCoordinate(coordinates, location, "longitude");

            if (latitude == null || longitude == null) {
                LOG.log(Level.WARNING, "CEP {0} nao possui coordenadas conhecidas", digits);
            }

            String city = body.optString("city", "").trim();
            String state = body.optString("state", "").trim();
            return new ZipCodeResult(digits, latitude, longitude, city, state);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Consulta de CEP interrompida", e);
            return null;
        } catch (IOException | JSONException | IllegalArgumentException e) {
            //falha de rede/timeout nao pode quebrar o cadastro
            LOG.log(Level.WARNING, "Nao foi possivel consultar o CEP " + digits, e);
            return null;
        }
    }

    //a BrasilAPI devolve as coordenadas como texto dentro de location.coordinates
    private static Double readCoordinate(JSONObject coordinates, JSONObject location, String key) {
        Object value = null;
        if (coordinates != null && coordinates.has(key)) {
            value = coordinates.opt(key);
        } else if (location != null && location.has(key)) {
            value = location.opt(key);
        }

        if (value == null || JSONObject.NULL.equals(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
