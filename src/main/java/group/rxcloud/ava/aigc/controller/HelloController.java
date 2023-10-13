package group.rxcloud.ava.aigc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/hello")
public class HelloController {

    @GetMapping(path = "health")
    public String health() {
        return "I'm health.";
    }
}
