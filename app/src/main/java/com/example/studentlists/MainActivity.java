package com.example.studentlists;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    EditText etName, etAge, etBirthday;
    Spinner spinnerSex;
    Button btnAdd, btnDelete, btnViewAll;
    String selectedSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDb = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etBirthday = findViewById(R.id.etBirthday);
        spinnerSex = findViewById(R.id.spinnerSex);
        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
        btnViewAll = findViewById(R.id.btnViewAll);

        // Set up spinner for Sex selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);
        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSex = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSex = "Male"; // Default to Male
            }
        });

        // Set up DatePicker for Birthday
        etBirthday.setOnClickListener(v -> showDatePickerDialog());

        addData();
        deleteData();
        viewAll();
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String birthday = (dayOfMonth) + "/" + (monthOfYear + 1) + "/" + year1;
                    etBirthday.setText(birthday);
                    calculateAge(year1, monthOfYear, dayOfMonth);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        etAge.setText(String.valueOf(age));
    }

    public void addData() {
        btnAdd.setOnClickListener(v -> {
            boolean isInserted = myDb.insertData(etName.getText().toString(),
                    Integer.parseInt(etAge.getText().toString()),
                    etBirthday.getText().toString(),
                    selectedSex);
            if (isInserted) {
                Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                // Clear the input fields
                etName.setText("");
                etBirthday.setText("");
                etAge.setText("");
                spinnerSex.setSelection(0);
            } else {
                Toast.makeText(MainActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteData() {
        btnDelete.setOnClickListener(v -> {
            Integer deletedRows = myDb.deleteData(etName.getText().toString());
            if (deletedRows > 0) {
                Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
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