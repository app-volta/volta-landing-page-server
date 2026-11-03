package br.com.volta.models;

//representa uma empresa geradora de residuos
public class Company implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    //declaracao de variaveis
    private Integer id; //preenchido pelo banco depois do insert
    private String name;
    private String cnpj;
    private String cep;
    private String address;
    private Double latitude;
    private Double longitude;

    //construtor
    public Company() {
    }

    //construtor
    public Company(String name, String cnpj, String address) {
        this.name = name;
        this.cnpj = cnpj;
        this.address = address;
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

    //retorna cep
    public String getCep() {
        return cep;
    }

    //define cep
    public void setCep(String cep) {
        this.cep = cep;
    }

    //retorna address
    public String getAddress() {
        return address;
    }

    //define address
    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", cep='" + cep + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
