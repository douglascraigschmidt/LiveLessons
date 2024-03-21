FROM amd64/gradle:8.0.1-jdk19-alpine as builder

ADD ./build.gradle /home/gradle/build.gradle
ADD ./src /home/gradle/src/

RUN gradle build -x test



FROM amazoncorretto:19-alpine-jdk

# copy jar from builder stage
COPY --from=builder /home/gradle/build/libs/*.jar app.jar

EXPOSE 8080 8080

CMD ["sh", "-c", "java --enable-preview -jar app.jar " ]