#!/bin/bash
echo "⏳ Waiting for Cassandra to be healthy..."
until cqlsh cassandra -e "describe keyspaces"; do
  sleep 5
done

echo "✅ Cassandra is ready. Running init.cql..."
cqlsh cassandra -f /init/init.cql
