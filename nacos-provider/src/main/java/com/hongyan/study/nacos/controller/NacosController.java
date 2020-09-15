package com.hongyan.study.nacos.controller;

import com.hongyan.study.nacos.bean.UserInfo;
import com.hongyan.study.nacos.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * springcloud provider代码
 */
@RefreshScope
@RestController
@RequestMapping("/nacos")
public class NacosController {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @GetMapping("/user/query")
    public List<UserInfo> queryAll(){
        return userInfoMapper.queryAll();
    }

    @Value("${useLocalCache}")
    private boolean useLocalCache;

    @GetMapping(value = "/get")
    @ResponseBody
    public boolean get() {
        return useLocalCache;
    }
}
