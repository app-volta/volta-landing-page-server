package br.com.volta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exclui um registro e, antes dele, todos os registros que dependem dele.
 *
 * A lista de dependencias e lida do proprio banco (information_schema), entao
 * o cascade funciona tambem para tabelas que ainda nao possuem DAO proprio
 * (collection_status, review, conversation, message, complaint etc.).
 */
public final class CascadeDeleteDAO {

    //construtor
    private CascadeDeleteDAO() {
        //evita instanciacao da classe utilitaria
    }

    //exclui o registro
    public static boolean delete(String table, Integer id) {
        if (table == null || table.isBlank() || id == null) {
            return false;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try {
                boolean deleted = deleteRow(connection, table, id, new HashSet<>());
                if (!deleted) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DaoException("Erro ao excluir registro e dependentes", e);
            }

        } catch (SQLException e) {
            throw new DaoException("Erro de conexao ao excluir registro", e);
        }
    }

    //exclui primeiro as dependencias e depois a propria linha
    private static boolean deleteRow(Connection connection, String table, int id,
                                     Set<String> visited) throws SQLException {

        String key = table + ":" + id;
        if (!visited.add(key)) {
            //a linha ja foi tratada por outro caminho do cascade
            return true;
        }

        for (ForeignKey foreignKey : foreignKeysReferencing(connection, table)) {
            if (hasIdColumn(connection, foreignKey.childTable())) {
                for (int childId : findIds(connection, foreignKey.childTable(),
                        foreignKey.childColumn(), id)) {
                    deleteRow(connection, foreignKey.childTable(), childId, visited);
                }
            }

            //remove a linha filha; em tabelas sem id (ex.: cooperative_waste_type)
            //esse e o unico passo necessario
            deleteByColumn(connection, foreignKey.childTable(),
                    foreignKey.childColumn(), id);
        }

        return deleteById(connection, table, id);
    }

    //executa a operacao foreignKeysReferencing
    private static List<ForeignKey> foreignKeysReferencing(Connection connection, String table)
            throws SQLException {

        String sql = "SELECT DISTINCT tc.table_name AS child_table, "
                + "kcu.column_name AS child_column "
                + "FROM information_schema.table_constraints tc "
                + "JOIN information_schema.key_column_usage kcu "
                + "  ON kcu.constraint_name = tc.constraint_name "
                + " AND kcu.table_schema = tc.table_schema "
                + "JOIN information_schema.constraint_column_usage ccu "
                + "  ON ccu.constraint_name = tc.constraint_name "
                + " AND ccu.table_schema = tc.table_schema "
                + "WHERE tc.constraint_type = 'FOREIGN KEY' "
                + "  AND tc.table_schema = current_schema() "
                + "  AND ccu.table_name = ? "
                + "ORDER BY tc.table_name, kcu.column_name";

        List<ForeignKey> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, table);
            try (ResultSet rows = stmt.executeQuery()) {
                while (rows.next()) {
                    result.add(new ForeignKey(rows.getString("child_table"),
                            rows.getString("child_column")));
                }
            }
        }
        return result;
    }

    //verifica se possui id column
    private static boolean hasIdColumn(Connection connection, String table) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns "
                + "WHERE table_schema = current_schema() AND table_name = ? "
                + "AND column_name = 'id' LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, table);
            try (ResultSet rows = stmt.executeQuery()) {
                return rows.next();
            }
        }
    }

    //busca os ids das linhas dependentes
    private static List<Integer> findIds(Connection connection, String table,
                                         String column, int parentId) throws SQLException {
        String sql = "SELECT id FROM " + quote(table) + " WHERE " + quote(column) + " = ?";

        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            try (ResultSet rows = stmt.executeQuery()) {
                while (rows.next()) {
                    ids.add(rows.getInt("id"));
                }
            }
        }
        return ids;
    }

    //exclui by id
    private static boolean deleteById(Connection connection, String table, int id)
            throws SQLException {
        String sql = "DELETE FROM " + quote(table) + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    //exclui as linhas que referenciam a coluna
    private static void deleteByColumn(Connection connection, String table,
                                       String column, int parentId) throws SQLException {
        String sql = "DELETE FROM " + quote(table) + " WHERE " + quote(column) + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            stmt.executeUpdate();
        }
    }

    //executa a operacao quote
    private static String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    //desfaz a transacao atual
    private static void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    //executa a operacao ForeignKey
    private record ForeignKey(String childTable, String childColumn) {
    }
}
