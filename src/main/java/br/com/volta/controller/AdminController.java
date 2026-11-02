package br.com.volta.controller;

import br.com.volta.models.AdminSection;
import br.com.volta.dao.AreaDAO;
import br.com.volta.dao.CollectionDAO;
import br.com.volta.dao.CompanyDAO;
import br.com.volta.dao.CooperativeDAO;
import br.com.volta.dao.DaoException;
import br.com.volta.dao.IncidentDAO;
import br.com.volta.dao.RoleDAO;
import br.com.volta.dao.UserDAO;
import br.com.volta.dao.WasteTypeDAO;
import br.com.volta.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

//painel inicial do admin global
@WebServlet("/admin")
public class AdminController extends BaseController {

    private static final String PANEL_JSP = "/WEB-INF/views/login/painel.jsp";

    private final CompanyDAO companyDAO = new CompanyDAO();
    private final CooperativeDAO cooperativeDAO = new CooperativeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AreaDAO areaDAO = new AreaDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final CollectionDAO collectionDAO = new CollectionDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final WasteTypeDAO wasteTypeDAO = new WasteTypeDAO();

    //trata requisicoes get
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.isSystemAdmin(request)) {
            SessionUtil.forbidden(response);
            return;
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        try {
            counts.put(AdminSection.COMPANIES.getKey(), companyDAO.count());
            counts.put(AdminSection.COOPERATIVES.getKey(), cooperativeDAO.count());
            counts.put(AdminSection.USERS.getKey(), userDAO.count());
            counts.put(AdminSection.AREAS.getKey(), areaDAO.count());
            counts.put(AdminSection.INCIDENTS.getKey(), incidentDAO.count());
            counts.put(AdminSection.COLLECTIONS.getKey(), collectionDAO.count());
            counts.put(AdminSection.ROLES.getKey(), roleDAO.count());
            counts.put(AdminSection.WASTE_TYPES.getKey(), wasteTypeDAO.count());
        } catch (DaoException e) {
            LOG.log(Level.SEVERE, "Erro ao carregar contadores do painel", e);
            counts.clear();
            for (AdminSection section : AdminSection.values()) {
                counts.put(section.getKey(), 0);
            }
            request.setAttribute("error", "Servico indisponivel, tente novamente.");
        }

        request.setAttribute("sectionCounts", counts);
        request.setAttribute("operationSections", List.of(
                AdminSection.COMPANIES,
                AdminSection.COOPERATIVES,
                AdminSection.AREAS,
                AdminSection.INCIDENTS,
                AdminSection.COLLECTIONS
        ));
        request.setAttribute("accessSections", List.of(
                AdminSection.USERS,
                AdminSection.WASTE_TYPES,
                AdminSection.ROLES
        ));
        forward(request, response, PANEL_JSP);
    }
}
