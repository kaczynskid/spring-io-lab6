package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/message")
// #3
@EnableConfigurationProperties(MyMessage.class)
public class MessageController {

//    #1 BEZ REFRESH'A !!!
//    @Value("${my.refreshable.message}")
//    private String msg;

//    #2
//    @Autowired
//    private Environment environment;

//    #3 i 4
    @Autowired
    private MyMessage message;

    @RequestMapping(method = GET)
    public MyMessage message() {
//        #1
//        return new MyMessage(msg);
//        #2
//        return new MyMessage(environment.getProperty("my.refreshable.message"));
//        #3
        return message;
//        #4
//        return new MyMessage(message.message);
    }

}

// # 1, 2, 3
@NoArgsConstructor
@AllArgsConstructor
@Data
// #3
@ConfigurationProperties(prefix = "my.refreshable")
// #4
//@Component
//@RefreshScope
class MyMessage {

    String message;

//    #4
//    @Autowired
//    public MyMessage(@Value("${my.refreshable.message}") String msg) {
//        this.message = msg;
//    }
}
