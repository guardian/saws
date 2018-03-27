#!/bin/bash
cd "$( dirname "$0" )"
SCALA_FOLDER="scala-2.12"

sbt assembly

cat "./generate-executable-prefix" "target/$SCALA_FOLDER/saws.jar" > "target/$SCALA_FOLDER/saws" 
chmod +x "./target/$SCALA_FOLDER/saws"
echo "saws executable now available at ./target/$SCALA_FOLDER/saws"
cd "target/$SCALA_FOLDER"
tar -czf saws.tar.gz saws
echo "saws tar.gz file now available at ./target/$SCALA_FOLDER/saws.tar.gz"
echo "saws.tar.gz sha256:"
shasum -a 256 saws.tar.gz
