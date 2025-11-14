# Starts Zookeeper, Kafka, and all 5 microservices in new tabs
# Requires Windows Terminal (wt.exe) on PATH

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path

# Set this to the root of your Kafka installation folder
$KafkaPath = "C:\kafka" 

if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
    Write-Error "Windows Terminal (wt.exe) not found. Install from Microsoft Store, then retry."
    exit 1
}

# Define service paths
$svcGateway = Join-Path $Root 'apigateway'
$svcAuth = Join-Path $Root 'authservice'
$svcBooking = Join-Path $Root 'bookingservice'
$svcDriver = Join-Path $Root 'driverservice'
$svcEureka = Join-Path $Root 'eurekaserver'
$svcRider = Join-Path $Root 'riderservice'
$svcUser = Join-Path $Root 'userservice'
$svcWallet = Join-Path $Root 'walletservice'

# --- START INFRASTRUCTURE FIRST ---
# Start Zookeeper (Must be first)
Write-Host "Starting Zookeeper..."
wt -w 0 new-tab --title "Zookeeper" -d $KafkaPath powershell -NoExit -NoProfile -Command ".\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties" | Out-Null
Start-Sleep -Seconds 15 # Give Zookeeper time to start

# Start Kafka (Must be second)
Write-Host "Starting Kafka..."
wt -w 0 new-tab --title "Kafka" -d $KafkaPath powershell -NoExit -NoProfile -Command ".\bin\windows\kafka-server-start.bat .\config\server.properties" | Out-Null
Start-Sleep -Seconds 15 # Give Kafka time to start

# --- START CORE SERVICES ---
Write-Host "Starting Eureka Server..."
wt -w 0 new-tab --title "Eureka" -d $svcEureka powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 20 # Give Eureka extra time to register itself

# --- START APPLICATION SERVICES ---
Write-Host "Starting API Gateway..."
wt -w 0 new-tab --title "Gateway" -d $svcGateway powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting Auth Service..."
wt -w 0 new-tab --title "Auth" -d $svcAuth powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting Booking Service..."
wt -w 0 new-tab --title "Booking" -d $svcBooking powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting Driver Service..."
wt -w 0 new-tab --title "Driver" -d $svcDriver powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting Rider Service..."
wt -w 0 new-tab --title "Rider" -d $svcRider powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting User Service..."
wt -w 0 new-tab --title "User" -d $svcUser powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null
Start-Sleep -Seconds 10

Write-Host "Starting Wallet Service..."
wt -w 0 new-tab --title "Wallet" -d $svcWallet powershell -NoExit -NoProfile -Command "mvn spring-boot:run" | Out-Null

Write-Host "All services started in tabs. Your system is booting up!"