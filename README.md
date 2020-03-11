# Data Reader

This software can handle multitudes of spreadsheets and upload into a multi-dimensional database which allows querying by various dimensions.

## Setup
* Install java 8
* Install postgresql
* Install elastic search
* `git clone https://gitlab.com/asdofindia/data-reader`
* Open data-reader in IntelliJ. This will:
    * Synchronize maven dependencies
    * Allow you to access run configurations
* Build once (alternatively, `mvn package` should do)


## Add data
* `git clone https://gitlab.com/asdofindia/healthheatmap-data`
* `cd data-reader`
* `./scripts/insert-data.sh /path/to/healthheatmap-data/`

## Run server
* Use the "Server" run configuration in intellij OR
* `mvn war:war && sudo cp target/data-reader-0.0.1.war /var/lib/tomcat8/webapps && sudo systemctl start tomcat8` and navigate to localhost:8080/data-reader-0.0.1/api/openapi.json