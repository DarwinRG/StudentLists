package com.example.studentlists;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    EditText etName, etAge, etBirthday, etSex, etId;
    Button btnAdd, btnDelete, btnViewAll, btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etBirthday = findViewById(R.id.etBirthday);
        etSex = findViewById(R.id.etSex);
        etId = findViewById(R.id.etId);
        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnUpdate = findViewById(R.id.btnUpdate);

        addData();
        deleteData();
        viewAll();
        updateData();
    }

    private boolean validateInputs() {
        if (etName.getText().toString().isEmpty() ||
            etAge.getText().toString().isEmpty() ||
            etBirthday.getText().toString().isEmpty() ||
            etSex.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void clearInputs() {
        etName.setText("");
        etAge.setText("");
        etBirthday.setText("");
        etSex.setText("");
        etId.setText("");
    }

    public void updateData() {
        btnUpdate.setOnClickListener(v -> {
            if (!validateInputs() || etId.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields including ID", Toast.LENGTH_LONG).show();
                return;
            }
            boolean isUpdated = myDb.updateData(etId.getText().toString(),
                    etName.getText().toString(),
                    etAge.getText().toString(),
                    etBirthday.getText().toString(),
                    etSex.getText().toString());
            if (isUpdated) {
                Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_LONG).show();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Data not Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addData() {
        btnAdd.setOnClickListener(v -> {
            if (!validateInputs()) return;
            boolean isInserted = myDb.insertData(etName.getText().toString(),
                    etAge.getText().toString(),
                    etBirthday.getText().toString(),
                    etSex.getText().toString());
            if (isInserted) {
                Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteData() {
        btnDelete.setOnClickListener(v -> {
            if (etName.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter the name to delete", Toast.LENGTH_LONG).show();
                return;
            }
            Integer deletedRows = myDb.deleteData(etName.getText().toString());
            if (deletedRows > 0) {
                Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Data not Deleted", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void viewAll() {
        btnViewAll.setOnClickListener(v -> {
            Cursor res = myDb.getAllData();
            if (res.getCount() == 0) {
                showMessage("Error", "No data found");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            while (res.moveToNext()) {
                buffer.append("ID :").append(res.getString(0)).append("\n");
                buffer.append("Name :").append(res.getString(1)).append("\n");
                buffer.append("Age :").append(res.getString(2)).append("\n");
                buffer.append("Birthday :").append(res.getString(3)).append("\n");
                buffer.append("Sex :").append(res.getString(4)).append("\n\n");
            }

            showMessage("Data", buffer.toString());
        });
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }
}