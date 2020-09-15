# springcloud集成nacos实现注册中心、配置中心

#### 什么是Nacos？

Nacos 致力于帮助您发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，帮助您快速实现动态服务发现、服务配置、服务元数据及流量管理。

Nacos 的关键特性包括:

**服务发现和服务健康监测**

**动态配置服务**

**动态 DNS 服务**

**服务及其元数据管理**

具体见：[Nacos官网及文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)







#### 快速开始：

#### 一、启动nacos服务（单机模式&内嵌数据库）

下载源码或者安装包
安装包地址：https://github.com/alibaba/nacos/releases
解压后进入nacos/bin目录
输入命令启动服务

```
#linux：
sh startup.sh -m standalone
#windows：
cmd startup.cmd
```


控制台启动下，看到"Nacos started successfully in stand alone mode.”后表示服务已启动

nacos默认使用8848端口，访问http://127.0.0.1:8848/nacos/index.html

进入自带的控制台界面，默认用户名/密码是nacos/nacos

![image-20200915151207549](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200915151207549.png)

### 二、配置集群

> 3个或3个以上Nacos节点才能构成集群，仅支持linux/unix/mac

1. 在nacos的解压目录**conf**目录下，有配置文件**cluster.conf**（若无则手动创建），每行配置成**ip:port**。（配置3个或3个以上节点）

```shell
#cluster.conf
192.168.0.1:8848
192.168.0.2:8848
192.168.0.3:8848
1234
```

2. 配置后在各个节点服务器输入命令启动所有服务：`sh startup.sh`

### 三、配置Mysql

> 默认使用嵌入式数据库，0.7版本以后增加支持mysql数据源能力

1. 初始化nacos相关表：运行**conf/nacos-mysql.sql**文件
2. 修改**conf/application.properties**文件，增加支持mysql数据源配置（目前只支持mysql），添加mysql数据源的url、用户名和密码

```text
spring.datasource.platform=mysql
db.num=1
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
db.user=user
db.password=password
12345
```

1. 配置后输入命令启动服务（参照上文后续启动服务命令）



#### SpringCloud项目中接入Nacos作为配置中心

采用nacos作为配置中心，ms级别内实现配置的动态变更



实施步骤：

1. 引入spring-cloud-starter-alibaba-nacos-config 所需jar包

```
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>${latest.version}</version>
</dependency>

<!--为了实现springcloud功能，咱们引入alibaba微服务-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>${spring.cloud.alibaba.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```



2. 新增bootstrap.yml/bootstrap.properties文件用于配置nacos server配置(实现nacos server地址、应用名等，必须包含应用名称、nacos注册地址，之所以需要配置 `spring.application.name` ，是因为它是构成 Nacos 配置管理 `dataId`字段的一部分)

   `dataId` 的完整格式如下：

   ```
   ${prefix}-${spring.profiles.active}.${file-extension}
   ```

   主要是因为如果需要实现配置文件动态更新nacos需要读取服务文件，写入application.yml文件内无效，bootstrap.yml文件优先级高于application.yml

```
spring:
  application:
    name: spring-cloud-nacos
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
      discovery:
        server-addr: 127.0.0.1:8848
```

application.yml文件中用于配置其他application相关配置（如果已经在nacos服务端配置了spring-cloud-nacos.yml文件，则无需配置application.yml文件）

```
server:
  port: 8080
#mybatis实体类扫描路径
mybatis:
  type-aliases-package: com.hongyan.study.nacos.bean
#spring相关配置(例如datasource相关配置)
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/user_0?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
  application:
    name: spring-cloud-nacos
#业务配置
useLocalCache: false
```



3. 在对应Controller中通过 Spring Cloud 原生注解 `@RefreshScope` 实现配置自动更新

```
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
```



4. 在nacos服务端中增加服务所需配置（此处配置为可动态配置的参数）

   如何确定咱们的dataId呢？spring.application.name-spring.profiles.active.file.extension 

   

   即项目文件名-环境配置.文件后缀 ：spring-boot-nacos.yml

```
server:
  port: 8080

mybatis:
  type-aliases-package: com.hongyan.study.nacos.bean

spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/user_0?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
  application:
    name: spring-boot-nacos
useLocalCache: true
```

![image-20200909172825241](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200909172825241.png)



6. 启动项目，并访问项目接口地址

   ![image-20200909174233678](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200909174233678.png)

![image-20200909174245876](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200909174245876.png)

发现，咱们的配置已经动态变更了，然后咱们可以去nacos服务端中修改配置文件内容，然后再访问对应的接口，发现数据已经实时变更过来。

![image-20200909174427345](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200909174427345.png)





#### SpringCloud项目中接入Nacos作为注册中心

采用nacos作为注册中心，可实现服务的注册与发现

实施步骤：

1. 引入spring-cloud-starter-alibaba-nacos-discovery 所需jar包

```
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>${latest.version}</version>
</dependency>
```





2. 在 `application.yml` 中配置 Nacos server 的地址：

```
server.port=8070
spring.application.name=service-provider
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```



3. 通过 Spring Cloud 原生注解 `@EnableDiscoveryClient` 开启服务注册发现功能：

```
@SpringBootApplication
@EnableDiscoveryClient
public class NacosProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NacosProviderApplication.class, args);
	}

	@RestController
	class EchoController {
		@RequestMapping(value = "/echo/{string}", method = RequestMethod.GET)
		public String echo(@PathVariable String string) {
			return "Hello Nacos Discovery " + string;
		}
	}
}
```



4. 配置服务消费者，从而服务消费者可以通过 Nacos 的服务注册发现功能从 Nacos server 上获取到它要调用的服务。

   i. 在 `application.properties` 中配置 Nacos server 的地址：

   ```
   server.port=8080
   spring.application.name=service-consumer
   
   spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
   ```

   ii. 通过 Spring Cloud 原生注解 `@EnableDiscoveryClient` 开启服务注册发现功能。给 [RestTemplate](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-resttemplate.html) 实例添加 `@LoadBalanced` 注解，开启 `@LoadBalanced` 与 [Ribbon](https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html) 的集成：

   ```
   @SpringBootApplication
   @EnableDiscoveryClient
   public class NacosConsumerApplication {
   
       @LoadBalanced
       @Bean
       public RestTemplate restTemplate() {
           return new RestTemplate();
       }
   
       public static void main(String[] args) {
           SpringApplication.run(NacosConsumerApplication.class, args);
       }
   
       @RestController
       public class TestController {
   
           private final RestTemplate restTemplate;
   
           @Autowired
           public TestController(RestTemplate restTemplate) {this.restTemplate = restTemplate;}
   
           @RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
           public String echo(@PathVariable String str) {
               return restTemplate.getForObject("http://service-provider/echo/" + str, String.class);
           }
       }
   }
   ```

   5. 启动 `ProviderApplication` 和 `ConsumerApplication` ，调用 `http://localhost:8080/echo/2018`，返回内容为 `Hello Nacos Discovery 2018`



### 看过来，看过来！！！

咱们在实现nacos的配置中心、注册中心时，其实可以不用在bootstrap.properties/bootstrap.yml文件中实现nacos的参数，咱们直接在启动脚本中实现即可，例如：java -jar -D-Dspring.cloud.nacos.discovery.server-addr=nacos.lp.com:80 xx.jar

具体样例：

```
set -m
#获取正在运行服务进程号并杀掉
pid=$(cat /home/app/data/runfile/weixin-service-pid.txt)
echo $pid
kill -9 $pid
#执行jar命令
nohup /home/app/jdk8/bin/java -Duser.timezone=GMT+08 -Xms1G -Xmx1G -Xmn1g -DLOG_HOME=/home/app/data/logs/weixin-service -Dspring.profiles.active=dev -Dspring.application.name=weixin-service -Dspring.cloud.nacos.config.file-extension=properties -Dspring.cloud.nacos.discovery.server-addr=nacos.lp.com:80 -Dspring.cloud.nacos.config.server-addr=nacos.lp.com:80 -Dspring.cloud.nacos.discovery.namespace=c2287d62-3b9d-44b6-a83d-67d896e6718f -Dspring.cloud.nacos.config.namespace=c2287d62-3b9d-44b6-a83d-67d896e6718f -jar /home/app/data/tmp/weixin-service.jar &> /home/app/data/logs/weixin-service/weixin-service.log& echo $! > /home/app/data/runfile/weixin-service-pid.txt
```



然后咱们去nacos服务端后台查看是否生效，最好的办法就是去修改配置文件中部分参数即可

![image-20200914210102405](/Users/dearzhang/Library/Application Support/typora-user-images/image-20200914210102405.png)





借鉴于:

- [nacos-examples(nacos集成样例)](https://github.com/nacos-group/nacos-examples)

- [SpringCloud项目中接入Nacos作为配置中心](https://www.cnblogs.com/larscheng/p/11392466.html)

- [SpringCloud项目中接入Nacos作为注册中心](https://www.cnblogs.com/larscheng/p/11388596.html)

- [Nacos官网及文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)

  