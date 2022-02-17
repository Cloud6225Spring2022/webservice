package com.app.cloudwebapp.Validators;

import com.app.cloudwebapp.Model.User;
import com.app.cloudwebapp.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class UserValidator implements Validator {

    @Autowired
    UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        User user = (User) target;
        System.out.println("Validate function executed  with target " + user.getUsername());



    }



}
