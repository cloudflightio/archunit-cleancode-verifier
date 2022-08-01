# Clean Code Rules for the Spring Boot

<a id="context-no-value-annotation"></a>
### Use @ConfigurationProperties instead of @Value
<sup>`Rule-ID: springboot.context-no-value-annotation`</sup>

Spring provides the annotation `org.springframework.beans.factory.annotation.Value` to inject properties from external
property sources into the application context. While this is handsome in plain Spring applications, Spring Boot offers something
better: `org.springframework.boot.context.properties.ConfigurationProperties`. This has the following advantage:

- No magic strings anymore inside `@Value`
- Typed and reusable properties
- Code completion in `application.yaml` and `application.properties` files when using the
  `org.springframework.boot:spring-boot-configuration-processor`

See [the official Spring documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-java-bean-binding) for more details
