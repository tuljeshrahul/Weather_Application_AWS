package com.home.amazon.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import com.home.amazon.serverless.model.WeatherData;
import com.home.amazon.serverless.utils.DependencyFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

public class PostItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<WeatherData> weatherTableSchema;
    private final String API_KEY = "3301519246e749b9b45247cd0fb0291c";
    private final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=" + API_KEY;

    public PostItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        weatherTableSchema = TableSchema.fromBean(WeatherData.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String cityName = request.getQueryStringParameters().get("city");
        String responseBody = "";

        if (cityName != null && !cityName.isEmpty()) {
            try {
                String weatherData = fetchWeatherData(cityName);
                WeatherData weatherItem = extractWeatherData(weatherData, cityName);

                if (weatherItem != null) {
                    DynamoDbTable<WeatherData> weatherTable = dbClient.table(tableName, weatherTableSchema);
                    WeatherData savedItem = weatherTable.updateItem(weatherItem);
                    responseBody = new ObjectMapper().writeValueAsString(savedItem);
                }
            } catch (Exception e) {
                context.getLogger().log("Error processing request: " + e.getMessage());
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(500)
                        .withBody("Error processing request");
            }
        } else {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("City name is required");
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.emptyMap())
                .withBody(responseBody);
    }

    private String fetchWeatherData(String cityName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_URL, cityName)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private WeatherData extractWeatherData(String jsonData, String cityName) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonData);
        JsonNode weatherNode = rootNode.path("weather").get(0);

        return new WeatherData(
                cityName,
                weatherNode.path("id").asInt(),
                weatherNode.path("main").asText(),
                weatherNode.path("description").asText(),
                weatherNode.path("icon").asText()
        );
    }
}