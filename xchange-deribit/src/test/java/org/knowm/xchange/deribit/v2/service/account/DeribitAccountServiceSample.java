package org.knowm.xchange.deribit.v2.service.account;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeribitAccountServiceSample {
    private static final Logger LOG = LoggerFactory.getLogger(DeribitAccountServiceSample.class);

    private static final String apiKey = "api-key";
    private static final String apiSecret = "api-secret";
    private static final String user = "001";

    public static void main(String[] args) throws InterruptedException {
        DeribitExchange exchange = ExchangeFactory.INSTANCE.createExchange(DeribitExchange.class);

        ExchangeSpecification specification = exchange.getSandboxExchangeSpecification();
        specification.setApiKey(apiKey);
        specification.setSecretKey(apiSecret);
        exchange.applySpecification(specification);

        ScheduledExecutorService openPositionsExecutor = Executors.newScheduledThreadPool(1);
        openPositionsExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("account wallet: " + exchange.getAccountService().getAccountInfo().getWallet());
                    LOG.info("account positions: " + exchange.getAccountService().getAccountInfo().getOpenPositions());
                    LOG.info("account margin info: " + exchange.getAccountService().getAccountInfo().getAccountMargin());
                } catch (Exception e) {
                    LOG.error("error getting positions: " + e);
                    e.printStackTrace();
                }
            }
        }, 0, 15, TimeUnit.SECONDS); // every 15 seconds

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                LOG.info(exchange + " disconnected.\n");
            }
        });

        TimeUnit.HOURS.sleep(1);
    }
}