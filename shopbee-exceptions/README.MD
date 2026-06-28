# Shopbee Exceptions Library

A standardized exception handling library for Shopbee microservices. This library provides a set of common exceptions and JAX-RS mappers to ensure consistent error responses across the platform.

## Features

- **Common Exceptions**: Predefined exceptions for typical business and technical scenarios:
    - `BusinessException`: For functional/logic errors.
    - `TechnicalException`: For unexpected system errors.
    - `ResourceNotFoundException`: For 404 scenarios.
    - `ConflictResourceException`: For 409 scenarios.
    - `UnauthorizedException`: For 401 scenarios.
    - `ForbiddenException`: For 403 scenarios.
    - `InvalidDataException`: For 400 validation errors.
- **Global Exception Mapping**: A `GlobalExceptionMapper` that catches all `Throwable` and converts them into standardized JSON responses.
- **MicroProfile REST Client Support**: `ServiceClientExceptionMapper` for consistent error handling when calling downstream services.

## Requirements

- Java 21+
- Jakarta EE 10 / Jakarta RS 3.1+
- MicroProfile REST Client 3.0+

## Usage

### 1. Register Mappers

Register the mappers in your JAX-RS Application or configuration:

```java
import com.shopbee.common.exception.GlobalExceptionMapper;

// In your JAX-RS resource configuration
resources.add(GlobalExceptionMapper.class);
```

### 2. Throwing Exceptions

```java
if (product == null) {
    throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product with ID " + id + " not found.");
}
```

### 3. REST Client Integration

Annotate your MicroProfile REST Client interface:

```java
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import com.shopbee.common.exception.ServiceClientExceptionMapper;

@RegisterProvider(ServiceClientExceptionMapper.class)
public interface ProductClient {
    // ...
}
```

## Response Format

All exceptions are mapped to the following JSON structure:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message"
}
```

## Development

### Build

```bash
./gradlew build
```

### Publish Locally

```bash
./gradlew publishToMavenLocal
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
