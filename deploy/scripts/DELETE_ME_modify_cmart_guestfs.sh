yum install guestfish -y
echo 'Entering the script for modifying ifcfg-eth0 for the vm image! - Ji Yang'

image=$1$2
ip_file_path=$1ifcfg-eth0

echo 'The given modifed VM image is '${image}
echo 'The given ifcfg eth0 file path is '${ip_file_path}

guestfish <<_EOF_
echo 'mounting image'
add /sharedstorage/shared/cmart_release/cmart_tmacat.qcow2
run
mount /dev/vg_cmartserver/lv_root /

echo 'removing cmart'
rm -r -f /cmart

echo 'uploading new cmart'
upload /sharedstorage/cmart /cmart

echo 'done'
_EOF_

guestfish <<_EOF_
echo 'mounting image'
add /sharedstorage/shared/cmart_release/cmart_tmacat.qcow2
run
mount /dev/vg_guest/lv_root /

echo 'removing cmart'
rm -r -f /cmart

echo 'uploading new cmart'
upload /sharedstorage/cmart /cmart

echo 'done'
_EOF_