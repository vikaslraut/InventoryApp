package com.example.v.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.v.inventoryapp.data.InventoryContract;
import com.example.v.inventoryapp.data.InventoryDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductAdapter extends CursorAdapter {

    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();


        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.Product.COLUMN_PRODUCT_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.Product.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.Product.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.Product.COLUMN_PRODUCT_QUANTITY);

        String image = cursor.getString(imageColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        final String quantity = cursor.getString(quantityColumnIndex);

        viewHolder.imageView.setImageURI(Uri.parse(image));
        viewHolder.nameTextView.setText(productName);
        viewHolder.priceTextView.setText(price);
        viewHolder.quantityTextView.setText(quantity);
        final int position = cursor.getPosition();
        viewHolder.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);
                int current = Integer.parseInt(quantity);
                if (current == 0) {
                    Toast.makeText(v.getContext(), R.string.not_in_stock, Toast.LENGTH_SHORT).show();
                    return;
                }
                current--;
                viewHolder.quantityTextView.setText(quantity);
                InventoryDbHelper dbHelper = new InventoryDbHelper(v.getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(InventoryContract.Product.COLUMN_PRODUCT_QUANTITY, current);
                long id = cursor.getLong(cursor.getColumnIndex(InventoryContract.Product._ID));
                Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.Product.CONTENT_URI, id);
                v.getContext().getContentResolver().update(currentProductUri, values, null, null);
                db.close();
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.imageViewList)
        ImageView imageView;
        @BindView(R.id.productName)
        TextView nameTextView;
        @BindView(R.id.price)
        TextView priceTextView;
        @BindView(R.id.quantity)
        TextView quantityTextView;
        @BindView(R.id.btnSell)
        ImageButton sellButton;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
