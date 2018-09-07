# uctwifimapper

Aim

This project involved the design and development of an Android app that allows users to view the quality of Eduroam wifi in different locations on UCT’s upper campus, in the form of a campus map with overlaid colour-coded wifi zones.

This prohect consist of two major components:
1. Android application
2. Server/Backend application

File Structure:
The android application is located in the frontend folder
The server/backend application is located in the backend folder

Requirements to compile and run
Android Application:
1. Android studio
2. At least java 7
3. Android sdk 28
3. Android tes device

Backend or Server application:
1. Java 7 or greater
2. Postgresql database server
3. PostGis plugin

How to run:
Android applicatio:
1. Open the application in android studio 
2. Coplile and build application to emulator
3. The application will require permissions to access location for it to work properly

Backend Server:
1. Have poatgresql intalled and running
2. Import into postgresql the file, wifimapper.sql, this will initialise the database used by the application. 


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

External Libraries
- mustache.java v0.9.5 requires java 1.8 "https://github.com/spullara/mustache.java"
- gson v2.8.5
- jquery
- google charts
- google maps
- bootstrap
- postgis jdbc v2.1.7.2
- postgresql 42.2.4
