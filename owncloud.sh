#!/bin/sh

echo "Linking webapp owncloud..."

ln -s /usr/share/webapps/owncloud /var/www/localhost/htdocs/

echo "Setting php upload/post max size ad 512M..."

sed -i "s/^\([^_]*\_max\_[^_]*size = \)[0-9]*M$/\1512M/g" /etc/php5/php.ini
