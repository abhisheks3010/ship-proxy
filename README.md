# Ship Proxy - HTTP Proxy Client & Server

A custom Java-based Proxy System with:

- Proxy Server → port 9000
- Proxy Client (Ship Proxy) → port 8080
- FIFO Request Handling
- Supports Browser & curl usage
- Dockerized Setup

---

## Tech Stack:
- Java 17
- Maven
- Docker
- HTTP Client/Server Communication

---

## Project Structure
```
ship-proxy/
├── client/        → Ship Proxy Client Code
├── server/        → Proxy Server Code
├── docker/        → Dockerfiles for client/server
├── pom.xml        → Parent Maven Build
└── README.md      → This file
```

---

## Run Locally (Without Docker)

### Build:
```
mvn clean package
```

### Run Server:
```
java -jar server/target/server-1.0-SNAPSHOT.jar
```

### Run Client:
```
java -jar client/target/client-1.0-SNAPSHOT.jar
```

---

## Run Using Docker

### Build:
```
docker build -t proxy-server -f docker/server.Dockerfile .
docker build -t proxy-client -f docker/client.Dockerfile .
```

### Run:
```
docker run -d -p 9000:9000 --name ship-server proxy-server
docker run -d -p 8080:8080 --name ship-client -e SERVER_HOST=host.docker.internal -e SERVER_PORT=9000 proxy-client
```

---

## Test with curl
```
curl.exe -x http://localhost:8080 http://httpforever.com/
```

or from browser:
```
http://httpforever.com
```

---

## GitHub Repository
https://github.com/abhisheks3010/ship-proxy
