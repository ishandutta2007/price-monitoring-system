# Price Monitoring/Notification System
Design a price monitoring system to monitor product price and notify subscribers if the price is reduced. Similar to Dealmoon.com.

## Features
- Keeps track of product prices at an e-commerce web site.
- Allows users to subscribe by emails for interested product categories.
- Notifies subscribers if there is any discount product in the subscribed categories.
- Subscribers can specify minimum discount threshold of each category for receiving notifications.
- Also allows users to query discount products online.

## Development environment
- **MySQL DB** is used to store categories, product list urls, products and users information.
- **Mongo DB** is used to store product crawling logs.
- **Lombok** is used to eliminate constructors and getter/setter implementation for cleaner coding style.
- **RabbitMQ** is used for communications between Product Crawler, Price Monitoring Service, Instant Notification Service and Product Log Service.
- **Redis** is used to cache last product price for fast price change comparison.
- **Jsoup** is used to retrieve web page content, parse the required text from the page.
- **Spring Boot** is used for fast REST API development and independant deployment.
- **Spring Boot Actuator** is used to provide monitoring information. (/health, /metrics... etc.)
- **Spring Cloud** is used to provide infrastructure services.

## Design Diagrams
![Overview](/Overview.png)
![MySQL Schema](/MySQLSchema.png)
![MongoDB Schema](/MongoDBSchema.png)
![Category Crawler](/CategoryCrawler.png)
![Product Crawler](/ProductCrawler.png)
![Price Monitoring Service](/PriceMonitoringService.png)
![Instant Notification Service](/InstantNotificationService.png)
![Product Log Service](/ProductLogService.png)
![User Service](/UserService.png)

## Getting Started
### Start MongoDB, RabbitMQ on Docker
```
docker-compose up -d
```
### Check data in MongoDB
Find mongodb container id
```
docker ps
```
Enter mongodb container by typing the first 3 charactters of the container id (ex: '9cd'), then type mongo inside the container to use mongodb shell command.
```
docker exec -it 9cd bash
# mongo                             // open mongo shell
> use test                          // Spring boot use test db as default
> show collections                  // show all collections inside test db
> db.productLog.find().pretty()     // show all data inside productLog table
> exit                              // quit mongo shell
> exit                              // exit container shell
```
### Setup RabbitMQ
Create durable queues
```
q_product_p1_default
q_product_p2_default
q_product_p3_default
q_product_log.default
q_discount_product_p1.default
q_discount_product_p2.default
q_discount_product_p3.default
q_product_log.default

```
Create topic exchanges
```
q_discount_product_p1
q_discount_product_p2
q_discount_product_p3
q_product_log
q_product_p1
q_product_p2
q_product_p3

```
Binding queue to exchanges with routing key "#"
```
q_discount_product_p1   -->     q_discount_product_p1.default
q_discount_product_p2   -->     q_discount_product_p2.default
q_discount_product_p3   -->     q_discount_product_p3.default
q_product_log   -->     q_product_log.default
q_product_p1    -->     q_product_p1
q_product_p2    -->     q_product_p2
q_product_p3    -->     q_product_p3
```
### Install Redis on Mac
```
> brew install redis
```
### Start Redis
```
> brew services start redis

```
### Run Redis Client
```
> redis-cli
```
### Installation
```
mvn clean install
```
### Start Category Crawler
```
> sh start-category-crawler.sh
```
### Start Product Crawler
```
> sh start-product-crawler.sh
```
### Start Price Monitoring Service
```
> sh start-price-monitoring-service.sh
```
### Start Instant Notification Service
```
> sh start-instant-notification-service.sh
```
### Start Product Log Service
```
> sh start-product-log-service.sh
```
### Start User Service
```
> sh start-user-service.sh
```
### Checkout application metrics
```
http://localhost:9004/health
http://localhost:9004/env
http://localhost:9004/metrics

```
### Manually Trigger Category Crawler with Postman
```
GET localhost:9000/category-crawlers/categories
```
### Manually Trigger Product Crawler with Postman
```
GET localhost:9001/product-crawlers/products/1  --> start high priority crawler
GET localhost:9001/product-crawlers/products/2  --> start medium priority crawler
GET localhost:9001/product-crawlers/products/3  --> start low priority crawler
```
## LICENSE

[MIT](./License.txt)

### Support:

If you want the good work to continue please support us on

* [PAYPAL](https://www.paypal.me/ishandutta2007)
* [BITCOIN ADDRESS: 3LZazKXG18Hxa3LLNAeKYZNtLzCxpv1LyD](https://www.coinbase.com/join/5a8e4a045b02c403bc3a9c0c)
