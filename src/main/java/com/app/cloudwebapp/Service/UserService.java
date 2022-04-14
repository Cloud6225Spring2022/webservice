package com.app.cloudwebapp.Service;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.app.cloudwebapp.Model.Account;
import com.app.cloudwebapp.Model.User;
import com.app.cloudwebapp.Model.UserPrincipleDetails;
import com.app.cloudwebapp.Repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import java.util.Optional;



@Service
public class UserService implements UserDetailsService {


    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsername(username);

        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));

        return user.map(UserPrincipleDetails::new).get();


    }
    
    public Account verifyAccount(String email, String token) {
        Account account;
        try {
           // logger.info("verifying account info");
            account = dynamoDBMapper.load(Account.class, email,token);

            if(account.getToken().equals(token)) {
                logger.info("Email: "+email+" is verified!!");
            }else {
                logger.info("Invalid token");
            }
        } catch (AmazonServiceException e) {
            logger.info("Amazon DynamoDB Service error !! \nStackTrace: \n" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode()), "Amazon DynamoDB Service error !! \nStackTrace: \n" + e.getMessage(), e);
        } catch (AmazonClientException e) {
            logger.info("INTERNAL_ERROR while connecting Amazon DynamoDB !! \nStackTrace: \n"+ e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR while connecting Amazon DynamoDB !! \nStackTrace: \n"+ e.getMessage(), e);
        }
        return account;
    }



}
