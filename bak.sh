#!/bin/sh

BAK_DIR="/DATA"
DIRS="/etc/owncloud /var/lib/owncloud/data /var/lib/mysql"

pull() {
    if [ "${DATA_GIT_SSH}x" != "x" -a "${DATA_GIT_BRANCH}x" != "x" -a "${SSH_RSA}x" != "x" -a "${EMAIL}x" != "x" -a "${NAME}x" != "x" ];then
        echo "Pull start"

        HOST=$(echo ${DATA_GIT_SSH} | sed "s/[^@]*@\([^:]*\).*/\1/")
        
        if [ ! -f ~/.ssh/id_rsa ];then
            mkdir ~/.ssh
            chmod 700 ~/.ssh

            echo ${SSH_RSA} | base64 -d > ~/.ssh/id_rsa
            chmod 600 ~/.ssh/id_rsa

        fi

        if [ "$(cat ~/.ssh/known_hosts | grep $HOST)x" = "x" ];then
            ssh-keyscan $HOST >> ~/.ssh/known_hosts
        fi

        OLDPWD=$(pwd)

        cd ${BAK_DIR}
        [ ! -d .git ]&&git init
        git remote add origin ${DATA_GIT_SSH}
        git pull origin ${DATA_GIT_BRANCH}

        [ -f .chown ]&&bash ./.chown

        cd ${OLDPWD}

        echo "Pull done"
    else
        echo "Read git repo info failed"
        return 1
    fi
}

push() {
    if [ "${DATA_GIT_SSH}x" != "x" -a "${DATA_GIT_BRANCH}x" != "x" -a "${SSH_RSA}x" != "x" -a "${EMAIL}x" != "x" -a "${NAME}x" != "x" ];then
        echo "Push start"

        HOST=$(echo ${DATA_GIT_SSH} | sed "s/[^@]*@\([^:]*\).*/\1/")
        
        if [ ! -f ~/.ssh/id_rsa ];then
            mkdir ~/.ssh
            chmod 700 ~/.ssh

            echo ${SSH_RSA} | base64 -d > ~/.ssh/id_rsa
            chmod 600 ~/.ssh/id_rsa

        fi

        if [ "$(cat ~/.ssh/known_hosts | grep $HOST)x" = "x" ];then
            ssh-keyscan $HOST >> ~/.ssh/known_hosts
        fi

        OLDPWD=$(pwd)
        cd $BAK_DIR

        if [ ! -d .git ];then
            echo "no git repo"
            exit 1
        fi

        echo "Reading file info"

        {
            for FILE in $(find -name "*" | sed "s|^\./||;/^\.git/d;s| |_FLAG_SPACE__|g")
            do
                FILE=$(echo ${FILE} | sed "s|_FLAG_SPACE__| |g")
                stat -c "[ ! -d '%n' -a ! -f '%n' ]&&mkdir '%n'; chmod %a '%n'; chown %U:%G '%n'" "${FILE}"
            done
        } > .chown 

        echo "Create git commit"

        git add .
        git commit -m "$(date)"

        echo "Pushing"
        git push origin ${DATA_GIT_BRANCH} -f

        echo "Push done"

        cd $OLDPWD
    else
        echo "Read git repo info failed"
        return 1
    fi
}


init() {
    [ ! -d ${BAK_DIR} ] && mkdir ${BAK_DIR}

    pull

    for DIR in $DIRS
    do
        if [ ! -d ${BAK_DIR}/${DIR} ];then
            echo "not found in ${BAK_DIR}"
            mkdir -p ${BAK_DIR}/$(echo $DIR | sed 's|/[^/]*$|/|')
            if [ -d ${DIR} ];then
                mv ${DIR} ${BAK_DIR}/${DIR}
            else
                mkdir ${BAK_DIR}/${DIR}
            fi
        else
            rm -rf ${DIR}
        fi
        echo "link $DIR"
        ln -s ${BAK_DIR}/${DIR} ${DIR}
    done
}

init_nopull() {
    [ ! -d ${BAK_DIR} ] && mkdir ${BAK_DIR}

    OLDPWD=$(pwd)
    cd ${BAK_DIR}
    
    git init
    git remote add origin ${DATA_GIT_SSH}
    
    for DIR in $DIRS
    do
        if [ ! -d ${BAK_DIR}/${DIR} ];then
            echo "not found in ${BAK_DIR}"
            mkdir -p ${BAK_DIR}/$(echo $DIR | sed 's|/[^/]*$|/|')
            if [ -d ${DIR} ];then
                mv ${DIR} ${BAK_DIR}/${DIR}
            else
                mkdir ${BAK_DIR}/${DIR}
            fi
        else
            rm -rf ${DIR}
        fi
        echo "link $DIR"
        ln -s ${BAK_DIR}/${DIR} ${DIR}
    done

    cd ${OLDPWD}
}

case $1 in
    init) init ;;
    pull) pull ;;
    push) push ;;
    *) echo "$0 [init|pull|push]" ;;
esac