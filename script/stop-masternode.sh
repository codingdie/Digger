#!/usr/bin/env bash
source  /etc/profile ;
pidStr=`jps -l | grep masternode `
if [ "$pidStr" ];then
    echo $pidStr
    arr=(${pidStr// / })
    kill -9  ${arr[0]}
fi

