#!/bin/sh

java -jar /server.jar 0.0.0.0 8123 &
exec httpd -DFOREGROUND