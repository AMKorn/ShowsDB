# ShowsDB

This is a REST API for a TV Shows DB using Spring Boot 3.

After running, you can open the swagger documentation by going to localhost:8080/swagger-ui.html 

## Requirements
- Docker
- Maven

## Initializing

1. Get the code from GitHub:
    
    ```
    git clone https://github.com/AMKorn/ShowsDB.git
    ```

2. Initialize the docker network
    ```
    docker-compose up -d
    ```

3. Migrate the database
    ```
    cd ../client-server
    mvn liquibase:update
    ```

4. Run the auth server main
5. Run the client server main

The script `initialize` does steps 2 and 3 for you