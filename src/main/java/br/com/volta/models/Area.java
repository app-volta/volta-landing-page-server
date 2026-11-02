package br.com.volta.models;

//representa uma area da empresa onde ocorrem descartes
public class Area {

    //declaracao de variaveis
    private Integer id;
    private Company company;
    private String sectorName;
    private String locationDescription;

    //construtor
    public Area() {
    }

    //construtor
    public Area(Company company, String sectorName, String locationDescription) {
        this.company = company;
        this.sectorName = sectorName;
        this.locationDescription = locationDescription;
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

    //retorna sector name
    public String getSectorName() {
        return sectorName;
    }

    //define sector name
    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    //retorna location description
    public String getLocationDescription() {
        return locationDescription;
    }

    //define location description
    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Area{" +
                "id=" + id +
                ", sectorName='" + sectorName + '\'' +
                '}';
    }
}
