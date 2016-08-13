FROM alpine:3.4

ENV LANG C.UTF-8
EXPOSE 80

#install openjdk8
RUN echo "LOG:Installing openjdk8..."
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ENV JAVA_VERSION 8u92
ENV JAVA_ALPINE_VERSION 8.92.14-r1

RUN set -x \
	&& apk add --no-cache \
		openjdk8="$JAVA_ALPINE_VERSION" \
	&& [ "$JAVA_HOME" = "$(docker-java-home)" ]

#install apache-ant
RUN ls
RUN apk add --no-cache apache-ant 

#build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN ant -f hackwlb-server.xml \
    && cp out/artifacts/out/out.jar /server.jar

CMD java -jar /server.jar 0.0.0.0 80
