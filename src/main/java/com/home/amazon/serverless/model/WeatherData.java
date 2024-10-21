package com.home.amazon.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;

@DynamoDbBean
public class WeatherData {

    private String cityName;
    private int id;
    private String main;
    private String description;
    private String icon;

    public WeatherData() {}

    public WeatherData(String cityName, int id, String main, String description, String icon) {
        this.cityName = cityName;
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }

    @DynamoDbPartitionKey
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
