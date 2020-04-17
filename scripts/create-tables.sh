DB=healthheatmap
username=metastring

dbql="psql -h localhost -U $username -d $DB -a"

$dbql -f scripts/create-tables.sql
