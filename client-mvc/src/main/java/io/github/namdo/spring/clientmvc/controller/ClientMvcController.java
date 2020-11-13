package io.github.namdo.spring.clientmvc.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class ClientMvcController {

  @Value("${remote.host}")
  String remoteHost;

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping("/resttemplate")
  public String restTemplate(@RequestParam final long delay) {
    return restTemplate.getForEntity(remoteHost + "/hello?delay={delay}", String.class, delay)
        .getBody();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
