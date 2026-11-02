package br.com.volta.models;

//representa um usuario do sistema
public class User implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    //declaracao de variaveis
    private Integer id;
    private Company company;
    private Cooperative cooperative;
    private Role role;
    private String name;
    private String email;
    private String password;  //guardamos a senha em hash
    private String position;

    //construtor
    public User() {}

    //construtor
    public User(Company company, Role role, String name, String email, String password, String position) {
        this.company = company;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
        this.position = position;
    }

    //construtor para usuario de cooperativa (sem empresa)
    public User(Cooperative cooperative, Role role, String name, String email, String password, String position) {
        this.cooperative = cooperative;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
        this.position = position;
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

    //retorna cooperative
    public Cooperative getCooperative() {
        return cooperative;
    }

    //define cooperative
    public void setCooperative(Cooperative cooperative) {
        this.cooperative = cooperative;
    }

    //retorna role
    public Role getRole() {
        return role;
    }

    //define role
    public void setRole(Role role) {
        this.role = role;
    }

    //retorna name
    public String getName() {
        return name;
    }

    //define name
    public void setName(String name) {
        this.name = name;
    }

    //retorna email
    public String getEmail() {
        return email;
    }

    //define email
    public void setEmail(String email) {
        this.email = email;
    }

    //retorna password
    public String getPassword() {
        return password;
    }

    //define password
    public void setPassword(String password) {
        this.password = password;
    }

    //retorna position
    public String getPosition() {
        return position;
    }

    //define position
    public void setPosition(String position) {
        this.position = position;
    }

    //devolve a representacao textual do objeto
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
