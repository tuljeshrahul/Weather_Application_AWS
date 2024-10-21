package com.home.amazon.serverless.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.amazon.serverless.model.WeatherData;
import com.home.amazon.serverless.utils.DependencyFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetItemFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbEnhancedClient dbClient;
    private final String tableName;
    private final TableSchema<WeatherData> weatherTableSchema;

    public GetItemFunction() {
        dbClient = DependencyFactory.dynamoDbEnhancedClient();
        tableName = DependencyFactory.tableName();
        weatherTableSchema = TableSchema.fromBean(WeatherData.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String response = "";
        LambdaLogger logger = context.getLogger();
        DynamoDbTable<WeatherData> weatherTable = dbClient.table(tableName, weatherTableSchema);

        try {
            List<WeatherData> allWeatherData = weatherTable.scan().items().stream().collect(Collectors.toList());
            response = new ObjectMapper().writeValueAsString(allWeatherData);
        } catch (JsonProcessingException e) {
            logger.log("Failed to create a JSON response: " + e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error processing weather data");
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(Boolean.FALSE)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(response);
    }


}
