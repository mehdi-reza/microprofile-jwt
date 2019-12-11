package org.acme.spi;

import org.acme.model.UserModel;

public interface UserModelProvider {

	public UserModel getUserByUserName(String userName);
	public boolean checkPassword(UserModel user, String password);
	
}
