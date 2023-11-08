package com.example.locationpinnedapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ExecutorService execServices = Executors.newFixedThreadPool(5); // instantiating 5 worker threads to ensure user interface is responsive

    // defining buttons from layouts
    private Button btnPickFile;
    private Button btnConvert;
    private Button btnQueryDB;
    private Button btnDB;
    private Button btnAddAnother;

    // defining TextViews and EditTexts from layouts
    private TextView fileTextView;
    private TextView addressTextView;
    private TextView latlongTextView;
    private EditText addressEditText;

    private GeofencingClient geoClient;
    private Uri selectedFile; // Store the selected lat/long coordinate file's URI
    private SQLiteHelper db;

    // Create an ActivityResultLauncher to handle file picking
    private ActivityResultLauncher<Intent> filePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) { Intent data = result.getData(); // getting selected file's uri
                    if (data != null) { // checking file isn't empty
                        selectedFile = data.getData(); // assign file URI to a variable
                        String fileContents = readLatLongFile(selectedFile).toString();
                        fileTextView.setText(fileContents); // previewing the file's content in activity_main layout
                        Toast.makeText(MainActivity.this, "File has been successfully imported!", Toast.LENGTH_SHORT).show();
                        enableConvertButton();}}}); // Enable the "Convert" button and show additional UI elements

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new SQLiteHelper(this); // instantiating the DatabaseHelper object
        // instantiating the buttons:
        btnPickFile = findViewById(R.id.fileButton);
        btnConvert = findViewById(R.id.convertButton);
        btnQueryDB = findViewById(R.id.queryButton);
        btnDB = findViewById(R.id.databaseButton);
        btnAddAnother = findViewById(R.id.addAnotherFile);
        // instantiating TextViews and EditTexts:
        fileTextView = findViewById(R.id.fileTextView);
        fileTextView.setMovementMethod(new ScrollingMovementMethod());
        latlongTextView = findViewById(R.id.latlongTextView);
        addressTextView = findViewById(R.id.addressTextView);
        addressTextView.setMovementMethod(new ScrollingMovementMethod());
        addressEditText = findViewById(R.id.addressTextInput);
        geoClient = LocationServices.getGeofencingClient(this); // instantiating geoFencing client
        // listening for btnPickFile clicks
        btnPickFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // filePickLauncher reference
            intent.setType("*/*"); // allowing all file types
            filePickLauncher.launch(intent); }); // launching filePickLauncher

        btnAddAnother.setOnClickListener(v -> {
            runOnUiThread(() -> {
                btnPickFile.setVisibility(View.VISIBLE);
                btnAddAnother.setVisibility(View.GONE);
                btnConvert.setVisibility(View.GONE);
                btnQueryDB.setVisibility(View.GONE);
                latlongTextView.setVisibility(View.GONE);
                addressTextView.setVisibility(View.GONE);
                addressEditText.setVisibility(View.GONE);
            });
        });

        // listening for btnConvert clicks
        btnConvert.setOnClickListener(v -> {
            addressTextView.setText("Converting latitude/longitude pairs to addresses...");
            addressTextView.setVisibility(View.VISIBLE);
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                if (selectedFile != null) { execServices.execute(() -> {
                        List<String> latlongList = readLatLongFile(selectedFile); // reading lat/long pairs from file
                        StringBuilder addressString = new StringBuilder(); // generating addresses as strings
                        for (String line : latlongList) {String[] coordinates = line.split(",");
                            if (coordinates.length >= 2) {String latitudeStr = coordinates[0].trim(); String longitudeStr = coordinates[1].trim();
                                try {
                                    double latitude = Double.parseDouble(latitudeStr);
                                    double longitude = Double.parseDouble(longitudeStr);
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    String address = geoLatLng(latLng);
                                    addressString.append("Latitude: ").append(latitude).append(", Longitude: ").append(longitude).append("\n").append("Address: ").append(address).append("\n\n");
                                } catch (NumberFormatException e) {addressString.append("Invalid coordinates: ").append(line).append("\n");}}}

                        // updating user-interface
                        runOnUiThread(() -> {addressTextView.setText(addressString.toString());
                            execServices.execute(() -> { // inserting data into DB
                                for (String line : latlongList) {String[] coordinates = line.split(",");
                                    if (coordinates.length >= 2) {
                                        String latString = coordinates[0].trim();
                                        String longString = coordinates[1].trim();
                                        try {
                                            double latitude = Double.parseDouble(latString);
                                            double longitude = Double.parseDouble(longString);
                                            String address = geoLatLng(new LatLng(latitude, longitude));
                                            saveToDatabase(address, latitude, longitude);
                                            showQueryAndDatabaseButtons();
                                        } catch (NumberFormatException e) { }}}});});});
                } else {Toast.makeText(MainActivity.this, "Must select a file..", Toast.LENGTH_SHORT).show();}}});

        // listener logic for btnQueryDB for user to query lat/long pair for an address
        btnQueryDB.setOnClickListener(v -> {
            hideFileConvertUI(); //hide earlier steps
            runOnUiThread(() -> {
                addressEditText.setVisibility(View.VISIBLE);
                latlongTextView.setVisibility(View.VISIBLE);
                btnQueryDB.setVisibility(View.VISIBLE);
            });
            String addressToQuery = addressEditText.getText().toString().trim();
            if (!addressToQuery.isEmpty()) {
                getLocationFromDB(addressToQuery); // Perform the database query and display results
            } else {Toast.makeText(MainActivity.this, "Please enter an address to query.", Toast.LENGTH_LONG).show();} });

        // listener logic for btnDB for user to add, delete, update items in DB
        btnDB.setOnClickListener(v -> {Intent intent = new Intent(MainActivity.this, DatabaseManager.class); startActivity(intent); });}

    // Helper method to show the "Query Addresses" and "Database" buttons
    private void showQueryAndDatabaseButtons() {
        runOnUiThread(() -> {
            btnPickFile.setVisibility(View.GONE);
            fileTextView.setVisibility(View.GONE);
            btnQueryDB.setVisibility(View.VISIBLE);
            btnAddAnother.setVisibility(View.VISIBLE);
            }
        );
    }

    // Helper method to hide file-related UI elements
    private void hideFileConvertUI() {
        runOnUiThread(() -> {
            btnPickFile.setVisibility(View.GONE);
            fileTextView.setVisibility(View.GONE);
            btnConvert.setVisibility(View.GONE);
            addressTextView.setVisibility(View.GONE);
            addressEditText.setVisibility(View.VISIBLE);
            btnQueryDB.setVisibility(View.VISIBLE);
            latlongTextView.setVisibility(View.VISIBLE);
        });
    }

    private void enableConvertButton() {
        runOnUiThread(() -> {
            //btnPickFile.setVisibility(View.GONE);
            btnConvert.setVisibility(View.VISIBLE);
            });
        }

    // read the file coordinates and put them into a list for processing
    private List<String> readLatLongFile(Uri fileUri) { List<String> latlongList = new ArrayList<>();
        try { InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {latlongList.add(line);}
                inputStream.close();}
        } catch (Exception e) {e.printStackTrace();}
        return latlongList;}

    // saving address, lat, and long to the DB
    private void saveToDatabase(String address, double latitude, double longitude) {
        SQLiteDatabase db = this.db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLiteHelper.COLUMN_ADDRESS, address); // storing address
        cv.put(SQLiteHelper.COLUMN_LATITUDE, latitude); // storing latitude
        cv.put(SQLiteHelper.COLUMN_LONGITUDE, longitude); // storing longitude
        db.insert(SQLiteHelper.TABLE_NAME, null, cv);} // inserting content into DB

    // geocoding (converting) latitude/longitude pair to an address
    private String geoLatLng(LatLng latLng) {Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {Address address = addresses.get(0);
                return address.getAddressLine(0);}
        } catch (IOException e) {e.printStackTrace();}
        return "Cannot find specified address.";}

    // querying DB for address, latitude, and longitude
    private void getLocationFromDB(String addressQuery) {
        SQLiteDatabase db = this.db.getReadableDatabase(); // DB reference
        String[] DBColumns = {SQLiteHelper.COLUMN_ADDRESS, SQLiteHelper.COLUMN_LATITUDE, SQLiteHelper.COLUMN_LONGITUDE};
        String userQuery = SQLiteHelper.COLUMN_ADDRESS + " LIKE ?"; // matching addresses from DB
        String[] queryArgs = new String[]{"%" + addressQuery + "%"};
        Cursor cursor = db.query(SQLiteHelper.TABLE_NAME, DBColumns, userQuery, queryArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {StringBuilder queryResult = new StringBuilder();
            int addressIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ADDRESS); // DB column index for addresses
            int latIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_LATITUDE); // DB column index for latitude
            int LongIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_LONGITUDE); // DB column index for longitude

            do { // querying address, lat, long, and then appending them to queryResult
                String address = cursor.getString(addressIndex); // cursor getting address
                double lat = cursor.getDouble(latIndex); // cursor getting latitude
                double longt = cursor.getDouble(LongIndex); // cursor getting longitude
                // Appending the data into queryResult:
                queryResult.append("Address: ").append(address).append("\n");
                queryResult.append("Latitude: ").append(lat).append("\n");
                queryResult.append("Longitude: ").append(longt).append("\n\n");
            } while (cursor.moveToNext()); cursor.close();
            latlongTextView.setText(queryResult.toString()); // displaying queryResult in activity_main layout's TextView
        } else {latlongTextView.setText("Cannot find address in the app's database");}} // print this msg when address not found

    @Override // permissions for geocoding
    public void onRequestPermissionsResult(int requestResult, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestResult, permissions, results);
        if (requestResult == REQUEST_LOCATION_PERMISSION) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {btnConvert.performClick();
            } else {Toast.makeText(this, "Please allow location permissions for this app..", Toast.LENGTH_LONG).show();}}}}