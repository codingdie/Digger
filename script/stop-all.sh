#!/usr/bin/env bash
USER_NAME='root';
SLAVES_CONF='conf/slaves.conf'
SPIDER_CONF='conf/work.conf'
MASTER_CONF='conf/master.conf'
DEPLOY_CONF='conf/deploy.conf'


for line in `cat $SPIDER_CONF`
do

    if [[ $line =~ "workdir=" ]]
    then
        WORKDIR=${line#"workdir="}
    fi
done


for line in `cat $DEPLOY_CONF`
do

    if [[ $line =~ "user=" ]]
    then
        USER_NAME=${line#"user="}
    fi
done


for line in `cat $SLAVES_CONF`
do
    if [[ $line =~ "hosts=" ]]
    then
        hosts=${line#"hosts="}
        OLD_IFS="$IFS"
        IFS=","
        array=($hosts)
        IFS="$OLD_IFS"
        for host in ${array[*]}
            do
            echo  "stop slave " $host
            ssh   $USER_NAME@${host}  "cd $WORKDIR;sh stop-slavenode.sh"
        done
    fi
done

for line in `cat $MASTER_CONF`
do
    if [[ $line =~ "user=" ]]
    then
        USER_NAME=${line#"user="}
    fi
    if [[ $line =~ "host=" ]]
    then
        host=${line#"host="}
        echo  "stop master " $host
        ssh   $USER_NAME@${host}  "cd $WORKDIR;sh stop-masternode.sh"
    fi
done
echo "finish stop "

