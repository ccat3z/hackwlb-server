#!/bin/sh

sed -i "/^\[mysqld\]$/auser = root" /etc/mysql/my.cnf
[ ! -d "/run/mysqld" ] && mkdir -p /run/mysqld
sed -i '/^pdo_mysql.default_socket=$/s/$/\/run\/mysqld\/mysqld.sock/' /etc/php5/php.ini

if [ "$(ls /var/lib/mysql)x" != "x" ];then
    echo "MySQL database exist, skip install database"
    exit
fi

echo "Installing MySQL database..."

mysql_install_db &> /dev/null

echo "Setting MySQL db..."

MYSQL_ROOT_PASSWORD=MTYxNTgK

MYSQL_OWNCLOUD_PASSWORD=MjQ4MjQK

echo "Launching mysqld..."

mysqld &> /dev/null &

i=0

while [ $i -lt 30 ]
do
    if [ "$(echo 'SELECT 1' | "mysql" 2> /dev/null)x" != "x" ];then
        break
    fi
    echo 'MySQL init process in progress...'
    sleep 1s
    i=$(($i + 1))
done

if [ "$i" = 30 ]; then
    echo >&2 'MySQL init process failed'
    exit 1
fi

echo "Setting root password..."

mysqladmin -u root password "$MYSQL_ROOT_PASSWORD"

echo "Creating owncloud user and database..."

cat << EOF | mysql -u root --password=$MYSQL_ROOT_PASSWORD
CREATE DATABASE owncloud;
GRANT ALL ON owncloud.* TO 'owncloud'@'localhost' IDENTIFIED BY '$MYSQL_OWNCLOUD_PASSWORD';
GRANT ALL ON owncloud.* TO 'owncloud'@'localhost.localdomain' IDENTIFIED BY '$MYSQL_OWNCLOUD_PASSWORD';
FLUSH PRIVILEGES;
EXIT
EOF

echo "Done, killing mysqld..."

killall mysqld
