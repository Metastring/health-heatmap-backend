#!/usr/bin/env bash

# Stop on error
set -e

DB=healthheatmap
username=metastring

dbql="psql -h localhost -U $username -d $DB -a -v ON_ERROR_STOP=1"


## DROP TABLES
###############
$dbql -c "select 'drop table if exists \"' || tablename || '\" cascade;' from pg_tables where schemaname = 'public';" > scripts/dropcommand1.sql
tail -n +3 scripts/dropcommand1.sql > scripts/dropcommand2.sql
head -n -2 scripts/dropcommand2.sql > scripts/dropcommand.sql

$dbql -f scripts/dropcommand.sql

rm scripts/dropcommand*.sql

## DROP SEQUENCES
#################
$dbql -c "select 'drop sequence if exists \"' || relname || '\" cascade;' from pg_class where relkind = 'S';" > scripts/dropcommand1.sql
tail -n +3 scripts/dropcommand1.sql > scripts/dropcommand2.sql
head -n -2 scripts/dropcommand2.sql > scripts/dropcommand.sql

$dbql -f scripts/dropcommand.sql

rm scripts/dropcommand*.sql

## DROP elastic index
set -x
curl -X DELETE http://localhost:9200/dataelement-000001

{ set +x; } 2>/dev/null
echo ""
echo "###############################"
echo "Successfully dropped everything"