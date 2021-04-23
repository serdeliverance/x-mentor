# OSNICK=stretch|bionic|buster
ARG OSNICK=buster

#----------------------------------------------------------------------------------------------
FROM redisfab/redisedgevision-${OSNICK}:0.2.0

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get -qq update

RUN set -ex ;\
	apt-get install -y wget python3-distutils ;\
	wget -q https://bootstrap.pypa.io/get-pip.py -O /tmp/get-pip.py ;\
	python3 /tmp/get-pip.py

WORKDIR /app
ADD . /app

RUN pip3 install -r requirements.txt

ENTRYPOINT [ "python3" ]