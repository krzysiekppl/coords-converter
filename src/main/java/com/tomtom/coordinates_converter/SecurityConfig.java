package com.tomtom.coordinates_converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override //fragment konfiguracji do zdefiniowania dostępu do przestrzeni aplikacji
    protected void configure(HttpSecurity http) throws Exception {
//        "/add" - blokowanie dokładnie na adres "/add"
//        "/add/*" - blokowanie adresów jeden liść dalej "/add","/add/sth", "/add/any"
//        "/add/**" - blokowanie adresów do końca gałęzi "/add","/add/sth", "/add/any", "/add/any/any" ,"add/any/any/sth"
        http.authorizeRequests()
                .antMatchers("/convert_coordinates").permitAll() //kolejność jest ważna - elementy wyższe nadpisują niższe
                .antMatchers("/").permitAll() //kolejność jest ważna - elementy wyższe nadpisują niższe
                .antMatchers("/login").permitAll()
                .antMatchers("/admin/**").hasAnyAuthority( "ROLE_ADMIN")
                .antMatchers("/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .anyRequest().permitAll()
                .and().csrf().disable()
                .formLogin()
                .loginPage("/login") //tutaj mówimy na jakiego urla aplikacja ma nas przekierować kiedy nie jestesmy zalogowani
                .usernameParameter("loginEmail") //nazwa inputa z loginemw htmlu(formularzu) logowania
                .passwordParameter("loginPassword")//nazwa inputa z hasłem htmlu(formularzu) logowania
                .loginProcessingUrl("/processLogin") //na jaki adres ma zostać wysłany formularz logowania
                .failureUrl("/login?error=1")
                .defaultSuccessUrl("/");
    }

}
