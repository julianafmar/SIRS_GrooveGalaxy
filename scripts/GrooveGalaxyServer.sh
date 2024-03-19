sudo ifconfig eth0 192.168.1.100/24 up
sudo ifconfig eth1 192.168.2.100/24 up
sudo ip route add 192.168.0.100 via 192.168.1.10 dev eth0
sudo ip route add default via 192.168.2.10
sudo systemctl restart NetworkManager
