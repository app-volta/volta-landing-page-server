package br.com.volta.dao;

import br.com.volta.models.Collection;
import br.com.volta.models.CollectionStatus;
import br.com.volta.models.Company;
import br.com.volta.models.Cooperative;
import br.com.volta.models.Incident;
import br.com.volta.models.WasteType;
import br.com.volta.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

//acesso ao banco para as coletas
public class CollectionDAO {

    private static final Logger LOG = Logger.getLogger(CollectionDAO.class.getName());

    //carrega empresa e cooperativa junto com a coleta para as verificacoes de permissao
    private static final String BASE_SELECT =
            "SELECT c.*, i.waste_type_id AS waste_type_id, a.company_id AS company_id, " +
            "comp.name AS company_name, coop.name AS cooperative_name " +
            "FROM collection c " +
            "JOIN incident i ON i.id = c.incident_id " +
            "JOIN area a ON a.id = i.area_id " +
            "JOIN company comp ON comp.id = a.company_id " +
            "JOIN cooperative coop ON coop.id = c.cooperative_id ";

    //insere uma coleta nova e seu historico inicial em uma unica transacao
    public Collection insert(Collection collection) {
        String sql = "INSERT INTO collection (incident_id, cooperative_id, request_date, " +
                "scheduled_date, current_status, collection_type, urgent, estimated_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, collection.getIncident().getId());
                stmt.setInt(2, collection.getCooperative().getId());

                //a data do pedido fica no mesmo minuto do formulario para respeitar
                //a constraint scheduled_date >= request_date
                LocalDateTime requestDate = collection.getRequestDate() != null
                        ? collection.getRequestDate()
                        : LocalDateTime.now().withSecond(0).withNano(0);
                collection.setRequestDate(requestDate);
                stmt.setTimestamp(3, Timestamp.valueOf(requestDate));

                if (collection.getScheduledDate() != null) {
                    stmt.setTimestamp(4, Timestamp.valueOf(collection.getScheduledDate()));
                } else {
                    stmt.setNull(4, Types.TIMESTAMP);
                }

                stmt.setString(5, collection.getCurrentStatus() != null
                        ? collection.getCurrentStatus().name()
                        : CollectionStatus.SOLICITADA.name());
                stmt.setString(6, collection.getCollectionType());
                stmt.setBoolean(7, collection.isUrgent());

                if (collection.getEstimatedTime() != null) {
                    stmt.setObject(8, collection.getEstimatedTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss")), Types.OTHER);
                } else {
                    stmt.setNull(8, Types.OTHER);
                }

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        collection.setId(generatedKeys.getInt(1));
                    }
                }

                if (collection.getId() == null) {
                    throw new SQLException("ID gerado e nulo");
                }

                recordStatusHistory(connection, collection.getId(),
                        collection.getCurrentStatus() != null
                                ? collection.getCurrentStatus()
                                : CollectionStatus.SOLICITADA,
                        "Coleta criada");

                connection.commit();
                return collection;

            } catch (SQLException e) {
                rollback(connection, e);
                collection.setId(null);
                throw new DaoException("Erro ao inserir coleta", e);
            }

        } catch (SQLException e) {
            collection.setId(null);
            throw new DaoException("Erro de conexao ao inserir coleta", e);
        }
    }

    //busca o registro pelo id
    public Collection findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE c.id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountCollection(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar coleta por id", e);
        }
    }

    //lista todos os registros
    public List<Collection> findAll() {
        return findByFilters(null, null);
    }

    //busca registros por cooperative
    public List<Collection> findByCooperative(Integer cooperativeId) {
        return findByFilters(null, cooperativeId);
    }

    //busca registros por company
    public List<Collection> findByCompany(Integer companyId) {
        if (companyId == null) {
            return new ArrayList<>();
        }
        return findByFilters(companyId, null);
    }

    //busca registros por filters
    public List<Collection> findByFilters(Integer companyId, Integer cooperativeId) {
        return findAllWithFilters(companyId, cooperativeId, null, null, null, null, null, null);
    }

    //filtros combinaveis da listagem de coletas
    public List<Collection> findAllWithFilters(Integer companyId, Integer cooperativeId,
                                               Integer incidentId, CollectionStatus status,
                                               Boolean urgent, String collectionType,
                                               LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (companyId != null) {
            sql.append(" AND a.company_id = ?");
            params.add(companyId);
        }

        if (cooperativeId != null) {
            sql.append(" AND c.cooperative_id = ?");
            params.add(cooperativeId);
        }

        if (incidentId != null) {
            sql.append(" AND c.incident_id = ?");
            params.add(incidentId);
        }

        if (status != null) {
            sql.append(" AND c.current_status = ?");
            params.add(status.name());
        }

        if (urgent != null) {
            sql.append(" AND c.urgent = ?");
            params.add(urgent);
        }

        if (collectionType != null && !collectionType.isBlank()) {
            sql.append(" AND c.collection_type = ?");
            params.add(collectionType.trim().toUpperCase(Locale.ROOT));
        }

        if (dateFrom != null) {
            sql.append(" AND c.request_date >= ?");
            params.add(Timestamp.valueOf(dateFrom.atStartOfDay()));
        }

        if (dateTo != null) {
            sql.append(" AND c.request_date < ?");
            params.add(Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY c.request_date DESC");
        List<Collection> collections = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    collections.add(mountCollection(result));
                }
            }
            return collections;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar coletas", e);
        }
    }

    //busca registros por incident
    public List<Collection> findByIncident(Integer incidentId) {
        String sql = BASE_SELECT + "WHERE c.incident_id = ? ORDER BY c.request_date DESC";
        List<Collection> collections = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, incidentId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    collections.add(mountCollection(result));
                }
            }
            return collections;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar coletas por ocorrencia", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM collection";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar coletas", e);
        }
    }

    //atualiza o registro
    public boolean update(Collection collection) {
        String sql = "UPDATE collection SET incident_id = ?, cooperative_id = ?, scheduled_date = ?, " +
                "collection_type = ?, urgent = ?, estimated_time = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, collection.getIncident().getId());
            stmt.setInt(2, collection.getCooperative().getId());

            if (collection.getScheduledDate() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(collection.getScheduledDate()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }

            stmt.setString(4, collection.getCollectionType());
            stmt.setBoolean(5, collection.isUrgent());

            if (collection.getEstimatedTime() != null) {
                stmt.setObject(6, collection.getEstimatedTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss")), Types.OTHER);
            } else {
                stmt.setNull(6, Types.OTHER);
            }

            stmt.setInt(7, collection.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar coleta", e);
        }
    }

    //atualiza o status atual e registra no historico em uma unica transacao
    public boolean updateStatus(Integer id, CollectionStatus newStatus) {
        String sql = "UPDATE collection SET current_status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newStatus.name());
                stmt.setInt(2, id);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                recordStatusHistory(connection, id, newStatus, "Status alterado para " + newStatus.name());
                connection.commit();
                return true;

            } catch (SQLException e) {
                rollback(connection, e);
                throw new DaoException("Erro ao atualizar status da coleta", e);
            }

        } catch (SQLException e) {
            throw new DaoException("Erro de conexao ao atualizar status da coleta", e);
        }
    }

    //grava a data explicitamente para nao depender do fuso/default do banco
    private void recordStatusHistory(Connection connection, Integer collectionId,
                                     CollectionStatus status, String observation) throws SQLException {
        String sql = "INSERT INTO collection_status (collection_id, status, change_date, observation) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            stmt.setString(2, status.name());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, observation);
            stmt.executeUpdate();
        }
    }

    //atualiza a data agendada da coleta
    public boolean updateScheduledDate(Integer id, LocalDateTime scheduledDate) {
        String sql = "UPDATE collection SET scheduled_date = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (scheduledDate != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(scheduledDate));
            } else {
                stmt.setNull(1, Types.TIMESTAMP);
            }
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar data agendada da coleta", e);
        }
    }

    //apaga o historico e a coleta na mesma transacao
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("collection", id);
    }

    //desfaz a transacao atual
    private void rollback(Connection connection, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    //executa a operacao mountCollection
    private Collection mountCollection(ResultSet result) throws SQLException {
        Collection collection = new Collection();
        collection.setId(result.getInt("id"));
        collection.setCollectionType(result.getString("collection_type"));
        collection.setUrgent(result.getBoolean("urgent"));

        String currentStatus = result.getString("current_status");
        if (currentStatus != null) {
            CollectionStatus parsed = Validator.parseEnum(CollectionStatus.class, currentStatus);
            if (parsed == null) {
                LOG.log(Level.WARNING, "Status invalido na coleta {0}: {1}",
                        new Object[]{collection.getId(), currentStatus});
            }
            collection.setCurrentStatus(parsed);
        }

        Timestamp requestDate = result.getTimestamp("request_date");
        if (requestDate != null) {
            collection.setRequestDate(requestDate.toLocalDateTime());
        }

        Timestamp scheduledDate = result.getTimestamp("scheduled_date");
        if (scheduledDate != null) {
            collection.setScheduledDate(scheduledDate.toLocalDateTime());
        }

        Time estimatedTime = result.getTime("estimated_time");
        if (estimatedTime != null) {
            collection.setEstimatedTime(estimatedTime.toLocalTime());
        }

        Incident incident = new Incident();
        incident.setId(result.getInt("incident_id"));

        int companyId = result.getInt("company_id");
        if (!result.wasNull()) {
            Company company = new Company();
            company.setId(companyId);
            company.setName(result.getString("company_name"));
            incident.setCompany(company);
        }

        int wasteTypeId = result.getInt("waste_type_id");
        if (!result.wasNull()) {
            WasteType wasteType = new WasteType();
            wasteType.setId(wasteTypeId);
            incident.setWasteType(wasteType);
        }

        collection.setIncident(incident);

        Cooperative cooperative = new Cooperative();
        cooperative.setId(result.getInt("cooperative_id"));
        cooperative.setName(result.getString("cooperative_name"));
        collection.setCooperative(cooperative);
        return collection;
    }
}
