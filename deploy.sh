set -e
sudo systemctl stop tomcat8
sudo rm -rf target
mvn clean
mvn package
mvn war:war
sudo rm -rf /var/lib/tomcat8/webapps/data-reader-0.0.1
sudo cp target/data-reader-0.0.1.war /var/lib/tomcat8/webapps/
sudo systemctl start tomcat8
