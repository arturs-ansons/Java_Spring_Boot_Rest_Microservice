💳 Banking Microservices Platform

A **Banking & Crypto Trading Microservices Platform** built with **Spring Boot**, **Eureka**, **RabbitMQ**, **React**, and a **Node.js API Gateway**.  
This system provides secure authentication, client onboarding, account management, banking transactions, crypto trading, and live market data — all using a scalable microservices architecture.

---

## 🛠 Built With

- Java 17  
- Spring Boot 3  
- Spring Cloud (Eureka, Config, WebClient)  
- RabbitMQ  
- MySQL & PostgreSQL  
- Node.js API Gateway  
- React Frontend  
- Docker & Docker Compose  
- CoinGecko API (live crypto pricing)

---

## 🚀 Features

### Authentication & Authorization

- **JWT-based authentication**
- Role-based authorization:
  - **USER:** Standard banking/trading operations  
  - **ADMIN:** System-wide visibility & management  
  - **MANAGER:** Elevated operational access
- Token validation handled by API Gateway
- Fully isolated & scalable Auth microservice

---

### Microservices Architecture

The platform is composed of independent, decoupled microservices:

### 🔐 Auth Service
- User login & registration  
- Issues JWT tokens  
- Publishes **UserRegistrationEvent** to RabbitMQ  

### 👤 Client Service
- Automatically creates client profiles after registration  
- Allows profile editing  
- Admin-level access to all clients  
- Consumes user registration events from RabbitMQ  

### 🧾 Account Service
- Creates bank & trading accounts for new users  
- Deposit / withdraw / transfer functionality  
- Full transaction history  
- Crypto wallet representation  
- Consumes registration events  

### 🪙 Crypto Trading Module
- Buy & sell crypto using live prices  
- Portfolio tracking  
- Crypto transaction history  
- Fetches real-time data from **CoinGecko API**  

### 🌐 Node.js API Gateway
- Central routing entry point  
- JWT validation & CORS handling  
- Routes:
  - `/api/auth`
  - `/api/clients`
  - `/api/account`

### 🎨 React Frontend
- User dashboard  
- Account overview  
- Crypto trading interface  
- Live portfolio valuation  
- Automatic logout/token refresh  

---

## 🔄 Event-Driven Integration (RabbitMQ)

The system uses **RabbitMQ** to ensure loose coupling.

### Events:
- **UserRegistrationEvent**  
  - Auth → Client Service  
  - Auth → Account Service  

Ensures:
- Automatic profile creation  
- Automatic initial account creation  
- No direct service-to-service calls  

---

## 🪙 Live Crypto Market Data

Provided via **CoinGecko API**, including:

- Current price  
- 24h change  
- Market cap  
- Volume  
- Last updated timestamp  

Used for:
- Trading operations  
- Portfolio valuation  
- Transaction validation  
- Frontend market display  

---

## 🧪 Error Handling & Reliability

- Graceful fallback if CoinGecko is down  
- Retry + timeout mechanisms  
- Centralized error handlers per microservice  
- Strict transaction validation  
- Strong RabbitMQ event consumption logic  

---

## 🔁 CI/CD (Optional)

Supports CI/CD pipelines (GitHub Actions or similar):

- Run tests  
- Lint & build services  
- Build Docker images  
- Deploy microservices  
- Start event consumers automatically  

---

## 🧪 Testing

- Unit & integration tests for:
  - Auth, Client, Account, Trading  
  - Role-based access  
  - Success & failure banking scenarios  
  - External service mocks (CoinGecko)  
  - RabbitMQ event mocks  

---

## 📈 Architecture & Flow

### User Flow (Authentication → Banking → Trading)

