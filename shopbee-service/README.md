# shopbee-service

## Description

This is a shopbee-service Quarkus project.

## Getting Started

### Prerequisites

- JDK 21
- Gradle

### Building and Running

To build the project, run:

```shell
./gradlew build
```

This will compile the project and package it into a JAR file.

### Running the Application in devmode

To run the application in devmod, use the following command:

```shell
./gradlew quarkusDev
```

## Building and Running with Docker

### Building the JVM Image

To build the JVM image, run:

```shell
docker build . -t com.example.my/shopbee-service
```

### Running the image


To run the image, run:

```shell
docker run -d -p 8080:8080 --name shopbee-service com.example.my/shopbee-service
```

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
