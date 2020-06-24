# Health Heat Map API
This software can handle multitudes of spreadsheets and upload into a multi-dimensional database which allows querying by various dimensions.

## Overview

This software can be conceptually divided into two equally important halves. The first half is an Extract-Transform-Load (ETL) pipeline, and the second half is a web API to query data.

### Data

Data sourced from public datasets is curated, annotated, and organized in [this git repository](https://gitlab.com/asdofindia/healthheatmap-data). This is in a form which can be directly ingested by the software.

### ETL pipeline

* **Extract**: Data from CSV files can be extracted directly at the moment. Data in PDF has to be converted to CSV first. Each spreadsheet that needs to be included in the database should come with a metadata.json file that describes the layout of data within the spreadsheet.
* **Transform**: Data extracted from the CSVs can optionally be put through a series of transformations which are specified through another set of CSV files.
* **Load**: The data points after applying all the transformations get uploaded to elasticsearch.

### Query API

* There is a JAX-RS API configured with endpoints to query data previously uploaded.


## Setup

* Java 11+ is required.
* Elasticsearch 7 is required.
* Clone this repo. Run `mvn clean compile package`. A `hhm-api.jar` file that contains all dependencies will be created in target folder.
* Run `java -jar target/hhm-api.jar` for command line options.


## Add data

* `git clone git@gitlab.com:asdofindia/healthheatmap-data` somewhere, say /home/metastring/healthheatmap-data
* Run `java -jar target/hhm-api.jar --batch --path /home/metastring/healthheatmap-data/data --transformers /home/metastring/healthheatmap-data/transformers`

## Run server

* Run `java -jar target/hhm-api.jar --server`
* Go to http://localhost:8080/data-reader/