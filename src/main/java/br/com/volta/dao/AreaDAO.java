package br.com.volta.dao;

import br.com.volta.models.Area;
import br.com.volta.models.Company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//acesso ao banco para as areas
public class AreaDAO {

    private static final Logger LOG = Logger.getLogger(AreaDAO.class.getName());

    //insere o registro
    public Area insert(Area area) {
        String sql = "INSERT INTO area (company_id, sector_name, location_description) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, area.getCompany().getId());
            stmt.setString(2, area.getSectorName());
            stmt.setString(3, area.getLocationDescription());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    area.setId(generatedKeys.getInt(1));
                }
            }

            if (area.getId() == null) {
                throw new SQLException("ID gerado e nulo");
            }
            return area;

        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir area", e);
        }
    }

    //busca o registro pelo id
    public Area findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT a.*, c.name AS company_name FROM area a " +
                "JOIN company c ON c.id = a.company_id WHERE a.id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountArea(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar area por id", e);
        }
    }

    //lista todos os registros
    public List<Area> findAll() {
        String sql = "SELECT a.*, c.name AS company_name FROM area a " +
                "JOIN company c ON c.id = a.company_id ORDER BY a.id";
        List<Area> areas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                areas.add(mountArea(result));
            }
            return areas;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar areas", e);
        }
    }

    //busca registros por company
    public List<Area> findByCompany(Integer companyId) {
        if (companyId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT a.*, c.name AS company_name FROM area a " +
                "JOIN company c ON c.id = a.company_id WHERE a.company_id = ? ORDER BY a.id";
        List<Area> areas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    areas.add(mountArea(result));
                }
            }
            return areas;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar areas por empresa", e);
        }
    }

    //filtros combinaveis da listagem de areas
    public List<Area> findAllWithFilters(Integer companyId, String sectorName, String locationDescription) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, c.name AS company_name FROM area a " +
                "JOIN company c ON c.id = a.company_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (companyId != null) {
            sql.append(" AND a.company_id = ?");
            params.add(companyId);
        }

        if (sectorName != null && !sectorName.isBlank()) {
            sql.append(" AND LOWER(a.sector_name) LIKE LOWER(?)");
            params.add("%" + sectorName.trim() + "%");
        }

        if (locationDescription != null && !locationDescription.isBlank()) {
            sql.append(" AND LOWER(a.location_description) LIKE LOWER(?)");
            params.add("%" + locationDescription.trim() + "%");
        }

        sql.append(" ORDER BY a.id");
        List<Area> areas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    areas.add(mountArea(result));
                }
            }
            return areas;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar areas com filtros", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM area";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar areas", e);
        }
    }

    //atualiza o registro
    public boolean update(Area area) {
        String sql = "UPDATE area SET company_id = ?, sector_name = ?, location_description = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, area.getCompany().getId());
            stmt.setString(2, area.getSectorName());
            stmt.setString(3, area.getLocationDescription());
            stmt.setInt(4, area.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar area", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("area", id);
    }

    //executa a operacao mountArea
    private Area mountArea(ResultSet result) throws SQLException {
        Area area = new Area();
        area.setId(result.getInt("id"));
        area.setSectorName(result.getString("sector_name"));
        area.setLocationDescription(result.getString("location_description"));

        Company company = new Company();
        company.setId(result.getInt("company_id"));
        company.setName(result.getString("company_name"));
        area.setCompany(company);
        return area;
    }
}
