package com.andre.rinha;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.andre.rinha.adapter.DBUtil;
import com.andre.rinha.adapter.JDBCPostgresAdapter;
import com.andre.rinha.errors.LimitExceededTransactionError;
import com.andre.rinha.errors.UnknownTransactionClientError;
import com.andre.rinha.features.MakeTransactionUseCase;
import com.google.gson.Gson;

import java.sql.Connection;

public class MakeTransactionFunctionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    static Connection connection = DBUtil.createConnection();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var jdbcPostgresAdapter = new JDBCPostgresAdapter(connection);
        var makeTransactionUseCase = new MakeTransactionUseCase(jdbcPostgresAdapter, jdbcPostgresAdapter, null);

        Integer clientId = this.getClientIdFromRequest(request);
        if (clientId == null) {
            context.getLogger().log("Invalid path variable for client id\n");
            return new APIGatewayProxyResponseEvent().withStatusCode(404);
        }

        try {
            Gson gson = new Gson();
            TransactionRequest transactionRequest = gson.fromJson(request.getBody(), TransactionRequest.class);
            ClientAccount clientAccount = makeTransactionUseCase.makeTransaction(transactionRequest);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(gson.toJson(clientAccount));
        } catch (UnknownTransactionClientError err) {
            return new APIGatewayProxyResponseEvent().withStatusCode(404);
        } catch (LimitExceededTransactionError err) {
            return new APIGatewayProxyResponseEvent().withStatusCode(422);
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
