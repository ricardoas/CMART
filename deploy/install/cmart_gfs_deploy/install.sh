#!/bin/sh
#
# This script sets up a server to work as the C-MART GFS server
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

# installing mongo
cd /usr/share
wget http://downloads.mongodb.org/linux/mongodb-linux-x86_64-2.0.4-rc0.tgz
tar xzf mongodb-linux-x86_64-2.0.4-rc0.tgz
rm -f mongodb-linux-x86_64-2.0.4-rc0.tgz
mv mongodb-linux-x86_64-2.0.4-rc0 mongo

# make data dir
mkdir -p /data/db/
chown `id -u` /data/db
mkdir /data/db/a /data/db/b /data/db/main

cd /usr/share/mongo/bin
./mongod --shardsvr --dbpath /data/db/a --port 10000 > /tmp/sharda.log &
./mongod --shardsvr --dbpath /data/db/b --port 10001 > /tmp/shardb.log &
echo "Waiting for Mongo to preallocate files..."
sleep 400


mkdir -p /data/db/config
./mongod --configsvr --dbpath /data/db/config --port 27019 > /tmp/configdb.log &
echo "Waiting for Mongo to preallocate more files..."
sleep 400

./mongos --configdb cmartserver > /tmp/mongos.log &
sleep 200

./mongo cmartserver/admin $ORIGDIR/my_commands.js
sleep 5
./mongo cmartserver/admin $ORIGDIR/shutdown.js
./mongod --shutdown --dbpath /data/db/config
./mongod --shutdown --dbpath /data/db/b
./mongod --shutdown --dbpath /data/db/a

sleep 10

./mongod --configsvr --dbpath /data/db/main --port 27017 > /tmp/mongod.log &
echo "Waiting for Mongo to preallocate more files..."
sleep 400

./mongod --shutdown --dbpath /data/db/main

/bin/cp -f $ORIGDIR/startup.sh /usr/share/mongo/bin
/bin/cp -f $ORIGDIR/shutdown.sh /usr/share/mongo/bin
/bin/cp -f $ORIGDIR/shutdown.js /usr/share/mongo/bin
chmod +x /usr/share/mongo/bin/startup.sh
chmod +x /usr/share/mongo/bin/shutdown.sh

cp $ORIGDIR/mongod /etc/init.d
chmod +x /etc/init.d/mongod

chkconfig mongod on
service mongod restart

echo "Finished"