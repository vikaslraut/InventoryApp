package com.example.v.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class InventoryProvider extends ContentProvider {
    public static final String TAG = InventoryProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher productUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        productUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, PRODUCTS);
        productUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY.concat("/#"), PRODUCT_ID);
    }

    private InventoryDbHelper inventoryDbHelper;

    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = inventoryDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = productUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = db.query(InventoryContract.Product.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.Product.ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryContract.Product.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = productUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.Product.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.Product.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown uri " + uri + " with match : " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = productUriMatcher.match(uri);

        Log.d(TAG, " inside insert : match " + match);
        switch (match) {
            case PRODUCTS:
                return InventoryProviderHelper.insertProduct(uri, values, getContext(), inventoryDbHelper);
            default:
                throw new IllegalArgumentException("Insertion not supported " + uri);

        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = productUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(InventoryContract.Product.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.Product._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.Product.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = productUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryProviderHelper.updateProduct(uri, contentValues, selection, selectionArgs, getContext(), inventoryDbHelper);
            case PRODUCT_ID:
                selection = InventoryContract.Product._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return InventoryProviderHelper.updateProduct(uri, contentValues, selection, selectionArgs, getContext(), inventoryDbHelper);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }
    }
}
