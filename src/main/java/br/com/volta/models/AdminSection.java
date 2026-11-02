package br.com.volta.models;

//fonte unica das secoes administrativas; novas entidades entram aqui e no painel
public enum AdminSection {

    COMPANIES("companies", "Empresas", "/empresa?action=list"),
    COOPERATIVES("cooperatives", "Cooperativas", "/cooperativa?action=list"),
    AREAS("areas", "Áreas", "/area?action=list"),
    INCIDENTS("incidents", "Ocorrências", "/ocorrencia?action=list"),
    COLLECTIONS("collections", "Coletas", "/coleta?action=list"),
    USERS("users", "Usuários", "/usuario?action=list"),
    ROLES("roles", "Perfis de acesso", "/perfil?action=list"),
    WASTE_TYPES("wasteTypes", "Tipos de resíduo", "/tipo-residuo?action=list");

    private final String key;
    private final String label;
    private final String path;

    AdminSection(String key, String label, String path) {
        this.key = key;
        this.label = label;
        this.path = path;
    }

    //retorna key
    public String getKey() {
        return key;
    }

    //retorna label
    public String getLabel() {
        return label;
    }

    //retorna path
    public String getPath() {
        return path;
    }
}
