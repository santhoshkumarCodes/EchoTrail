#!/bin/bash


echo "✅ Cassandra is ready. Running init.cql..."
cqlsh cassandra -f /init/init.cql
