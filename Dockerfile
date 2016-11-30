FROM alpine:3.4

ENV LANG C.UTF-8
EXPOSE 80

#config repo
RUN cat /etc/apk/repositories | sed -n "/main$/{s/^/@testing /;s/v[0-9\.]*/edge/;s/main$/testing/p}" >> /etc/apk/repositories
RUN cat /etc/apk/repositories | sed -n "/main$/{s/^/@edge /;s/v[0-9\.]*/edge/;s/main$/community/p}" >> /etc/apk/repositories
RUN sed -i "s/v3\.4/latest-stable/" /etc/apk/repositories

#install core utils
RUN apk --no-cache add coreutils bash

#install apache2
RUN apk --no-cache add apache2 apache2-proxy 

#install openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
RUN apk add --no-cache openjdk8

#install maven
RUN apk add --no-cache maven@edge

#build java server
ENV BUILD_DIR /tmp/src
RUN mkdir -p $BUILD_DIR
COPY pom.xml $BUILD_DIR/pom.xml
COPY src $BUILD_DIR/src
WORKDIR $BUILD_DIR
RUN mvn package \
    && cp target/hackwlb-server.jar /server.jar
WORKDIR /
RUN rm -rf $BUILD_DIR
COPY apache-java-server.conf /etc/apache2/conf.d/java-server.conf

#install launch scripts
COPY run.sh /run.sh

CMD /run.sh
