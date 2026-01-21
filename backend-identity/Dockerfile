# Étape 1 : deps
FROM maven:3.9.6-eclipse-temurin-17 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

# Étape 2 : build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Étape 3 : run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.war app.war
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.war"]
