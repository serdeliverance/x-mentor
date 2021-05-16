#!/bin/bash

COMMAND=$1

echo -n "Waiting for keycloak to start on x-keycloak:8080"
# loop until we connect successfully or failed
until curl -f -v "http://x-keycloak:8080/auth/realms/xmentor/.well-known/openid-configuration" > /dev/null
do
    echo "Waiting for keycloak to be up"
    sleep 5
done
echo -e "\nKeycloak is up. Executing command $COMMAND"
exec "$COMMAND"