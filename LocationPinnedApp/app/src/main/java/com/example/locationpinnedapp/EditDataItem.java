package com.example.locationpinnedapp;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditDataItem extends AppCompatActivity {
    private int dataID;
    private SQLiteHelper db; // sqlite database helper handler
    private SQLiteDatabase dbSQL; // SQL handler (writing)
    private TextView titleTextView;
    private TextView dataIDTextView;
    private EditText addressEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private Button btnSave;
    private Button btnBack;
    private Button btnDelete;

    @Override // default onCreate activity logic
    protected void onCreate(Bundle savedInstanceState) { // setting up user interface elements
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_editor);
        db = new SQLiteHelper(this); // db handler
        dbSQL = db.getWritableDatabase(); //
        addressEditText = findViewById(R.id.editTextAddress);
        latitudeEditText = findViewById(R.id.editTextLatitude);
        longitudeEditText = findViewById(R.id.editTextLongitude);
        titleTextView = findViewById(R.id.titleEditData);
        dataIDTextView = findViewById(R.id.textViewDataID);
        btnSave = findViewById(R.id.buttonSave);
        btnDelete = findViewById(R.id.buttonDelete);
        btnBack = findViewById(R.id.buttonBack);
        Intent intent = getIntent(); // getting selected data item info from Intent
        if (intent != null) {dataID = intent.getIntExtra("dataID", -1);
            System.out.println("DEBUG EXISTING ENTRY id is - " + dataID); // ######################### DEBUG ###############################
            if (intent.hasExtra("selectedData")) {
                String selectedData = intent.getStringExtra("selectedData");
                String[] dataFields = selectedData.split("\n");
                if (dataFields.length >= 4) {
                    String address = dataFields[1].replace("Address: ", "");
                    String lat = dataFields[2].replace("Latitude: ", "");
                    String longt = dataFields[3].replace("Longitude: ", "");
                    // Set text and edit views as data fields retrieved from DatabaseManager
                    dataIDTextView.setText("ID: " + String.valueOf(dataID));
                    addressEditText.setText(address);
                    latitudeEditText.setText(lat);
                    longitudeEditText.setText(longt);
                }
            } else if (intent.hasExtra("dataID")) { // get dataID from intent
                if (dataID == -1) {
                    titleTextView.setText("New Data Entry"); // set activity title for new data entry
                    dataIDTextView.setText("the dataID is auto-generated");
                    btnDelete.setVisibility(View.GONE); // delete btn is hidden by default for new data entries
                } else { dataIDTextView.setText("ID: " + String.valueOf(dataID));}}}

        // listening for clicks on btnSave and error handling logic
        btnSave.setOnClickListener(view -> {
            String addressText = this.addressEditText.getText().toString(); // gets data from EditText for address field
            String latitudeText = this.latitudeEditText.getText().toString(); // gets data from EditText for latitude field
            String longitudeText = this.longitudeEditText.getText().toString(); // gets data from EditText for longitude field
            if (!latitudeText.isEmpty() && !longitudeText.isEmpty()) { // checks if lat/long are empty
                double editedLatitudeValue = Double.parseDouble(latitudeText); // converted to double
                double editedLongitudeValue = Double.parseDouble(longitudeText); // converted to double
                if (dataID == -1) {long newDataID = db.addData(dbSQL, addressText, editedLatitudeValue, editedLongitudeValue);
                    if (newDataID != -1) { Toast.makeText(EditDataItem.this, "New data entry successfully added", Toast.LENGTH_SHORT).show();
                    } else { Toast.makeText(EditDataItem.this, "Failed to add the new data entry", Toast.LENGTH_SHORT).show();}
                } else {int rowsChanged = db.updateData(dbSQL, dataID, addressText, editedLatitudeValue, editedLongitudeValue);
                    if (rowsChanged > 0) {Toast.makeText(EditDataItem.this, "Data entry successfully updated", Toast.LENGTH_SHORT).show();
                    } else {Toast.makeText(EditDataItem.this, "Failed to update data entry", Toast.LENGTH_SHORT).show();}}
            } else {Toast.makeText(EditDataItem.this, "Must fill both latitude and longitude fields", Toast.LENGTH_SHORT).show();}
            onBackPressed(); });  // send user back to the listView of all other data entries

        // listener for delete button
        btnDelete.setOnClickListener(view -> {
            boolean deleted = db.deleteData(dbSQL, dataID); // deletes a data entry from db using its dataID
            if (deleted) {Toast.makeText(EditDataItem.this, "Data entry successfully deleted.", Toast.LENGTH_SHORT).show();
            } else {Toast.makeText(EditDataItem.this, "Failed to delete data entry.", Toast.LENGTH_SHORT).show();}
            onBackPressed(); }); // when back button is pressed, send user to prev activity

        // listens for clicks on btnBack - sends user to prev screen
        btnBack.setOnClickListener(v -> {onBackPressed();});
    }
}