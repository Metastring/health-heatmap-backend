set -e
systemctl --user stop hhm
mvn compile package
./scripts/drop-tables.sh
./scripts/create-tables.sh
java -jar target/data-reader-0.0.1.jar -p ../data/batchfile.csv -b
java -jar target/data-reader-0.0.1.jar -p ../data/indicatorGroupings/exported.csv -d 'in' -t indicators
systemctl --user start hhm
