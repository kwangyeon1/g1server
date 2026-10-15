package org.yht.exps.express.service.sms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.sms.UserMapper;
import org.yht.exps.express.model.sms.user.User;

@Service
@Transactional
public class SmsUserService implements UserDetailsService {
	
	public static final String ROLE_USER = "USER";
	public static final String ROLE_GUEST = "GUEST";
	
    @Autowired
    UserMapper userMapper;
    
    @SuppressWarnings("serial")
	static class SimpleUserDetails implements UserDetails {

		private String username;
		private String password;
		private boolean enabled = true;
		private Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

		public SimpleUserDetails(String username, String pw, String... extraRoles) {
			this.username = username;
			this.password = pw;

			// setup roles
			Set<String> roles = new HashSet<String>();
			roles.addAll(Arrays.<String>asList(null == extraRoles ? new String[0] : extraRoles));

			// export them as part of authorities
			for (String r : roles) {
				authorities.add(new SimpleGrantedAuthority(role(r)));
			}

		}

		public String toString() {
			return "{enabled:" + isEnabled() + ", username:'" + getUsername() + "', password:'" + getPassword() + "'}";
		}

		@Override
		public boolean isEnabled() {
			return this.enabled;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return this.enabled;
		}

		@Override
		public boolean isAccountNonLocked() {
			return this.enabled;
		}

		@Override
		public boolean isAccountNonExpired() {
			return this.enabled;
		}

		@Override
		public String getUsername() {
			return this.username;
		}

		@Override
		public String getPassword() {
			return this.password;
		}

		private String role(String i) {
			return "ROLE_" + i;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return this.authorities;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		if ("".equals(username))	return new SimpleUserDetails("guest", "guest", ROLE_GUEST);
		else						return new SimpleUserDetails(username, username, ROLE_USER);
	}
	
	/**
	 * 사용자 정보 가져오기
	 * @param userId
	 * @return
	 */
	public User getUser(String userId) {
		return userMapper.getUser(userId);
	}
	
	/**
	 * 로그인 아이디로 사용자 정보 가져오기
	 * @param userId
	 * @return
	 */
	public User getUserByLoginId(String userLoginId) {
		return userMapper.getUserByLoginId(userLoginId);
	}
}