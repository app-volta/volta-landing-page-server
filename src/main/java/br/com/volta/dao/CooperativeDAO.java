package br.com.volta.dao;

import br.com.volta.models.Cooperative;
import br.com.volta.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

//acesso ao banco para as cooperativas
public class CooperativeDAO {

    //a media vem da tabela review; sem avaliacoes, o coalesce devolve zero
    private static final String BASE_SELECT =
            "SELECT c.*, COALESCE((SELECT AVG(r.stars) FROM review r " +
            "JOIN collection col ON col.id = r.collection_id " +
            "WHERE col.cooperative_id = c.id), 0) AS average_rating " +
            "FROM cooperative c ";

    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //insere a cooperativa e os tipos de residuo na mesma transacao
    public Cooperative insertWithWasteTypes(Cooperative cooperative, List<Integer> wasteTypeIds) {
        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try {
                insert(cooperative, connection);
                wasteTypeDAO.insertLinks(connection, cooperative.getId(), wasteTypeIds);
                connection.commit();
                return cooperative;

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DaoException("Erro ao inserir cooperativa e tipos de residuo", e);
            }

        } catch (SQLException e) {
            throw new DaoException("Erro de conexao ao inserir cooperativa", e);
        }
    }

    //insere o registro
    private Cooperative insert(Cooperative cooperative, Connection connection) throws SQLException {
        String sql = "INSERT INTO cooperative (name, cnpj, latitude, longitude, cep, " +
                "opening_time, closing_time, monthly_capacity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cooperative.getName());
            stmt.setString(2, cooperative.getCnpj());
            setNullableDouble(stmt, 3, cooperative.getLatitude());
            setNullableDouble(stmt, 4, cooperative.getLongitude());
            stmt.setString(5, cooperative.getCep());

            if (cooperative.getOpeningTime() != null) {
                stmt.setTime(6, Time.valueOf(cooperative.getOpeningTime()));
            } else {
                stmt.setNull(6, Types.TIME);
            }

            if (cooperative.getClosingTime() != null) {
                stmt.setTime(7, Time.valueOf(cooperative.getClosingTime()));
            } else {
                stmt.setNull(7, Types.TIME);
            }

            setNullableInt(stmt, 8, cooperative.getMonthlyCapacity());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cooperative.setId(generatedKeys.getInt(1));
                }
            }
        }

        if (cooperative.getId() == null) {
            throw new SQLException("ID gerado e nulo");
        }
        return cooperative;
    }

    //busca o registro pelo id
    public Cooperative findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE c.id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountCooperative(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar cooperativa por id", e);
        }
    }

    //busca a cooperativa pelo CNPJ (somente digitos)
    public Cooperative findByCnpj(String cnpj) {
        String digits = Validator.onlyDigits(cnpj);
        if (digits.isEmpty()) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE c.cnpj = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, digits);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountCooperative(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar cooperativa por CNPJ", e);
        }
    }

    //lista todos os registros
    public List<Cooperative> findAll() {
        String sql = BASE_SELECT + "ORDER BY c.id";
        List<Cooperative> cooperatives = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                cooperatives.add(mountCooperative(result));
            }
            return cooperatives;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar cooperativas", e);
        }
    }

    //pesquisa os registros
    public List<Cooperative> search(String term) {
        if (term == null || term.isBlank()) {
            return new ArrayList<>();
        }
        String digits = Validator.onlyDigits(term);
        String sql = BASE_SELECT + "WHERE LOWER(c.name) LIKE LOWER(?)"
                + (digits.isEmpty() ? "" : " OR c.cnpj LIKE ?")
                + " ORDER BY c.id";
        List<Cooperative> cooperatives = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, "%" + term + "%");
            if (!digits.isEmpty()) {
                stmt.setString(2, "%" + digits + "%");
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    cooperatives.add(mountCooperative(result));
                }
            }
            return cooperatives;

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar cooperativas", e);
        }
    }

    //filtros combinaveis da listagem de cooperativas
    public List<Cooperative> findAllWithFilters(String name, String cnpj, String cep,
                                                Integer wasteTypeId, Integer minCapacity) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (wasteTypeId != null) {
            sql.append("JOIN cooperative_waste_type cwt ON cwt.cooperative_id = c.id ");
        }
        sql.append("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(c.name) LIKE LOWER(?)");
            params.add("%" + name.trim() + "%");
        }

        String cnpjDigits = Validator.onlyDigits(cnpj);
        if (!cnpjDigits.isEmpty()) {
            sql.append(" AND c.cnpj LIKE ?");
            params.add("%" + cnpjDigits + "%");
        }

        String cepDigits = Validator.onlyDigits(cep);
        if (!cepDigits.isEmpty()) {
            sql.append(" AND c.cep LIKE ?");
            params.add("%" + cepDigits + "%");
        }

        if (wasteTypeId != null) {
            sql.append(" AND cwt.waste_type_id = ?");
            params.add(wasteTypeId);
        }

        if (minCapacity != null) {
            sql.append(" AND c.monthly_capacity >= ?");
            params.add(minCapacity);
        }

        sql.append(" ORDER BY c.id");
        List<Cooperative> cooperatives = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    cooperatives.add(mountCooperative(result));
                }
            }
            return cooperatives;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar cooperativas com filtros", e);
        }
    }

    //usado para filtrar as cooperativas que atendem ao tipo de residuo da ocorrencia
    public List<Cooperative> findByWasteType(Integer wasteTypeId) {
        if (wasteTypeId == null) {
            return new ArrayList<>();
        }
        String sql = BASE_SELECT +
                "JOIN cooperative_waste_type cwt ON cwt.cooperative_id = c.id " +
                "WHERE cwt.waste_type_id = ? ORDER BY c.id";
        List<Cooperative> cooperatives = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, wasteTypeId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    cooperatives.add(mountCooperative(result));
                }
            }
            return cooperatives;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar cooperativas por tipo de residuo", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM cooperative";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar cooperativas", e);
        }
    }

    //atualiza a cooperativa e troca os tipos de residuo na mesma transacao
    public boolean updateWithWasteTypes(Cooperative cooperative, List<Integer> wasteTypeIds) {
        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try {
                if (!update(cooperative, connection)) {
                    connection.rollback();
                    return false;
                }

                wasteTypeDAO.replaceForCooperative(connection, cooperative.getId(), wasteTypeIds);
                connection.commit();
                return true;

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DaoException("Erro ao atualizar cooperativa e tipos de residuo", e);
            }

        } catch (SQLException e) {
            throw new DaoException("Erro de conexao ao atualizar cooperativa", e);
        }
    }

    //atualiza o registro
    private boolean update(Cooperative cooperative, Connection connection) throws SQLException {
        String sql = "UPDATE cooperative SET name = ?, cnpj = ?, latitude = ?, longitude = ?, " +
                "cep = ?, opening_time = ?, closing_time = ?, monthly_capacity = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cooperative.getName());
            stmt.setString(2, cooperative.getCnpj());
            setNullableDouble(stmt, 3, cooperative.getLatitude());
            setNullableDouble(stmt, 4, cooperative.getLongitude());
            stmt.setString(5, cooperative.getCep());

            if (cooperative.getOpeningTime() != null) {
                stmt.setTime(6, Time.valueOf(cooperative.getOpeningTime()));
            } else {
                stmt.setNull(6, Types.TIME);
            }

            if (cooperative.getClosingTime() != null) {
                stmt.setTime(7, Time.valueOf(cooperative.getClosingTime()));
            } else {
                stmt.setNull(7, Types.TIME);
            }

            setNullableInt(stmt, 8, cooperative.getMonthlyCapacity());
            stmt.setInt(9, cooperative.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    //apaga os vinculos de tipo de residuo e a cooperativa na mesma transacao
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("cooperative", id);
    }

    //define nullable double
    private void setNullableDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value != null) {
            stmt.setDouble(index, value);
        } else {
            stmt.setNull(index, Types.DOUBLE);
        }
    }

    //define nullable int
    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    //desfaz a transacao atual
    private void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    //executa a operacao mountCooperative
    private Cooperative mountCooperative(ResultSet result) throws SQLException {
        Cooperative cooperative = new Cooperative();
        cooperative.setId(result.getInt("id"));
        cooperative.setName(result.getString("name"));
        cooperative.setCnpj(result.getString("cnpj"));

        double latitude = result.getDouble("latitude");
        cooperative.setLatitude(result.wasNull() ? null : latitude);

        double longitude = result.getDouble("longitude");
        cooperative.setLongitude(result.wasNull() ? null : longitude);

        cooperative.setAverageRating(result.getDouble("average_rating"));
        cooperative.setCep(result.getString("cep"));

        Time openingTime = result.getTime("opening_time");
        if (openingTime != null) {
            cooperative.setOpeningTime(openingTime.toLocalTime());
        }

        Time closingTime = result.getTime("closing_time");
        if (closingTime != null) {
            cooperative.setClosingTime(closingTime.toLocalTime());
        }

        int monthlyCapacity = result.getInt("monthly_capacity");
        cooperative.setMonthlyCapacity(result.wasNull() ? null : monthlyCapacity);
        return cooperative;
    }
}
