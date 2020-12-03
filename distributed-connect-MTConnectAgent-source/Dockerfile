FROM wurstmeister/kafka:2.12-2.2.0

# we replace the default connect-standalone.properties so we can properly resolve to our local kafka docker development
COPY connect-distributed.properties /opt/kafka/config/

COPY mtconnect-source-connector-1.0-SNAPSHOT.jar /usr/local/share/kafka/plugins/

COPY connect-mtconnect-source.json /opt/kafka/config/

# we replace the start command creating a connector instead.
COPY start-connect.sh /usr/bin/

# permissions
RUN chmod a+x /usr/bin/start-connect.sh

