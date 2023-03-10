package com.example.multitable;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
   RecyclerView recyclerView;
   FloatingActionButton btnOpenDialog;
    RecyclerViewAdapter adapter;
    TextView noContactTxt;
    SharedPreferences pref;
    boolean fetched;
    MyDBHelper myDBHelper;
    ArrayList<ContactModel> contactArr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOpenDialog = findViewById(R.id.btnDialog);
        recyclerView = findViewById(R.id.recyclerView);

        contactArr.add(new ContactModel("Shivam","8456454544"));
        contactArr.add(new ContactModel("Shivam 1","1244557575"));
        contactArr.add(new ContactModel("Shivam 2","4576724454"));
        contactArr.add(new ContactModel("Shivam 3","8856457241"));
        contactArr.add(new ContactModel("Shivam 4","3132458787"));

        pref = getSharedPreferences("ContactFetch",MODE_PRIVATE);
        fetched = pref.getBoolean("fetched",false);
        //To show in linear layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myDBHelper = new MyDBHelper(this);
        if(fetched){
            contactArr = myDBHelper.fetchContacts();
        }
        //Initializing and set adapter
        adapter = new RecyclerViewAdapter(this,contactArr);
        recyclerView.setAdapter(adapter);


        //Operation when user search in search view


        //Perform operation when clicked on floating ation button for adding contact
        btnOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_update);
                TextView txtTitle = dialog.findViewById(R.id.txtTitle);
                EditText editName = dialog.findViewById(R.id.editName);
                EditText editNumber = dialog.findViewById(R.id.editNumber);
                Button btnSave = dialog.findViewById(R.id.btnAction);
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = "";
                        String number = "";
                        if (editName.getText().toString().equals("") || editNumber.getText().toString().equals("")){
                            Toast.makeText(MainActivity.this, "Please Enter Details ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            name = editName.getText().toString();
                            number = editNumber.getText().toString();
                            myDBHelper.addContacts(name,number);

                            //contactArr.add(new ContactModel(name,number));
                            adapter.notifyItemInserted(contactArr.size());
                            recyclerView.scrollToPosition(contactArr.size());
                            dialog.dismiss();
                        }
                    }
                });
             dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        MenuItem.OnActionExpandListener actionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        };
        menu.findItem(R.id.searchMenu).setOnActionExpandListener(actionExpandListener);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchMenu).getActionView();
        searchView.setQueryHint("Search Contacts");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            //for when user enters text
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.importContactMenu:
                //checking version of android
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //checking read contacts permission is granted or not
                    if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                        //if granted
                        if(!fetched) {
                            getContacts();
                            Toast.makeText(this, "Fetched", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(this, "Contacts already imported", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        //if permission not granted request permission
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1);
                    }
                }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(!fetched) {
                    getContacts();
                }
            }
        }
    }

    //function for getting contacts from phone storage
    private void getContacts(){
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //contactArr.add(new ContactModel(R.drawable.profile,name,number));
            myDBHelper.addContacts(name,number);
        }
        contactArr = myDBHelper.fetchContacts();
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("fetched",true);
        editor.apply();
    }

    //function for search operation
    private void filterList(String text){
        ArrayList<ContactModel> filteredList = new ArrayList<>();
        for (ContactModel item: contactArr) {
            if(item.name.toString().toLowerCase().contains(text.toLowerCase()) || item.number.toString().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()){
            Toast.makeText(this, "No Contact found", Toast.LENGTH_SHORT).show();
            adapter.setFilteredList(filteredList);
        }
        else {
            //setting array with adapter
            adapter.setFilteredList(filteredList);
        }
    }

}