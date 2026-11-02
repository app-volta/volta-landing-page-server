package br.com.volta.dao;

import br.com.volta.models.Area;
import br.com.volta.models.Company;
import br.com.volta.models.ContaminationLevel;
import br.com.volta.models.Incident;
import br.com.volta.models.IncidentStatus;
import br.com.volta.models.Priority;
import br.com.volta.models.User;
import br.com.volta.models.WasteType;
import br.com.volta.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//acesso ao banco para as ocorrencias
public class IncidentDAO {

    private static final Logger LOG = Logger.getLogger(IncidentDAO.class.getName());

    //a empresa da ocorrencia vem da area, independente de mudancas posteriores no usuario
    private static final String BASE_SELECT =
            "SELECT i.*, a.company_id AS company_id, comp.name AS company_name, " +
            "a.sector_name AS area_sector_name, u.name AS user_name, w.category AS waste_type_category " +
            "FROM incident i " +
            "JOIN area a ON a.id = i.area_id " +
            "JOIN company comp ON comp.id = a.company_id " +
            "JOIN users u ON u.id = i.user_id " +
            "JOIN waste_type w ON w.id = i.waste_type_id ";

    //insere o registro
    public Incident insert(Incident incident) {
        String sql = "INSERT INTO incident (user_id, area_id, waste_type_id, photo_url, " +
                "employee_description, contamination_level, priority, status, " +
                "registered_at, estimated_weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, incident.getUser().getId());
            stmt.setInt(2, incident.getArea().getId());
            stmt.setInt(3, incident.getWasteType().getId());
            stmt.setString(4, incident.getPhotoUrl());
            stmt.setString(5, incident.getEmployeeDescription());
            stmt.setString(6, incident.getContaminationLevel());
            stmt.setString(7, incident.getPriority() != null
                    ? incident.getPriority().name()
                    : Priority.MEDIA.name());
            stmt.setString(8, incident.getStatus() != null
                    ? incident.getStatus().name()
                    : IncidentStatus.PENDENTE.name());

            if (incident.getRegisteredAt() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(incident.getRegisteredAt()));
            } else {
                stmt.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            }

            if (incident.getEstimatedWeight() != null) {
                stmt.setDouble(10, incident.getEstimatedWeight());
            } else {
                stmt.setNull(10, Types.DECIMAL);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    incident.setId(generatedKeys.getInt(1));
                }
            }

            if (incident.getId() == null) {
                throw new SQLException("ID gerado e nulo");
            }
            return incident;

        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir ocorrencia", e);
        }
    }

    //busca o registro pelo id
    public Incident findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE i.id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountIncident(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar ocorrencia por id", e);
        }
    }

    //lista todos os registros
    public List<Incident> findAll() {
        return findAllWithFilters(null, null, null);
    }

    //busca registros por company
    public List<Incident> findByCompany(Integer companyId) {
        if (companyId == null) {
            return new ArrayList<>();
        }
        return findAllWithFilters(companyId, null, null);
    }

    //busca registros por status
    public List<Incident> findByStatus(IncidentStatus status) {
        return findAllWithFilters(null, null, status);
    }

    //busca registros por company and filters
    public List<Incident> findByCompanyAndFilters(Integer companyId, String sectorName, IncidentStatus status) {
        return findAllWithFilters(companyId, sectorName, status);
    }

    //lista os registros com filters
    public List<Incident> findAllWithFilters(Integer companyId, String sectorName, IncidentStatus status) {
        return findAllWithFilters(companyId, null, null, null, sectorName, status,
                null, null, null, null);
    }

    //filtros combinaveis da listagem de ocorrencias
    public List<Incident> findAllWithFilters(Integer companyId, Integer areaId, Integer userId,
                                             Integer wasteTypeId, String sectorName,
                                             IncidentStatus status, Priority priority,
                                             ContaminationLevel contamination,
                                             LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (companyId != null) {
            sql.append(" AND a.company_id = ?");
            params.add(companyId);
        }

        if (areaId != null) {
            sql.append(" AND i.area_id = ?");
            params.add(areaId);
        }

        if (userId != null) {
            sql.append(" AND i.user_id = ?");
            params.add(userId);
        }

        if (wasteTypeId != null) {
            sql.append(" AND i.waste_type_id = ?");
            params.add(wasteTypeId);
        }

        if (sectorName != null && !sectorName.isBlank()) {
            sql.append(" AND LOWER(a.sector_name) LIKE LOWER(?)");
            params.add("%" + sectorName.trim() + "%");
        }

        if (status != null) {
            sql.append(" AND i.status = ?");
            params.add(status.name());
        }

        if (priority != null) {
            sql.append(" AND i.priority = ?");
            params.add(priority.name());
        }

        if (contamination != null) {
            sql.append(" AND i.contamination_level = ?");
            params.add(contamination.name());
        }

        if (dateFrom != null) {
            sql.append(" AND i.registered_at >= ?");
            params.add(Timestamp.valueOf(dateFrom.atStartOfDay()));
        }

        if (dateTo != null) {
            sql.append(" AND i.registered_at < ?");
            params.add(Timestamp.valueOf(dateTo.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY i.registered_at DESC");
        List<Incident> incidents = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    incidents.add(mountIncident(result));
                }
            }
            return incidents;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar ocorrencias", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM incident";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar ocorrencias", e);
        }
    }

    //atualiza o registro
    public boolean update(Incident incident) {
        String sql = "UPDATE incident SET user_id = ?, area_id = ?, waste_type_id = ?, " +
                "photo_url = ?, employee_description = ?, contamination_level = ?, " +
                "priority = ?, estimated_weight = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, incident.getUser().getId());
            stmt.setInt(2, incident.getArea().getId());
            stmt.setInt(3, incident.getWasteType().getId());
            stmt.setString(4, incident.getPhotoUrl());
            stmt.setString(5, incident.getEmployeeDescription());
            stmt.setString(6, incident.getContaminationLevel());
            stmt.setString(7, incident.getPriority() != null
                    ? incident.getPriority().name()
                    : Priority.MEDIA.name());

            if (incident.getEstimatedWeight() != null) {
                stmt.setDouble(8, incident.getEstimatedWeight());
            } else {
                stmt.setNull(8, Types.DECIMAL);
            }

            stmt.setInt(9, incident.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar ocorrencia", e);
        }
    }

    //atualiza o status do registro
    public boolean updateStatus(Integer id, IncidentStatus newStatus) {
        String sql = "UPDATE incident SET status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, newStatus.name());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar status da ocorrencia", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("incident", id);
    }

    //executa a operacao mountIncident
    private Incident mountIncident(ResultSet result) throws SQLException {
        Incident incident = new Incident();
        incident.setId(result.getInt("id"));
        incident.setPhotoUrl(result.getString("photo_url"));
        incident.setEmployeeDescription(result.getString("employee_description"));

        String contamination = result.getString("contamination_level");
        if (contamination != null) {
            ContaminationLevel level = Validator.parseEnum(ContaminationLevel.class, contamination);
            if (level == null) {
                LOG.log(Level.WARNING, "Nivel de contaminacao invalido na ocorrencia {0}: {1}",
                        new Object[]{incident.getId(), contamination});
            } else {
                incident.setContaminationLevel(level.name());
            }
        }

        double estimatedWeight = result.getDouble("estimated_weight");
        incident.setEstimatedWeight(result.wasNull() ? null : estimatedWeight);

        Timestamp registeredAt = result.getTimestamp("registered_at");
        if (registeredAt != null) {
            incident.setRegisteredAt(registeredAt.toLocalDateTime());
        }

        String priority = result.getString("priority");
        if (priority != null) {
            Priority parsed = Validator.parseEnum(Priority.class, priority);
            if (parsed == null) {
                LOG.log(Level.WARNING, "Prioridade invalida na ocorrencia {0}: {1}",
                        new Object[]{incident.getId(), priority});
            }
            incident.setPriority(parsed);
        }

        String status = result.getString("status");
        if (status != null) {
            IncidentStatus parsed = Validator.parseEnum(IncidentStatus.class, status);
            if (parsed == null) {
                LOG.log(Level.WARNING, "Status invalido na ocorrencia {0}: {1}",
                        new Object[]{incident.getId(), status});
            }
            incident.setStatus(parsed);
        }

        int companyId = result.getInt("company_id");
        if (!result.wasNull()) {
            Company company = new Company();
            company.setId(companyId);
            company.setName(result.getString("company_name"));
            incident.setCompany(company);
        }

        User user = new User();
        user.setId(result.getInt("user_id"));
        user.setName(result.getString("user_name"));
        incident.setUser(user);

        Area area = new Area();
        area.setId(result.getInt("area_id"));
        area.setSectorName(result.getString("area_sector_name"));
        incident.setArea(area);

        WasteType wasteType = new WasteType();
        wasteType.setId(result.getInt("waste_type_id"));
        wasteType.setCategory(result.getString("waste_type_category"));
        incident.setWasteType(wasteType);

        return incident;
    }
}
