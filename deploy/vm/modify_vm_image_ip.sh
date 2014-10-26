echo 'Entering the script for modifying ifcfg-eth0 for the vm image!'

image=$1$2
ip_file_path=$1ifcfg-eth0

echo 'The given modifed VM image is '${image}
echo 'The given ifcfg eth0 file path is '${ip_file_path}

guestfish <<_EOF_
add ${image}
run
mount /dev/vg_cmartserver/lv_root /
echo 'Before editing, the file is '
cat /etc/sysconfig/network-scripts/ifcfg-eth0
upload ${ip_file_path} /etc/sysconfig/network-scripts/ifcfg-eth0
echo 'After editing, the file is '
cat /etc/sysconfig/network-scripts/ifcfg-eth0
_EOF_

echo 'Finished runing the script for '${image}