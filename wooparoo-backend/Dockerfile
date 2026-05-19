FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/wooparoo-0.0.1-SNAPSHOT.jar"]
