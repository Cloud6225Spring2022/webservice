package com.app.cloudwebapp.Dependecy;

import com.app.cloudwebapp.Service.UserService;
import com.app.cloudwebapp.Validators.UserValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactory {

    @Bean
    public UserValidator userValidator()
    {
        return new UserValidator();

    }


}
