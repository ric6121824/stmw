#!/bin/bash


# Run the drop.sql batch file to drop existing tables.
# Inside the drop.sql, you should check whether the table exists. Drop them ONLY if they exist.
mysql < drop.sql

# Run the create.sql batch file to create the database (if it does not exist) and the tables.
mysql < create.sql

# generating csv files...
# Compile and run the convertor
EBAY_DATA="../ebay-data"

javac MySAX.java
java MySAX $EBAY_DATA/items-*.xml


# Run the load.sql batch file to load the data

mysql asih < load.sql

#run all queries

mysql asih < queries.sql

#run single quries
#1
mysql -ustmw -D asih -e "SELECT COUNT(*) FROM (SELECT userID FROM sellerrating UNION SELECT userID FROM buyerrating) e;"
#2
mysql -ustmw -D asih -e "SELECT COUNT(*) FROM items JOIN locations ON items.sellerID = locations.userID WHERE BINARY locations.location = 'New York';"
#3
mysql -ustmw -D asih -e "SELECT COUNT(itemID) FROM (SELECT itemID FROM categories GROUP BY itemID HAVING COUNT(category) = 4) e;"
#4
mysql -ustmw -D asih -e "SELECT bids.itemID, amount FROM bids JOIN items ON bids.itemID = items.itemID WHERE ends > '2001-12-20 00:00:01' AND bids.amount = (SELECT amount FROM bids ORDER BY amount DESC LIMIT 1);"
#5
mysql -ustmw -D asih -e "SELECT COUNT(*) FROM sellerrating WHERE srating > 1000;"
#6
mysql -ustmw -D asih -e "SELECT COUNT(*) FROM sellerrating JOIN buyerrating ON sellerrating.userID = buyerrating.userID;"
#7
mysql -ustmw -D asih -e "SELECT COUNT(DISTINCT(category)) FROM categories JOIN bids ON categories.itemID = bids.itemID WHERE amount > 100;"

# Remove all temporary files

rm *.class
rm *.csv