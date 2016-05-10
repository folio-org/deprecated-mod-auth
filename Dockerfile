###
# vert.x docker example using a Java verticle packaged as a fatjar
# To build:
#  docker build -t indexdata/okapi-sample-module .
# To run:
#   docker run -t -i -p 8080:8081 indexdata/okapi-sample-module
###

FROM java:8

ENV VERTICLE_FILE auth-prototype-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8081

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/
COPY src/main/resources/authSecrets.json $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar -Dport=8081 -DauthType=flatfile -DsecretsFilepath=/usr/verticles/authSecrets.json $VERTICLE_FILE"]
