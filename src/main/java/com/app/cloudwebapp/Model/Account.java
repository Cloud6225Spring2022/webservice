package com.app.cloudwebapp.Model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "csye")
public class Account {
    @DynamoDBHashKey(attributeName="username")
    private String username;
    @DynamoDBRangeKey(attributeName="token")
    private String token;

    private String TimeToLive;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTtl() {
        return TimeToLive;
    }

    public void setTtl(String ttl) {
        this.TimeToLive = ttl;
    }
}
