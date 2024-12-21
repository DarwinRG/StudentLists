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
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // Declare UI elements and variables
    DatabaseHelper myDb;
    EditText etName, etAge, etBirthday, etEmail;
    Spinner spinnerSex;
    Button btnAdd, btnDelete, btnViewAll;
    String selectedSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply app animation to the root view
        View rootView = findViewById(android.R.id.content);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootView.startAnimation(fadeIn);

        // Initialize database helper and UI elements
        myDb = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etBirthday = findViewById(R.id.etBirthday);
        etEmail = findViewById(R.id.etEmail);
        spinnerSex = findViewById(R.id.spinnerSex);
        btnAdd = findViewById(R.id.btnAdd);
        btnDelete = findViewById(R.id.btnDelete);
        btnViewAll = findViewById(R.id.btnViewAll);

        // Set input filter and text watcher for name field
        setupNameField();

        // Setup spinner, date picker, and buttons
        setupSpinner();
        setupDatePicker();
        setupButtons();
    }

    // Setup input filter and text watcher for name field
    private void setupNameField() {
        etName.setFilters(new InputFilter[]{(source, start, end, dest, destinationStart, destinationEnd) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }});

        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        etName.addTextChangedListener(new TextWatcher() {
            boolean isEditing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;

                isEditing = true;
                String input = s.toString();
                StringBuilder capitalized = new StringBuilder();
                boolean capitalizeNext = true;
                boolean lastCharWasSpace = false;

                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (i == 0 && Character.isWhitespace(c)) {
                        continue;
                    }
                    if (Character.isWhitespace(c)) {
                        if (!lastCharWasSpace) {
                            capitalized.append(' ');
                            lastCharWasSpace = true;
                        }
                        capitalizeNext = true;
                    } else {
                        if (capitalizeNext) {
                            c = Character.toUpperCase(c);
                            capitalizeNext = false;
                        }
                        capitalized.append(c);
                        lastCharWasSpace = false;
                    }
                }

                String result = capitalized.toString().replaceAll("^\\s+", "");
                if (!result.equals(s.toString())) {
                    etName.setText(result);
                    etName.setSelection(result.length());
                }
                isEditing = false;
            }
        });
    }

    // Setup spinner for gender selection
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);
        spinnerSex.setSelection(0);

        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSex = parent.getItemAtPosition(position).toString();
                if (selectedSex.equals("Select Gender")) {
                    selectedSex = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSex = null;
            }
        });
    }

    // Setup date picker for birthday field
    private void setupDatePicker() {
        etBirthday.setOnClickListener(v -> showDatePickerDialog());
    }

    // Setup button click listeners
    private void setupButtons() {
        btnAdd.setOnClickListener(v -> addData());
        btnDelete.setOnClickListener(v -> fetchDataForDelete());
        btnViewAll.setOnClickListener(v -> viewAll());
    }

    // Show date picker dialog
    private void showDatePickerDialog() {
        int year = 2000;
        int month = 6;
        int day = 1;

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etBirthday.setText(birthday);
                    calculateAge(year1, monthOfYear, dayOfMonth);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Calculate age based on selected date
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

    // Add student data to the database
    private void addData() {
        String name = etName.getText().toString();
        String ageStr = etAge.getText().toString();
        String birthday = etBirthday.getText().toString();
        String email = etEmail.getText().toString();

        if (name.isEmpty() || ageStr.isEmpty() || birthday.isEmpty() || email.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedSex == null) {
            Toast.makeText(MainActivity.this, "Please select a gender", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidName(name)) {
            Toast.makeText(MainActivity.this, "Name must have at least 2 words, each with at least 2 letters", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "Invalid email format", Toast.LENGTH_LONG).show();
            return;
        }

        if (myDb.isEmailExists(email)) {
            Toast.makeText(MainActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Invalid age", Toast.LENGTH_LONG).show();
            return;
        }

        if (age < 4 || age > 99) {
            Toast.makeText(MainActivity.this, "Age must be between 4 and 99 years old", Toast.LENGTH_LONG).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean isInserted = myDb.insertData(name, age, birthday, selectedSex, email);
            handler.post(() -> {
                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Student Data Added", Toast.LENGTH_LONG).show();
                    clearFields();
                } else {
                    Toast.makeText(MainActivity.this, "Student Data not Added", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Validate name format
    private boolean isValidName(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length < 2) {
            return false;
        }
        for (String word : words) {
            if (word.length() < 2) {
                return false;
            }
        }
        return true;
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+(\\.[a-z]+)*";
        return email.matches(emailPattern);
    }

    // Fetch data for delete operation
    private void fetchDataForDelete() {
        fetchData(true);
    }

    // View all student data
    private void viewAll() {
        fetchData(false);
    }

    // Fetch data from the database
    private void fetchData(boolean isDeleteOperation) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Cursor res = myDb.getAllData();
            if (res.getCount() == 0) {
                handler.post(() -> showMessage("No Data Found", "There are no student records available."));
                return;
            }

            ArrayList<String> entryList = new ArrayList<>();
            StringBuilder buffer = new StringBuilder();
            while (res.moveToNext()) {
                String entry = "ID: " + res.getString(0) + "\nName: " + res.getString(1) + "\nAge: "
                        + res.getString(2) + "\nBirthday: " + res.getString(3) + "\nGender: " + res.getString(4)
                        + "\nEmail: " + res.getString(5) + "\n\n";
                entryList.add(entry);
                buffer.append(entry);
            }

            handler.post(() -> {
                if (isDeleteOperation) {
                    showDeleteDialog(entryList);
                } else {
                    showMessage("Student Data", buffer.toString());
                }
            });
        });
    }

    // Show message dialog
    private void showMessage(String title, String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("Back", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    // Show delete dialog
    private void showDeleteDialog(ArrayList<String> entryList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Entry to Delete");
        builder.setItems(entryList.toArray(new String[0]), (dialog, which) -> {
            String selectedEntry = entryList.get(which);
            String selectedName;
            try {
                selectedName = selectedEntry.split("\n")[1].split(":")[1].trim();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error parsing entry", Toast.LENGTH_LONG).show();
                return;
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                int deletedRows = myDb.deleteData(selectedName);
                handler.post(() -> {
                    if (deletedRows > 0) {
                        Toast.makeText(MainActivity.this, "Student Data Deleted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Student Data not Deleted", Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
        builder.setPositiveButton("Back", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Clear input fields
    private void clearFields() {
        etName.setText("");
        etBirthday.setText("");
        etAge.setText("");
        etEmail.setText("");
        spinnerSex.setSelection(0);
    }
}