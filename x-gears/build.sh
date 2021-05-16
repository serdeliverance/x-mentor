#!/bin/sh

docker stop x-gears
docker rm x-gears
docker rmi xmentor/x-gears:latest

docker build -t xmentor/x-gears:latest .
docker push xmentor/x-gears:latest