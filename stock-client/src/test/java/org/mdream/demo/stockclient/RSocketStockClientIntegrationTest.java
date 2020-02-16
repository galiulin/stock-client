package org.mdream.demo.stockclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
class RSocketStockClientIntegrationTest {

  @Autowired
  private RSocketRequester.Builder builder;

  private RSocketRequester createRSocketRequester() {
    return builder.connectTcp("localhost", 7000).block();
  }

  @Test
  void shouldRetieveStockPricesFromTheService() {
    //given
    RSocketStockClient rSocketStockClient = new RSocketStockClient(createRSocketRequester());

    //when
    Flux<StockPrice> prices = rSocketStockClient.pricesFor("SYMBOL");

    //then
    Assertions.assertNotNull(prices);
    Flux<StockPrice> fivePrices = prices.take(5);
    Assertions.assertEquals(5, fivePrices.count().block());
    Assertions.assertEquals("SYMBOL", fivePrices.blockFirst().getSymbol());

    StepVerifier.create(prices.take(2))
        .expectNextMatches(stockPrice -> stockPrice.getSymbol().equals("SYMBOL"))
        .expectNextMatches(stockPrice -> stockPrice.getSymbol().equals("SYMBOL"))
        .verifyComplete();
  }

}