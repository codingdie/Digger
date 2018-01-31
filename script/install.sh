#!/usr/bin/env bash
USER_NAME='root';
SLAVES_CONF='conf/slaves.conf'
MASTER_CONF='conf/master.conf'
SPIDER_CONF='conf/work.conf'
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
            echo  $host
            ssh   $USER_NAME@${host} mkdir -p $WORKDIR

            rsync  --progress -rut conf $USER_NAME@${host}:$WORKDIR
            rsync  --progress -rut libs $USER_NAME@${host}:$WORKDIR
            rsync  --progress -rut *.sh  $USER_NAME@${host}:$WORKDIR
            rsync  --progress -rut logs $USER_NAME@${host}:$WORKDIR



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
        echo  $host
        ssh   $USER_NAME@${host} mkdir -p $WORKDIR
        rsync  --progress -rut conf $USER_NAME@${host}:$WORKDIR
        rsync  --progress -rut libs $USER_NAME@${host}:$WORKDIR
        rsync  --progress -rut *.sh  $USER_NAME@${host}:$WORKDIR
        rsync  --progress -rut logs $USER_NAME@${host}:$WORKDIR
    fi
done

echo "finish install "

