# News Dashboard

A Java and Spring Boot app that searches The Guardian and GNews and displays the results in a web page. It also adds a basic word-list sentiment label to each story.

The Java application and API integrations were written by me. I used AI assistance for the frontend.

## Setup

You need Java 17 and API keys for [The Guardian](https://open-platform.theguardian.com/access/) and [GNews](https://gnews.io/register).

Copy the example configuration and add your keys to the local file:

```powershell
Copy-Item application-local.example.properties application-local.properties
```

The local file is ignored by Git. Do not commit it.

## Run

From the project folder, run:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

## Tests

```powershell
.\mvnw.cmd test
```
