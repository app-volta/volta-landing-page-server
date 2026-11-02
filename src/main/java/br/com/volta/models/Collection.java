package br.com.volta.models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//representa uma coleta vinculada a uma ocorrencia
public class Collection {

    //declaracao de variaveis
    private Integer id;
    private Incident incident;
    private Cooperative cooperative;
    private LocalDateTime requestDate;
    private LocalDateTime scheduledDate;
    private CollectionStatus currentStatus;
    private String collectionType;
    private boolean urgent;
    private LocalTime estimatedTime;

    //construtor
    public Collection() {
    }

    //construtor
    public Collection(Incident incident, Cooperative cooperative, LocalDateTime scheduledDate,
                      String collectionType, boolean urgent, LocalTime estimatedTime) {
        this.incident = incident;
        this.cooperative = cooperative;
        this.scheduledDate = scheduledDate;
        this.collectionType = collectionType;
        this.urgent = urgent;
        this.estimatedTime = estimatedTime;
        this.currentStatus = CollectionStatus.SOLICITADA; //toda coleta nova começa como solicitada
        this.requestDate = LocalDateTime.now();
    }

    //getters e setters
    public Integer getId() {
        return id;
    }

    //define id
    public void setId(Integer id) {
        this.id = id;
    }

    //retorna incident
    public Incident getIncident() {
        return incident;
    }

    //define incident
    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    //retorna cooperative
    public Cooperative getCooperative() {
        return cooperative;
    }

    //define cooperative
    public void setCooperative(Cooperative cooperative) {
        this.cooperative = cooperative;
    }

    //retorna request date
    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    //define request date
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    //retorna scheduled date
    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    //data no formato que o input type="datetime-local" entende
    public String getScheduledDateInput() {
        return scheduledDate == null ? "" : scheduledDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    //define scheduled date
    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    //retorna current status
    public CollectionStatus getCurrentStatus() {
        return currentStatus;
    }

    //define current status
    public void setCurrentStatus(CollectionStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    //retorna collection type
    public String getCollectionType() {
        return collectionType;
    }

    //define collection type
    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    //indica se a coleta e urgente
    public boolean isUrgent() {
        return urgent;
    }

    //define urgent
    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    //retorna estimated time
    public LocalTime getEstimatedTime() {
        return estimatedTime;
    }

    //hora no formato HH:mm que o input type="time" entende
    public String getEstimatedTimeInput() {
        return estimatedTime == null ? "" : estimatedTime.toString().substring(0, 5);
    }

    //define estimated time
    public void setEstimatedTime(LocalTime estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Collection{" +
                "id=" + id +
                ", currentStatus=" + currentStatus +
                ", collectionType='" + collectionType + '\'' +
                ", urgent=" + urgent +
                '}';
    }
}
