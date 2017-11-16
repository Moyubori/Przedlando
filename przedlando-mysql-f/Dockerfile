FROM mysql:5.6

MAINTAINER Przedlando przedlando@gmail.com

ENV MYSQL_ROOT_PASSWORD=admin
ENV MYSQL_DATABASE=prestashop

COPY mysql.cnf /etc/mysql/conf.d/mysql.cnf

#COPY backup.sql /var/
#CMD cat /var/backup.sql | /usr/bin/mysql -u root --password=admin prestashop 