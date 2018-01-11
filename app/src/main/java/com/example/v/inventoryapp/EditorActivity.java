package com.example.v.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.v.inventoryapp.data.InventoryContract.Product;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int APP_PERMISSION_TO_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;

    private static final int INSERT_ACTION = 0;
    private static final int UPDATE_ACTION = 1;
    private static int action;

    Uri currentProductUri;
    Uri imgUri;
    int currentQuantity;
    //Picture
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.btnSelectImage)
    Button btnSelectImage;
    //Overview
    @BindView(R.id.edit_name)
    TextView editName;
    @BindView(R.id.edit_price)
    TextView editPrice;
    //Stock
    @BindView(R.id.edit_stock_dec)
    Button editStockDec;
    @BindView(R.id.edit_stock_inc)
    Button editStockInc;
    @BindView(R.id.edit_stock)
    TextView editStock;
    //Supplier
    @BindView(R.id.edit_supplier)
    TextView editSupplier;
    @BindView(R.id.edit_supplier_phone)
    TextView editSupplierPhone;
    @BindView(R.id.btnOrderMore)
    Button orderMore;
    private boolean mProductHasChanged = false;
    //onTouchListener to detect moidifications
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    //Phone validation
    public static boolean isValidPhone(String phone) {
        String expression = "^([0-9\\+]|\\(\\d{1,3}\\))[0-9\\-\\. ]{5,10}$";
        CharSequence inputString = phone;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        //set appropriate title as edit or add new
        final Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri == null) {
            action = INSERT_ACTION;
            setTitle(getString(R.string.editer_activity_title_add));
            orderMore.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            action = UPDATE_ACTION;
            setTitle(getString(R.string.editer_activity_title_edit));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //click listener for detecting modification
        btnSelectImage.setOnTouchListener(mOnTouchListener);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });
        editName.setOnTouchListener(mOnTouchListener);
        editPrice.setOnTouchListener(mOnTouchListener);
        editStockDec.setOnTouchListener(mOnTouchListener);
        editStockDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantity == 0) {
                    Toast.makeText(getApplicationContext(), R.string.msg_no_negative_quantity, Toast.LENGTH_LONG).show();
                    return;
                }
                currentQuantity--;
                editStock.setText(String.valueOf(currentQuantity));
            }
        });
        editStock.setOnTouchListener(mOnTouchListener);
        editStockInc.setOnTouchListener(mOnTouchListener);
        editStockInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuantity++;
                editStock.setText(String.valueOf(currentQuantity));
            }
        });
        editSupplier.setOnTouchListener(mOnTouchListener);
        editSupplierPhone.setOnTouchListener(mOnTouchListener);

        orderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialerIntent = new Intent(Intent.ACTION_DIAL);
                dialerIntent.setData(Uri.parse("tel:" + editSupplierPhone.getText().toString().trim()));
                if (dialerIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(dialerIntent);
                }
            }
        });

    }

    //get selected image from picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                imgUri = resultData.getData();
                imageView.setImageURI(imgUri);
                imageView.invalidate();
            }
        }
    }

    //open image selecter
    public void openImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    APP_PERMISSION_TO_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        //open selector
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Loader implementation
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String projection[] = {
                Product._ID,
                Product.COLUMN_PRODUCT_IMAGE,
                Product.COLUMN_PRODUCT_NAME,
                Product.COLUMN_PRODUCT_PRICE,
                Product.COLUMN_PRODUCT_QUANTITY,
                Product.COLUMN_SUPPLIER,
                Product.COLUMN_SUPPLIER_PHONE
        };

        return new CursorLoader(
                this,
                currentProductUri,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(Product.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(Product.COLUMN_PRODUCT_PRICE);
            int imgColumnIndex = cursor.getColumnIndex(Product.COLUMN_PRODUCT_IMAGE);
            int quantityColumnIndex = cursor.getColumnIndex(Product.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(Product.COLUMN_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(Product.COLUMN_SUPPLIER_PHONE);

            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String imgUri = cursor.getString(imgColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            imageView.setImageURI(Uri.parse(imgUri));
            editName.setText(name);
            editPrice.setText(price);
            editStock.setText(quantity);
            editSupplier.setText(supplier);
            editSupplierPhone.setText(supplierPhone);

            currentQuantity = Integer.parseInt(quantity.toString().trim());

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editName.setText("");
        editPrice.setText("");
        editStock.setText("0");
        editSupplier.setText("");
        editSupplierPhone.setText("");
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {
        ContentValues values = new ContentValues();
        boolean valid = true;
        StringBuilder validationAlert = new StringBuilder("Required : ");

        int price = 0;
        int quantity = 0;
        String nameString = editName.getText().toString().trim();
        String priceString = editPrice.getText().toString().trim();
        String quantityString = editStock.getText().toString().trim();
        String supplier = editSupplier.getText().toString().trim();
        String supplierPhone = editSupplierPhone.getText().toString().trim();

        //Check empty,negative fields
        if (imgUri == null && action == INSERT_ACTION) {
            validationAlert.append("Image, ");
            valid = false;
        }

        if (TextUtils.isEmpty(nameString)) {
            validationAlert.append("Name, ");
            valid = false;
        }
        if (TextUtils.isEmpty(priceString)) {
            validationAlert.append("Price, ");
            valid = false;
        } else {
            price = Integer.parseInt(priceString);
            if (price < 0) {
                validationAlert.append("\nPrice can't be negative");
                valid = false;
            }
        }
        if (TextUtils.isEmpty(quantityString)) {
            validationAlert.append("Quantity, ");
            valid = false;
        } else {
            quantity = Integer.parseInt(quantityString);
            if (quantity < 0) {
                validationAlert.append("\nQuantity can't be negative");
                valid = false;
            }
        }
        if (TextUtils.isEmpty(supplier)) {
            validationAlert.append("Supplier name, ");
            valid = false;
        }
        if (TextUtils.isEmpty(supplierPhone)) {
            validationAlert.append("Supplier phone, ");
            valid = false;
        } else {
            if (!isValidPhone(supplierPhone)) {
                validationAlert.append("\nPhone number not valid");
                valid = false;
            }
        }

        if (valid) {
            if (imgUri != null)
                values.put(Product.COLUMN_PRODUCT_IMAGE, imgUri.toString());
            values.put(Product.COLUMN_PRODUCT_NAME, nameString);
            values.put(Product.COLUMN_PRODUCT_PRICE, price);
            values.put(Product.COLUMN_PRODUCT_QUANTITY, quantity);
            values.put(Product.COLUMN_SUPPLIER, supplier);
            values.put(Product.COLUMN_SUPPLIER_PHONE, supplierPhone);

            if (currentProductUri == null) {
                Uri newRow = getContentResolver().insert(Product.CONTENT_URI, values);
                if (newRow == null) {
                    Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Product saved", Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(this, "Update failed please try again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        } else {;
            Toast.makeText(this, "" + validationAlert.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Product Delete failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Product Deleted", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this product?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
