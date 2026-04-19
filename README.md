# Personal Finance & Subscription Tracker

A containerized Spring Boot web application for managing recurring digital subscriptions and one-time financial expenses. 

## Prerequisites

Before you begin, ensure you have the following installed on your system (or within your WSL environment):
* Docker
* Docker Compose

## Getting Started

This project is configured to run entirely within Docker, meaning you do not need to install Java or Maven locally on your host machine to build and run the application.

### 1. Build and Run the Application

Open your terminal, navigate to the root directory of the project (where the docker-compose.yml file is located), and run the following command:

docker-compose up --build

This command will:
1. Download the necessary Maven and Java images.
2. Compile the Spring Boot application.
3. Package it into an executable JAR.
4. Start the web server on port 8080.

### 2. Accessing the Database (H2 Console)

This project uses an in-memory H2 database for local development and testing. Once the container is running, you can access the database interface via your web browser:

* URL: http://localhost:8080/h2-console

Use the following credentials to connect:
* JDBC URL: jdbc:h2:mem:financedb
* User Name: sa
* Password: (Leave this field blank)

*Note: The database is stored in-memory. All data will be wiped when the container is stopped.*

### 3. Stopping the Application

To gracefully stop the running application, press Ctrl+C in the terminal where Docker Compose is running. 

To tear down the containers entirely, run:

docker-compose down

## Project Structure

* src/main/java/com/finance/tracker: Core backend source code (Models, Controllers, Services, Repositories).
* src/main/resources: Application properties and static frontend files.
* Dockerfile / docker-compose.yml: Containerization environment configuration.
