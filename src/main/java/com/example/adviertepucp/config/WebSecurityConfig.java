package com.example.adviertepucp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

        http.authorizeRequests()
                .antMatchers("/usuario","/usuario/**").hasAnyAuthority("Alumno","Jefe de Practica","Profesor","Egresado")
                .antMatchers("/seguridad","/seguridad/**").hasAnyAuthority("Seguridad")
                .antMatchers("/administrador","/administrador/**").hasAnyAuthority("Administrativo")
                .anyRequest().permitAll();

        http.logout();
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