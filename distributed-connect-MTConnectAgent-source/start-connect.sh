#!/bin/bash -e

# connector start command here.
exec "/opt/kafka/bin/connect-distributed.sh" "/opt/kafka/config/connect-distributed.properties"
#sleep 15
#command=(curl -POST -H "Content-Type: application/json" --data /opt/kafka/config/connect-mtconnect-source.json http://localhost:8084/connectors/)
#exec "$command[@]"

