DB=healthheatmap
username=metastring

dbql="psql -h localhost -U $username -d $DB"


$dbql -c "select 'drop table if exists \"' || tablename || '\" cascade;' from pg_tables where schemaname = 'public';" > scripts/dropcommand1.sql
tail -n +3 scripts/dropcommand1.sql > scripts/dropcommand2.sql
head -n -2 scripts/dropcommand2.sql > scripts/dropcommand.sql


cat scripts/dropcommand.sql

$dbql -f scripts/dropcommand.sql

$dbql -c "drop sequence public.hibernate_sequence;"


rm scripts/dropcommand*.sql
