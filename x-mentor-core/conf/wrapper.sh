#!/bin/bash

APPNAME="x-mentor-core"
INSTALLDIR="/opt/docker"

${INSTALLDIR}/bin/${APPNAME} -Dlogger.file=${INSTALLDIR}/conf/logback-prod.xml