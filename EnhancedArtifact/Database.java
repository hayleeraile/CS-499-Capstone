package com.zybooks.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory_pro.db";
    private static final int DB_VERSION = 5;

    //centralizes the user table and column names
    public static final class UserDatabase {
        public static final String TABLE_USERS = "users";
        public static final String COL_USERID = "id";
        public static final String COL_USER_FIRST = "first_name";
        public static final String COL_USER_LAST = "last_name";
        public static final String COL_USER_EMAIL = "email";
        public static final String COL_USER_PHONE = "phone";
        public static final String COL_USERNAME = "username";
        public static final String COL_PASSWORD = "password";
    }

    //centralizes the items table and column names
    public static final class ItemDatabase {
        public static final String TABLE_ITEMS = "items";
        public static final String COL_ITEM_ID = "id";
        public static final String COL_ITEM_NUMBER = "item_number";
        public static final String COL_ITEM_QTY = "quantity";
        public static final String COL_ITEM_DESC = "description";
        public static final String COL_ITEM_LOC = "location";
        public static final String COL_ITEM_MIN_STOCK = "min_stock";
    }

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + UserDatabase.TABLE_USERS + " (" +
                    UserDatabase.COL_USERID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserDatabase.COL_USER_FIRST + " TEXT, " +
                    UserDatabase.COL_USER_LAST + " TEXT, " +
                    UserDatabase.COL_USER_EMAIL + " TEXT, " +
                    UserDatabase.COL_USER_PHONE + " TEXT, " +
                    UserDatabase.COL_USERNAME + " TEXT UNIQUE, " +
                    UserDatabase.COL_PASSWORD + " TEXT)";

    private static final String CREATE_ITEMS_TABLE =
            "CREATE TABLE " + ItemDatabase.TABLE_ITEMS + " (" +
                    ItemDatabase.COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ItemDatabase.COL_ITEM_NUMBER + " TEXT, " +
                    ItemDatabase.COL_ITEM_QTY + " INTEGER, " +
                    ItemDatabase.COL_ITEM_DESC + " TEXT, " +
                    ItemDatabase.COL_ITEM_LOC + " TEXT, " +
                    ItemDatabase.COL_ITEM_MIN_STOCK + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //recreates both tables when the schema version changes
        //any existing stored data is deleted
        db.execSQL("DROP TABLE IF EXISTS " + UserDatabase.TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + ItemDatabase.TABLE_ITEMS);
        onCreate(db);
    }

    public boolean createUser(String firstName,
                              String lastName,
                              String email,
                              String phone,
                              String userName,
                              String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createUserValues(firstName, lastName, email, phone);

        values.put(UserDatabase.COL_USERNAME, userName);
        values.put(UserDatabase.COL_PASSWORD, password);

        long result = db.insert(UserDatabase.TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean validateUser(String userName, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(UserDatabase.TABLE_USERS,
                new String[]{UserDatabase.COL_USERID},
                UserDatabase.COL_USERNAME +
                        "=? AND " +
                        UserDatabase.COL_PASSWORD +
                        "=?",
                new String[]{userName,
                        password},
                null,
                null,
                null);

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();

        return exists;
    }

    public Cursor getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(UserDatabase.TABLE_USERS,
                null,
                UserDatabase.COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null);
    }

    public boolean updateUser(String username,
                              String firstName,
                              String lastName,
                              String email,
                              String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createUserValues(firstName, lastName, email, phone);

        int rows = db.update(UserDatabase.TABLE_USERS,
                values,
                UserDatabase.COL_USERNAME + "=?",
                new String[]{username});

        db.close();

        return rows > 0;
    }

    public long addItem(String itemNumber,
                        String description,
                        String location,
                        int quantity,
                        int minStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createItemValues(itemNumber,
                description,
                location,
                quantity,
                minStock);

        long result = db.insert(ItemDatabase.TABLE_ITEMS, null, values);

        db.close();
        return result;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(ItemDatabase.TABLE_ITEMS,
                null,
                null,
                null,
                null,
                null,
                ItemDatabase.COL_ITEM_NUMBER + " ASC");
    }

    public Cursor getItemById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(ItemDatabase.TABLE_ITEMS,
                null,
                ItemDatabase.COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null);
    }

    public int updateItem(long id,
                          String itemNumber,
                          String description,
                          String location,
                          int quantity,
                          int minStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createItemValues(itemNumber,
                description,
                location,
                quantity,
                minStock);

        int rows = db.update(ItemDatabase.TABLE_ITEMS,
                values,
                ItemDatabase.COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)});

        db.close();
        return rows;
    }

    public int deleteItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rows = db.delete(ItemDatabase.TABLE_ITEMS, ItemDatabase.COL_ITEM_ID + "=?",
                new String[]{String.valueOf(id)});

        db.close();
        return rows;
    }

    //reuses same field mapping for both item creation and item updates
    private ContentValues createItemValues(String itemNumber,
                                           String description,
                                           String location,
                                           int quantity,
                                           int minStock) {
        ContentValues values = new ContentValues();
        values.put(ItemDatabase.COL_ITEM_NUMBER, itemNumber);
        values.put(ItemDatabase.COL_ITEM_DESC, description);
        values.put(ItemDatabase.COL_ITEM_LOC, location);
        values.put(ItemDatabase.COL_ITEM_QTY, quantity);
        values.put(ItemDatabase.COL_ITEM_MIN_STOCK, minStock);

        return values;
    }

    //reuses same field mapping for both user creation and user updates
    private ContentValues createUserValues(String firstName,
                                           String lastName,
                                           String email,
                                           String phone) {
        ContentValues values = new ContentValues();
        values.put(UserDatabase.COL_USER_FIRST, firstName);
        values.put(UserDatabase.COL_USER_LAST, lastName);
        values.put(UserDatabase.COL_USER_EMAIL, email);
        values.put(UserDatabase.COL_USER_PHONE, phone);

        return values;
    }
}
