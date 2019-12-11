package org.acme.api;

import java.security.Principal;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/secured")
@RequestScoped
public class TokenSecuredResource {

	@Inject
	JsonWebToken jwt;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("permit-all")
	@PermitAll
	public String greeting(@Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String name = caller == null ? "anonymous" : caller.getName();
		boolean hasJWT = jwt.getClaimNames() != null;
		String helloReply = String.format("hello + %s, isSecure: %s, authScheme: %s, hasJWT: %s", name, ctx.isSecure(),
				ctx.getAuthenticationScheme(), hasJWT);
		return helloReply;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("role1")
	@Path("role1")
	public String hello(@Context SecurityContext ctx) {
		Principal caller = ctx.getUserPrincipal();
		String name = caller == null ? "anonymous" : caller.getName();
		boolean hasJWT = jwt.getClaimNames() != null;
		String helloReply = String.format("hello + %s, isSecure: %s, authScheme: %s, hasJWT: %s", name, ctx.isSecure(),
				ctx.getAuthenticationScheme(), hasJWT);
		return helloReply;
	}
}