sudo ifconfig eth0 192.168.2.10/24 up
sudo ifconfig eth1 192.168.5.10/24 up
sudo sysctl net.ipv4.ip_forward=1
sudo systemctl restart NetworkManager

#
# Firewall
#
sudo iptables -F
sudo iptables -t nat -F
sudo iptables -P FORWARD DROP
sudo iptables -P INPUT DROP
sudo iptables -P OUTPUT DROP

sudo iptables -A FORWARD -m state --state ESTABLISHED -j ACCEPT

sudo iptables -A FORWARD -i eth0 -s 192.168.2.100 -j ACCEPT
sudo iptables -A FORWARD -i eth1 -d 192.168.2.100 -j ACCEPT

sudo iptables -t nat -A PREROUTING -i eth1 --dst 192.168.5.10 -p tcp --dport 8080 -j DNAT --to-destination 192.168.2.100:8080
