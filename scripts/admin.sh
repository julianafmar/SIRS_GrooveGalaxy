rm -rf *.crt
rm -rf *.csr
rm -rf *.key
rm -rf *.srl
rm -rf *.p12
rm -rf *.pem
rm -rf *.jks
rm -rf *.truststore
rm -rf *.keystore

# ------------------------------------------------------------------------------------------
#                                      Certificates
# ------------------------------------------------------------------------------------------

# -------------------------------------------------------
#        Certificate SSL database - server API
# -------------------------------------------------------
openssl genrsa -out ca.key
openssl req -new -x509 -days 365 -key ca.key -out ca.crt -subj "/C=PT/CN=CA"
openssl x509 -in ca.crt -out ca.pem

openssl genrsa -out database.key
openssl req -new -key database.key -out database.csr -subj "/C=PT/ST=Lisbon/L=Lisbon/O=IST/CN=192.168.0.100"
openssl x509 -req -days 365 -in database.csr -CA ca.crt -CAkey ca.key -out database.crt
openssl x509 -in database.crt -out database-cert.pem
openssl rsa -in database.key -text > database.pem

openssl genrsa -out serverAPI.key
openssl req -new -key serverAPI.key -out serverAPI.csr -subj "/CN=serverAPI/OU=serverAPI/O=IST/L=Lisbon/ST=Portugal/C=PT"
openssl x509 -req -days 365 -in serverAPI.csr -CA ca.crt -CAkey ca.key  -out serverAPI.crt
openssl x509 -in serverAPI.crt -out serverAPI-cert.pem

openssl pkcs12 -password pass:changeme -export -in serverAPI.crt -inkey serverAPI.key -out serverAPI.p12

mv serverAPI-cert.pem serverAPI.crt serverAPI.csr serverAPI.key serverAPI.p12 GrooveGalaxyServer/certificates_database/
cp database-cert.pem GrooveGalaxyServer/certificates_database/
mv ca.crt ca.key ca.pem database-cert.pem database.crt database.csr database.key database.pem Database/certificates

# -------------------------------------------------------
#        Certificate SSL server API - client
# -------------------------------------------------------
rm -rf *.crt
rm -rf *.csr
rm -rf *.key
rm -rf *.srl
rm -rf *.p12
rm -rf *.pem
rm -rf *.jks
rm -rf *.truststore
rm -rf *.keystore

openssl genrsa -out ca.key 2048
openssl req -new -x509 -days 365 -key ca.key -out ca.crt -subj "/CN=CA"

# Server
openssl genrsa -out server.key 2048 
openssl req -new -key server.key -out server.csr -subj "/CN=server"
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pem
mv server.key server.csr server.pem server.crt GrooveGalaxyServer/certificates_client

# Client
openssl genrsa -out client.key 2048 
openssl req -new -key client.key -out client.csr -subj "/CN=client"
openssl x509 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt
openssl pkcs8 -topk8 -nocrypt -in client.key -out client.pem
mv client.key client.csr client.pem client.crt ca.crt ca.key Client/certificates


# ------------------------------------------------------------------------------------------
#                                           Keys
# ------------------------------------------------------------------------------------------
openssl genrsa -out juliana.key
openssl genrsa -out ines.key
openssl genrsa -out mario.key
openssl genrsa -out ana.key
openssl genrsa -out madalena.key
openssl genrsa -out diogo.key
openssl genrsa -out global.key

cp *.key Client/keys
mv *.key GrooveGalaxyServer/keys
