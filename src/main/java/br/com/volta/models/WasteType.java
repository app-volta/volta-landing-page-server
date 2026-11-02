package br.com.volta.models;

//representa um tipo de residuo
public class WasteType {

    //declaracao de variaveis
    private Integer id;
    private String category;
    private String description;
    private RiskLevel defaultRisk;

    //construtor
    public WasteType() {
    }

    //construtor
    public WasteType(String category, String description, RiskLevel defaultRisk) {
        this.category = category;
        this.description = description;
        this.defaultRisk = defaultRisk;
    }

    //getters e setters
    public Integer getId() {
        return id;
    }

    //define id
    public void setId(Integer id) {
        this.id = id;
    }

    //retorna category
    public String getCategory() {
        return category;
    }

    //define category
    public void setCategory(String category) {
        this.category = category;
    }

    //retorna description
    public String getDescription() {
        return description;
    }

    //define description
    public void setDescription(String description) {
        this.description = description;
    }

    //retorna default risk
    public RiskLevel getDefaultRisk() {
        return defaultRisk;
    }

    //define default risk
    public void setDefaultRisk(RiskLevel defaultRisk) {
        this.defaultRisk = defaultRisk;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "WasteType{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", defaultRisk=" + defaultRisk +
                '}';
    }
}
