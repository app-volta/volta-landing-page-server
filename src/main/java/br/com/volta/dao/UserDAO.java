package br.com.volta.dao;

import br.com.volta.models.Company;
import br.com.volta.models.Cooperative;
import br.com.volta.models.Role;
import br.com.volta.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

//acesso ao banco para os usuarios
public class UserDAO {

    //insere o registro
    public User insert(User user) {
        try (Connection connection = DatabaseConnection.connect()) {
            return insert(user, connection);
        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir usuario", e);
        }
    }

    //insere o registro usando uma conexao ja aberta (permite transacao)
    public User insert(User user, Connection connection) throws SQLException {
        String sql = "INSERT INTO users (company_id, cooperative_id, role_id, name, email, password, position) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (user.getCompany() != null && user.getCompany().getId() != null) {
                stmt.setInt(1, user.getCompany().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (user.getCooperative() != null && user.getCooperative().getId() != null) {
                stmt.setInt(2, user.getCooperative().getId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setInt(3, user.getRole().getId());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getPosition());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }

        if (user.getId() == null) {
            throw new SQLException("ID gerado e nulo");
        }
        return user;
    }

    //busca o registro pelo id
    public User findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT u.*, r.type AS role_type FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id WHERE u.id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountUser(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar usuario por id", e);
        }
    }

    //busca o usuario pelo email
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String sql = "SELECT u.*, r.type AS role_type FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id WHERE LOWER(u.email) = LOWER(?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountUser(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar usuario por email", e);
        }
    }

    //lista todos os registros
    public List<User> findAll() {
        String sql = "SELECT u.*, r.type AS role_type FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id ORDER BY u.id";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                users.add(mountUser(result));
            }
            return users;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar usuarios", e);
        }
    }

    //lista os registros com name and company
    public List<User> findAllWithNameAndCompany(String name, Integer companyId) {
        return findAllWithFilters(name, null, null, companyId, null);
    }

    //filtros combinaveis da listagem de usuarios
    public List<User> findAllWithFilters(String name, String email, Integer roleId,
                                         Integer companyId, Integer cooperativeId) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.*, r.type AS role_type, c.name AS company_name, co.name AS cooperative_name " +
                "FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id " +
                "LEFT JOIN company c ON c.id = u.company_id " +
                "LEFT JOIN cooperative co ON co.id = u.cooperative_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(u.name) LIKE LOWER(?)");
            params.add("%" + name.trim() + "%");
        }

        if (email != null && !email.isBlank()) {
            sql.append(" AND LOWER(u.email) LIKE LOWER(?)");
            params.add("%" + email.trim() + "%");
        }

        if (roleId != null) {
            sql.append(" AND u.role_id = ?");
            params.add(roleId);
        }

        if (companyId != null) {
            sql.append(" AND u.company_id = ?");
            params.add(companyId);
        }

        if (cooperativeId != null) {
            sql.append(" AND u.cooperative_id = ?");
            params.add(cooperativeId);
        }

        sql.append(" ORDER BY u.id");
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    users.add(mountUserWithNames(result));
                }
            }
            return users;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar usuarios com filtros", e);
        }
    }

    //busca registros por company
    public List<User> findByCompany(Integer companyId) {
        if (companyId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT u.*, r.type AS role_type FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id WHERE u.company_id = ? ORDER BY u.id";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    users.add(mountUser(result));
                }
            }
            return users;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar usuarios por empresa", e);
        }
    }

    //busca registros por company and name
    public List<User> findByCompanyAndName(Integer companyId, String name) {
        if (companyId == null || name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        String sql = "SELECT u.*, r.type AS role_type FROM users u " +
                "LEFT JOIN role r ON r.id = u.role_id " +
                "WHERE u.company_id = ? AND LOWER(u.name) LIKE LOWER(?) ORDER BY u.id";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            stmt.setString(2, "%" + name + "%");

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    users.add(mountUser(result));
                }
            }
            return users;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar usuarios por empresa e nome", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar usuarios", e);
        }
    }

    //atualiza o registro
    public boolean update(User user) {
        String sql = "UPDATE users SET company_id = ?, cooperative_id = ?, role_id = ?, " +
                "name = ?, email = ?, position = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (user.getCompany() != null && user.getCompany().getId() != null) {
                stmt.setInt(1, user.getCompany().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (user.getCooperative() != null && user.getCooperative().getId() != null) {
                stmt.setInt(2, user.getCooperative().getId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setInt(3, user.getRole().getId());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPosition());
            stmt.setInt(7, user.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar usuario", e);
        }
    }

    //atualiza password
    public boolean updatePassword(Integer id, String passwordHash) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar senha do usuario", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("users", id);
    }

    //executa a operacao mountUserWithNames
    private User mountUserWithNames(ResultSet result) throws SQLException {
        User user = mountUser(result);

        if (user.getCompany() != null) {
            user.getCompany().setName(result.getString("company_name"));
        }
        if (user.getCooperative() != null) {
            user.getCooperative().setName(result.getString("cooperative_name"));
        }

        return user;
    }

    //executa a operacao mountUser
    private User mountUser(ResultSet result) throws SQLException {
        User user = new User();
        user.setId(result.getInt("id"));
        user.setName(result.getString("name"));
        user.setEmail(result.getString("email"));
        user.setPassword(result.getString("password"));
        user.setPosition(result.getString("position"));

        int companyId = result.getInt("company_id");
        if (!result.wasNull()) {
            Company company = new Company();
            company.setId(companyId);
            user.setCompany(company);
        }

        int cooperativeId = result.getInt("cooperative_id");
        if (!result.wasNull()) {
            Cooperative cooperative = new Cooperative();
            cooperative.setId(cooperativeId);
            user.setCooperative(cooperative);
        }

        Role role = new Role();
        role.setId(result.getInt("role_id"));
        role.setType(result.getString("role_type"));
        user.setRole(role);
        return user;
    }
}
