sudo ifconfig eth0 192.168.0.100/24 up
sudo ip route add default via 192.168.0.10 
sudo systemctl restart NetworkManager


sudo mkdir /etc/mysql/ssl
sudo cp certificates/ca.pem certificates/database.pem certificates/database-cert.pem /etc/mysql/ssl

sudo mkdir /etc/mysql/encryption

(echo -n "1;" ; openssl rand -hex 32 ) | sudo tee -a  /etc/mysql/encryption/keyfile
openssl rand -hex 128 | sudo tee -a /etc/mysql/encryption/keyfile.key
sudo openssl enc -aes-256-cbc -md sha1 \
   -pass file:/etc/mysql/encryption/keyfile.key \
   -in /etc/mysql/encryption/keyfile \
   -out /etc/mysql/encryption/keyfile.enc

sudo systemctl restart mariadb.service

sudo mysql  < create.sql
sudo mysql SIRS < populate.sql

# give permissions to the Groove Galaxy server
sudo mysql -u root -e "USE mysql; CREATE USER 'root'@'192.168.1.100' IDENTIFIED BY ''; GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.1.100' WITH GRANT OPTION;"
