package com.example.studentlists;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        setupSpinner();
        setupDatePicker();
        setupButtons();
    }

    private void setupSpinner() {
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
    }

    private void setupDatePicker() {
        etBirthday.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupButtons() {
        btnAdd.setOnClickListener(v -> addData());
        btnDelete.setOnClickListener(v -> fetchDataForDelete());
        btnViewAll.setOnClickListener(v -> viewAll());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
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

    private void addData() {
        String name = etName.getText().toString();
        String ageStr = etAge.getText().toString();
        String birthday = etBirthday.getText().toString();

        if (name.isEmpty() || ageStr.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Invalid age", Toast.LENGTH_LONG).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean isInserted = myDb.insertData(name, age, birthday, selectedSex);
            handler.post(() -> {
                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                    clearFields();
                } else {
                    Toast.makeText(MainActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void fetchDataForDelete() {
        fetchData(true);
    }

    private void viewAll() {
        fetchData(false);
    }

    private void fetchData(boolean isDeleteOperation) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Cursor res = myDb.getAllData();
            handler.post(() -> {
                if (res.getCount() == 0) {
                    showMessage("Error", "No data found");
                    return;
                }

                ArrayList<String> entryList = new ArrayList<>();
                StringBuilder buffer = new StringBuilder();
                while (res.moveToNext()) {
                    String entry = "ID: " + res.getString(0) + ", Name: " + res.getString(1) + ", Age: "
                            + res.getString(2) + ", Birthday: " + res.getString(3) + ", Sex: " + res.getString(4);
                    entryList.add(entry);
                    buffer.append("ID :").append(res.getString(0)).append("\n");
                    buffer.append("Name :").append(res.getString(1)).append("\n");
                    buffer.append("Age :").append(res.getString(2)).append("\n");
                    buffer.append("Birthday :").append(res.getString(3)).append("\n");
                    buffer.append("Sex :").append(res.getString(4)).append("\n\n");
                }

                if (isDeleteOperation) {
                    showDeleteDialog(entryList);
                } else {
                    showMessage("Student Data", buffer.toString());
                }
            });
        });
    }

    private void showMessage(String title, String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.show();
        });
    }

    private void showDeleteDialog(ArrayList<String> entryList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Entry to Delete");
        builder.setItems(entryList.toArray(new String[0]), (dialog, which) -> {
            String selectedEntry = entryList.get(which);
            String selectedName = selectedEntry.split(",")[1].split(":")[1].trim();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                int deletedRows = myDb.deleteData(selectedName);
                handler.post(() -> {
                    if (deletedRows > 0) {
                        Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Data not Deleted", Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
        builder.show();
    }

    private void clearFields() {
        etName.setText("");
        etBirthday.setText("");
        etAge.setText("");
        spinnerSex.setSelection(0);
    }
}