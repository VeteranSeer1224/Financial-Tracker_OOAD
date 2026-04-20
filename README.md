# Financial Tracker OOAD

Java 17 application for tracking income, expenses, balances, monthly summaries, category budgets, and recurring subscriptions.

## Build with Docker

Make sure Docker Desktop or the Docker daemon is running first.

```bash
docker build -t financial-tracker .
```

## Run with Docker

```bash
docker run --rm -it financial-tracker
```

Note: Docker runs the CLI app (`com.minip.financialtracker.App`).  
The JavaFX desktop UI is intended to run on your local machine (not a headless container).

## Local Build

```bash
mvn test
```

## Local Run

```bash
java -cp target/classes com.minip.financialtracker.App
```

## Desktop UI (JavaFX)

Run the MVC-style JavaFX desktop client:

```bash
mvn javafx:run
```
"# Financial-Tracker_OOAD" 
