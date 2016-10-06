FROM alpine:3.4

ENV LANG C.UTF-8
EXPOSE 80

#config repo
RUN cat /etc/apk/repositories | sed -n "/main$/{s/^/@testing /;s/v[0-9\.]*/edge/;s/main$/testing/p}" >> /etc/apk/repositories
RUN cat /etc/apk/repositories | sed -n "/main$/{s/^/@edge /;s/v[0-9\.]*/edge/;s/main$/community/p}" >> /etc/apk/repositories
RUN sed -i "s/v3\.4/latest-stable/" /etc/apk/repositories

#install apache2 php5 mysql
RUN apk --no-cache add apache2 apache2-proxy php5-apache2 mysql-client \
    && mkdir /run/apache2 \
    && sed -i "s/#\(.*mod_slotmem_shm.*.so\)$/\1/" /etc/apache2/httpd.conf \
    && sed -i "s/^\([^#].*mod_proxy_fdpass.*.so\)$/#\1/" /etc/apache2/conf.d/proxy.conf

#install core utils
RUN apk --no-cache add git openssh coreutils

#install owncloud
RUN apk --no-cache add owncloud-mysql 

#install openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
RUN apk add --no-cache openjdk8

#install maven
RUN apk add --no-cache maven@edge

#install cron
RUN apk --no-cache add dcron

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

#cope data
COPY DATA.tar.gz DATA.tar.gz
RUN tar -zxf DATA.tar.gz -C / && rm DATA.tar.gz

#init mysql script
COPY init-mysql.sh init-mysql.sh
RUN ./init-mysql.sh && rm init-mysql.sh

#init owncloud script
COPY owncloud.sh owncloud.sh
RUN ./owncloud.sh && rm owncloud.sh

#install crontab
COPY crontab /etc/crontabs/root

#install launch scripts
COPY bak.sh /bak.sh
COPY run.sh /run.sh

CMD /run.sh
