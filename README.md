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

2. Create a `.env` file with your private ip as an enviroment variable called `privateip`

3. Initialize the docker network
    ```
    docker-compose up -d
    ```

4. Migrate the database
    ```
    cd ../client-server
    mvn liquibase:update
    ```

The script `initialize` does all the steps after 2 for you. It has four possible flags, which should all be passed when running for the first time:
* `-a` : Re-compiles the authentication server
* `-c` : Re-compiles the client server
* `-d` : Migrates the database.
* `-i` : Creates a `.env` file which includes the private IP, needed for easier communication between the docker network and the user