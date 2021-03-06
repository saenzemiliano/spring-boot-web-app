package com.example.springboot.demospringboot;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import com.example.springboot.demospringboot.component.UsrMgrConfiguration;

import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.ldap.urls}")
	private String ldap_urls;

	@Value("${spring.ldap.base-environment.search-base}")
	private String ldap_search_base;

	@Value("${spring.ldap.base-environment.search-filter}")
	private String ldap_search_filter;

	@Autowired
	private DataSource dataSource;
	@Autowired
	private UsrMgrConfiguration usrMgrConfiguration;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (!usrMgrConfiguration.isSecurity()) {
			http.csrf().disable().authorizeRequests().anyRequest().permitAll();
		} else {
			http.authorizeRequests().antMatchers("/", "/img/**", "/vendor/**", "/resources/**", "/page/public/**")
					.permitAll().antMatchers("/repo/**").hasAnyRole("ADMIN").anyRequest().authenticated().and()
					.formLogin().loginPage("/page/login").permitAll().and().logout().permitAll();
		}
	}

	@Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		jdbcUserDetailsManager.setDataSource(dataSource);

		UserDetails user = User.withUsername("user").password("pass").roles("USER", "OPERATOR")
				.passwordEncoder(x -> new BCryptPasswordEncoder().encode(x)).build();

		UserDetails admin = User.withUsername("admin").password("pass").roles("USER", "OPERATOR", "ADMIN")
				.passwordEncoder(x -> new BCryptPasswordEncoder().encode(x)).build();

		UserDetails oUser = User.withUsername("esaenz").password("pass").roles("USER", "OPERATOR")
				.passwordEncoder(x -> new BCryptPasswordEncoder().encode(x)).build();

		addUser(jdbcUserDetailsManager, user);
		addUser(jdbcUserDetailsManager, admin);
		addUser(jdbcUserDetailsManager, oUser);
		return jdbcUserDetailsManager;
	}

//
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .ldapAuthentication()
//                .userSearchBase(ldap_search_base)
//                .userSearchFilter(ldap_search_filter)
//                .contextSource()
//                .url(ldap_urls)
//                .and()
//                .ldapAuthoritiesPopulator(new JdbcAuthoritiesPopulator(dataSource));
//
////        auth
////			.ldapAuthentication()
////				.userDnPatterns("uid={0},ou=people")
////				.groupSearchBase("ou=groups")
////				.contextSource()
////					.url("ldap://localhost:8389/dc=springframework,dc=org")
////					.and()
////				.passwordCompare()
////					.passwordEncoder(new LdapShaPasswordEncoder())
////					.passwordAttribute("userPassword");
//    }
//
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//        UserDetails user
//                = User.withDefaultPasswordEncoder()
//                        .username("user")
//                        .password("password")
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
	private void addUser(JdbcUserDetailsManager jdbcUserDetailsManager, UserDetails user) {
		if (!jdbcUserDetailsManager.userExists(user.getUsername())) {
			jdbcUserDetailsManager.createUser(user);
		} else {
			jdbcUserDetailsManager.updateUser(user);
		}
	}
}
