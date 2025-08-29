# SpringBoot SQL Webhook

**Author:** Aaditya Trivedi  
**Reg No:** 22BCE7937  

**Company Test:** Bajaj Finserv Health | JAVA | VIT  
**Submission Date:** 29-Aug-2025  

---

## Overview
This project is a solution for the **Java test** conducted by Bajaj Finserv Health.  
It is implemented using **Java** and **Spring Boot**, and demonstrates:  

- Sending POST requests on application startup.  
- Receiving a webhook URL and JWT token from the server.  
- Preparing a final SQL query based on the assigned question.  
- Sending the SQL query securely to the provided webhook endpoint.  

> **Note:** No actual database creation is required; the SQL query is handled in memory and sent as the solution.

---

## Features
- Automatic workflow on application startup (no controllers/endpoints required).  
- REST API integration using **RestTemplate** / **WebClient**.  
- Handles JWT authentication for secure webhook submission.  
- Includes final JAR output for easy execution.  

---

## Project Structure

autosolver/
│
├── src/main/java/... # Java source code
├── src/main/resources/... # Configuration files (application.properties)
├── target/... # Compiled JAR output
├── README.md # Project documentation
└── pom.xml # Maven dependencies

---

## How to Run

### 1. Clone the Repository
```bash
git clone https://github.com/Aaditya-7937/springboot-sql-webhook.git
cd springboot-sql-webhook
```
mvn clean install
java -jar target/autosolver-0.0.1-SNAPSHOT.jar

# Submission Links
---
GitHub Repository: https://github.com/Aaditya-7937/springboot-sql-webhook.git

Public JAR Download Link: https://github.com/Aaditya-7937/springboot-sql-webhook/raw/refs/heads/main/target/autosolver-0.0.1-SNAPSHOT.jar
