# Part B

## 1. List your relations

1. User (UserID, Rating, Location, Country)
   - Primary Key: UserID

2. Item (ItemID, Name, Currently, Buy_Price, First_Bid, Number_of_Bids, Location, Latitude, Longitude, Country, Started, Ends, UserID, Description)
   - Primary Key: ItemID
   - Foreign Key: UserID references User(UserID)

3. Category (ItemID, Category)
   - Primary Key: (ItemID, Category)
   - Foreign Key: ItemID references Item(ItemID)

4. Bids (ItemID, UserID, Time, Amount)
   - Primary Key: (ItemID, UserID, Time)
   - Foreign Keys: 
     - ItemID references Item(ItemID)
     - UserID references User(UserID)

## 2. List all completely non-trivial functional dependencies

1. User:
   - UserID → Rating, Location, Country

2. Item:
   - ItemID → Name, Currently, Buy_Price, First_Bid, Number_of_Bids, Location, Latitude, Longitude, Country, Started, Ends, UserID, Description

3. Category:
   - No non-trivial functional dependencies (already in 2NF)

4. Bids:
   - (ItemID, UserID, Time) → Amount

There are no other functional dependencies because each attribute in each relation is fully dependent on its primary key.

## 3. Are all of your relations in BCNF?

Yes, all relations are in BCNF:

1. User: The only functional dependency is on the primary key (UserID).
2. Item: All attributes are functionally dependent on the primary key (ItemID).
3. Category: The relation is already in 2NF and 3NF, which implies BCNF for this case.
4. Bids: All attributes are functionally dependent on the primary key (ItemID, UserID, Time).

## 4. Are all of your relations in 4NF?

Yes, all relations are in 4NF:

1. User: There are no multi-valued dependencies.
2. Item: There are no multi-valued dependencies.
3. Category: This relation represents a many-to-many relationship between Item and Category, which is the correct way to handle this in 4NF.
4. Bids: There are no multi-valued dependencies.

