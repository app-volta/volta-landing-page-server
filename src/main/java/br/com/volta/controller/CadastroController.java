package br.com.volta.controller;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.RoleDAO;
import br.com.volta.dao.UserDAO;
import br.com.volta.models.Company;
import br.com.volta.models.Role;
import br.com.volta.models.User;
import br.com.volta.service.RegistrationService;
import br.com.volta.util.CnpjService;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import br.com.volta.util.ZipCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;

//cadastro publico de empresas; o primeiro usuario da empresa recebe o cargo ADMIN
@WebServlet("/cadastro")
public class CadastroController extends BaseController {

    private static final String FORM_JSP = "/WEB-INF/views/cadastro/cadastro.jsp";
    private static final String LOGIN_PATH = "/login#form-login";
    private static final String PASSWORD_ERROR =
            "A senha precisa ter no mínimo 8 caracteres, uma letra maiúscula e um caractere especial (., @, #, etc).";

    private final CompanyDAO companyDAO = new CompanyDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final RegistrationService registrationService = new RegistrationService();

    //exibe o formulario publico de cadastro
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (logged != null) {
            redirect(request, response, SessionUtil.homePath(logged));
            return;
        }
        forward(request, response, FORM_JSP);
    }

    //processa o cadastro da empresa e do seu administrador
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = Validator.trimToNull(request.getParameter("name"));
        String email = Validator.normalizeEmail(request.getParameter("email"));
        String cnpj = Validator.onlyDigits(request.getParameter("cnpj"));
        String cep = Validator.onlyDigits(request.getParameter("cep"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Company company = new Company();
        company.setName(name);
        company.setCnpj(cnpj.isEmpty() ? null : cnpj);
        company.setCep(cep.isEmpty() ? null : cep);

        String validationError = validate(name, email, cnpj, cep, password, confirmPassword);
        if (validationError != null) {
            showForm(request, response, company, email, validationError);
            return;
        }

        try {
            if (companyDAO.findByCnpj(cnpj) != null) {
                showForm(request, response, company, email, "CNPJ já cadastrado.");
                return;
            }
            if (userDAO.findByEmail(email) != null) {
                showForm(request, response, company, email, "E-mail já cadastrado.");
                return;
            }
        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Erro ao verificar duplicidade no cadastro público", e);
            showForm(request, response, company, email,
                    "Não foi possível concluir o cadastro. Tente novamente.");
            return;
        }

        String cnpjError = CnpjService.validateExistence(cnpj);
        if (cnpjError != null) {
            showForm(request, response, company, email, cnpjError);
            return;
        }

        ZipCodeService.AddressResult addressResult = ZipCodeService.findAddress(cep);
        if (addressResult == null) {
            showForm(request, response, company, email,
                    "Não foi possível localizar o CEP. Confira os 8 dígitos e tente novamente.");
            return;
        }
        if (!Validator.isSaoPaulo(addressResult.city(), addressResult.state())) {
            showForm(request, response, company, email,
                    "O CEP informado não pertence à cidade de São Paulo.");
            return;
        }
        company.setAddress(addressResult.address());
        if (!Validator.fits(company.getAddress(), Validator.TEXT_MAX)) {
            showForm(request, response, company, email,
                    "Endereço retornado pelo CEP excede o limite de caracteres.");
            return;
        }

        try {
            Role adminRole = roleDAO.findByType(SessionUtil.ROLE_ADMIN);
            if (adminRole == null) {
                LOG.severe("Cargo ADMIN indisponível para o cadastro público de empresas.");
                showForm(request, response, company, email,
                        "Perfil de administrador não configurado. Tente novamente mais tarde.");
                return;
            }

            registrationService.registerCompanyAdmin(company, name, email, password, adminRole);
            redirectSuccess(request, response, LOGIN_PATH,
                    "Cadastro realizado com sucesso. Faça login para acessar sua empresa.");

        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Erro no cadastro público de empresa", e);
            String message = e.isUniqueViolation()
                    ? "CNPJ ou e-mail já cadastrado."
                    : "Não foi possível concluir o cadastro. Tente novamente.";
            showForm(request, response, company, email, message);
        }
    }

    //valida os dados do formulario antes de abrir a transacao
    private String validate(String name, String email, String cnpj, String cep,
                            String password, String confirmPassword) {

        if (name == null || email == null || cnpj == null || cep == null) {
            return "Preencha todos os campos obrigatórios.";
        }
        if (!Validator.fits(name, Validator.NAME_MAX)) {
            return "Nome da empresa excede o limite de caracteres.";
        }
        if (!Validator.isValidCnpj(cnpj)) {
            return "CNPJ inválido.";
        }
        if (!Validator.isValidZipCode(cep)) {
            return "CEP inválido.";
        }
        if (!Validator.isValidEmail(email)) {
            return "E-mail inválido.";
        }
        if (password == null || password.isBlank()) {
            return "Informe uma senha.";
        }
        if (!Validator.isValidPassword(password)) {
            return PASSWORD_ERROR;
        }
        if (!password.equals(confirmPassword)) {
            return "As senhas não coincidem.";
        }
        return null;
    }

    //devolve o formulario com os dados preenchidos
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Company company, String email, String error)
            throws ServletException, IOException {

        request.setAttribute("company", company);
        request.setAttribute("email", email);
        request.setAttribute("error", error);
        forward(request, response, FORM_JSP);
    }
}
