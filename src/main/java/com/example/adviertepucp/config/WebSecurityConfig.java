package com.example.adviertepucp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginReactiveAuthenticationManager;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.formLogin()
                .loginPage("/loginForm")
                .loginProcessingUrl("/processLogin")
                .usernameParameter("id")
                .passwordParameter("pwd")
                .defaultSuccessUrl("/redirectByRole",true);

        http.oauth2Login().loginPage("/loginForm").defaultSuccessUrl("/oauth2/login",true);

        http.authorizeRequests()
                .antMatchers("/usuario","/usuario/**").access("isAuthenticated() and not hasAnyAuthority('Administrativo','Seguridad','transitorio')")
                .antMatchers("/seguridad","/seguridad/**").hasAnyAuthority("Seguridad")
                .antMatchers("/administrador","/administrador/**").hasAnyAuthority("Administrativo")
                .antMatchers("/suspendido").access("isAuthenticated() and not hasAnyAuthority('Administrativo','Seguridad','transitorio')")
                .antMatchers("/autenticacion").access("isAuthenticated() and hasAnyAuthority('transitorio')")
                .anyRequest().permitAll();

        http.logout()
                .logoutSuccessUrl("/").deleteCookies().invalidateHttpSession(true);
    }



    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder())
                .usersByUsernameQuery("select codigo,pwd,habilitado from usuario where codigo=?")
                .authoritiesByUsernameQuery("select u.codigo,c.nombre from usuario u inner join categoria c on (u.categoria=c.idcategoria) where habilitado=1 and codigo=?")
                ;

    }


}
