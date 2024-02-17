package com.andre.rinha.spring.adapter.jdbc;

import com.andre.rinha.ClientAccount;
import com.andre.rinha.spring.BasePostgresTestContainerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.andre.rinha.BalanceUpdateResult.*;

@ActiveProfiles("test")
@DataJpaTest
@ContextConfiguration(initializers = {BasePostgresTestContainerConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientAccountRepositoryAdapterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAccountRepositoryAdapterTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClientAccountRepositoryAdapter underTest;

    @BeforeEach
    public void setup() {
        this.underTest = new ClientAccountRepositoryAdapter(jdbcTemplate);
    }

    @Test
    void shouldDenyDebitUpdateOverLimit() {
        Optional<ClientAccount> clientAccount = underTest.fetchById(1);
        Assertions.assertTrue(clientAccount.isPresent());

        long currentLimit = clientAccount.get().limit();
        long updateValue = currentLimit - 1;


        Assertions.assertEquals(COMPLETED, underTest.update(clientAccount.get().id(), -updateValue).getKey()); // == limit -1
        Assertions.assertEquals(COMPLETED, underTest.update(clientAccount.get().id(), -1L).getKey()); // == limit
        Assertions.assertEquals(LIMIT_EXCEEDED,
                underTest.update(clientAccount.get().id(), -1L).getKey() // == limit + 1 (NOT ALLOWED)
        );
    }

    @Test
    void shouldUpdateConcurrently_WithoutRaceConditions() throws InterruptedException {
        final int clientId = 2;
        final long updateValue = 50L;
        final int concurrentUpdates = 50;

        Optional<ClientAccount> clientAccount = underTest.fetchById(clientId);
        Assertions.assertTrue(clientAccount.isPresent());
        Assertions.assertEquals(0L, clientAccount.get().balance());

        final long expectedBalance = updateValue * concurrentUpdates;

        doSyncAndConcurrently(concurrentUpdates, s -> underTest.update(clientId, updateValue));

        clientAccount = underTest.fetchById(clientId);
        Assertions.assertTrue(clientAccount.isPresent());
        Assertions.assertEquals(expectedBalance, clientAccount.get().balance());
    }

    @Test
    void shouldFailAllConcurrentUpdates_AfterLimitIsReached() throws InterruptedException {
        final int clientId = 3; // limit == 2000
        final long updateValue = -50L;
        final int concurrentUpdates = 50;
        final int expectedSucessfulUpdates = 40; // 40 * 50 == 2000 == limit
        final int expectedFailedUpdates = 10; // 50 - 40

        Optional<ClientAccount> clientAccount = underTest.fetchById(clientId);
        Assertions.assertTrue(clientAccount.isPresent());
        Assertions.assertEquals(0L, clientAccount.get().balance());
        Assertions.assertEquals(2000L, clientAccount.get().limit());

        AtomicInteger sucessfulUpdates = new AtomicInteger(0);
        AtomicInteger failedUpdates = new AtomicInteger(0);

        doSyncAndConcurrently(concurrentUpdates, threadNum -> {
            var updateResult = underTest.update(clientId, updateValue);
            switch (updateResult.getKey()) {
                case LIMIT_EXCEEDED -> failedUpdates.incrementAndGet();
                case COMPLETED -> sucessfulUpdates.incrementAndGet();
            }
        });

        Assertions.assertEquals(expectedSucessfulUpdates, sucessfulUpdates.get());
        Assertions.assertEquals(expectedFailedUpdates, failedUpdates.get());

        clientAccount = underTest.fetchById(clientId);
        Assertions.assertTrue(clientAccount.isPresent());
        Assertions.assertEquals(-2000L, clientAccount.get().balance());
    }

    protected void doSyncAndConcurrently(int threadCount, Consumer<Integer> operation) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            String threadName = "Thread-" + i;
            int finalI = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    operation.accept(finalI);
                } catch (Exception e) {
                    LOGGER.error("error while executing operation {}: {}", threadName, e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await();
    }

}
