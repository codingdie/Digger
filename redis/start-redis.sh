#!/usr/bin/env bash
ls ../build
mkdir ../build/redis
cp -r  ../redis  ../build
cd ../build/redis
tar -zxvf redis-4.0.1.tar.gz
cd redis-4.0.1
make
cd src
./redis-cli shutdown
./redis-server ../../redis.conf