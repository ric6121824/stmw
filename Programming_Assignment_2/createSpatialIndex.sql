USE ad;
CREATE TABLE IF NOT EXISTS Geocoordinates(item_id INT PRIMARY KEY, longlat POINT NOT NULL) ENGINE = MyISAM;
DELETE FROM Geocoordinates;
INSERT INTO Geocoordinates SELECT ItemId, POINT(longitude, latitude) FROM ItemLatLon;