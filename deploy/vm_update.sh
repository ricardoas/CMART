yum install guestfish -y
echo 'Uploading new C-MART to VM Images'


guestfish -a ./updatetest/SQL.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/SQL.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_
