package br.com.volta.dao;

//erro de banco traduzido para uma excecao propria, sem expor detalhes tecnicos
public class DaoException extends RuntimeException {

    private final String sqlState;

    //construtor
    public DaoException(String message, String sqlState, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
    }

    //construtor
    public DaoException(String message, Throwable cause) {
        this(message, cause instanceof java.sql.SQLException sql ? sql.getSQLState() : null, cause);
    }

    //retorna sql state
    public String getSqlState() {
        return sqlState;
    }

    //chave estrangeira impede a exclusao porque existem registros vinculados
    public boolean isForeignKeyViolation() {
        return "23503".equals(sqlState);
    }

    //valor unico duplicado, como cnpj ou email
    public boolean isUniqueViolation() {
        return "23505".equals(sqlState);
    }
}
