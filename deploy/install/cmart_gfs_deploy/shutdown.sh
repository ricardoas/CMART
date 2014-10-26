echo "Shutting Down Mongod"
#/usr/share/mongo/bin/mongo cmartserver/admin /usr/share/mongo/bin/shutdown.js
/usr/share/mongo/bin/mongod --shutdown --dbpath /data/db/main
/usr/share/mongo/bin/mongod --shutdown --dbpath /data/db/a
/usr/share/mongo/bin/mongod --shutdown --dbpath /data/db/b
sleep 5
echo "Done"