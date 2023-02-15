package com.example.multitable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ContactDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "contacts";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String NUMBER = "number";

    Context context;
    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+"("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+NAME+" TEXT,"+NUMBER+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

        onCreate(db);
    }

    public void addContacts(String name,String number){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NAME,name);
        cv.put(NUMBER,number);

        database.insert(TABLE_NAME,null,cv);
        Toast.makeText(context, "Contact Added, restart app to see changes\"", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<ContactModel> fetchContacts(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        ArrayList<ContactModel> dbContactArr = new ArrayList<>();
        while (cursor.moveToNext()){
            ContactModel model = new ContactModel();
            model.name = cursor.getString(1);
            model.number = cursor.getString(2);

            dbContactArr.add(model);
        }
        return dbContactArr;
    }

    public void updateContact(String name,String number,int position){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(NAME,name);
        cv.put(NUMBER,number);

        database.update(TABLE_NAME,cv,ID+" = "+position,null);
        Toast.makeText(context, "Contact updated, Restart app to see changes", Toast.LENGTH_SHORT).show();
    }

    public void deleteContact(int position){
        SQLiteDatabase database = this.getWritableDatabase();

        database.delete(TABLE_NAME,ID+" = "+position,null);
        Toast.makeText(context, "Contact Deleted, Restart app to see changes", Toast.LENGTH_SHORT).show();
    }
}
