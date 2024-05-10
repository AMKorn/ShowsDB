mvn clean package -DskipTests
docker build --tag=auth-server:latest .