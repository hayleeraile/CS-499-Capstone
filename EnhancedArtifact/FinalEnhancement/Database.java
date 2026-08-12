package com.zybooks.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Manages the items, users, and item history databases
public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory_pro.db";
    private static final int DB_VERSION = 7;

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

    //Created an item history table that records any changes to the inventory with the user that did it
    public static final class ItemHistoryDatabase {
        public static final String TABLE_ITEM_HISTORY = "item_history";
        public static final String COL_HISTORY_ID = "history_id";
        public static final String COL_HISTORY_ITEM_ID = "item_id";
        public static final String COL_HISTORY_USER_ID = "user_id";
        public static final String COL_HISTORY_ACTION = "action";
        public static final String COL_HISTORY_DATE = "change_date";
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

    //creates a table that tracks who created or updated an item
    private static final String CREATE_ITEM_HISTORY_TABLE =
            "CREATE TABLE " + ItemHistoryDatabase.TABLE_ITEM_HISTORY + " (" +
            ItemHistoryDatabase.COL_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ItemHistoryDatabase.COL_HISTORY_ITEM_ID + " INTEGER NOT NULL, " +
            ItemHistoryDatabase.COL_HISTORY_USER_ID + " INTEGER NOT NULL, " +
            ItemHistoryDatabase.COL_HISTORY_ACTION + " TEXT NOT NULL, " +
            ItemHistoryDatabase.COL_HISTORY_DATE + " INTEGER NOT NULL, " +
            " FOREIGN KEY (" + ItemHistoryDatabase.COL_HISTORY_ITEM_ID + ") REFERENCES " +
                    ItemDatabase.TABLE_ITEMS + "(" + ItemDatabase.COL_ITEM_ID + "), " +
                    "FOREIGN KEY (" + ItemHistoryDatabase.COL_HISTORY_USER_ID + ") REFERENCES " +
                    UserDatabase.TABLE_USERS + "(" + UserDatabase.COL_USERID + "))";

    //enables the foreign key enforcement
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    //creates the database tables on the first launch
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);
        db.execSQL(CREATE_ITEM_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //recreates the database tables when the schema version changes
        //any existing stored data is deleted because migrations are not implemented
        db.execSQL("DROP TABLE IF EXISTS " + ItemHistoryDatabase.TABLE_ITEM_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + ItemDatabase.TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + UserDatabase.TABLE_USERS);

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

        //deletes the history records associated with the item first
        db.delete(ItemHistoryDatabase.TABLE_ITEM_HISTORY,
                ItemHistoryDatabase.COL_HISTORY_ITEM_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        //deletes the item after history records have been removed.
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

    //retrieves the userid for the authenticated user. returns -1 if user/password are invalid
    public long getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                UserDatabase.TABLE_USERS,
                new String[]{UserDatabase.COL_USERID},
                UserDatabase.COL_USERNAME + "=? AND "+
                        UserDatabase.COL_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        long userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(UserDatabase.COL_USERID)
            );
        }

        cursor.close();
        db.close();

        return userId;
    }
    //records the activity of the inventory by storing the item, user, action performed and the timestamp
    public long addItemHistory(long itemId,
                               long userId,
                               String action) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(
                ItemHistoryDatabase.COL_HISTORY_ITEM_ID,
                itemId
        );
        values.put(
                ItemHistoryDatabase.COL_HISTORY_USER_ID,
                userId
        );
        values.put(
                ItemHistoryDatabase.COL_HISTORY_ACTION,
                action
        );
        values.put(
                ItemHistoryDatabase.COL_HISTORY_DATE,
                System.currentTimeMillis()
        );

        long result = db.insert(
                ItemHistoryDatabase.TABLE_ITEM_HISTORY,
                null,
                values
        );

        db.close();
        return result;
    }

    //retrieves the history for a single item by joining the history and user tables to show usernames
    public Cursor getItemHistory(long itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT " + "h." + ItemHistoryDatabase.COL_HISTORY_ACTION +
                        ", " + "h." + ItemHistoryDatabase.COL_HISTORY_DATE +
                        ", " + "u." + UserDatabase.COL_USERNAME + " " +
                        "FROM " + ItemHistoryDatabase.TABLE_ITEM_HISTORY + " h " +
                        "INNER JOIN " + UserDatabase.TABLE_USERS + " u " + "ON h." +
                        ItemHistoryDatabase.COL_HISTORY_USER_ID + " = u." + UserDatabase.COL_USERID
                        + " " + "WHERE h." +
                        ItemHistoryDatabase.COL_HISTORY_ITEM_ID + " = ? " +
                        "ORDER BY h." + ItemHistoryDatabase.COL_HISTORY_DATE + " DESC";

        return db.rawQuery(query, new String[]{String.valueOf(itemId)});
    }
}
