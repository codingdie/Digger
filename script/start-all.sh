#!/usr/bin/env bash
USER_NAME='xupeng';
SLAVES_CONF='conf/slaves.conf'
WORKDIR='/opt/tieba-spider'
for line in `cat $SLAVES_CONF`
do
    echo "${line}"
    ssh   $USER_NAME@${line} mkdir -p $WORKDIR 
    rsync  --progress -rut conf $USER_NAME@${line}:$WORKDIR
    rsync  --progress -rut libs $USER_NAME@${line}:$WORKDIR
    rsync  --progress -rut *.sh  $USER_NAME@${line}:$WORKDIR
done

echo "end"

