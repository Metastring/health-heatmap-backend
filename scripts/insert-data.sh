BASEDATADIR="$1"

JAVACOMMAND="java -jar target/data-reader-0.0.1.jar"


for file in $BASEDATADIR/nfhs/nfhs-4/*.csv
do
    $JAVACOMMAND -p "$file"
done

$JAVACOMMAND -p "$BASEDATADIR/covid/covid19india.org/2020-04-19.csv"
$JAVACOMMAND -p "$BASEDATADIR/AHS/2010-bulletin-UP.csv"
