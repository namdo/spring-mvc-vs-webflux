package io.github.namdo.spring.remote.controller;

import java.time.Duration;

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

  /**
   * @param delay Milliseconds of delay
   * @return
   */
  @GetMapping("/hello")
  public Mono<String> sayHelloWorld(@RequestParam final long delay) {
    return Mono.just("Hello World!").delayElement(Duration.ofMillis(delay));
  }

}
