# we remove the eth MAC addresses as the VMs often change MAC addresses during deployment
rm -f /etc/udev/rules.d/70-persistent-net.rules
rm -f /etc/udev/rules.d/75-persistent-net-generator.rules
echo "# " > /etc/udev/rules.d/75-persistent-net-generator.rules

# we turn off IP tables to prevent any experiment problems - this is not designed to be used on a production server
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

service tomcat6 stop
service httpd stop
service cassandra stop
service mysqld stop
service solr stop
service mongod stop

history -c && history -w
rm -f ~/.bash_history
cat /dev/null > ~/.bash_history
rm -f /root/.bash_history

yum clean all

echo "Zeroing disk so we can resize the qcow image. Will take some time"
dd if=/dev/zero of=./zero bs=1M
sync
rm -f ./zero

shutdown -h now
