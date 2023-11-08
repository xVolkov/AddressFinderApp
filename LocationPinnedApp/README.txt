# LocationPinnedApp:
This app takes latitude/longitude pairs and converts them to addresses. Also, the app saves these addresses to an SQLite database.
The database allows CRUD operations.

# Instructions:
1) Ensure all dependencies are correctly imported in build.gradle file
2) Setup an API 34 android device (I used Pixel 6)
3) Launch the app and click on "Open Lat/Long Pairs File" to select a file containing latitude/longitude pairs
4) Click "Convert to Addresses / Save to DB" to convert the latitude/longitude entries to addresses and save data to the database
5) To query for an address, click "Query an Address frin DB" then type an address and click the button again
6) You can manage the app's DB at anytime by clicking "Database Manager"
7) Once you are in the Database Manager screen, you can either select one of the entries and edit/delete it, create a new entry, or delete all entries