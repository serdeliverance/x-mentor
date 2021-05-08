#!/bin/bash

APPNAME="x-mentor-core"
INSTALLDIR="/opt/docker"

bash wait-for-keycloak.sh

${INSTALLDIR}/bin/${APPNAME} -Dlogger.file=${INSTALLDIR}/conf/logback-prod.xml