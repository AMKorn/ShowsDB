param(
    [switch]$a,
    [switch]$c,
    [switch]$d
)
if($a){
    Write-Output "** Compiling auth server **"
    Set-Location auth-server
    mvn clean package -DskipTests
    docker build --tag=auth-server:latest .
    Set-Location ..
}
if($c){
    Write-Output "** Compiling main ShowsDB server **"
    Set-Location client-server
    mvn clean package -DskipTests
    docker build --tag=client-server:latest .
    Set-Location ..
}
Write-Output "** Setting up docker network**"
docker-compose up -d
if($d){
    Write-Output "** Setting up Database **"
    Set-Location client-server
    mvn liquibase:update
    Set-Location ..
}