

echo "Starting the setup process..."

# Step 1: Ensure MySQL is running
echo "Starting MySQL service..."
service mysql start

# Step 2: Drop existing spatial index (if any) to avoid duplicate entry errors.
echo "Dropping existing spatial index..."
mysql -u root ad < dropSpatialIndex.sql


# Step 3: Create and populate the spatial index in MySQL
echo "Creating spatial index in MySQL..."
mysql -u root ad < createSpatialIndex.sql

echo "creating lucene index..."
javac -cp Indexer.java
java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector Indexer  

# #removing the indexer class files. not necessary, really...
# rm *.class

echo "creating search engine..."
javac -cp Searcher.java
#running one of the sample queries from the exercise
java -cp Searcher "star trek" -x -73.997255 -y 40.732371 -w 10
