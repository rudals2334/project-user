################################################################################
# 1) Build Stage : Gradle로 bootJar 생성
################################################################################
FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Gradle wrapper가 zip을 풀 때 unzip 필요 (slim 이미지는 기본 미포함)
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*

# Windows 에서 복사된 gradlew 가 CRLF 일 수 있으니 줄바꿈/실행권한 정리
COPY gradlew ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 설정 파일 먼저 복사(캐시 최적화)
COPY build.gradle settings.gradle ./

# 소스 복사 후 빌드 (테스트는 스킵)
COPY src ./src
RUN ./gradlew --no-daemon -x test bootJar

# (선택) Spring Boot Layered JAR 추출
# RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination /app/extracted


################################################################################
# 2) Runtime Stage : JRE만 포함해 경량화 & non-root 실행
################################################################################
FROM eclipse-temurin:17-jre-jammy AS final
# openjdk:17-jre-slim 도 가능하지만 temurin jre 이미지가 더 경량/안정적

# 실행 사용자 생성 (root 회피)
RUN addgroup --system spring && adduser --system --ingroup spring spring

# 👇 HEALTHCHECK용 curl 설치 (root로 잠깐 승격)
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*
USER spring:spring

WORKDIR /app

# 런타임 환경 변수(선택)
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

# Build 단계에서 만든 jar 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# (선택) Layered JAR 사용 시
# COPY --from=build /app/extracted/dependencies/ ./
# COPY --from=build /app/extracted/spring-boot-loader/ ./
# COPY --from=build /app/extracted/snapshot-dependencies/ ./
# COPY --from=build /app/extracted/application/ ./
# ENTRYPOINT ["sh","-c","java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]

# ✅ HEALTHCHECK: curl 사용 (이미지에 curl 설치済)
HEALTHCHECK --interval=10s --timeout=5s --retries=20 \
  CMD curl -fsS http://127.0.0.1:8081/actuator/health | grep -q '"status":"UP"' || exit 1

EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
