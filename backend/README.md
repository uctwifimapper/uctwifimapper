# uctwifimapper

Database
- Postgress with PostGIS plugin

Architecture
- WifiMapperServer -> initialise server and listen on port 8800
- WifimapperRouter -> Process request and send processed request to correct dao implementation class
- Dao -> Interface
- AccessPointDao -> Perform database queries
- AccessPoint -> Business object, mapped to database.
-Database -> Setup singelton object for connection to db

API:
/       -> returns generic respose

/apn -> GET: returns list of accesspoints, POST: saves access point to database 

Example Queries
http://localhost:8800/apn?location=-33.9676258;18.4603946
http://localhost:8800/apn?bssid=ee:00:8c:b8:b7:01
http://localhost:8800/apn?ssid=eduroam
