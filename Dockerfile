FROM amazoncorretto:21-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV DATASOURCE_URL=jdbc:postgresql://postgres:5432/event-subscription-manager
ENV DATASOURCE_USERNAME=postgres
ENV DATASOURCE_PASSWORD=123456
ENV DATASOURCE_DRIVER=org.postgresql.Driver

ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.eventsubscriptionmanagerapi.EventSubscriptionManagerApiApplication"]