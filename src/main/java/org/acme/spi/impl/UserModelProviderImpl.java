package org.acme.spi.impl;

import javax.enterprise.context.ApplicationScoped;

import org.acme.model.UserModel;
import org.acme.spi.UserModelProvider;

@ApplicationScoped
public class UserModelProviderImpl implements UserModelProvider {

	@Override
	public UserModel getUserByUserName(String userName) {
		return null; //new UserModel("mehdi@venturdive.com", "mehdi", "Mehdi Raza", new String[] { "role1", "role2" }, null);
	}

	@Override
	public boolean checkPassword(UserModel user, String password) {
		return true;
	}

}
