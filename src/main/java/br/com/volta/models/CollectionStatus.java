package br.com.volta.models;

//os valores precisam ser iguais aos aceitos pelo banco
public enum CollectionStatus {
    SOLICITADA,
    AGENDADA,
    ACEITA,
    EM_ANDAMENTO,
    CONCLUIDA,
    CANCELADA;

    //impede voltar no fluxo ou sair de um estado final
    public boolean canTransitionTo(CollectionStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case SOLICITADA -> target == AGENDADA || target == ACEITA || target == CANCELADA;
            case AGENDADA -> target == ACEITA || target == EM_ANDAMENTO || target == CANCELADA;
            case ACEITA -> target == EM_ANDAMENTO || target == CANCELADA;
            case EM_ANDAMENTO -> target == CONCLUIDA || target == CANCELADA;
            case CONCLUIDA, CANCELADA -> false;
        };
    }
}
