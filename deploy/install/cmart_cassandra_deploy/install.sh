#!/bin/sh
#
# This script sets up a server to serve as the cmart cassandra data tier
#

echo "Removing MAC and firewall"
		
# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

echo "Installing JAVA"
ORIGPATH=$(pwd)

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

echo "Installing Cassandra"

# install cassandra
cd $ORIGPATH
wget http://archive.apache.org/dist/cassandra/1.1.6/apache-cassandra-1.1.6-bin.tar.gz
cd /usr/share
tar -xvf apache-cassandra-1.1.6-bin.tar.gz
mv apache-cassandra-1.1.6 cassandra

mkdir -p /var/lib/cassandra
mkdir -p /var/lib/cassandra/data
mkdir -p /var/lib/cassandra/commitlog
mkdir -p /var/lib/cassandra/saved_caches

/bin/cp -f $ORIGPATH/cassandra.yaml /usr/share/cassandra/conf
/bin/cp -f $ORIGPATH/cassandra-env.sh /usr/share/cassandra/conf
sed -i "s/127.0.0.1/127.0.0.1 $(hostname)/g" /etc/hosts
sed -i "s/::1/::1 $(hostname)/g" /etc/hosts

cp $ORIGPATH/cassandra /etc/init.d/cassandra
chmod 755 /etc/init.d/cassandra
chkconfig cassandra on

service cassandra start

echo "Finished installing C-MART cassandra server"
exit 0