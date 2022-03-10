package com.app.cloudwebapp.Model;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@JsonIgnoreProperties(value = {"upload_date"},
        allowGetters = true)
@Entity
@Table
public class ProfilePic {
    @Id
    @GeneratedValue
    @Column(length=16)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String file_name;

    private String url;

    @Column
    private Timestamp upload_date;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(Timestamp upload_date) {
        this.upload_date = upload_date;
    }

    public ProfilePic(UUID id, User user, String file_name, String url, Timestamp upload_date) {
        this.id = id;
        this.user = user;
        this.file_name = file_name;
        this.url = url;
        this.upload_date = upload_date;
    }

    public ProfilePic(){

    }
}
