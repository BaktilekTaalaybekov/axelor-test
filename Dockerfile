FROM --platform=linux/amd64 gradle:8.5-jdk17 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies

COPY . .
RUN gradle --no-daemon clean jar

FROM --platform=linux/amd64 axelor/aio-erp:latest
WORKDIR /tmp

COPY --from=builder /workspace/build/libs/*.jar /tmp/axelor-nbkr.jar
RUN mkdir -p /tmp/nbkr-war-overlay/WEB-INF/lib \
    && cp /tmp/axelor-nbkr.jar /tmp/nbkr-war-overlay/WEB-INF/lib/axelor-nbkr.jar \
    && cd /tmp/nbkr-war-overlay \
    && jar uf /var/lib/tomcat/webapps/ROOT.war WEB-INF/lib/axelor-nbkr.jar \
    && mkdir -p /usr/local/share/axelor \
    && cp /var/lib/tomcat/webapps/ROOT.war /usr/local/share/axelor/ROOT.war \
    && rm -rf /tmp/nbkr-war-overlay /tmp/axelor-nbkr.jar

ENV DB_URL=jdbc:postgresql://db:5432/axelor \
    DB_USER=axelor \
    DB_PASSWORD=axelor
