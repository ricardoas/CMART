echo 'Script for install KVM & Libvrt - 10/25/2011'

yum install -y qemu-kvm libvirt virt-manager
modprobe kvm-intel
modprobe kvm
service libvirt start
