#!/bin/bash
# This script sets up a server to work as the C-MART load balancer
#

echo "Making sure we can compile, installing gcc"
yum install make gcc gcc-c++ -y
export CFLAGS=-O2


echo "Removing MAC and firewall"

# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules
	
# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off


echo "Untaring apache"
wget http://archive.apache.org/dist/httpd/httpd-2.4.6.tar.gz
tar -zxf httpd-2.4.6.tar.gz


echo "Installing pcre"
wget https://autosetup1.googlecode.com/files/pcre-8.20.tar.gz
tar -zxf pcre-8.20.tar.gz
cd ./pcre-8.20
./configure --prefix=/opt
make
make install


echo "Installing apr"
cd ..
wget http://mirrors.axint.net/apache/apr/apr-1.4.6.tar.gz
tar -zxf apr-1.4.6.tar.gz


mkdir ./httpd-2.4.6/srclib/apr
/bin/cp -rf ./apr-1.4.6/* ./httpd-2.4.6/srclib/apr


echo "Installing apr-util"
wget https://archive.apache.org/dist/apr/apr-util-1.4.1.tar.gz
tar -zxf apr-util-1.4.1.tar.gz

mkdir ./httpd-2.4.6/srclib/apr-util
/bin/cp -rf ./apr-util-1.4.1/* ./httpd-2.4.6/srclib/apr-util


echo "Installing httpd"
mkdir -p /usr/share/apache2

cd ./httpd-2.4.6
./configure --prefix=/usr/share/apache2 --with-included-apr --with-pcre=/opt --with-web-apache-path=/usr/share/apache2
make
make install


echo "Copying configs"
cd ..
/bin/cp -f ./httpd /etc/init.d/httpd
chmod +x /etc/init.d/httpd
/bin/cp -f ./httpd.conf /usr/share/apache2/conf/
mkdir -p /usr/share/apache2/conf.d/
/bin/cp -f ./lb.conf /usr/share/apache2/conf.d/lb.conf


echo "Starting the service"
mkdir /var/run/httpd/
chkconfig httpd on
service httpd start


echo "Finished installing apache"
echo ""
echo "********************************************************************************"
echo "*                                                                              *"
echo "* Now add your app IPs to /usr/share/apache2/conf.d/lb.conf then restart httpd *"
echo "*                                                                              *"
echo "********************************************************************************"