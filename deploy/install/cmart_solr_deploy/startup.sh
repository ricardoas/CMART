#!/bin/sh
cd /usr/share/solr/example
JAVA_HOME=/usr/share/java/jdk1.7.0_01
export JAVA_HOME
PATH=$JAVA_HOME/bin:$PATH
export PATH

java -jar start.jar &
