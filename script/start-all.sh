#!/usr/bin/env bash
USER_NAME='xupeng';
SLAVES_CONF='conf/slaves.conf'
WORKDIR='/opt/tieba-spider'
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
            ssh   $USER_NAME@${host} mkdir -p $WORKDIR
            rsync  --progress -rut conf $USER_NAME@${host}:$WORKDIR
            rsync  --progress -rut libs $USER_NAME@${host}:$WORKDIR
            rsync  --progress -rut *.sh  $USER_NAME@${host}:$WORKDIR
            ssh   $USER_NAME@${host}  "source  /etc/profile ;java -jar $WORKDIR/libs/slavenode-1.0.jar"

        done
    fi
done
echo "finish start "

