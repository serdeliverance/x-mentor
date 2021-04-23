#!/bin/sh

docker stop x-mentor-client
docker rm x-mentor-client
docker rmi xmentor/x-mentor-client:latest

docker build -t xmentor/x-mentor-client:latest .
docker push xmentor/x-mentor-client:latest