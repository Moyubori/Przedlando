docker-compose up -d
echo "Waiting for everything to boot up ... 15s"
timeout 15
echo "Updating database"
cat ./przedlando-mysql-f/backup1.sql | docker exec -i dockerfile_przedlando-mysql-f_1 /usr/bin/mysql -u root --password=admin prestashop
echo "Go to http://localhost:8080"
pause
