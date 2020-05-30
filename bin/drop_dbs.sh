#!/usr/bin/env bash

##
## Drops JSONpedia MongoDB and Elasticsearch indexes after asking confirmation.
##

MONGO_PORT=7654
ELASTIC_PORT=9200

echo DROP MongoDB? [Y/n]
read r
if [[ "Y" == "$r" ]]; then
mongo --port $MONGO_PORT <<EOF
use jsonpedia
db.dropDatabase()
use jsonpedia_test_db
db.dropDatabase()
use jsonpedia_loader_test
db.dropDatabase()
use en
db.dropDatabase()
print("====== Existing DBs ======")
show dbs
EOF
else
echo Refused
fi

echo DROP Elastisearch? [Y/n]
read r
if [[ "Y" == "$r" ]]; then
curl -XDELETE "http://localhost:$ELASTIC_PORT/jsonpedia/" -o -
echo
curl -XDELETE "http://localhost:$ELASTIC_PORT/jsonpedia_loader_test/" -o -
echo
curl -XDELETE "http://localhost:$ELASTIC_PORT/jsonpedia_test_facet/" -o -
echo
curl -XDELETE "http://localhost:$ELASTIC_PORT/jsonpedia_test_db/" -o -
echo
echo ====== Existing DBs ======
curl 'http://localhost:9200/_cat/indices?v' -o -
else
echo Refused
fi