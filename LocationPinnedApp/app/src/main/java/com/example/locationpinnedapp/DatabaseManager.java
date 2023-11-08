package com.example.locationpinnedapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class DatabaseManager extends AppCompatActivity {
    private int id; // unique data item ID
    private SQLiteHelper db; // helper handler
    private SQLiteDatabase dbSQL; // SQL handler
    private ListView dataListView; // layout listView
    private ArrayAdapter<String> arrayAdapter; // array adapter

    // instantiating buttons:
    private Button btnNew; // adding new data item
    private Button btnBack; // going back to main screen
    private Button btnDeleteAll;

    @Override // onCreate default method logic
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_manager);
        btnNew = findViewById(R.id.buttonNewEntry); // new entry button
        btnBack = findViewById(R.id.buttonBackToMain); // back button
        btnDeleteAll = findViewById(R.id.buttonDeleteAllData);
        dataListView = findViewById(R.id.listViewDataEntries); // listing stored data items
        db = new SQLiteHelper(this); // database handler
        dbSQL = db.getWritableDatabase(); // writing into database handler
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dataListView.setAdapter(arrayAdapter); // setting the array adapter as the dataListView
        dataListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedData = arrayAdapter.getItem(position); // get parent from array adapter
            int dataID = getDataID(selectedData); // get data ID
            Intent intent = new Intent(DatabaseManager.this, EditDataItem.class); // intent that references EditDataItem
            intent.putExtra("selectedData", selectedData); // sending selectedData
            System.out.println("DEBUG - FROM DATABASE ACTIVITY - id is " + dataID); // #################### DEBUG ########################
            intent.putExtra("dataID", dataID); // sending id
            startActivity(intent);});

        btnNew.setOnClickListener(v -> { // listens for clicks on btnNew
            Intent intent = new Intent(DatabaseManager.this, EditDataItem.class); // intent that references EditDataItem
            intent.putExtra("dataID", -1); // sends -1 id for newly created entries (not editing)
            startActivity(intent);});

        btnDeleteAll.setOnClickListener(v -> { // on click listener for btnDeleteAll
            deleteAllData();
            refreshListView();});

        btnBack.setOnClickListener(v -> {onBackPressed();}); // listens for clicks on back button (sends user back to main screen)
        refreshListView();} // ensures list view is refreshed (up-to-date)

    @Override
    protected void onResume() {super.onResume(); refreshListView();} // ensures list view is being refreshed each time this activity resumes

    @Override // ensure DB is closed when activity is destroyed
    protected void onDestroy() {super.onDestroy(); db.close();}

    private void deleteAllData() {
        if (dbSQL != null) {
            dbSQL.delete(SQLiteHelper.TABLE_NAME, null, null);
            arrayAdapter.clear();
            arrayAdapter.notifyDataSetChanged();}}

    // refresh the layout's listView with data from DB
    private void refreshListView() { arrayAdapter.clear(); // Clear the existing items in the adapter
        if (dbSQL != null) {
            String[] projection = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_ADDRESS, SQLiteHelper.COLUMN_LATITUDE, SQLiteHelper.COLUMN_LONGITUDE};
            Cursor cursor = dbSQL.query(SQLiteHelper.TABLE_NAME, projection, null, null, null, null, null);
            if (cursor != null) {Log.d("Cursor Rows", "# of rows in the selected cursor: " + cursor.getCount());
                while (cursor.moveToNext()) { // gets values using cursor
                    int idColumnIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ID); // gets id index
                    int addressColumnIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ADDRESS); // gets address index
                    int latitudeColumnIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_LATITUDE); // gets latitude index
                    int longitudeColumnIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_LONGITUDE); // gets longitude index
                    if (addressColumnIndex != -1 && latitudeColumnIndex != -1 && longitudeColumnIndex != -1) {
                        id = cursor.getInt(idColumnIndex); // getting id
                        String address = cursor.getString(addressColumnIndex); // getting address
                        double latitude = cursor.getDouble(latitudeColumnIndex); // getting latitude
                        double longitude = cursor.getDouble(longitudeColumnIndex); // getting longitude
                        String dataName = "ID: " + id + "\nAddress: " + address + "\nLatitude: " + latitude + "\nLongitude: " + longitude;
                        arrayAdapter.add(dataName);
                    } else {Log.e("Index Error - Column", "Error: Cannot find column(s) in selected cursor.");}}
                cursor.close();
            }
            arrayAdapter.notifyDataSetChanged();
        } else {Log.e("DB Error", "Error: Database not initialized.");}}

    // gets data ID for certain data item
    private int getDataID(String selectData) {
        String[] parts = selectData.split("\n");
        if (parts.length > 0) {
            String idPart = parts[0].replace("ID: ", "");
            try { return Integer.parseInt(idPart);
            } catch (NumberFormatException e) {}}
        return -1;}}