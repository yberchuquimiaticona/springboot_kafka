package dev.ychuquimia.product_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hola Spring Boot!";
  }

  @GetMapping("/hello/{name}")
  public String helloName(@PathVariable String name) {
    return "Hola " + name + "!";
  }
}
