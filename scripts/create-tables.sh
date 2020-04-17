#!/usr/bin/env bash

# Stop on error
set -e

DB=healthheatmap
username=metastring

dbql="psql -h localhost -U $username -d $DB -a -v ON_ERROR_STOP=1"

$dbql -f scripts/create-tables.sql
