package org.yht.exps.express.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("authenticationToken")
public class AuthenticationToken {
	private String username;
	private String[] roles;
	private Map<String, Boolean> roles2;
	private String result;
	private String token;
	private String refreshToken;
	
	public AuthenticationToken() {
		super();
	}

	public AuthenticationToken(String username, String token, String refreshToken, List<String> roles) {
		Map<String, Boolean> mapOfRoles = new ConcurrentHashMap<String, Boolean>();
		for (String k : roles) {
			mapOfRoles.put(k, true);
		}
		this.roles = roles.toArray(new String[roles.size()]);
		this.roles2 = mapOfRoles;
		this.username = username;
		this.result = "success";
		this.token = token;
		this.refreshToken = refreshToken;
	}
}
