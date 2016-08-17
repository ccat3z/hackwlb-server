FROM alpine:latest

ENV LANG C.UTF-8
EXPOSE 80

#add testing repo
RUN cat /etc/apk/repositories | sed -n "/main$/{s/^/@testing /;s/v[0-9\.]*/edge/;s/main$/testing/p}" >> /etc/apk/repositories

#update repo
RUN apk update

#install openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
RUN apk add --no-cache openjdk8

#install maven
RUN apk add --no-cache maven@testing

#build
COPY . /usr/src
WORKDIR /usr/src
RUN mvn package \
    && cp target/hackwlb-server.jar /server.jar

CMD java -jar /server.jar 0.0.0.0 80
