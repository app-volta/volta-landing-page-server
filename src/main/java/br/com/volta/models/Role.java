package br.com.volta.models;

//representa um perfil de acesso
public class Role implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    //declaracao de variaveis
    private Integer id;
    private String type;
    private int userCount;

    //construtor
    public Role() {}

    //construtor
    public Role(String type) {
        this.type = type;
    }

    //getters e setters
    public Integer getId() {
        return id;
    }

    //define id
    public void setId(Integer id) {
        this.id = id;
    }

    //retorna type
    public String getType() {
        return type;
    }

    //define type
    public void setType(String type) {
        this.type = type;
    }

    //retorna user count
    public int getUserCount() {
        return userCount;
    }

    //define user count
    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
