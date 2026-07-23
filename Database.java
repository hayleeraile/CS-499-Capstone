package com.zybooks.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    //variable for the database name and the version of the database
    private static final String DATABASE_NAME = "inventory_pro.db";
    private static final int DB_VERSION = 5;

    //creates the helper for the userDatabase table with the variables for each column
    public static final class userDatabase {
        public static final String TABLE_USERS = "users";
        public static final String COL_USERID = "id";
        public static final String COL_USER_FIRST = "first_name";
        public static final String COL_USER_LAST = "last_name";
        public static final String COL_USER_EMAIL = "email";
        public static final String COL_USER_PHONE = "phone";
        public static final String COL_USERNAME = "username";
        public static final String COL_PASSWORD = "password";
    }
    //creates the helper for the itemDatabase table with the variables for each column
    public static final class itemDatabase {
        public static final String TABLE_ITEMS = "items";
        public static final String COL_ITEM_ID = "id";
        public static final String COL_ITEM_NUMBER = "item_number";
        public static final String COL_ITEM_QTY = "quantity";
        public static final String COL_ITEM_DESC = "description";
        public static final String COL_ITEM_LOC = "location";
        public static final String COL_ITEM_MIN_STOCK = "min_stock";
    }


    //initializes the database helper variable
    public Database(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //when the app is started, both tables are created
        //creates the user table
        db.execSQL("CREATE TABLE " + userDatabase.TABLE_USERS + " (" +
                userDatabase.COL_USERID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
               userDatabase.COL_USER_FIRST + " TEXT, " + userDatabase.COL_USER_LAST + " TEXT, " + userDatabase.COL_USER_EMAIL + " TEXT, "
                + userDatabase.COL_USER_PHONE + " TEXT, " +
               userDatabase.COL_USERNAME + " TEXT UNIQUE, " + userDatabase.COL_PASSWORD +
                " TEXT)");

        //creates the item table
        db.execSQL("CREATE TABLE " + itemDatabase.TABLE_ITEMS + " (" +
                itemDatabase.COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + itemDatabase.COL_ITEM_NUMBER + " TEXT, " +
                itemDatabase.COL_ITEM_QTY + " INTEGER, " + itemDatabase.COL_ITEM_DESC + " TEXT, " + itemDatabase.COL_ITEM_LOC + " TEXT, "
                + itemDatabase.COL_ITEM_MIN_STOCK + " INTEGER)");
    }

    //when the version of the database is changed, it drops the old version and adds the new version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + userDatabase.TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + itemDatabase.TABLE_ITEMS);
        onCreate(db);
    }

    //creates a new user and inputs the values to the database
    public boolean createUser(String firstName, String lastName, String email, String phone, String userName, String password) {
        //accesses the database to write new users
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(userDatabase.COL_USER_FIRST, firstName);
        values.put(userDatabase.COL_USER_LAST, lastName);
        values.put(userDatabase.COL_USER_EMAIL, email);
        values.put(userDatabase.COL_USER_PHONE, phone);
        values.put(userDatabase.COL_USERNAME, userName);
        values.put(userDatabase.COL_PASSWORD, password);
        long result = db.insert(userDatabase.TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    //validates that the user exists in the database
    public boolean validateUser(String userName, String password) {
        //accesses the database to view users
        SQLiteDatabase db = this.getReadableDatabase();
        //query with id, username and password
        Cursor cursor = db.query(userDatabase.TABLE_USERS, new String[]{userDatabase.COL_USERID},
                userDatabase.COL_USERNAME + "=? AND " + userDatabase.COL_PASSWORD + "=?",
                new String[]{userName, password},
                null, null, null);
        //if the first row has data that matches, return true. if no data mathces, return false.
        boolean exists = cursor.moveToFirst();
        //close cursor and database
        cursor.close();
        db.close();
        //return matching user if found
        return exists;
    }

    //locates a user by the username. I used this to create my user profile screen
    public Cursor getUserByUsername(String username) {
        //accesses the database to view users
        SQLiteDatabase db = this.getReadableDatabase();
        //query with the table and username
        return db.query(userDatabase.TABLE_USERS, null,
                userDatabase.COL_USERNAME + "=?",
                new String[]{username},
                null, null, null);
    }

    // the user info updates without the password. this is for the user profile screen if they need to make changes
    public boolean updateUser(String username, String firstName, String lastName,
                              String email, String phone) {
        //access the database to write new data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //puts the values that were entered with the matching variable
        values.put(userDatabase.COL_USER_FIRST, firstName);
        values.put(userDatabase.COL_USER_LAST, lastName);
        values.put(userDatabase.COL_USER_EMAIL, email);
        values.put(userDatabase.COL_USER_PHONE, phone);

        //updates the table with those values that match the username
        int rows = db.update(userDatabase.TABLE_USERS, values,
                userDatabase.COL_USERNAME + "=?",
                new String[]{username});
        //close the database
        db.close();
        //if at least one row was updated, return true
        return rows > 0;
    }

    //adds an item to the database
    public long addItem(String itemNumber, String description, String location, int quantity, int minStock) {
        //accesses the database to write new items
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //adding new values to the item database by variable
        values.put(itemDatabase.COL_ITEM_NUMBER, itemNumber);
        values.put(itemDatabase.COL_ITEM_DESC, description);
        values.put(itemDatabase.COL_ITEM_LOC, location);
        values.put(itemDatabase.COL_ITEM_QTY, quantity);
        values.put(itemDatabase.COL_ITEM_MIN_STOCK, minStock);
        //insert those values into the item table database
        long result = db.insert(itemDatabase.TABLE_ITEMS, null, values);
        //close the database
        db.close();
        return result;
    }

    //this returns all items in order based off of the item number
    public Cursor getAllItems() {
        //access the database to view the items
        SQLiteDatabase db = this.getReadableDatabase();
        //returns all items in the database
        return db.query(itemDatabase.TABLE_ITEMS, null, null, null, null, null, itemDatabase.COL_ITEM_NUMBER + " ASC");
    }

    //returns an item by the id
    public Cursor getItemById(long id) {
        //accesses the database to view the items
        SQLiteDatabase db = this.getReadableDatabase();
        //query by item id in the table of items
        return db.query(itemDatabase.TABLE_ITEMS, null,
                itemDatabase.COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    //updates an inventory item if the user makes changes
    public int updateItem(long id, String itemNumber, String description, String location, int quantity, int minStock) {
        //access database to write new itmes
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //assigns the values of the items to the variables
        values.put(itemDatabase.COL_ITEM_NUMBER, itemNumber);
        values.put(itemDatabase.COL_ITEM_DESC, description);
        values.put(itemDatabase.COL_ITEM_LOC, location);
        values.put(itemDatabase.COL_ITEM_QTY, quantity);
        values.put(itemDatabase.COL_ITEM_MIN_STOCK, minStock);
        //updates the table with the new values
        int rows = db.update(itemDatabase.TABLE_ITEMS, values, itemDatabase.COL_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        //close the database
        db.close();
        return rows;
    }

    //deletes an item that was selected by the user from the database.
    public int deleteItem(long id) {
        //access the database to delete data
        SQLiteDatabase db = this.getWritableDatabase();
        //query by item id to delete item
        int rows = db.delete(itemDatabase.TABLE_ITEMS, itemDatabase.COL_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        //close the database
        db.close();
        return rows;
    }
}
