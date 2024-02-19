package com.andre.rinha;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.andre.rinha.adapter.DBUtil;
import com.andre.rinha.adapter.JDBCPostgresAdapter;
import com.andre.rinha.errors.UnknownTransactionClientError;
import com.andre.rinha.features.GenerateBalanceStatementUseCase;
import com.google.gson.Gson;

import java.sql.Connection;

public class GetBalanceFunctionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static Connection connection = DBUtil.createConnection();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var jdbcPostgresAdapter = new JDBCPostgresAdapter(connection);
        var getBalanceUseCase = new GenerateBalanceStatementUseCase(jdbcPostgresAdapter, jdbcPostgresAdapter);

        Integer clientId = this.getClientIdFromRequest(request);
        if (clientId == null) {
            context.getLogger().log("Invalid path variable for client id\n");
            return new APIGatewayProxyResponseEvent().withStatusCode(404);
        }

        try {
            ClientAccountStatement statement = getBalanceUseCase.generateStatement(clientId);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(new Gson().toJson(statement));
        } catch (UnknownTransactionClientError err) {
            return new APIGatewayProxyResponseEvent().withStatusCode(404);
        } catch (Exception ex) {
            context.getLogger().log("Internal error: " + ex.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(500);
        }
    }

    private Integer getClientIdFromRequest(APIGatewayProxyRequestEvent request) {
        String clientId = request.getPathParameters().get("clientId");
        boolean isNumeric = clientId.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(clientId) : null;
    }
}
