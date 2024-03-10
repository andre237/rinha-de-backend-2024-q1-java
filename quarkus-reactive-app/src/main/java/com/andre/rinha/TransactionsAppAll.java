package com.andre.rinha;

import com.andre.rinha.dto.AccountStatementDTO;
import com.andre.rinha.dto.TransactionRequestDTO;
import com.andre.rinha.dto.TransactionResponseDTO;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Path("/clientes")
public class TransactionsAppAll {

    private static final String UPDATE_BALANCE_SQL_QUERY = "update public.client_account " +
            "set balance = balance + $1 " +
            "where id = $2 " +
            "returning id, \"limit\", balance;";

    private static final String INSERT_TRANSACTION_SQL_QUERY = "insert into public.transaction_requests" +
            "(value, type, description, account_id) " +
            "VALUES ($1, $2, $3, $4)";

    private static final String BALANCE_SQL_QUERY = "select current_timestamp as data_extrato, c.\"limit\", c.balance, t.* " +
            "from public.transaction_requests t " +
            "right join public.client_account c on c.id = t.account_id " +
            "where c.id = $1 order by t.created_at desc limit 10";

    @Inject
    io.vertx.mutiny.sqlclient.Pool pgClient;

    @Inject
    org.jboss.logging.Logger logger;

    public void warmup(@Observes StartupEvent startupEvent) {
        long start = System.currentTimeMillis();
        for (int i = 1; i < 100; i++) {
            Tuple updateValues = Tuple.of(0L, (i % 5) + 1);
            pgClient.preparedQuery(UPDATE_BALANCE_SQL_QUERY).execute(updateValues).await().indefinitely();
        }

        logger.infov("Warmup finished in {0} millis", System.currentTimeMillis() - start);
    }

    @POST
    @Transactional
    @Path("/{clientId}/transacoes")
    public Uni<Response> makeTransaction(@PathParam("clientId") Integer clientId,
                                         TransactionRequestDTO transactionRequest) {
        if (!isValidRequest(transactionRequest))
            return Uni.createFrom().item(Response.status(422).build());

        final Long updateValue = transactionRequest.tipo().equals("c") ?
                +transactionRequest.valor() :
                -transactionRequest.valor();

        Tuple updateValues = Tuple.of(updateValue, clientId);
        Tuple insertValues = Tuple.of(transactionRequest.valor(), transactionRequest.tipo(), transactionRequest.descricao(), clientId);

        return pgClient.preparedQuery(UPDATE_BALANCE_SQL_QUERY).execute(updateValues)
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? TransactionResponseDTO.fromRow(iterator.next()) : null)
                .flatMap(r -> r != null ?
                        pgClient.preparedQuery(INSERT_TRANSACTION_SQL_QUERY).execute(insertValues).replaceWith(r) :
                        Uni.createFrom().nullItem()
                )
                .onItem().transform(r -> Response.status(r != null ? 200 : 404).entity(r).build())
                .onFailure().recoverWithItem(ex -> Response.status(ex.getMessage().contains("v2_check") ? 422 : 500).build());
    }

    @GET
    @Path("{clientId}/extrato")
    public Uni<Response> generateStatement(@PathParam("clientId") Integer clientId) {
        return pgClient.preparedQuery(BALANCE_SQL_QUERY).execute(Tuple.of(clientId))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(this::toTuple)
                .collect().asList()
                .onItem().transform(this::mapTuplesToResponse);
    }

    private boolean isValidRequest(TransactionRequestDTO request) {
        if (request.valor() == null || request.valor() <= 0)
            return false;

        if (request.tipo() == null)
            return false;

        return request.descricao() != null && !request.descricao().isEmpty() && request.descricao().length() <= 10;
    }

    private Pair<AccountStatementDTO.Balance, TransactionRequestDTO> toTuple(Row row) {
        AccountStatementDTO.Balance balance = AccountStatementDTO.Balance.fromRow(row);
        TransactionRequestDTO transcation = TransactionRequestDTO.fromRow(row);
        return Pair.of(balance, transcation);
    }

    private Response mapTuplesToResponse(List<Pair<AccountStatementDTO.Balance, TransactionRequestDTO>> tuples) {
        if (tuples.isEmpty()) return Response.status(404).build();

        AccountStatementDTO.Balance balance = null;
        List<TransactionRequestDTO> transactions = new ArrayList<>();
        for (var tuple : tuples) {
            if (balance == null) balance = tuple.getLeft();
            if (tuple.getRight() != null) transactions.add(tuple.getRight());
        }

        return Response.status(200).entity(new AccountStatementDTO(balance, transactions)).build();
    }

}
