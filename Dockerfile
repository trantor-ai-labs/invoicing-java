FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/invoicing.jar /app/invoicing.jar
COPY --from=build /build/target/lib /app/lib
COPY samples /app/samples
ENTRYPOINT ["java", "-jar", "/app/invoicing.jar"]
CMD ["render", "samples/order-1001.csv"]
