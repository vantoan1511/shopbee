# shopbee-category-service-api

## Description

API specification library for shopbee-category-service-api.

This project uses OpenAPI 3.0 to define API contracts and generates Java models and JAX-RS interfaces.

## Getting Started

### Prerequisites

- JDK 21
- Gradle

### Editing the API Specification

Edit the OpenAPI specification file:
```
shopbee-category-service-api-spec.yaml
```

The specification follows OpenAPI 3.0 format. You can:
- Define endpoints under `paths`
- Define data models under `components/schemas`
- Add validation rules, descriptions, and examples

### Building the Library

To generate code and build the JAR:

```shell
.\wi.ps1 build
```

Or using Gradle directly:

```shell
.\gradlew build
```

This will:
1. Generate Java models and JAX-RS interfaces from the OpenAPI spec
2. Compile the generated code
3. Package into a JAR file: `build/libs/shopbee-category-service-api-1.0.0.jar`

### Generated Code

The build process generates:
- **Models**: POJOs with validation annotations in `com.shopbee.category.entity` package
- **API Interfaces**: JAX-RS interfaces in `com.shopbee.category.boundary.api` package

### Using the Generated Library

Add the generated JAR to your Quarkus or Jakarta EE project and implement the generated interfaces.

Example:
```java
@Path("/api")
public class ExampleResourceImpl implements ExampleApi {
    @Override
    public Response getExample() {
        ExampleResponse response = new ExampleResponse();
        // ... implementation
        return Response.ok(response).build();
    }
}
```

## Publishing (Future)

Publishing configuration is included but commented out in `build.gradle`. 
Uncomment and configure the publishing section when ready to publish to a Maven repository.

## Related Resources

- [OpenAPI Specification](https://swagger.io/specification/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Quarkus REST](https://quarkus.io/guides/rest)

