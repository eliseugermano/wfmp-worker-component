package br.health.workflow.core.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class TokenFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		
		String header = req.getHeader("Authorization");
		
		System.out.println("< ---------------------------------------------------- >");
		System.out.println("\n JWTFilter => "+header+"\n");
		
		if(header==null || !header.startsWith("Bearer ")){
			throw new ServletException("Token inexistente ou inválido.");
		}
		
		String token = header.substring(7); // Extracting only the token's key
		
		String service = req.getRequestURI().split("/")[2]+"/"+req.getRequestURI().split("/")[3];
		String method = req.getMethod();
		String profile;
		
		int count = 1;
		boolean hasProfile = true;
		
		// Verifying that the token is valid
		while(hasProfile){
			try {
				System.out.println("Consultando o serviço de segurança da API ["+count+"] method ["+method+"] Endpoint ["+service+"] ...");
				profile = Key.getKeyByProfile(count); // Key with a set of permissions
				Jwts.parser().setSigningKey(profile).parseClaimsJws(token).getBody();
				
				if(Key.isAllowed(method, service, profile)){
					System.out.println("Acesso autorizado.");
					System.out.println("</ --------------------------------------------------- >");
					chain.doFilter(request, response);
				} else {
					System.out.println("Acesso negado.");
					System.out.println("</ --------------------------------------------------- >");
					((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
				}
				break;
				
			} catch (SignatureException e){
				count ++;
				
				if(count > 4){
					hasProfile=false;
					throw new ServletException(e);
				}
			}
		}
	}
	
}