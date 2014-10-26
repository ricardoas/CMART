#!/bin/bash
service tomcat7 stop
sleep 1
rm -f /usr/share/tomcat7/logs/*
cd /usr/share/tomcat7/webapps/cmartsrc
bash -c "cd /usr/share/tomcat7/webapps/cmartsrc"
ant clean
ant clean dist
service tomcat7 start
