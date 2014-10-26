#!/bin/sh
#
# This script sets up a server to work as the C-MART mysql server
#
ORIGPATH=$(pwd)

echo "Installing make and gcc"
yum install make gcc gcc-c++ -y

echo "Removing security"
# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

echo "Untaring mysql"
groupadd mysql
useradd -r -g mysql mysql
wget https://downloads.mariadb.com/archives/mysql-5.5/mysql-5.5.27-linux2.6-x86_64.tar.gz
tar zxf mysql-5.5.27-linux2.6-x86_64.tar.gz

echo "Deleting old mysql"
service mysqld stop

rm -r -f /var/lib/mysql
rm -r -f /usr/share/mysql
rm -r -f /var/lock/subsys/mysql
rm -r -f /usr/local/mysql
mv -f ./mysql-5.5.27-linux2.6-x86_64 /usr/share/mysql

echo "Starting mysql"
cd /usr/local
ln -s /usr/share/mysql mysql
cd mysql
chown -R mysql .
chgrp -R mysql .

echo "Configuring mysql - repeating multiple times as this seems to fail often"
sleep 30
/usr/share/mysql/scripts/mysql_install_db --user=mysql
sleep 30

chown -R root .
chown -R mysql data

rm -f /etc/my.cnf
/bin/cp -f $ORIGPATH/my.cnf /etc/my.cnf
/bin/cp -f $ORIGPATH/my_4096.cnf /etc/my_4096.cnf
/bin/cp -f $ORIGPATH/my_2048.cnf /etc/my_2048.cnf
/bin/cp -f $ORIGPATH/my_1024.cnf /etc/my_1024.cnf

/bin/cp -f /usr/share/mysql/support-files/mysql.server /etc/init.d/mysqld

sleep 10
service mysqld start

sleep 10
cd $ORIGPATH

tar zxf mysql-5.5.27-linux2.6-x86_64.tar.gz

echo "Deleting old mysql"
service mysqld stop

rm -r -f /var/lib/mysql
rm -r -f /usr/share/mysql
rm -r -f /var/lock/subsys/mysql
rm -r -f /usr/local/mysql
mv -f ./mysql-5.5.27-linux2.6-x86_64 /usr/share/mysql

echo "Starting mysql"
cd /usr/local
ln -s /usr/share/mysql mysql
cd mysql
chown -R mysql .
chgrp -R mysql .

echo "Configuring mysql - repeating multiple times as this seems to fail often"
sleep 30
/usr/share/mysql/scripts/mysql_install_db --user=mysql
sleep 130

chown -R root .
chown -R mysql data

rm -f /etc/my.cnf
/bin/cp -f $ORIGPATH/my.cnf /etc/my.cnf
/bin/cp -f $ORIGPATH/my_4096.cnf /etc/my_4096.cnf
/bin/cp -f $ORIGPATH/my_2048.cnf /etc/my_2048.cnf
/bin/cp -f $ORIGPATH/my_1024.cnf /etc/my_1024.cnf

/bin/cp -f /usr/share/mysql/support-files/mysql.server /etc/init.d/mysqld

sleep 10
service mysqld start


echo "Creating C-MART DB"
cd /usr/share/mysql/bin/
		
./mysqladmin -u root password 'Cm4rt!'
./mysqladmin -u root -pCm4rt! -h cmartserver password 'Cm4rt!'
./mysqladmin -u root -pCm4rt! -h $(hostname) password 'Cm4rt!'

./mysql -u root -pCm4rt! < $ORIGPATH/configure_cmart.sql

sleep 10

service mysqld restart
chkconfig mysqld on

echo "Installing phpmyadmin"
yum install httpd php php-mysql -y
		
cd $ORIGPATH
wget http://downloads.sourceforge.net/project/phpmyadmin/phpMyAdmin/3.4.7/phpMyAdmin-3.4.7-english.tar.gz?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fphpmyadmin%2Ffiles%2FphpMyAdmin%2F3.4.7%2F&ts=1414352560&use_mirror=softlayer-dal
tar zxvf phpMyAdmin-3.4.7-english.tar.gz
mv ./phpMyAdmin-3.4.7-english /usr/share/phpMyAdmin
		
mv /usr/share/phpMyAdmin/config.sample.inc.php /usr/share/phpMyAdmin/config.inc.php

echo "alias /phpmyadmin /usr/share/phpMyAdmin" > /etc/httpd/conf.d/phpMyAdmin.conf
		
chkconfig httpd on
service httpd start

echo "Finished installing C-MART mysql server"
exit 0