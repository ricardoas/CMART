qemu-img convert -O qcow2 new.qcow2 new_min.qcow2
rm -f new.qcow2
mv new_min.qcow2 new.qcow2
