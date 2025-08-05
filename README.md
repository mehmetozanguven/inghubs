# INGHubs - Software Engineer Test Case

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.4** with:
  - Web
  - JPA, Hibernate,
  - Kafka
  - Modulith
  - Testcontainers
  - Postgres
  - flyway
  - openapi
  - Mapstruct
  - Shedlock

## How to run

- Project relies on code generation from OPENAPI-Generator and mapstruct make sure that you can compile the project with java 21
- Make sure that docker(docker-compose) is running.
  - Project will load the environment variables from a file (for docker-compose solution) called **.env**
- Run the following command:
  - `./mvnw clean package -DskipTests && docker build -t my-springboot-app . && docker compose up`
  - For the first run it will create a folder called **postgres_data** in the project folder and run the **init-db.sh** script. 
    - Therefore, for the first run spring boot project may fail. Please run the command again
- Then open the swagger documentation: 
  - [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Manually

- Run postgres docker image and create database called **example_ing_test_db**:
  - `CREATE DATABASE example_ing_test_db;`
- Run kafka docker image
- Update postgres and kafka connection address in the application.yml:

```yml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS}
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/example_ing_test_db}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
# ...
app:
  kafka:
    bootstrapServers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS}
```

- Run the spring boot project from your favorite IDE.
- Then open the swagger documentation:
  - [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## How to run test cases

- Test cases heavily rely on testcontainers. To run test cases make sure that you are running docker:

- Then run test cases with `mvn test`
  - Testcontainers will automatically run required containers (postgres, kafka)

### Manual testing

- Meke sure that project is running and accessible through swagger address
- Create customer & employee
  - POST `/api/auth/customer/create`
  - POST `/api/auth/employee/create`
- Then login with customer email & password :
  - POST `/api/auth/login`
- Create a wallet for customer
  - POST `/api/customer/wallets`
- Then create a transaction (either deposit or withdraw)
  - POST `/api/customer/wallet/deposit` || `/api/customer/wallet/withdraw`
- Check customer transactions
  - GET `/api/customer/wallet/{walletid}/transactions`
- If any transactions require approve, login with employee email & password:
- Filter transactions
  - GET `/api/employee/transactions`
- Approve transaction
  - POST `/api/employee/transaction/post`

## Project Overview

This Digital Wallet Service is designed to demonstrate comprehensive understanding of:
- **Enterprise Java Development** with Spring Boot ecosystem
- **Financial Services Architecture** with proper transaction handling
- **Security-First Design** with JWT authentication and role-based access
- **API Documentation**: OpenAPI 3.0 with Swagger UI. Look at the file **resources/openapi/contact.yml** for details

### Other notes

- **Spring modulith** is a great tool to visualize modules in your application. And verify that none of the modules has circular reference to each other. You may find the app architecture inside `resources/static` folder
  - This module generates picture of the app when you run test case

- To handle transaction without getting any concurrency update issue and to handle transaction(s) in async manner, I have decided to use Kafka.
  - After created transaction as processing, **TransactionPublisher** runs and publish the event to the kafka
  - Then KafkaListener processes the given transaction
  - **To handle account's transaction one by one: I decided to use `wallet.getId()` as Kafka's key before publishing to the kafka. In that way I can handle the transaction for the specific wallet in an ordered manner**

## Future improvements

- Depends on the usage, we may partition the transaction table per TransactionStatus.
  - With that partition, we may speed up to process transactions (i am assuming that count of processing transaction should always be zero or close to zero)
