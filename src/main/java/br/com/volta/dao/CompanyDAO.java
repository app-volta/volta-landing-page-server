package br.com.volta.dao;

import br.com.volta.models.Company;
import br.com.volta.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

//acesso ao banco para as empresas
public class CompanyDAO {

    //insere o registro
    public Company insert(Company company) {
        try (Connection connection = DatabaseConnection.connect()) {
            return insert(company, connection);
        } catch (SQLException e) {
            throw new DaoException("Erro ao inserir empresa", e);
        }
    }

    //insere o registro usando uma conexao ja aberta (permite transacao)
    public Company insert(Company company, Connection connection) throws SQLException {
        String sql = "INSERT INTO company (name, cnpj, cep, address, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, company.getName());
            stmt.setString(2, company.getCnpj());
            stmt.setString(3, company.getCep());
            stmt.setString(4, company.getAddress());
            setNullableDouble(stmt, 5, company.getLatitude());
            setNullableDouble(stmt, 6, company.getLongitude());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    company.setId(generatedKeys.getInt(1));
                }
            }
        }

        if (company.getId() == null) {
            throw new SQLException("ID gerado e nulo");
        }
        return company;
    }

    //busca o registro pelo id
    public Company findById(Integer id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM company WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountCompany(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar empresa por id", e);
        }
    }

    //busca a empresa pelo CNPJ (somente digitos)
    public Company findByCnpj(String cnpj) {
        String digits = Validator.onlyDigits(cnpj);
        if (digits.isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM company WHERE cnpj = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, digits);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? mountCompany(result) : null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar empresa por CNPJ", e);
        }
    }

    //lista todos os registros
    public List<Company> findAll() {
        String sql = "SELECT * FROM company ORDER BY id";
        List<Company> companies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {

            while (result.next()) {
                companies.add(mountCompany(result));
            }
            return companies;

        } catch (SQLException e) {
            throw new DaoException("Erro ao listar empresas", e);
        }
    }

    //pesquisa os registros
    public List<Company> search(String term) {
        if (term == null || term.isBlank()) {
            return new ArrayList<>();
        }
        String digits = Validator.onlyDigits(term);
        String sql = "SELECT * FROM company WHERE LOWER(name) LIKE LOWER(?)"
                + (digits.isEmpty() ? "" : " OR cnpj LIKE ?")
                + " ORDER BY id";
        List<Company> companies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, "%" + term + "%");
            if (!digits.isEmpty()) {
                stmt.setString(2, "%" + digits + "%");
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    companies.add(mountCompany(result));
                }
            }
            return companies;

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar empresas", e);
        }
    }

    //filtros combinaveis usados pela listagem administrativa
    public List<Company> findAllWithFilters(String name, String cnpj, String address) {
        StringBuilder sql = new StringBuilder("SELECT * FROM company WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + name.trim() + "%");
        }

        String cnpjDigits = Validator.onlyDigits(cnpj);
        if (!cnpjDigits.isEmpty()) {
            sql.append(" AND cnpj LIKE ?");
            params.add("%" + cnpjDigits + "%");
        }

        if (address != null && !address.isBlank()) {
            sql.append(" AND LOWER(address) LIKE LOWER(?)");
            params.add("%" + address.trim() + "%");
        }

        sql.append(" ORDER BY id");
        List<Company> companies = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    companies.add(mountCompany(result));
                }
            }
            return companies;

        } catch (SQLException e) {
            throw new DaoException("Erro ao buscar empresas com filtros", e);
        }
    }

    //conta os registros
    public int count() {
        String sql = "SELECT COUNT(*) FROM company";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet result = stmt.executeQuery()) {
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new DaoException("Erro ao contar empresas", e);
        }
    }

    //atualiza o registro
    public boolean update(Company company) {
        String sql = "UPDATE company SET name = ?, cnpj = ?, cep = ?, address = ?, " +
                "latitude = ?, longitude = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, company.getName());
            stmt.setString(2, company.getCnpj());
            stmt.setString(3, company.getCep());
            stmt.setString(4, company.getAddress());
            setNullableDouble(stmt, 5, company.getLatitude());
            setNullableDouble(stmt, 6, company.getLongitude());
            stmt.setInt(7, company.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erro ao atualizar empresa", e);
        }
    }

    //preenche coordenadas ausentes sem substituir dados que outro processo ja atualizou
    public boolean updateCoordinatesIfMissing(Integer id, Double latitude, Double longitude) {
        if (id == null || latitude == null || longitude == null) {
            return false;
        }
        String sql = "UPDATE company SET latitude = ?, longitude = ? " +
                "WHERE id = ? AND (latitude IS NULL OR longitude IS NULL)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, latitude);
            stmt.setDouble(2, longitude);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erro ao preencher coordenadas da empresa", e);
        }
    }

    //exclui o registro
    public boolean delete(Integer id) {
        return CascadeDeleteDAO.delete("company", id);
    }

    //executa a operacao mountCompany
    private Company mountCompany(ResultSet result) throws SQLException {
        Company company = new Company();
        company.setId(result.getInt("id"));
        company.setName(result.getString("name"));
        company.setCnpj(result.getString("cnpj"));
        company.setCep(result.getString("cep"));
        company.setAddress(result.getString("address"));
        double latitude = result.getDouble("latitude");
        company.setLatitude(result.wasNull() ? null : latitude);
        double longitude = result.getDouble("longitude");
        company.setLongitude(result.wasNull() ? null : longitude);
        return company;
    }

    private void setNullableDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }
}
