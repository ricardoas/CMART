#!/bin/sh
#
# This script sets up a server to work as the C-MART solr server
#
ORIGDIR=$(pwd)
echo Removing MAC address and firewall

# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

# install java
mkdir -p /usr/share/java
cd /usr/share/java
wget http://download.oracle.com/otn/java/jdk/7u1-b08/jdk-7u1-linux-x64.tar.gz
tar -zxf jdk-7u1-linux-x64.tar.gz

JAVA_HOME=/usr/share/java/jdk1.7.0_01
export JAVA_HOME
PATH=$JAVA_HOME/bin:$PATH
export PATH

echo "JAVA_HOME=/usr/share/java/jdk1.7.0_01" >> ~/.bashrc
echo "export JAVA_HOME" >> ~/.bashrc
echo "PATH=$JAVA_HOME/bin:$PATH" >> ~/.bashrc
echo "export PATH" >> ~/.bashrc

# install tomcat
cd /usr/share
wget https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.22/bin/apache-tomcat-7.0.22.tar.gz
tar -xvf apache-tomcat-7.0.22.tar.gz
mv apache-tomcat-7.0.22 tomcat7

/bin/cp -f $ORIGDIR/tomcat7 /etc/init.d/tomcat7
chmod 755 /etc/init.d/tomcat7
#chkconfig tomcat7 on

rm -f /usr/share/tomcat7/conf/tomcat-users.xml
/bin/cp -f $ORIGDIR/tomcat-users.xml /usr/share/tomcat7/conf/

echo "CATALINA_HOME=/usr/share/tomcat7/" >> ~/.bashrc
echo "export CATALINA_HOME" >> ~/.bashrc
echo "PATH=$CATALINA_HOME:$PATH" >> ~/.bashrc
echo "export PATH" >> ~/.bashrc


cd /usr/share
wget https://archive.apache.org/dist/lucene/solr/3.4.0/apache-solr-3.4.0.tgz
tar -xvf apache-solr-3.4.0.tgz

mv apache-solr-3.4.0 solr
/bin/cp -f $ORIGDIR/schema.xml /usr/share/solr/example/solr/conf/schema.xml

/bin/cp -f $ORIGDIR/solr /etc/init.d/solr
chmod 755 /etc/init.d/solr

/bin/cp -f $ORIGDIR/startup.sh /usr/share/solr/startup.sh
/bin/cp -f $ORIGDIR/shutdown.sh /usr/share/solr/shutdown.sh

chkconfig solr on
service solr start