#!/bin/sh
#
# This script sets up a server to serve the C-MART client
#
echo Removing MAC and firewall

# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

echo Installing JAVA

# install java
mkdir -p /usr/share/java
cd /usr/share/java
wget http://download.oracle.com/otn/java/jdk/7u1-b08/jdk-7u1-linux-x64.tar.gz
tar -zxf jdk-7u1-linux-x64.tar.gz

JAVA_HOME=/usr/share/java/jdk1.7.0_01
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

echo "JAVA_HOME=/usr/share/java/jdk1.7.0_01" >> ~/.bashrc
echo "export JAVA_HOME" >> ~/.bashrc
echo "PATH=$JAVA_HOME/bin:$PATH" >> ~/.bashrc
echo "export PATH" >> ~/.bashrc

echo Finished installing C-MART client server
exit 0