package br.com.volta.dao;

import br.com.volta.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//acesso ao banco para os perfis de acesso
public class RoleDAO {

    //insere o registro
    public Role insert(String type) {
        String sql = "INSERT INTO role (type) VALUES (?)";
        Role role = new Role(type);

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, type);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    role.setId(generatedKeys.getInt(1));
                }
            }

            if (role.getId() == null) {
                throw new SQLException("ID gerado e nulo");
            }
            return role;

        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir role", e);
        }
    }

    //busca o registro pelo id
    public Role findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM role WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountRole(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar role por id", e);
        }
    }

    //busca o perfil pelo tipo
    public Role findByType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM role WHERE LOWER(type) = LOWER(?) ORDER BY id LIMIT 1";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, type);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountRole(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar role por type", e);
        }
    }

    //lista todos os registros
    public List<Role> findAll() {
        String sql = "SELECT * FROM role ORDER BY id";
        List<Role> roles = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                roles.add(mountRole(result));
            }
            return roles;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar roles", e);
        }
    }

    //filtros combinaveis da listagem de perfis; withUsers limita por vinculo de usuario
    public List<Role> findAllWithFilters(String type, Boolean withUsers) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, COUNT(u.id) AS user_count FROM role r " +
                "LEFT JOIN users u ON u.role_id = r.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isBlank()) {
            sql.append(" AND LOWER(r.type) LIKE LOWER(?)");
            params.add("%" + type.trim() + "%");
        }

        sql.append(" GROUP BY r.id, r.type");
        if (withUsers != null) {
            sql.append(withUsers ? " HAVING COUNT(u.id) > 0" : " HAVING COUNT(u.id) = 0");
        }
        sql.append(" ORDER BY r.id");

        List<Role> roles = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    Role role = mountRole(result);
                    role.setUserCount(result.getInt("user_count"));
                    roles.add(role);
                }
            }
            return roles;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar perfis com filtros", e);
        }
    }

    //atualiza o registro
    public boolean update(Role role) {
        String sql = "UPDATE role SET type = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, role.getType());
            stmt.setInt(2, role.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar role", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("role", id);
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM role";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar roles", e);
        }
    }

    //executa a operacao mountRole
    private Role mountRole(ResultSet result) throws SQLException {
        Role role = new Role();
        role.setId(result.getInt("id"));
        role.setType(result.getString("type"));
        return role;
    }
}
