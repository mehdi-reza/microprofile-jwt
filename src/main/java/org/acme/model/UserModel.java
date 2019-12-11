package org.acme.model;

import java.util.Map;

public class UserModel {

	private String subject;
	private String principalName;
	private String userName;
	private String[] roles;
	private Map<String, String> additionalClaims;
	
	public UserModel(String subject, String principalName, String userName, String[] roles, Map<String, String> additionalClaims) {
		this.subject = subject;
		this.principalName = principalName;
		this.userName = userName;
		this.roles = roles;
		this.additionalClaims = additionalClaims;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public Map<String, String> getAdditionalClaims() {
		return additionalClaims;
	}

	public void setAdditionalClaims(Map<String, String> additionalClaims) {
		this.additionalClaims = additionalClaims;
	}

}
