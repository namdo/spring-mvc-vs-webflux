package io.github.namdo.spring.clientwebflux.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Mono;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class ClientWebFluxController {

  private final String remoteHost;

  private final HttpClient javaHttpClient;

  private final CloseableHttpAsyncClient apacheClient;

  private final WebClient webClient;

  public ClientWebFluxController(@Value("${remote.host}") final String remoteHost) throws IOReactorException {
    this.remoteHost = remoteHost;
    this.javaHttpClient = HttpClient.newBuilder().build();
    this.webClient = WebClient.builder().baseUrl(remoteHost).build();

    final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(IOReactorConfig.custom()
        .setSelectInterval(100)
        .build());

    final PoolingNHttpClientConnectionManager apacheConnectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
    apacheConnectionManager.setDefaultMaxPerRoute(200);
    apacheConnectionManager.setMaxTotal(200);

    this.apacheClient = HttpAsyncClients.custom()
        .setConnectionManager(apacheConnectionManager)
        .build();
    this.apacheClient.start();
  }

  @GetMapping("blockingApacheHttpClient")
  public String blockingApacheHttpClient(@RequestParam final long delay) {
    return useApacheHttpClient(delay).join();
  }

  @GetMapping("nonBlockingApacheHttpClient")
  public CompletableFuture<String> nonBlockingApacheHttpClient(@RequestParam final long delay) {
    return useApacheHttpClient(delay);
  }

  @GetMapping("nonBlockingJavaHttpClient")
  public CompletableFuture<String> nonBlockingJavaHttpClient(@RequestParam final long delay) {
    return useJavaHttpClient(delay);
  }

  @GetMapping("reactiveApacheHttpClient")
  public Mono<String> reactiveApacheHttpClient(@RequestParam final long delay) {
    return Mono.fromFuture(useApacheHttpClient(delay));
  }

  @GetMapping("reactiveJavaHttpClient")
  public Mono<String> reactiveJavaHttpClient(@RequestParam final long delay) {
    return Mono.fromFuture(useJavaHttpClient(delay));
  }

  @GetMapping("reactiveSpringWebClient")
  public Mono<String> reactiveSpringWebClient(@RequestParam final long delay) {
    return webClient.get()
        .uri(remoteHost + "/hello?delay={delay}", delay)
        .retrieve()
        .bodyToMono(String.class);
  }

  private CompletableFuture<String> useJavaHttpClient(final long delay) {
    final HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(String.format("%s/hello?delay=%d", remoteHost, delay)))
        .GET()
        .build();
    return javaHttpClient
        .sendAsync(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
        .thenApply(java.net.http.HttpResponse::body);
  }

  private CompletableFuture<String> useApacheHttpClient(final long delay) {
    final CompletableFuture<HttpResponse> cf = new CompletableFuture<>();
    final FutureCallback<HttpResponse> callback = new HttpResponseFutureCallback(cf);
    final HttpUriRequest request = new HttpGet(remoteHost + "/hello?delay=" + delay);
    apacheClient.execute(request, callback);

    return cf.thenApply(response -> {
      try {
        return EntityUtils.toString(response.getEntity());
      } catch (final Exception e) {
        return null;
      }
    }).exceptionally(Throwable::toString);
  }

  class HttpResponseFutureCallback implements FutureCallback<HttpResponse> {

    private final CompletableFuture<HttpResponse> cf;

    public HttpResponseFutureCallback(final CompletableFuture<HttpResponse> cf) {
      this.cf = cf;
    }

    @Override
    public void completed(final HttpResponse result) {
      cf.complete(result);
    }

    @Override
    public void failed(final Exception ex) {
      cf.completeExceptionally(ex);
    }

    @Override
    public void cancelled() {
      cf.completeExceptionally(new Exception("Cancelled by http async client"));
    }
  }
}
