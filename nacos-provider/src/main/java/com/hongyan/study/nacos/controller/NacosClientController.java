package com.hongyan.study.nacos.controller;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;

/**
 * springcloud consumer代码
 */
//@RestController
//@RequestMapping("/client/nacos")
//public class NacosClientController {
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Value("${spring.application.name}")
//    private String serverName;
//
//    @GetMapping("/user/query")
//    public String queryAll(){
//        String url = String.format("http://%s/nacos/user/query",serverName);
//        return restTemplate.getForObject(url,String.class);
//    }
//
//}
