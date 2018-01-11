package com.example.v.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class InventoryProviderHelper {
    private static final String TAG = InventoryProviderHelper.class.getSimpleName();

    /**
     * Helper methods
     */
    public static Uri insertProduct(Uri uri, ContentValues contentValues, Context context, InventoryDbHelper inventoryDbHelper){
        String name = contentValues.getAsString(InventoryContract.Product.COLUMN_PRODUCT_NAME);
        if(name.isEmpty() || name == null){
            throw new IllegalArgumentException("Product name require");
        }

        Integer price = contentValues.getAsInteger(InventoryContract.Product.COLUMN_PRODUCT_PRICE);
        if(price == null || price < 0){
            throw new IllegalArgumentException("Product requires valid price");
        }

        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        long id = db.insert(InventoryContract.Product.TABLE_NAME,null,contentValues);

        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeners if data has changed for the content uri
        context.getContentResolver().notifyChange(uri,null);

        //return the new URI with id (of newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri,id);
    }

    public static int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs,Context context,InventoryDbHelper inventoryDbHelper) {

        if(contentValues.size() == 0){
            return 0;
        }

        if(contentValues.containsKey(InventoryContract.Product.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryContract.Product.COLUMN_PRODUCT_NAME);
            if (name.isEmpty() || name == null) {
                throw new IllegalArgumentException("Name require");
            }
        }
        if(contentValues.containsKey(InventoryContract.Product.COLUMN_PRODUCT_PRICE)) {
            Integer weight = contentValues.getAsInteger(InventoryContract.Product.COLUMN_PRODUCT_PRICE);
            if (weight == null || weight < 0) {
                throw new IllegalArgumentException("reuired vaild price");
            }
        }

        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        int rowUpdated = db.update(InventoryContract.Product.TABLE_NAME,contentValues,selection,selectionArgs);
        if(rowUpdated !=0){
            context.getContentResolver().notifyChange(uri,null);
        }
        return rowUpdated;
    }
}
