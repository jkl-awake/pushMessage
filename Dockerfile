FROM openjdk:17-jdk-slim
WORKDIR /app
COPY App.java /app/
RUN javac App.java
CMD ["java", "App"]