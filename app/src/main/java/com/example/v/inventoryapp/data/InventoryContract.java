package com.example.v.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {
    public static final String CONTENT_AUTHORITY = "com.example.v.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";
    private InventoryContract() {
    }

    public static class Product implements BaseColumns {

        /*content provider*/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * Table columns
         */
        public static final String TABLE_NAME = "products";
        public static final String ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_IMAGE = "image";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        /**
         * Create table statement
         */
        public static final String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " +
                Product.TABLE_NAME + " (" +
                Product.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Product.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL," +
                Product.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                Product.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL," +
                Product.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                Product.COLUMN_SUPPLIER + " TEXT NOT NULL," +
                Product.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL);";
    }
}
