#!/bin/sh
#
# This script sets up a server to serve the C-MART app
#
ORIGDIR=$(pwd)
yum install make gcc gcc-c++ -y

echo "Removing MAC and firewall"

# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

echo "Installing NFS"
yum install nfs-utils portmap rpcbind nfs4-acl-tools -y
chkconfig rpcbind on
service rpcbind restart
chkconfig portmap on
service portmap restart
chkconfig nfs on
service nfs restart

echo "Installing JAVA"

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

echo "Installing Tomcat"

# install tomcat
cd /usr/share
wget https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.22/bin/apache-tomcat-7.0.22.tar.gz
tar -xvf apache-tomcat-7.0.22.tar.gz
mv apache-tomcat-7.0.22 tomcat7

cp $ORIGDIR/tomcat7 /etc/init.d/tomcat7
chmod 755 /etc/init.d/tomcat7
chkconfig tomcat7 on

rm -f /usr/share/tomcat7/conf/tomcat-users.xml
rm -f /usr/share/tomcat7/conf/server.xml
rm -f /usr/share/tomcat7/conf/web.xml
rm -f /usr/share/tomcat7/conf/context.xml
rm -f /usr/share/tomcat7/conf/logging.properties

/bin/cp -f $ORIGDIR/../../../cmartconfig.txt /		
/bin/cp -f $ORIGDIR/tomcat-users.xml /usr/share/tomcat7/conf/
/bin/cp -f $ORIGDIR/server.xml /usr/share/tomcat7/conf/
/bin/cp -f $ORIGDIR/web.xml /usr/share/tomcat7/conf/
/bin/cp -f $ORIGDIR/context.xml /usr/share/tomcat7/conf/
/bin/cp -f $ORIGDIR/logging.properties /usr/share/tomcat7/conf/
/bin/cp -f $ORIGDIR/mysql-connector-java-5.1.15-bin.jar /usr/share/tomcat7/lib/

mkdir /usr/share/tomcat7/webapps/temp
mkdir /usr/share/tomcat7/webapps/img
mkdir /usr/share/tomcat7/webapps/netimg
chmod 777 /usr/share/tomcat7/webapps/img
chmod 777 /usr/share/tomcat7/webapps/temp
chmod 777 /usr/share/tomcat7/webapps/netimg
mkdir /usr/share/tomcat7/webapps/cmartsrc

/bin/cp -fr /cmart/WebContent /usr/share/tomcat7/webapps/cmartsrc
mv -f /usr/share/tomcat7/webapps/cmartsrc/WebContent/WEB-INF /usr/share/tomcat7/webapps/cmartsrc
/bin/cp -fr /cmart/src /usr/share/tomcat7/webapps/cmartsrc/WEB-INF 
/bin/cp -f $ORIGDIR/build.xml /usr/share/tomcat7/webapps/cmartsrc	

# install ANT to build the project
echo "Installing ANT"

cd /usr/share
wget https://archive.apache.org/dist/ant/binaries/apache-ant-1.8.2-bin.tar.gz
tar -xvf apache-ant-1.8.2-bin.tar.gz
mv apache-ant-1.8.2 ant

echo "ANT_HOME=/usr/share/ant" >> ~/.bashrc
echo "export ANT_HOME" >> ~/.bashrc
echo "PATH=$ANT_HOME/bin:$PATH" >> ~/.bashrc
echo "export PATH" >> ~/.bashrc

ANT_HOME=/usr/share/ant
export ANT_HOME
PATH=$ANT_HOME/bin:$PATH
export PATH


echo Building webapp

cd /usr/share/tomcat7/webapps/cmartsrc
ant dist
service tomcat7 start

echo "Finished installing C-MART tomcat server"
echo "Access the site at http://IP:/cmart-1/index"
echo "You will need to configure the DB settings in /cmartconfig.txt and restart tomcat before the site is functional"
exit 0