package br.com.volta.service;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.DatabaseConnection;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.Company;
import br.com.volta.models.Role;
import br.com.volta.models.User;
import br.com.volta.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;

//cadastro publico de empresa: cria a empresa e seu primeiro administrador na mesma transacao
public class RegistrationService {

    private final CompanyDAO companyDAO = new CompanyDAO();
    private final UserDAO userDAO = new UserDAO();

    //cria a empresa e o usuario ADMIN vinculado a ela
    public User registerCompanyAdmin(Company company, String adminName, String email,
                                     String rawPassword, Role adminRole) {

        if (company == null) {
            throw new IllegalArgumentException("Empresa nao informada.");
        }
        if (adminRole == null || adminRole.getId() == null) {
            throw new IllegalArgumentException("Perfil de administrador nao configurado.");
        }

        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(email);
        admin.setPassword(PasswordUtil.hash(rawPassword));
        admin.setPosition("ADMIN");
        admin.setRole(adminRole);

        try (Connection connection = DatabaseConnection.connect()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                Company saved = companyDAO.insert(company, connection);
                admin.setCompany(saved);
                userDAO.insert(admin, connection);
                connection.commit();
                return admin;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e instanceof DaoException ? (DaoException) e
                        : new DaoException("Erro ao cadastrar empresa", e);

            } finally {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                    //a conexao volta ao pool; o Hikari restaura o estado ao devolver
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erro ao cadastrar empresa", e);
        }
    }
}
