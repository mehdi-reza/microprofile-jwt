package org.acme.jwt;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.JsonWebStructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
public class TokenGenerator {

	final String privateKeyResource = "/META-INF/resources/key.pem";
	
	private PrivateKey privateKey;
	

	private static ObjectMapper mapper = new ObjectMapper();
	
	@Inject
	@ConfigProperty(name="mp.jwt.verify.issuer")
	public String issuer;
	
	@Inject
	@ConfigProperty(name="client_id")
	public String aud;
	
	@Inject
	@ConfigProperty(name="expires_in")
	public Integer DEFAULT_EXPIRATION_IN_SECONDS;
	
	@PostConstruct
	public void readPrivateKey() {

		String encodedPem=null;
		final String errorMessage = "An exception occured while reading private key";
		
		try (InputStream is = TokenGenerator.class.getResourceAsStream(privateKeyResource)) {
			byte[] tmp = new byte[4096];
			int length = is.read(tmp);
			encodedPem = new String(tmp, 0, length, "UTF-8");			
		} catch(IOException e) {
			throw new RuntimeException(errorMessage, e);
		}
		
		encodedPem = encodedPem
				.replaceAll("-----BEGIN (.*)-----", "")
				.replaceAll("-----END (.*)----", "")
				.replaceAll("\r\n", "")
				.replaceAll("\n", "");
		
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedPem.getBytes()));
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
			privateKey = kf.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(errorMessage, e);
		}
	}
	
	public String tokenJson(String subject, String principalName, String userName, Map<String, String> additionalClaims, String ... roles) {
		ObjectNode claimObject = mapper.createObjectNode()
		.put("iss", issuer)
		.put("jti", UUID.randomUUID().toString())
		.put("sub", subject)
		.put("upn", principalName)
		.put("preferred_username", userName)
		.put("aud", aud);
		
		if(additionalClaims!=null) {
			for(String key : additionalClaims.keySet()) {
				if(additionalClaims.get(key)!=null)
					claimObject.put(key, additionalClaims.get(key));
			}
		}

		//claimObject.putObject("roleMappings").put("group1", "Group1MappedRole").put("group2", "Group2MappedRole");
		ArrayNode groups = claimObject.putArray("groups");
		Arrays.stream(roles).forEach(role -> groups.add(role));
		
		return claimObject.toString();
	}
	
	/**
	 * Utility method to generate a JWT string from a JSON resource file that is
	 * signed by the privateKey.pem test resource key, possibly with invalid fields.
	 *
	 * @param jsonResName - name of test resources file
	 * @param timeClaims  - used to return the exp, iat, auth_time claims
	 * @return the JWT string
	 * @throws Exception on parse failure
	 */
	public ObjectNode generate(String tokenJson, Integer expireInSeconds) throws Exception {
		int expires = Optional.ofNullable(expireInSeconds).orElse(DEFAULT_EXPIRATION_IN_SECONDS);
		JsonWebSignature sign = generateTokenString(privateKey, "*****.pem", tokenJson, expires);
		
		ObjectNode payload = (ObjectNode) mapper.readTree(Base64.getDecoder().decode(sign.getEncodedPayload()));
		return mapper.createObjectNode().put("access_token", sign.getCompactSerialization()).put("exp", payload.get("exp").asInt()).put("jti", payload.get("jti").asText());
	}

	private JsonWebSignature generateTokenString(PrivateKey privateKey, String kid, String tokenJson,
			Integer expireInSeconds) throws Exception {

		Objects.requireNonNull(expireInSeconds);
		JwtClaims claims = JwtClaims.parse(tokenJson);
		
		long currentTimeInSecs = currentTimeInSecs();
		long exp = currentTimeInSecs + expireInSeconds;

		claims.setIssuedAt(NumericDate.fromSeconds(currentTimeInSecs));
		claims.setClaim(Claims.auth_time.name(), NumericDate.fromSeconds(currentTimeInSecs));
		claims.setExpirationTime(NumericDate.fromSeconds(exp));

		/*for (Map.Entry<String, Object> entry : claims.getClaimsMap().entrySet()) {
			System.out.printf("\tAdded claim: %s, value: %s\n", entry.getKey(), entry.getValue());
		}*/

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(privateKey);
		jws.setKeyIdHeaderValue(kid);
		jws.setHeader("typ", "JWT");
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

		return jws;
	}

	/**
	 * @return the current time in seconds since epoch
	 */
	private int currentTimeInSecs() {
		long currentTimeMS = System.currentTimeMillis();
		return (int) (currentTimeMS / 1000);
	}

}
