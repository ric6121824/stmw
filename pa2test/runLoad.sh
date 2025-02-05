#!/bin/bash

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

# Step 4: Compile Java files
echo "Compiling Indexer.java and Searcher.java..."
javac Indexer.java Searcher.java

# Run indexer (permitting restricted native access features and enable optimization via vectorization (optional parameters))
java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector Indexer  

#removing the indexer class files. not necessary, really...
# echo "Removing unnecessary classes..."
# rm *.class

# Step 7: Print success message
echo "Setup completed successfully. You can now run searches with:"
echo "java Searcher \"list of keywords\" number_of_results [-x longitude -y latitude -w width]"
