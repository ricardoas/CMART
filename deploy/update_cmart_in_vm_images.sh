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

guestfish -a ./updatetest/Application.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Application.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/CasMaster.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/CasMaster.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Image.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Image.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Solr.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Solr.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Client.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/Client.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/LoadBalancer.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/LoadBalancer.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/GFS.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'deleting cmart'
rm-rf /cmart
echo 'done'
_EOF_

guestfish -a ./updatetest/GFS.qcow2 <<_EOF_
echo 'mounting image'
run
mount /dev/vg_cmartserver/lv_root /
echo 'uploading new cmart'
mkdir /cmart
copy-in /cmart /cmart
echo 'done'
_EOF_

echo "You might also want to update the tomcat site if you made changes?"
echo "In Application it is in /usr/share/tomcat6/webapps"