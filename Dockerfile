# FROM flyceek/centos7-jdk
FROM openjdk:8-jdk-alpine

ENV JAVA_OPTS=""
ENV DOC_ROOT /datainsight 
ENV DI_VER=1.0
#ENV SPRING_PROFILES_ACTIVE tlc
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en

RUN mkdir -p /datainsight   \
             /logs

RUN mkdir -p /livy-files

ADD ./target/datainsight-${DI_VER}.jar /datainsight/datainsight-${DI_VER}.jar

VOLUME /tmp /datainsight

USER root
ENV TZ 'Asia/Seoul'
RUN echo $TZ > /etc/timezone

# RUN sh -c 'touch /datainsight-${DI_VER}.jar'

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /datainsight/datainsight-${DI_VER}.jar"]