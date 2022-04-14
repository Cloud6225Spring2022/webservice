package com.app.cloudwebapp.Model;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;







@JsonIgnoreProperties(value = {"account_created", "account_updated"},
        allowGetters = true)
@Entity
@Table
    public class User {

//    @GenericGenerator(
//            name = "UUID",
//            strategy = "org.hibernate.id.UUIDGenerator"
//    )
//    @Column(name = "id", updatable = false, nullable = false)
    @Id
    @GeneratedValue
    @Column(length=16)
    private UUID id;


   // @NotNull
    //@Column
    private String first_name;

   // @NotNull
   // @Column
    private String last_name;

    //@NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    //@Column
    private String password;




    @Column(nullable = false, unique = true)
    @Email
    private String username;




    public String getUsername() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }


    @Column
    private Timestamp account_created;


    @Column
    private Timestamp account_updated;
    
    private Boolean isActive = false;
    
    



        public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean isActive) {
		this.isActive = isActive;
	}


		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private boolean active = true;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Timestamp getAccount_created() {
            return account_created;
        }

        public void setAccount_created(Timestamp account_created) {
            this.account_created = account_created;
        }

        public Timestamp getAccount_updated() {
            return account_updated;
        }

        public void setAccount_updated(Timestamp account_updated) {
            this.account_updated = account_updated;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", first_name='" + first_name + '\'' +
                    ", last_name='" + last_name + '\'' +
                    ", username='" + username + '\'' +
                    ", account_created=" + account_created +
                    ", account_updated=" + account_updated +
                    '}';
        }
    }
