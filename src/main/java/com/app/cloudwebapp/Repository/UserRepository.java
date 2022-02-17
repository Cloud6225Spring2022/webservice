package com.app.cloudwebapp.Repository;

import org.hibernate.annotations.OptimisticLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.cloudwebapp.Model.User;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public  interface  UserRepository extends JpaRepository<User, UUID> {


    
    Optional<User> findByUsername(String username);



    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User SET first_name= :fName, last_name= :lName, password=:password, account_updated=:accountUpdated WHERE username in :email")
    public int updateUser(@Param(value = "email") String email, @Param("fName") String fName, @Param("lName") String LName,
                          @Param("password") String password, @Param("accountUpdated") Timestamp accountUpdated);



    

}
