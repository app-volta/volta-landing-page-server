package br.com.volta.controller;

import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.models.Company;
import br.com.volta.models.User;
import br.com.volta.util.CnpjService;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import br.com.volta.util.ZipCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//empresa: admin global gerencia todas; admin de empresa gerencia apenas a propria
@WebServlet("/empresa")
public class CompanyController extends BaseController {

    private static final String LIST_PATH = "/empresa?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/empresa/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/empresa/formulario.jsp";

    private final CompanyDAO companyDAO = new CompanyDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (!SessionUtil.isSystemAdmin(logged) && !SessionUtil.isCompany(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("edit".equals(action)) {
                showEditForm(request, response, logged);
            } else if ("new".equals(action)) {
                if (SessionUtil.isSystemAdmin(logged)) {
                    request.setAttribute("company", new Company());
                    forward(request, response, FORM_JSP);
                } else {
                    redirect(request, response, LIST_PATH);
                }
            } else {
                showList(request, response, logged);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User logged = SessionUtil.user(request);
        if (!SessionUtil.canManage(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteCompany(request, response, logged);
            } else if ("insert".equals(action)) {
                saveCompany(request, response, logged, false);
            } else if ("update".equals(action)) {
                saveCompany(request, response, logged, true);
            } else {
                redirect(request, response, LIST_PATH);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //exibe o formulario de edicao
    private void showEditForm(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        if (!SessionUtil.canManage(logged)) {
            redirect(request, response, LIST_PATH);
            return;
        }

        Integer id = SessionUtil.isSystemAdmin(logged)
                ? Validator.parseId(request.getParameter("id"))
                : SessionUtil.companyId(logged);
        Company company = id != null ? companyDAO.findById(id) : null;
        if (company == null) {
            redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
            return;
        }

        request.setAttribute("company", company);
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response, User logged)
            throws ServletException, IOException {

        List<Company> companies;
        String nameFilter = Validator.trimToNull(request.getParameter("name"));
        String cnpjFilter = Validator.trimToNull(request.getParameter("cnpj"));
        String addressFilter = Validator.trimToNull(request.getParameter("address"));
        if (SessionUtil.isSystemAdmin(logged)) {
            companies = companyDAO.findAllWithFilters(nameFilter, cnpjFilter, addressFilter);
        } else {
            Company own = companyDAO.findById(SessionUtil.companyId(logged));
            companies = own != null ? List.of(own) : Collections.emptyList();
        }

        request.setAttribute("companies", companies);
        request.setAttribute("nameFilter", nameFilter);
        request.setAttribute("cnpjFilter", cnpjFilter);
        request.setAttribute("addressFilter", addressFilter);
        forward(request, response, LIST_JSP);
    }

    //exclui company
    private void deleteCompany(HttpServletRequest request, HttpServletResponse response, User logged)
            throws IOException {

        if (!SessionUtil.isSystemAdmin(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        Integer id = Validator.parseId(request.getParameter("id"));
        if (id == null) {
            redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
            return;
        }

        Integer loggedCompanyId = SessionUtil.companyId(logged);
        if (loggedCompanyId != null && loggedCompanyId.equals(id)) {
            redirectError(request, response, LIST_PATH,
                    "Não é possível excluir a empresa vinculada à conta do administrador.");
            return;
        }

        try {
            if (!companyDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Empresa excluída com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir empresa", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a esta empresa."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva company
    private void saveCompany(HttpServletRequest request, HttpServletResponse response,
                             User logged, boolean update) throws ServletException, IOException {

        if (!update && !SessionUtil.isSystemAdmin(logged)) {
            SessionUtil.forbidden(response);
            return;
        }

        Integer id = update ? Validator.parseId(request.getParameter("id")) : null;
        if (update && id == null) {
            redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
            return;
        }

        Company existing = update ? companyDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
            return;
        }
        if (update && !SessionUtil.canManageCompany(logged, id)) {
            SessionUtil.forbidden(response);
            return;
        }

        Company company = new Company();
        company.setId(id);
        company.setName(Validator.trimToNull(request.getParameter("name")));
        company.setCnpj(Validator.trimToNull(request.getParameter("cnpj")));
        company.setCep(Validator.trimToNull(request.getParameter("cep")));

        String validationError = validateCompany(company);
        if (validationError != null) {
            showForm(request, response, company, validationError);
            return;
        }

        company.setCnpj(Validator.onlyDigits(company.getCnpj()));
        company.setCep(Validator.onlyDigits(company.getCep()));

        String existingCnpj = existing != null ? Validator.onlyDigits(existing.getCnpj()) : null;
        boolean cnpjChanged = existing == null || !company.getCnpj().equals(existingCnpj);
        try {
            Company sameCnpj = companyDAO.findByCnpj(company.getCnpj());
            if (sameCnpj != null && (existing == null || !sameCnpj.getId().equals(existing.getId()))) {
                showForm(request, response, company, "CNPJ já cadastrado.");
                return;
            }
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao verificar CNPJ da empresa", e);
            showForm(request, response, company,
                    "Não foi possível concluir a operação. Tente novamente.");
            return;
        }

        if (cnpjChanged) {
            String cnpjError = CnpjService.validateExistence(company.getCnpj());
            if (cnpjError != null) {
                showForm(request, response, company, cnpjError);
                return;
            }
        }

        String existingCep = existing != null ? Validator.onlyDigits(existing.getCep()) : null;
        boolean cepChanged = existing == null || !company.getCep().equals(existingCep);
        if (!cepChanged && existing.getAddress() != null) {
            company.setAddress(existing.getAddress());
        } else {
            ZipCodeService.AddressResult addressResult = ZipCodeService.findAddress(company.getCep());
            if (addressResult == null) {
                showForm(request, response, company,
                        "Não foi possível localizar o CEP. Confira os 8 dígitos e tente novamente.");
                return;
            }
            if (!Validator.isSaoPaulo(addressResult.city(), addressResult.state())) {
                showForm(request, response, company,
                        "O CEP informado não pertence à cidade de São Paulo.");
                return;
            }
            company.setAddress(addressResult.address());
        }
        if (!Validator.fits(company.getAddress(), Validator.TEXT_MAX)) {
            showForm(request, response, company,
                    "Endereço retornado pelo CEP excede o limite de caracteres.");
            return;
        }

        try {
            if (update) {
                if (!companyDAO.update(company)) {
                    redirectError(request, response, LIST_PATH, "Empresa não encontrada.");
                    return;
                }

                if (!SessionUtil.isSystemAdmin(logged)) {
                    Company updated = companyDAO.findById(id);
                    if (updated != null) {
                        logged.setCompany(updated);
                    }
                }
                redirectSuccess(request, response, LIST_PATH, "Empresa atualizada com sucesso.");
                return;
            }

            companyDAO.insert(company);
            redirectSuccess(request, response, LIST_PATH, "Empresa cadastrada com sucesso.");

        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar empresa", e);
            String message = e.isUniqueViolation()
                    ? "CNPJ já cadastrado."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //valida company
    private String validateCompany(Company company) {
        if (company.getName() == null || company.getCnpj() == null || company.getCep() == null) {
            return "Preencha todos os campos obrigatórios.";
        }
        if (!Validator.fits(company.getName(), Validator.NAME_MAX)) {
            return "Nome excede o limite de caracteres.";
        }
        if (!Validator.isValidCnpj(company.getCnpj())) {
            return "CNPJ inválido.";
        }
        if (!Validator.isValidZipCode(company.getCep())) {
            return "CEP inválido.";
        }
        return null;
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Company company, String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("company", company);
        forward(request, response, FORM_JSP);
    }
}
