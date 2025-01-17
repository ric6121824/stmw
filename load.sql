USE asih;
LOAD DATA LOCAL INFILE 'items.csv' INTO TABLE items FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'bids.csv' INTO TABLE bids FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'categories.csv' INTO TABLE categories FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'buyprice.csv' INTO TABLE buyprice FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'locations.csv' INTO TABLE locations FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'countries.csv' INTO TABLE countries FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'sellerrating.csv' INTO TABLE sellerrating FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'buyerrating.csv' INTO TABLE buyerrating FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';
LOAD DATA LOCAL INFILE 'userlatlong.csv' INTO TABLE userlatlong FIELDS TERMINATED BY '<' LINES TERMINATED BY '>';