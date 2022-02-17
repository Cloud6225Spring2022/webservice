package com.app.cloudwebapp.Config;



import com.app.cloudwebapp.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@EnableWebSecurity
public class UserSecurityConfigration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;


    @Autowired
    private UserDetailsService userDetailsService;


    @Autowired
    public void SecurityConfiguration(UserService userPrincipalDetailsService) {
        this.userService = userPrincipalDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.csrf().disable();


        http
                .httpBasic()
                .and()
                .authorizeRequests().antMatchers("/api/user/self/**").authenticated().anyRequest()
                .permitAll();


    }







    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
