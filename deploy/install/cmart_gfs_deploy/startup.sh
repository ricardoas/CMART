echo "Starting Mongod"
cd /usr/share/mongo/bin
./mongod --shardsvr --dbpath /data/db/a --port 10000 > /tmp/sharda.log &
./mongod --shardsvr --dbpath /data/db/b --port 10001 > /tmp/shardb.log &
sleep 10
./mongod --dbpath /data/db/main --port 27017 > /tmp/mongod.log &
#./mongod --configsvr --dbpath /data/db/config --port 27017 > /tmp/mongod.log &
#./mongos --configdb cmartserver > /tmp/mongos.log &
echo "Done"