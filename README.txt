1. table itemdata (itemID int unsigned primary key, name varchar, sellerID varchar, firstBid decimal, currentBid decimal, started datetime, ends datetime, description varchar) 
//number of bids is removed as it can be derived from the bids table. it would also be possible to derive the current bid from it, but this is slightly more complicated as there can be 
no bids. it was left in.
table bids (itemID int unsigned, bidderID varchar, amount decimal, bidtime datetime) primary key (itemID, amount) 
//amount chosen as primary key component as presumably no two bids on the same item can bid the same amount. likely this also applies to the time of the bid, but this seemed slightly 
less certain.
table categories (itemID int unsigned, category varchar) primary key (itemID, category)
table buyprice (itemID int unsigned primary key, price decimal)
table locations (userID varchar primary key, location varchar) 
//there is (as far as i can tell) no attribute that is guaranteed to exist for every given user. as such, there is no 'master list' of users anywhere, only relations with potential
 attributes. a 'master list' can be created from the union of seller and buyer ratings.
table countries (userID varchar primary key, country varchar) 
//at first glance it looks like every user has a location and country, but buyers not necessarily so. for an example, items-0.xml line 12298 CALVIN
table sellerrating (userID varchar primary key, sellerrating int) 
//as either a user's seller rating or buyer rating could be null if they only appear in one category, these are stored in separate tables
table buyerrating (userID varchar primary key, buyerrating int)
table userlatlong (userID varchar primary key, latitude decimal, longitude decimal)

As delimiters and EOL characters, < and > were chosen, as they would not appear in the XML data. (This is by definition for <. I believe > could appear in theory, but this does not appear to be the case.)

2. In the table itemdata, all non-key attributes are functionally dependent on the itemID, as this ID specifies the listed item that has these attributes.
(itemID -> name, sellerID, firstBid currentBid, started, ends, description)
For the table bids, there can be only one bid on one item for a certain amount. As such, the other attributes can be derived from itemID and amount. Presumably, a user can also make 
one bid at a specific datetime, but this could be circumvented using automated bidding, so this is not as certain.
(itemID, amount -> bidderID, bidtime)
There are no functional dependencies in the table categories. An item can have multiple categories and vice versa.

The optional buying price of an item is again dependent on the itemID.
(itemID -> price)
A user's location and country in the respective tables are functionally dependent on the userID. Locations seem to be freely entered by the user rather than referring to specific 
places in the world, so they cannot specify a country (e.g. the location 'Bremen' could refer to Bremen, Germany, or one of several places called Bremen in the USA).
(userID -> {location,country})
In the sellerrating and buyerrating tables, the ratings are functionally dependent on the userID.
(userID -> {seller,buyer}rating)
The optional latitude and longitude are also dependent on the specific user. They can also be different for the same location, as far as I can tell.
(userID -> latitude, longitude)

3. Functional dependencies, in which non-primary characteristics are dependent on the main key, are present in all relations save from bids. They are, therefore, BCNF.
The situation becomes more convoluted when considering table bids. It has been hypothesised that a user is unable to submit several bids at the same time, in which case 
the pair (bidderID, bidtime) could serve as a key. However, I would argue that there may be several bids in the system with the same user and datetime, but none with the 
same itemID and quantity (in a 'infinite monkeys on infinite typewriters' approach). To be honest, I can't think of a way to break this table down that would actually help anything.

4. In addition to the functional dependencies that have already been listed, there are no multivalued dependencies that have been found. Therefore, according to the same line of 
reasoning as before, the relations are in 4NF.