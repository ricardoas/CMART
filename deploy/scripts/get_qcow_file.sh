echo 'Entering the script for getting files. for the vm image! - Ji Yang'

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
echo 'The remote file looks like '
cat ${remote_path}
download ${remote_path} ${local_path}
_EOF_
