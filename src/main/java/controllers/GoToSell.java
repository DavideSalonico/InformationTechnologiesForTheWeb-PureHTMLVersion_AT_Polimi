package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.AuctionDAO;
import beans.Auction;
import beans.User;
import utils.ConnectionHandler;

@WebServlet("/GoToSell")
public class GoToSell extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
    
	private AuctionDAO auctionDAO;
    
    public GoToSell() {
        super();
    }

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	
		connection = ConnectionHandler.getConnection(getServletContext());
		
		auctionDAO = new AuctionDAO(connection);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		User user = (User) request.getSession().getAttribute("user");
		List <Auction> openAuctions;
		List <Auction> closedAuctions;
		
		try {
			openAuctions = auctionDAO.getOpenAuctions(user.getUser_id());
			closedAuctions = auctionDAO.getClosedAuctions(user.getUser_id());
		}catch(SQLException e){
			e.printStackTrace();
			return;
		}

		String path = "/WEB-INF/sell.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		// PASSO VALORI ALLA PAGINA DI RITORNO 
		ctx.setVariable("openAuctions", openAuctions);
		ctx.setVariable("closedAuctions", closedAuctions);
		templateEngine.process(path, ctx, response.getWriter());

	}

	//DEFAULT
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
