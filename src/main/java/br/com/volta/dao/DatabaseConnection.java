package br.com.volta.dao;

import br.com.volta.util.Env;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

//fonte unica de conexoes; o pool e criado sob demanda para nao derrubar o deploy sem banco
public final class DatabaseConnection {

    private static volatile HikariDataSource dataSource;

    //construtor
    private DatabaseConnection() {
        //evita instanciacao da classe utilitaria
    }

    //abre uma conexao com o banco
    public static Connection connect() throws SQLException {
        return getDataSource().getConnection();
    }

    //fecha os recursos abertos
    public static void close() {
        HikariDataSource current = dataSource;
        if (current != null) {
            current.close();
            dataSource = null;
        }
    }

    //retorna data source
    private static HikariDataSource getDataSource() throws SQLException {
        HikariDataSource current = dataSource;
        if (current != null) {
            return current;
        }

        synchronized (DatabaseConnection.class) {
            if (dataSource == null) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    }

    //cria data source
    private static HikariDataSource createDataSource() throws SQLException {
        String url = Env.get("DB_URL");
        String username = Env.get("DB_USERNAME");
        String password = Env.get("DB_PASSWORD");

        if (url == null || username == null || password == null) {
            throw new SQLException("DB_URL, DB_USERNAME e DB_PASSWORD precisam estar configurados.");
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("volta-pool");
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        //folga para o banco remoto responder no primeiro acesso da aplicacao
        config.setConnectionTimeout(15_000);
        config.setValidationTimeout(5_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setInitializationFailTimeout(-1);

        //evita que uma conexao remota travada bloqueie o startup ou uma requisicao
        config.addDataSourceProperty("connectTimeout", "10000");
        config.addDataSourceProperty("socketTimeout", "30000");
        config.addDataSourceProperty("loginTimeout", "10");
        return new HikariDataSource(config);
    }

}
