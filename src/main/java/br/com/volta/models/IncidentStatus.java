package br.com.volta.models;

//os valores precisam ser iguais aos aceitos pelo banco (chk_incident_status)
public enum IncidentStatus {
    PENDENTE,
    EM_ANALISE,
    RESOLVIDA,
    CANCELADA;

    //so permite avancar no fluxo; resolvida e cancelada sao estados finais
    public boolean canTransitionTo(IncidentStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case PENDENTE -> target == EM_ANALISE || target == CANCELADA;
            case EM_ANALISE -> target == RESOLVIDA || target == CANCELADA;
            case RESOLVIDA, CANCELADA -> false;
        };
    }
}
