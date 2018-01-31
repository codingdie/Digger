#!/usr/bin/env bash
source  /etc/profile ;
WORKDIR='..'
if [ "$1" ];then
   WORKDIR=$1
fi

cd $WORKDIR
nohup java -jar libs/masternode-1.0.jar $WORKDIR/conf $2  > $WORKDIR/logs/master.log 2>&1 &
