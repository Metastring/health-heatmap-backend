DB=healthheatmap
username=metastring

dbql="psql -h localhost -U $username -d $DB"

$dbql -f scripts/create-tables.sql


