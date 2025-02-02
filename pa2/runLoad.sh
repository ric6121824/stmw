#!/bin/bash

# This script sets up the environment for Programming Assignment 2:
# 1. Drops any existing spatial index (to avoid duplicate key errors)
# 2. Creates the spatial index in MySQL
# 3. Compiles Indexer.java and Searcher.java
# 4. Runs Indexer.java to build the Lucene index

# Set workspace directory (inside Docker container)
WORKSPACE="/stmw/student_workspace"
rm -rf /stmw/lib
mkdir -p /stmw/lib
cp /usr/share/java/mysql-connector-j-9.1.0.jar /stmw/lib/
cp /stmw/lucene-10.0.0/lucene/core/build/libs/*.jar /stmw/lib/
cp /stmw/lucene-10.0.0/lucene/analysis/common/build/libs/*.jar /stmw/lib/
cp /stmw/lucene-10.0.0/lucene/queryparser/build/libs/*.jar /stmw/lib/
cp /stmw/lucene-10.0.0/lucene/queries/build/libs/*.jar /stmw/lib/


echo "Starting the setup process..."

# Step 1: Ensure MySQL is running
echo "Starting MySQL service..."
service mysql start

# Step 2: Drop existing spatial index (if any) to avoid duplicate entry errors.
echo "Dropping existing spatial index..."
mysql -u root ad < "$WORKSPACE/dropSpatialIndex.sql"


# Step 3: Create and populate the spatial index in MySQL
echo "Creating spatial index in MySQL..."
mysql -u root ad < "$WORKSPACE/createSpatialIndex.sql"

# Step 4: Compile Java files
echo "Compiling Indexer.java and Searcher.java..."
javac -cp ".:/stmw/lib/*" "$WORKSPACE/Indexer.java" "$WORKSPACE/Searcher.java"
if [ $? -ne 0 ]; then
    echo "Compilation errors occurred. Please fix them and try again."
    exit 1
fi

# # Step 5: Ensure the Lucene index directory exists and is initialized
# echo "Resetting Lucene index directory..."
# rm -rf "$INDEX_DIR"
# mkdir -p "$INDEX_DIR"

# # **Force Lucene to initialize an empty index before running Indexer**
# echo "Initializing empty Lucene index..."
# java -cp ".:/stmw/lib/*" InitializeIndex "$INDEX_DIR"

# Step 6: Run Indexer to build the Lucene index
echo "Running Indexer to create the Lucene index..."
java  --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp ".:/stmw/lib/*" Indexer
if [ $? -ne 0 ]; then
    echo "Indexer encountered an error."
    exit 1
fi

#removing the indexer class files. not necessary, really...
echo "Removing unnecessary classes..."
rm *.class

# Step 7: Print success message
echo "Setup completed successfully. You can now run searches with:"
echo "java -cp \".:/stmw/lib/*\" Searcher \"<query>\" <numResults> [-x longitude -y latitude -w width]"
