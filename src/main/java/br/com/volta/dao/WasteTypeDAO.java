package br.com.volta.dao;

import br.com.volta.models.RiskLevel;
import br.com.volta.models.WasteType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//acesso ao banco para os tipos de residuo
public class WasteTypeDAO {

    private static final Logger LOG = Logger.getLogger(WasteTypeDAO.class.getName());

    //busca o registro pelo id
    public WasteType findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM waste_type WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountWasteType(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar tipo de residuo por id", e);
        }
    }

    //busca o tipo de residuo pela categoria
    public WasteType findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM waste_type WHERE LOWER(category) = LOWER(?) ORDER BY id LIMIT 1";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, category);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountWasteType(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar tipo de residuo por categoria", e);
        }
    }

    //lista todos os registros
    public List<WasteType> findAll() {
        String sql = "SELECT * FROM waste_type ORDER BY id";
        List<WasteType> wasteTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                wasteTypes.add(mountWasteType(result));
            }
            return wasteTypes;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar tipos de residuo", e);
        }
    }

    //filtros combinaveis da listagem de tipos de residuo
    public List<WasteType> findAllWithFilters(String category, RiskLevel risk, String description) {
        StringBuilder sql = new StringBuilder("SELECT * FROM waste_type WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(category) LIKE LOWER(?)");
            params.add("%" + category.trim() + "%");
        }

        if (risk != null) {
            sql.append(" AND default_risk = ?");
            params.add(risk.name());
        }

        if (description != null && !description.isBlank()) {
            sql.append(" AND LOWER(description) LIKE LOWER(?)");
            params.add("%" + description.trim() + "%");
        }

        sql.append(" ORDER BY id");
        List<WasteType> wasteTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    wasteTypes.add(mountWasteType(result));
                }
            }
            return wasteTypes;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar tipos de residuo com filtros", e);
        }
    }

    //insere o registro
    public WasteType insert(WasteType wasteType) {
        String sql = "INSERT INTO waste_type (category, description, default_risk) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, wasteType.getCategory());
            stmt.setString(2, wasteType.getDescription());
            stmt.setString(3, riskName(wasteType));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    wasteType.setId(generatedKeys.getInt(1));
                }
            }

            if (wasteType.getId() == null) {
                throw new SQLException("ID gerado e nulo");
            }
            return wasteType;

        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir tipo de residuo", e);
        }
    }

    //atualiza o registro
    public boolean update(WasteType wasteType) {
        String sql = "UPDATE waste_type SET category = ?, description = ?, default_risk = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, wasteType.getCategory());
            stmt.setString(2, wasteType.getDescription());
            stmt.setString(3, riskName(wasteType));
            stmt.setInt(4, wasteType.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar tipo de residuo", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("waste_type", id);
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM waste_type";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar tipos de residuo", e);
        }
    }

    //busca registros por cooperative
    public List<WasteType> findByCooperative(Integer cooperativeId) {
        String sql = "SELECT w.* FROM waste_type w " +
                "JOIN cooperative_waste_type cw ON cw.waste_type_id = w.id " +
                "WHERE cw.cooperative_id = ? ORDER BY w.id";
        List<WasteType> wasteTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, cooperativeId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    wasteTypes.add(mountWasteType(result));
                }
            }
            return wasteTypes;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar tipos de residuo da cooperativa", e);
        }
    }

    //troca os vinculos da cooperativa usando a conexao de uma transacao externa
    public void replaceForCooperative(Connection connection, Integer cooperativeId, List<Integer> wasteTypeIds)
            throws SQLException {

        String deleteSql = "DELETE FROM cooperative_waste_type WHERE cooperative_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setInt(1, cooperativeId);
            stmt.executeUpdate();
        }

        insertLinks(connection, cooperativeId, wasteTypeIds);
    }

    //insere os vinculos sem apagar os existentes, usado no cadastro novo
    public void insertLinks(Connection connection, Integer cooperativeId, List<Integer> wasteTypeIds)
            throws SQLException {

        if (wasteTypeIds == null || wasteTypeIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO cooperative_waste_type (cooperative_id, waste_type_id) VALUES (?, ?)";
        Set<Integer> uniqueIds = new LinkedHashSet<>(wasteTypeIds);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Integer wasteTypeId : uniqueIds) {
                if (wasteTypeId == null) {
                    continue;
                }
                stmt.setInt(1, cooperativeId);
                stmt.setInt(2, wasteTypeId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    //executa a operacao riskName
    private String riskName(WasteType wasteType) {
        return wasteType.getDefaultRisk() == null ? null : wasteType.getDefaultRisk().name();
    }

    //executa a operacao mountWasteType
    private WasteType mountWasteType(ResultSet result) throws SQLException {
        WasteType wasteType = new WasteType();
        wasteType.setId(result.getInt("id"));
        wasteType.setCategory(result.getString("category"));
        wasteType.setDescription(result.getString("description"));

        String defaultRisk = result.getString("default_risk");
        if (defaultRisk != null) {
            RiskLevel risk = br.com.volta.util.Validator.parseEnum(RiskLevel.class, defaultRisk);
            if (risk == null) {
                LOG.log(Level.WARNING, "Risco invalido no banco para waste_type {0}: {1}",
                        new Object[]{wasteType.getId(), defaultRisk});
            }
            wasteType.setDefaultRisk(risk);
        }

        return wasteType;
    }
}
