package br.com.volta.models;

import java.time.LocalDateTime;

//representa uma ocorrencia de residuo registrada por um usuario
public class Incident {

    //declaracao de variaveis
    private Integer id;
    private Company company;
    private User user;
    private Area area;
    private WasteType wasteType;
    private Priority priority;
    private IncidentStatus status;
    private String contaminationLevel;
    private String photoUrl;
    private String employeeDescription;
    private LocalDateTime registeredAt;
    private Double estimatedWeight;
    //construtor
    public Incident() {}

    //construtor
    public Incident(Company company, User user, Area area, WasteType wasteType, Priority priority,
                    String contaminationLevel, String photoUrl, String employeeDescription,
                    Double estimatedWeight) {
        this.company = company;
        this.user = user;
        this.area = area;
        this.wasteType = wasteType;
        this.priority = priority;
        this.contaminationLevel = contaminationLevel;
        this.photoUrl = photoUrl;
        this.employeeDescription = employeeDescription;
        this.estimatedWeight = estimatedWeight;
        this.status = IncidentStatus.PENDENTE; // toda ocorrencia nova começa pendente
        this.registeredAt = LocalDateTime.now();
    }

    //getters e setters
    public Integer getId() {
        return id;
    }

    //define id
    public void setId(Integer id) {
        this.id = id;
    }

    //retorna company
    public Company getCompany() {
        return company;
    }

    //define company
    public void setCompany(Company company) {
        this.company = company;
    }

    //retorna user
    public User getUser() {
        return user;
    }

    //define user
    public void setUser(User user) {
        this.user = user;
    }

    //retorna area
    public Area getArea() {
        return area;
    }

    //define area
    public void setArea(Area area) {
        this.area = area;
    }

    //retorna waste type
    public WasteType getWasteType() {
        return wasteType;
    }

    //define waste type
    public void setWasteType(WasteType wasteType) {
        this.wasteType = wasteType;
    }

    //retorna priority
    public Priority getPriority() {
        return priority;
    }

    //define priority
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    //retorna status
    public IncidentStatus getStatus() {
        return status;
    }

    //define status
    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    //retorna contamination level
    public String getContaminationLevel() {
        return contaminationLevel;
    }

    //define contamination level
    public void setContaminationLevel(String contaminationLevel) {
        this.contaminationLevel = contaminationLevel;
    }

    //retorna photo url
    public String getPhotoUrl() {
        return photoUrl;
    }

    //define photo url
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    //retorna employee description
    public String getEmployeeDescription() {
        return employeeDescription;
    }

    //define employee description
    public void setEmployeeDescription(String employeeDescription) {
        this.employeeDescription = employeeDescription;
    }

    //retorna registered at
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    //define registered at
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    //retorna estimated weight
    public Double getEstimatedWeight() {
        return estimatedWeight;
    }

    //define estimated weight
    public void setEstimatedWeight(Double estimatedWeight) {
        this.estimatedWeight = estimatedWeight;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", status=" + status +
                ", priority=" + priority +
                ", registeredAt=" + registeredAt +
                '}';
    }
}