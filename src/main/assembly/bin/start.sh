#!/bin/bash
# 不可自行修改脚本，统一管理
# 开启脚本调试
#set -x

#设置生效那个环境配置文件
#---- start.sh stdout 为开启stdout.log
# 目前有 dev kx.verify kx.prod 这几个环境，对应application-*.yml 的配置文件
PROFILES_ACTIVE=dev

# 定义日志输出位置，如果没定义的话，在./logs目录 必须/结尾
LOGPATH=./logs/

#设置 Sentinel 控制台地址 -Dcsp.sentinel.dashboard.server=172.20.21.40:58080
#SENTINEL_OPTS='-Dcsp.sentinel.dashboard.server=localhost:58080 -Dproject.name=openAccess -Dcsp.sentinel.log.use.pid=true'

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true   "
green_echo ()    { echo -e "\033[032;1m$@\033[0m"; }

cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/config

LOGS_FILE=$DEPLOY_DIR/logs

if [ -z "$SERVER_NAME" ]; then
    SERVER_NAME=`hostname`
fi

PIDS=`ps -f | grep java | grep ".jar" | grep "$CONF_DIR" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi

if [ -n "$SERVER_PORT" ]; then
    SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
    if [ $SERVER_PORT_COUNT -gt 0 ]; then
        echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
        exit 1
    fi
fi

#LOGS_DIR=""
#if [ -n "$LOGS_FILE" ]; then
#    LOGS_DIR=`dirname $LOGS_FILE`
#else
#    LOGS_DIR=$DEPLOY_DIR/logs
#fi
LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
STDOUT_FILE=$LOGS_DIR/stdout.log

LIB_DIR=$DEPLOY_DIR
MAIN_JAR=`ls $LIB_DIR|grep .jar |awk '{print "'$LIB_DIR'/"$0}' `
echo $MAIN_JAR
#LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
JAVA_DEBUG_OPTS=""
if [ "$1" = "debug" ]; then
    JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
fi
JAVA_JMX_OPTS=""
if [ "$1" = "jmx" ]; then
    JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
fi
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
    JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn512m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
else
    JAVA_MEM_OPTS=" -server -Xms1g -Xmx1g -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
fi

echo -e "Starting the $SERVER_NAME ...\c"
if [ "$1" == "stdout" ]; then
    nohup java  $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS  $SENTINEL_OPTS  -Xbootclasspath/a:$CONF_DIR  -DlogPath=$LOGPATH -Dspring.profiles.active=$PROFILES_ACTIVE  -jar $MAIN_JAR > $STDOUT_FILE 2>&1 &
  else
     nohup java  $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS  $SENTINEL_OPTS  -Xbootclasspath/a:$CONF_DIR  -DlogPath=$LOGPATH  -Dspring.profiles.active=$PROFILES_ACTIVE  -jar $MAIN_JAR > /dev/null 2>&1 &
fi

COUNT=0
while [ $COUNT -lt 1 ]; do
    echo -e ".\c"
    sleep 1
    if [ -n "$SERVER_PORT" ]; then
        if [ "$SERVER_PROTOCOL" == "thrift" ]; then
    	    #COUNT=`echo status | nc -i 1 127.0.0.1 $SERVER_PORT | grep -c OK`
    	    COUNT=`netstat -an | grep $SERVER_PORT | wc -l`
        else
            COUNT=`netstat -an | grep $SERVER_PORT | wc -l`
        fi
    else
    	COUNT=`ps -f | grep java | grep ".jar" | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`
    fi
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "OK!"
PIDS=`ps -f | grep java | grep ".jar" | grep "$DEPLOY_DIR" | awk '{print $2}'`
sleep 2
green_echo  "------ Prog info PLS check &  dev(开发环境) kx.prod (科兴验证环境) kx.prod (科兴生产环境)  ks.prod (金山生产环境) -----"
echo "PID: $PIDS"
echo "PROG_ENV: $PROFILES_ACTIVE"
echo "LOGS": `ls -l /proc/$PIDS/fd | grep .log | awk '{print $NF}' | sort |uniq`