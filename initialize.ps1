param(
    [switch]$a,
    [switch]$c,
    [switch]$d,
    [switch]$i
)
if($i){
    Write-Output "** Getting private IP **"
    # Get the local computer's IPv4 address
    $env:HostIP = (
        Get-NetIPConfiguration |
        Where-Object {
            $_.IPv4DefaultGateway -ne $null -and
            $_.NetAdapter.Status -ne "Disconnected"
        }
    ).IPv4Address.IPAddress

    # Write the IP address to a .env file
    "privateip=$env:HostIP" | Out-File -FilePath '.env' -Encoding UTF8 -Force
}
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