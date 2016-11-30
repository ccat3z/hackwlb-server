#!/bin/sh
httpd&                 
java -jar /server.jar 0.0.0.0 8123
