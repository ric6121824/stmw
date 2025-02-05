echo "creating spatial index..."
mysql < createSpatialIndex.sql

echo "creating lucene index..."
javac xIndexer.java
java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector xIndexer

#removing the indexer class files. not necessary, really...
rm *.class

echo "creating search engine..."
javac xSearcher.java
#running one of the sample queries from the exercise
java xSearcher "star trek" -x -73.997255 -y 40.732371 -w 10
