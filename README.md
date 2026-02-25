

Hands on microservice, how internally server side communication happens

components involved in this project:

1. ServiceDiscovery(Eureka)(appName:server) - a central repository where each microservice(client) registers here there 
                                             basically IP/Host+ serviceId(application name)+ port.
                                             And EurekaServer sends heartbeart's for checking alive or not. if not alive then remove them from list for realiability.
                                             
                                             ex:(service1: 192.168.1.25:api-gate-way:8083)

2. Microservices(appName:productService,orderService) - each registers as EurekaClient(discoveryClient) in EurekaServer.
                                                        everything auto registers when added spring-cloud-starter-netflix-eureka-client depedency.
                                                        In prod setup have multiple instances.

3. ApiGateway(appName:api-gate-way) - apiGateway contains all the services info. 
                                        Generally, in production setup external load balancer in infra level forwards requests to ApiGateway, ApiGateway search for the request uri matching path predicates (linear scan). if matches make a request to it.

                                        uri=lb://ORDERSERVICE:
                                        lb:// tells the gateway to use client-side load balancing(spring cloud loadBalancer)
                                        ORDERSERVICE must match the service ID registered in Eureka.
                                        
                                        1. loadbalancer every client(API Gateway's) have their own spring-cloud-loadbalancer (client-side loadbalancer). 
                                        
                                        2. Load balancing: It is client-side; the gateway chooses the target instance using a load-balancing algorithm over the list from Eureka.
                                    
ex:
spring.cloud.gateway.routes[0].id=orderService
spring.cloud.gateway.routes[0].uri=lb://ORDERSERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/orders/**

spring.cloud.gateway.routes[1].id=productService
spring.cloud.gateway.routes[1].uri=lb://PRODUCTSERVICE
spring.cloud.gateway.routes[1].predicates[0]=Path=/products/**


Finally, To test this project
fork this and download repo.
open 4 terminals.
1. cd server (port:8761) - mvn spring-boot:run
2. cd api-gate-way (port:8083) - mvn spring-boot:run
3. cd productService (port:8081) - mvn spring-boot:run
4. cd orderService (port:8082) - mvn spring-boot:run

so, in microservices client-side request flow starts from api-gate-way

test on localhost:8082/orders/1 hitting orderservice directly
but your api-gate-way takes these things so localhost:8083/orders/1 
internally requests localhost:8082/orders/1 and orderService internally calls productservice localhost:8081/products/1


