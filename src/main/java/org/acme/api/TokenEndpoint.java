package org.acme.api;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.jwt.TokenGenerator;
import org.acme.model.UserModel;
import org.acme.spi.UserModelProvider;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Path("oauth")
@ApplicationScoped
public class TokenEndpoint {
	
	@Inject 
	TokenGenerator tokenGenerator;
	
	@Inject 
	UserModelProvider userModelProvider;
	
	private Logger logger = LoggerFactory.getLogger(TokenEndpoint.class);
	
	@Path("token")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response token(@FormParam("grant_type") String grantType, @FormParam("username") String userName, @FormParam("password") String password) {
		
		if(!grantType.equals("password")) {
			logger.error(String.format("%s grant type is not supported", grantType));
			return Response.status(400, String.format("%s grant type is not supported", grantType)).build();
		}
		
		UserModel user = userModelProvider.getUserByUserName(userName);
		if(Objects.isNull(user)) {
			logger.error("User not found");
			return Response.status(500, "User not found").build();
		}
		
		if(!userModelProvider.checkPassword(user, password))
			return Response.status(401, "Invalid credentials").build();
			
		String tokenJson = tokenGenerator.tokenJson(user.getSubject(), user.getPrincipalName(), user.getUserName(), user.getAdditionalClaims(), user.getRoles());
		
		try {
			return Response.ok(tokenGenerator.generate(tokenJson, null).toString()).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
