#!/usr/bin/env bash
source  /etc/profile ;
WORKDIR='..'
if [ "$1" ];then
   WORKDIR=$1
fi

cd $WORKDIR/libs
nohup java -jar masternode-1.0.jar $WORKDIR/conf $2  > ../logs/master.log 2>&1 &
