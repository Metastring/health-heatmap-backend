BASEDATADIR="$1"

JAVACOMMAND="/usr/lib/jvm/java-1.11.0-openjdk-amd64/bin/java -javaagent:$HOME/.local/idea-ce/lib/idea_rt.jar=35553:$HOME/.local/idea-ce/bin -Dfile.encoding=UTF-8 -classpath $HOME/src/github/Metastring/data-reader/target/classes:$HOME/.m2/repository/org/apache/commons/commons-csv/1.7/commons-csv-1.7.jar:$HOME/.m2/repository/org/mongodb/mongodb-driver/3.12.0/mongodb-driver-3.12.0.jar:$HOME/.m2/repository/org/mongodb/bson/3.12.0/bson-3.12.0.jar:$HOME/.m2/repository/org/mongodb/mongodb-driver-core/3.12.0/mongodb-driver-core-3.12.0.jar:$HOME/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar:$HOME/.m2/repository/org/postgresql/postgresql/42.2.9/postgresql-42.2.9.jar:$HOME/.m2/repository/org/jooq/jooq/3.12.3/jooq-3.12.3.jar:$HOME/.m2/repository/org/reactivestreams/reactive-streams/1.0.2/reactive-streams-1.0.2.jar:$HOME/.m2/repository/javax/xml/bind/jaxb-api/2.3.0/jaxb-api-2.3.0.jar:$HOME/.m2/repository/org/jooq/jooq-meta/3.12.3/jooq-meta-3.12.3.jar:$HOME/.m2/repository/org/jooq/jooq-codegen/3.12.3/jooq-codegen-3.12.3.jar org.metastringfoundation.healthheatmap.Main"

FILES="$1/nfhs-manual-downloads/*"
for file in $FILES
do
    $JAVACOMMAND -n $(basename $file .csv) -p $file -x "A1:A,B0:0,B1: "
done

AHS="$1/AHS/AHS.csv"
$JAVACOMMAND -n $(basename $AHS .csv) -p $file -x "U0:0,B1:B,U1: "

