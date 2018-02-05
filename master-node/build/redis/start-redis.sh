#!/usr/bin/env bash
tar -zxvf redis-4.0.1.tar.gz
cd redis-4.0.1
make
cd src
./redis-cli shutdown
./redis-server ../../redis.conf