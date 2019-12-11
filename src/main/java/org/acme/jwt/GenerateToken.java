package org.acme.jwt;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A simple utility class to generate and print a JWT token string to stdout.
 * Can be run with: mvn exec:java -Dexec.mainClass=org.acme.jwt.GenerateToken
 * -Dexec.classpathScope=test
 */
public class GenerateToken {
	/**
	 *
	 * @param args - [0]: optional name of classpath resource for json document of
	 *             claims to add; defaults to "/JwtClaims.json" [1]: optional time
	 *             in seconds for expiration of generated token; defaults to 300
	 * @throws Exception
	 */
	
	
	public static void main(String[] args) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode claimObject = mapper.createObjectNode()
				.put("iss", "https://quarkus.io/using-jwt-rbac")
				.put("jti", UUID.randomUUID().toString())
				.put("sub", "jdoe-using-jwt-rbac")
				.put("upn", "jdoe@quarkus.io")
				.put("preferred_username", "jdoe")
				.put("aud", "using-jwt-rbac")
				.put("birthdate", "2001-07-13");

		claimObject.putObject("roleMappings").put("group1", "Group1MappedRole").put("group2", "Group2MappedRole");
		claimObject.putArray("groups").add("Echoer").add("Tester").add("Subscriber").add("group2");

		
		TokenGenerator generator = new TokenGenerator();
		generator.readPrivateKey();
		
		String claims = generator.tokenJson("mehdi@venturedive.com", "mehdi", "mehdi", null, "role1", "role2");
		String token = ""; //generator.generate(claims, args.length > 1 ? new Integer(args[1]) : null);
		System.out.println(token);
	}
}
