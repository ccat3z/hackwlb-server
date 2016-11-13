#!/bin/sh
/bak.sh init
mysqld&
java -jar /server.jar 0.0.0.0 8123 &
httpd&                 
echo START_LOG > /tmp/cronlog
crond&
tail -f /tmp/cronlog
