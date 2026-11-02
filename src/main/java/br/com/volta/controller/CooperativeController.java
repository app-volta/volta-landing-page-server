package br.com.volta.controller;

import br.com.volta.dao.CooperativeDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.WasteTypeDAO;
import br.com.volta.models.Cooperative;
import br.com.volta.models.WasteType;
import br.com.volta.util.CnpjService;
import br.com.volta.util.Flash;
import br.com.volta.util.SessionUtil;
import br.com.volta.util.Validator;
import br.com.volta.util.ZipCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//o CRUD de cooperativas nao e acessivel por empresas; cooperativas consultam apenas o proprio cadastro
@WebServlet("/cooperativa")
public class CooperativeController extends BaseController {

    private static final String LIST_PATH = "/cooperativa?action=list";
    private static final String LIST_JSP = "/WEB-INF/views/cooperativa/lista.jsp";
    private static final String FORM_JSP = "/WEB-INF/views/cooperativa/formulario.jsp";

    private final CooperativeDAO cooperativeDAO = new CooperativeDAO();
    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //limites da area de atendimento definidos pelas constraints do banco
    private static final double MIN_LATITUDE = -24.10;
    private static final double MAX_LATITUDE = -23.30;
    private static final double MIN_LONGITUDE = -46.90;
    private static final double MAX_LONGITUDE = -46.30;

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request) && !SessionUtil.isCooperative(request)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("edit".equals(action)) {
                showEditForm(request, response);
            } else if ("new".equals(action)) {
                if (SessionUtil.isSystemAdmin(request)) {
                    prepareForm(request);
                    request.setAttribute("cooperative", new Cooperative());
                    forward(request, response, FORM_JSP);
                } else {
                    redirect(request, response, LIST_PATH);
                }
            } else {
                showList(request, response);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //trata requisicoes post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request)) {
            SessionUtil.forbidden(response);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                deleteCooperative(request, response);
            } else if ("insert".equals(action)) {
                saveCooperative(request, response, false);
            } else if ("update".equals(action)) {
                saveCooperative(request, response, true);
            } else {
                redirect(request, response, LIST_PATH);
            }
        } catch (DaoException e) {
            handleDatabaseError(request, response, LIST_PATH, e);
        }
    }

    //exibe o formulario de edicao
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request)) {
            redirect(request, response, LIST_PATH);
            return;
        }

        Integer id = Validator.parseId(request.getParameter("id"));
        Cooperative cooperative = id != null ? cooperativeDAO.findById(id) : null;
        if (cooperative == null) {
            redirectError(request, response, LIST_PATH, "Cooperativa não encontrada.");
            return;
        }

        cooperative.setWasteTypeIds(loadWasteTypeIds(id));
        prepareForm(request);
        request.setAttribute("cooperative", cooperative);
        forward(request, response, FORM_JSP);
    }

    //exibe a listagem
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Cooperative> cooperatives;
        String nameFilter = Validator.trimToNull(request.getParameter("name"));
        String cnpjFilter = Validator.trimToNull(request.getParameter("cnpj"));
        String cepFilter = Validator.trimToNull(request.getParameter("cep"));
        String wasteTypeValue = Validator.trimToNull(request.getParameter("wasteTypeId"));
        Integer wasteTypeFilter = Validator.parseId(wasteTypeValue);
        String capacityValue = Validator.trimToNull(request.getParameter("minCapacity"));
        Integer minCapacityFilter = Validator.parseInteger(capacityValue);

        if (SessionUtil.isSystemAdmin(request)) {
            cooperatives = cooperativeDAO.findAllWithFilters(
                    nameFilter, cnpjFilter, cepFilter, wasteTypeFilter, minCapacityFilter);
        } else {
            Cooperative own = cooperativeDAO.findById(SessionUtil.cooperativeId(request));
            cooperatives = own != null ? List.of(own) : Collections.emptyList();
        }

        request.setAttribute("cooperatives", cooperatives);
        request.setAttribute("nameFilter", nameFilter);
        request.setAttribute("cnpjFilter", cnpjFilter);
        request.setAttribute("cepFilter", cepFilter);
        request.setAttribute("selectedWasteTypeId", wasteTypeFilter);
        request.setAttribute("minCapacityFilter", capacityValue);
        request.setAttribute("wasteTypes", wasteTypeDAO.findAll());
        forward(request, response, LIST_JSP);
    }

    //exclui cooperative
    private void deleteCooperative(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Integer id = Validator.parseId(request.getParameter("id"));
        if (id == null) {
            redirectError(request, response, LIST_PATH, "Cooperativa não encontrada.");
            return;
        }

        try {
            if (!cooperativeDAO.delete(id)) {
                redirectError(request, response, LIST_PATH, "Cooperativa não encontrada.");
                return;
            }
            redirectSuccess(request, response, LIST_PATH, "Cooperativa excluída com sucesso.");
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao excluir cooperativa", e);
            String message = e.isForeignKeyViolation()
                    ? "Existem registros vinculados a esta cooperativa."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //salva cooperative
    private void saveCooperative(HttpServletRequest request, HttpServletResponse response, boolean update)
            throws ServletException, IOException {

        Integer id = update ? Validator.parseId(request.getParameter("id")) : null;
        Cooperative existing = update && id != null ? cooperativeDAO.findById(id) : null;
        if (update && existing == null) {
            redirectError(request, response, LIST_PATH, "Cooperativa não encontrada.");
            return;
        }

        Cooperative cooperative = new Cooperative();
        cooperative.setId(id);
        cooperative.setName(Validator.trimToNull(request.getParameter("name")));
        cooperative.setCnpj(Validator.trimToNull(request.getParameter("cnpj")));
        cooperative.setCep(Validator.trimToNull(request.getParameter("cep")));

        List<Integer> wasteTypeIds = readWasteTypeIds(request);
        if (wasteTypeIds == null) {
            cooperative.setWasteTypeIds(Collections.emptyList());
            showForm(request, response, cooperative, "Tipo de resíduo inválido.");
            return;
        }
        cooperative.setWasteTypeIds(wasteTypeIds);

        String validationError;
        try {
            validationError = validateCooperative(request, cooperative, existing);
        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao validar CNPJ da cooperativa", e);
            showForm(request, response, cooperative,
                    "Não foi possível concluir a operação. Tente novamente.");
            return;
        }
        if (validationError != null) {
            showForm(request, response, cooperative, validationError);
            return;
        }

        String cnpjDigits = Validator.onlyDigits(cooperative.getCnpj());
        String cepDigits = Validator.onlyDigits(cooperative.getCep());
        cooperative.setCnpj(cnpjDigits);
        cooperative.setCep(cepDigits);

        CoordinateLookup lookup = resolveCoordinates(existing, cooperative, cepDigits);
        if (lookup.error() != null) {
            showForm(request, response, cooperative, lookup.error());
            return;
        }
        String warning = lookup.warning();

        String coordinateError = validateCoordinates(cooperative);
        if (coordinateError != null) {
            showForm(request, response, cooperative, coordinateError);
            return;
        }

        try {
            if (update) {
                if (!cooperativeDAO.updateWithWasteTypes(cooperative, wasteTypeIds)) {
                    redirectError(request, response, LIST_PATH, "Cooperativa não encontrada.");
                    return;
                }
            } else {
                cooperativeDAO.insertWithWasteTypes(cooperative, wasteTypeIds);
            }

            Flash.success(request, update
                    ? "Cooperativa atualizada com sucesso."
                    : "Cooperativa cadastrada com sucesso.");
            if (warning != null) {
                Flash.warning(request, warning);
            }
            redirect(request, response, LIST_PATH);

        } catch (DaoException e) {
            LOG.log(java.util.logging.Level.SEVERE, "Erro ao salvar cooperativa", e);
            String message = e.isUniqueViolation()
                    ? "CNPJ já cadastrado."
                    : "Erro inesperado, tente novamente.";
            redirectError(request, response, LIST_PATH, message);
        }
    }

    //consulta o cep apenas quando ele mudou ou quando faltam coordenadas
    private CoordinateLookup resolveCoordinates(Cooperative existing, Cooperative cooperative, String cepDigits) {
        boolean zipChanged = existing == null || existing.getCep() == null
                || !existing.getCep().equals(cepDigits);
        boolean noCoordinates = cooperative.getLatitude() == null || cooperative.getLongitude() == null;
        if (!zipChanged && !noCoordinates) {
            return CoordinateLookup.ok(null);
        }

        ZipCodeService.ZipCodeResult result = ZipCodeService.find(cepDigits);
        if (result == null) {
            if (zipChanged) {
                cooperative.setLatitude(null);
                cooperative.setLongitude(null);
                return CoordinateLookup.error("Não foi possível localizar o CEP. Confira os 8 dígitos e tente novamente.");
            }
            return CoordinateLookup.ok("Não foi possível atualizar as coordenadas pelo CEP.");
        }

        if (!Validator.isSaoPaulo(result.city(), result.state())) {
            return CoordinateLookup.error("O CEP informado não pertence à cidade de São Paulo.");
        }

        if (zipChanged) {
            cooperative.setLatitude(result.latitude());
            cooperative.setLongitude(result.longitude());
        } else {
            if (result.latitude() != null) {
                cooperative.setLatitude(result.latitude());
            }
            if (result.longitude() != null) {
                cooperative.setLongitude(result.longitude());
            }
        }

        String warning = result.latitude() == null || result.longitude() == null
                ? "Não foi possível localizar as coordenadas pelo CEP."
                : null;
        return CoordinateLookup.ok(warning);
    }

    //resultado da consulta de coordenadas: erro impede o salvamento; aviso apenas informa
    private record CoordinateLookup(String error, String warning) {
        private static CoordinateLookup ok(String warning) {
            return new CoordinateLookup(null, warning);
        }

        private static CoordinateLookup error(String error) {
            return new CoordinateLookup(error, null);
        }
    }

    //valida cooperative
    private String validateCooperative(HttpServletRequest request, Cooperative cooperative, Cooperative existing) {
        if (cooperative.getName() == null || cooperative.getCnpj() == null || cooperative.getCep() == null) {
            return "Preencha todos os campos obrigatórios.";
        }
        if (!Validator.fits(cooperative.getName(), Validator.NAME_MAX)) {
            return "Nome excede o limite de caracteres.";
        }
        if (!Validator.isValidCnpj(cooperative.getCnpj())) {
            return "CNPJ inválido.";
        }
        if (!Validator.isValidZipCode(cooperative.getCep())) {
            return "CEP inválido. Use o formato 00000-000 ou 8 dígitos.";
        }

        String cnpjDigits = Validator.onlyDigits(cooperative.getCnpj());
        Cooperative sameCnpj = cooperativeDAO.findByCnpj(cnpjDigits);
        if (sameCnpj != null && (existing == null || !sameCnpj.getId().equals(existing.getId()))) {
            return "CNPJ já cadastrado.";
        }
        String existingCnpj = existing != null ? Validator.onlyDigits(existing.getCnpj()) : null;
        if (existing == null || !cnpjDigits.equals(existingCnpj)) {
            String cnpjError = CnpjService.validateExistence(cnpjDigits);
            if (cnpjError != null) {
                return cnpjError;
            }
        }

        String capacityValue = Validator.trimToNull(request.getParameter("monthlyCapacity"));
        Integer monthlyCapacity = null;
        if (capacityValue != null) {
            monthlyCapacity = Validator.parseInteger(capacityValue);
            if (monthlyCapacity == null) {
                return "Capacidade mensal inválida.";
            }
            if (monthlyCapacity < 0) {
                return "A capacidade mensal não pode ser negativa.";
            }
        }
        cooperative.setMonthlyCapacity(monthlyCapacity);

        String openingTimeValue = Validator.trimToNull(request.getParameter("openingTime"));
        LocalTime openingTime = null;
        if (openingTimeValue != null) {
            try {
                openingTime = LocalTime.parse(openingTimeValue);
            } catch (DateTimeParseException e) {
                return "Horário de funcionamento inválido.";
            }
        }
        cooperative.setOpeningTime(openingTime);

        String closingTimeValue = Validator.trimToNull(request.getParameter("closingTime"));
        LocalTime closingTime = null;
        if (closingTimeValue != null) {
            try {
                closingTime = LocalTime.parse(closingTimeValue);
            } catch (DateTimeParseException e) {
                return "Horário de fechamento inválido.";
            }
        }
        cooperative.setClosingTime(closingTime);

        if ((openingTime == null) != (closingTime == null)) {
            return "Informe os horários de abertura e fechamento juntos.";
        }
        if (openingTime != null && !closingTime.isAfter(openingTime)) {
            return "O horário de fechamento deve ser maior que o de abertura.";
        }
        return null;
    }

    //confere se as coordenadas retornadas pelo CEP ficam dentro da area atendida
    private String validateCoordinates(Cooperative cooperative) {
        if (cooperative.getLatitude() == null && cooperative.getLongitude() == null) {
            return null;
        }
        if (cooperative.getLatitude() == null || cooperative.getLongitude() == null) {
            return "Não foi possível localizar as coordenadas pelo CEP.";
        }
        if (cooperative.getLatitude() < MIN_LATITUDE || cooperative.getLatitude() > MAX_LATITUDE
                || cooperative.getLongitude() < MIN_LONGITUDE || cooperative.getLongitude() > MAX_LONGITUDE) {
            return "CEP fora da área de atendimento (São Paulo).";
        }
        return null;
    }

    //carrega os tipos de residuo vinculados
    private List<Integer> loadWasteTypeIds(Integer cooperativeId) {
        List<Integer> ids = new ArrayList<>();
        for (WasteType wasteType : wasteTypeDAO.findByCooperative(cooperativeId)) {
            ids.add(wasteType.getId());
        }
        return ids;
    }

    //le os tipos de residuo selecionados
    private List<Integer> readWasteTypeIds(HttpServletRequest request) {
        String[] values = request.getParameterValues("wasteTypeIds");
        if (values == null) {
            return new ArrayList<>();
        }

        Set<Integer> ids = new LinkedHashSet<>();
        for (String value : values) {
            Integer id = Validator.parseId(value);
            if (id == null || wasteTypeDAO.findById(id) == null) {
                return null;
            }
            ids.add(id);
        }
        return new ArrayList<>(ids);
    }

    //prepara os dados do formulario
    private void prepareForm(HttpServletRequest request) {
        request.setAttribute("wasteTypes", wasteTypeDAO.findAll());
    }

    //exibe o formulario
    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Cooperative cooperative, String error)
            throws ServletException, IOException {
        prepareForm(request);
        request.setAttribute("error", error);
        request.setAttribute("cooperative", cooperative);
        forward(request, response, FORM_JSP);
    }
}
