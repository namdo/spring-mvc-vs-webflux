package io.github.namdo.spring.clientwebflux

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BootLoadSimulation extends Simulation {

  private val baseUrl = "http://localhost:8081"
  private val blockingApacheHttpClient = "/blockingApacheHttpClient?delay=10"
  private val nonBlockingApacheHttpClient = "/nonBlockingApacheHttpClient?delay=10"
  private val nonBlockingJavaHttpClient = "/nonBlockingJavaHttpClient?delay=10"
  private val reactiveApacheHttpClient = "/reactiveApacheHttpClient?delay=10"
  private val reactiveJavaHttpClient = "/reactiveJavaHttpClient?delay=10"
  private val reactiveSpringWebClient = "/reactiveSpringWebClient?delay=10"
  private val contentType = "application/json"
  private val requestCount = 100
  private val atOnce = 1000

  private val httpConf = http.baseUrl(baseUrl)
    .acceptHeader("application/json;charset=UTF-8")
    .shareConnections

  private val blockingApacheHttpClientTest = repeat(requestCount) {
    exec(http("blockingApacheHttpClientTest")
      .get(blockingApacheHttpClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }
  private val nonBlockingApacheHttpClientTest = repeat(requestCount) {
    exec(http("nonBlockingApacheHttpClientTest")
      .get(nonBlockingApacheHttpClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val nonBlockingJavaHttpClientTest = repeat(requestCount) {
    exec(http("nonBlockingJavaHttpClientTest")
      .get(nonBlockingJavaHttpClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val reactiveApacheHttpClientTest = repeat(requestCount) {
    exec(http("reactiveApacheHttpClientTest")
      .get(reactiveApacheHttpClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val reactiveJavaHttpClientTest = repeat(requestCount) {
    exec(http("reactiveJavaHttpClientTest")
      .get(reactiveJavaHttpClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val reactiveSpringWebClientTest = repeat(requestCount) {
    exec(http("reactiveSpringWebClientTest")
      .get(reactiveSpringWebClient)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val blockingApacheHttpClientScenario = scenario("blockingApacheHttpClientScenario")
    .exec(blockingApacheHttpClientTest)

  private val nonBlockingApacheHttpClientScenario = scenario("nonBlockingApacheHttpClientScenario")
    .exec(nonBlockingApacheHttpClientTest)

  private val nonBlockingJavaHttpClientScenario = scenario("nonBlockingJavaHttpClientScenario")
    .exec(nonBlockingJavaHttpClientTest)

  private val reactiveApacheHttpClientScenario = scenario("reactiveApacheHttpClientScenario")
    .exec(reactiveApacheHttpClientTest)

  private val reactiveJavaHttpClientScenario = scenario("reactiveJavaHttpClientScenario")
    .exec(reactiveJavaHttpClientTest)

  private val reactiveSpringWebClientScenario = scenario("reactiveSpringWebClientScenario")
    .exec(reactiveSpringWebClientTest)


  setUp(
    nonBlockingApacheHttpClientScenario.inject(atOnceUsers(atOnce))
      .andThen(nonBlockingJavaHttpClientScenario.inject(atOnceUsers(atOnce))
        .andThen(reactiveApacheHttpClientScenario.inject(atOnceUsers(atOnce))
          .andThen(reactiveJavaHttpClientScenario.inject(atOnceUsers(atOnce))
            .andThen(reactiveSpringWebClientScenario.inject(atOnceUsers(atOnce)))))))
    .protocols(httpConf);

  //  setUp(blockingApacheHttpClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)
  //  setUp(nonBlockingApacheHttpClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)
  //  setUp(nonBlockingJavaHttpClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)
  //  setUp(reactiveApacheHttpClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)
  //  setUp(reactiveJavaHttpClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)
  //  setUp(reactiveSpringWebClientScenario.inject(atOnceUsers(atOnce))).protocols(httpConf)

}