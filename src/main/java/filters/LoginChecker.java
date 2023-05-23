package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter({"/CloseAuction", "/CreateAuction", "/GoToAuctionDetails", "/GoToHome", "/GoToOffer", "/Purchase", "/GoToSell", "/MakeOffer"})
public class LoginChecker implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		System.out.print("Login checker filter executing ...\n");

		// java.lang.String login path = "/index.html";
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/index.html";

		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			res.sendRedirect(loginpath);
			return;
		}

		try {
	        chain.doFilter(request, response);
	    } catch (IOException | ServletException e) {
	        // Handle the exception appropriately
	        e.printStackTrace();
	    };
	}

}
