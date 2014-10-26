echo 'Entering the script for modifying files. for the vm image!'

image=$1$2
local_path=$3
remote_path=$4

echo 'The given modifed VM image is '${image}
echo 'The given path in local is '${local_path}
echo 'The given remote path in qcow2 is '${remote_path}

guestfish <<_EOF_
add ${image}
run
mount /dev/vg_cmartserver/lv_root /
echo 'Before editing, the file is '
cat ${remote_path}
upload ${local_path} ${remote_path}
echo 'After editing, the file is '
cat ${remote_path}
_EOF_
