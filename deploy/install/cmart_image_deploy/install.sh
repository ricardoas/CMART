#!/bin/sh
#
# This script sets up a server to work as the C-MART image server
#

echo Removing MAC address and firewall

# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

yum install nfs-utils portmap rpcbind nfs4-acl-tools -y

echo Making image share

# create the NFS share and start the service

if [ ! -d /img ]; then
mkdir /img
chmod 777 /img
echo "/img *(rw,async,no_root_squash)" >> /etc/exports
fi

chkconfig rpcbind on
service rpcbind restart
chkconfig portmap on
service portmap restart
chkconfig nfs on
service nfs restart
exportfs -r

echo Finished installing C-MART image server
exit 0