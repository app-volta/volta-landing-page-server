package br.com.volta.models;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

//representa uma cooperativa que realiza coletas
public class Cooperative implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    //declaracao de variaveis
    private Integer id;
    private String name;
    private String cnpj;
    private Double latitude;
    private Double longitude;
    private Double averageRating;     //media das avaliacoes recebidas
    private String cep;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer monthlyCapacity;
    private List<Integer> wasteTypeIds = new ArrayList<>(); //tipos de residuo vinculados

    //construtor
    public Cooperative() {
    }

    //construtor
    public Cooperative(String name, String cnpj, Double latitude, Double longitude,
                       LocalTime openingTime, Integer monthlyCapacity) {
        this.name = name;
        this.cnpj = cnpj;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingTime = openingTime;
        this.monthlyCapacity = monthlyCapacity;
        this.averageRating = 0.0; //toda cooperativa nova comeca sem avaliacao
    }

    //getters e setters
    public Integer getId() {
        return id;
    }

    //define id
    public void setId(Integer id) {
        this.id = id;
    }

    //retorna name
    public String getName() {
        return name;
    }

    //define name
    public void setName(String name) {
        this.name = name;
    }

    //retorna cnpj
    public String getCnpj() {
        return cnpj;
    }

    //define cnpj
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    //retorna latitude
    public Double getLatitude() {
        return latitude;
    }

    //define latitude
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    //retorna longitude
    public Double getLongitude() {
        return longitude;
    }

    //define longitude
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    //retorna average rating
    public Double getAverageRating() {
        return averageRating;
    }

    //define average rating
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    //retorna waste type ids
    public List<Integer> getWasteTypeIds() {
        return wasteTypeIds;
    }

    //define waste type ids
    public void setWasteTypeIds(List<Integer> wasteTypeIds) {
        this.wasteTypeIds = wasteTypeIds == null ? new ArrayList<>() : wasteTypeIds;
    }

    //lista em formato csv para atributos data-* usados pelo filtro de cooperativas no JSP
    public String getWasteTypeIdsCsv() {
        StringBuilder csv = new StringBuilder();
        for (Integer wasteTypeId : wasteTypeIds) {
            if (wasteTypeId == null) {
                continue;
            }
            if (!csv.isEmpty()) {
                csv.append(',');
            }
            csv.append(wasteTypeId);
        }
        return csv.toString();
    }

    //retorna cep
    public String getCep() {
        return cep;
    }

    //define cep
    public void setCep(String cep) {
        this.cep = cep;
    }

    //retorna opening time
    public LocalTime getOpeningTime() {
        return openingTime;
    }

    //hora no formato HH:mm que o input type="time" entende
    public String getOpeningTimeInput() {
        return openingTime == null ? "" : openingTime.toString().substring(0, 5);
    }

    //define opening time
    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    //retorna closing time
    public LocalTime getClosingTime() {
        return closingTime;
    }

    //hora no formato HH:mm que o input type="time" entende
    public String getClosingTimeInput() {
        return closingTime == null ? "" : closingTime.toString().substring(0, 5);
    }

    //define closing time
    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    //retorna monthly capacity
    public Integer getMonthlyCapacity() {
        return monthlyCapacity;
    }

    //define monthly capacity
    public void setMonthlyCapacity(Integer monthlyCapacity) {
        this.monthlyCapacity = monthlyCapacity;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Cooperative{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", averageRating=" + averageRating +
                ", monthlyCapacity=" + monthlyCapacity +
                ", wasteTypeIds=" + wasteTypeIds +
                '}';
    }
}
