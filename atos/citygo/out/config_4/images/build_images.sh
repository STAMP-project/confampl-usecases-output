#!/bin/bash
#
# Generated by CAMP. Edit carefully
#
# Build all images and set the appropriate tags
#
docker build -t camp-postgres_0 ./postgres_0
docker build -t camp-showcase_0 ./showcase_0
echo 'All images ready.'
