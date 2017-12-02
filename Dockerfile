FROM prestashop/prestashop:1.6

MAINTAINER Przedlando przedlando@gmail.com

RUN apt-get -y update
RUN apt-get -y install git nano


#RUN rm -r /var/www
#RUN mkdir /var/www
WORKDIR /var/www/html
RUN rm -rf /var/www/html/*

WORKDIR /var/

RUN git init
RUN git remote add origin https://github.com/PiotrowskiD/presta.git
RUN git fetch --all
RUN git reset --hard origin/master

WORKDIR /var/www
RUN chmod -R 777 /var/www/html/*

#RUN chmod -R 777 /var/www/html/config
#RUN chmod -R 777 /var/www/html/upload
#RUN chmod -R 777 /var/www/html/download
#RUN chmod -R 777 /var/www/html/cache
#RUN chmod -R 777 /var/www/html/log
#RUN chmod -R 777 /var/www/html/img
#RUN chmod -R 777 /var/www/html/mails
#RUN chmod -R 777 /var/www/html/modules
#RUN chmod -R 777 /var/www/html/themes
#RUN chmod -R 777 /var/www/html/translations
#RUN chmod -R 777 /var/www/html/admin1

RUN rm -rf /var/www/html/install

EXPOSE 80
