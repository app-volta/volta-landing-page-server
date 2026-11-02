package br.com.volta.util;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DatabaseConnection;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.RoleDAO;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.Company;
import br.com.volta.models.Role;
import br.com.volta.models.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

//garante os cargos do sistema e cria o primeiro admin global a partir do .env
@WebListener
public class AppBootstrap implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppBootstrap.class.getName());

    //executa a inicializacao da aplicacao
    @Override
    public void contextInitialized(ServletContextEvent event) {
        RoleDAO roleDAO = new RoleDAO();
        ensureRole(roleDAO, SessionUtil.ROLE_SYSTEM_ADMIN);
        ensureRole(roleDAO, SessionUtil.ROLE_COMPANY_USER);
        ensureRole(roleDAO, SessionUtil.ROLE_ADMIN);
        ensureRole(roleDAO, SessionUtil.ROLE_COOPERATIVE);
        createSystemAdmin(roleDAO);
    }

    //executa o encerramento da aplicacao
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DatabaseConnection.close();
    }

    //garante a existencia do perfil do sistema
    private void ensureRole(RoleDAO roleDAO, String type) {
        try {
            if (roleDAO.findByType(type) == null) {
                roleDAO.insert(type);
                LOG.info("Cargo criado: " + type);
            }
        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Nao foi possivel garantir o cargo " + type, e);
        }
    }

    //cria system admin
    private void createSystemAdmin(RoleDAO roleDAO) {
        String email = Validator.normalizeEmail(Env.get("ADMIN_EMAIL"));
        String password = Env.get("ADMIN_PASSWORD");

        if (email == null || password == null) {
            LOG.warning("ADMIN_EMAIL/ADMIN_PASSWORD nao configurados; nenhum admin global foi criado.");
            return;
        }
        if (!Validator.isValidEmail(email) || !Validator.isValidPassword(password)) {
            LOG.warning("ADMIN_EMAIL ou ADMIN_PASSWORD invalidos; nenhum admin global foi criado.");
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            Role role = roleDAO.findByType(SessionUtil.ROLE_SYSTEM_ADMIN);
            if (role == null || role.getId() == null) {
                LOG.severe("Cargo SYSTEM_ADMIN indisponivel; admin global nao foi criado.");
                return;
            }

            User existing = userDAO.findByEmail(email);
            if (existing != null) {
                boolean changed = false;

                if (!SessionUtil.isSystemAdmin(existing)) {
                    existing.setRole(role);
                    changed = true;
                }

                if (!SessionUtil.isCompany(existing) && !SessionUtil.isCooperative(existing)) {
                    Company company = resolveAdminCompany();
                    if (company != null) {
                        existing.setCompany(company);
                        changed = true;
                    }
                }

                if (changed) {
                    userDAO.update(existing);
                    LOG.info("Vinculos do admin global ajustados.");
                }

                //sincroniza a senha com o .env apenas uma vez; depois disso a senha
                //pode ser alterada pelo fluxo de recuperacao sem ser sobrescrita
                if (!adminPasswordResetDone()) {
                    if (userDAO.updatePassword(existing.getId(), PasswordUtil.hash(password))) {
                        markAdminPasswordReset();
                        LOG.info("Senha do admin global sincronizada com o .env.");
                    } else {
                        LOG.warning("Nao foi possivel sincronizar a senha do admin global.");
                    }
                }
                return;
            }

            //a tabela users possui a constraint chk_users_owner: o usuario precisa
            //estar vinculado a uma empresa ou cooperativa. Como o schema nao pode
            //ser alterado, o admin global usa uma empresa de referencia configuravel.
            Company company = resolveAdminCompany();
            if (company == null) {
                LOG.warning("Nenhuma empresa disponivel para vincular o admin global; " +
                        "cadastre uma empresa antes ou configure ADMIN_COMPANY_ID.");
                return;
            }

            String adminName = Validator.trimToNull(Env.get("ADMIN_NAME"));
            User admin = new User();
            admin.setName(adminName != null ? adminName : "Administrador do Sistema");
            admin.setEmail(email);
            admin.setPassword(PasswordUtil.hash(password));
            admin.setPosition("Administrador do Sistema");
            admin.setRole(role);
            admin.setCompany(company);

            userDAO.insert(admin);
            markAdminPasswordReset();
            LOG.info("Admin global criado para o email " + email);

        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Nao foi possivel criar o admin global", e);
        }
    }

    //grava o marcador de senha do admin
    private void markAdminPasswordReset() {
        try {
            Files.writeString(adminResetMarker(), Instant.now().toString());
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "Nao foi possivel gravar o marcador de senha do admin", e);
        }
    }

    //verifica se o marcador de senha do admin existe
    private boolean adminPasswordResetDone() {
        try {
            return Files.exists(adminResetMarker());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Nao foi possivel ler o marcador de senha do admin", e);
            return false;
        }
    }

    //marcador na pasta do .env; se nao houver permissao, tenta catalina.base e a pasta temporaria
    private Path adminResetMarker() {
        var envDirectory = Env.getDotenvDirectory();
        if (envDirectory != null) {
            Path base = envDirectory.toPath();
            if (Files.isWritable(base)) {
                return base.resolve(".volta-admin-password-reset");
            }
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            Path base = Path.of(catalinaBase);
            if (Files.isWritable(base)) {
                return base.resolve(".volta-admin-password-reset");
            }
        }
        return Path.of(System.getProperty("java.io.tmpdir"), ".volta-admin-password-reset");
    }

    //permite escolher a empresa de referencia; sem a variavel, usa a primeira cadastrada.
    //se nenhuma empresa existe, cria uma empresa de vinculacao tecnica para o admin global.
    private Company resolveAdminCompany() {
        CompanyDAO companyDAO = new CompanyDAO();
        Integer configuredId = Validator.parseId(Env.get("ADMIN_COMPANY_ID"));
        if (configuredId != null) {
            Company configured = companyDAO.findById(configuredId);
            if (configured != null) {
                return configured;
            }
        }
        var companies = companyDAO.findAll();
        if (!companies.isEmpty()) {
            return companies.get(0);
        }

        //sem empresa cadastrada, cria uma empresa tecnica de vinculacao para o admin global
        //(o schema exige CNPJ de 14 digitos e endereco preenchidos)
        Company technical = new Company();
        technical.setName("Vinculacao Tecnica - Equipe Volta");
        //CNPJ de exemplo valido para que a empresa tecnica possa ser editada no CRUD
        technical.setCnpj("11222333000181");
        technical.setAddress("Registro tecnico para vinculo do administrador do sistema");
        try {
            return companyDAO.insert(technical);
        } catch (DaoException e) {
            LOG.log(Level.WARNING, "Nao foi possivel criar empresa de vinculacao tecnica para o admin global", e);
            return null;
        }
    }
}
