param(
    [switch]$a,
    [switch]$c,
    [switch]$d,
    [switch]$i
)
if($i){
    Write-Output "** Updating private IP **"
    
    # Write-Output 'Previous .env:'
    $fileExisted = Test-Path '.env'
    if($fileExisted){
        $old = Get-Content .env
    }

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

    # Write-Output 'New .env:'
    $new = Get-Content .env
    $wasIpUpdated = -not $fileExisted -or $old -ne $new
    if($wasIpUpdated) {
        Write-Output 'IP changed'
    }
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
Write-Output "** Process finished **"
if($wasIpUpdated) {
    Write-Output "Remember to update the IP on any places you may have manually written it (like PostMan) to its new value: $env:HostIP"
}