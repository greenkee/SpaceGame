package com.greenkee.spaceshooter.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.greenkee.spaceshooter.GameActivity;
import com.greenkee.spaceshooter.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by student on 7/14/2014.
 */
public class PowerUpSQLHelper extends SQLiteOpenHelper{

    public PowerUpSQLHelper(Context context){
        super(context, "databases.db", null, 1);
    } //last parameter is version number; higher number to upgrade database

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE powerUps (powerUpID INTEGER PRIMARY KEY, powerUpName TEXT, powerUpImage INTEGER, speedX INTEGER, speedY INTEGER, pointValue INTEGER, powerUpSound INTEGER, powerUpEffect TEXT)"; //phone ID is the primary key, used as identifier (array[0])
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS powerUps";
        db.execSQL(query);

        onCreate(db);

    }

    public void loadInitialData(){
        String[]nameArray = {"blue_gem", "bomb"};
        int [] imageArray = {R.drawable.gem_blue, R.drawable.bomb};
        int [] speedXArray = {0, 0 }; //percent of BASE_SPEED
        int [] speedYArray = {50, 50 }; //percent of BASE_SPEED
        int [] pointArray = {1000, 0};
        int [] powerUpSoundArray = {GameActivity.gemSound, GameActivity.bombSound};


        //ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < nameArray.length; i++){
            HashMap<String, String> queryValues = new HashMap<String, String>();

            queryValues.put("powerUpName", nameArray[i] );
            queryValues.put("powerUpImage", ""+imageArray[i]);
            queryValues.put("speedX", ""+speedXArray[i] );
            queryValues.put("speedY",""+speedYArray[i]);
            queryValues.put("pointValue", "" +  pointArray[i]);
            queryValues.put("destroySound", "" +  powerUpSoundArray[i]);

            insertInfo(queryValues);
        }
    }


    public void insertInfo(HashMap<String, String> queryValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("powerUpName", queryValue.get("powerUpName"));//keys are column titles
        values.put("powerUpImage", queryValue.get("powerUpImage"));
        values.put("speedX", queryValue.get("speedX"));
        values.put("speedY", queryValue.get("speedY"));
        values.put("damage", queryValue.get("damage"));
        values.put("health", queryValue.get("health"));
        values.put("pointValue", queryValue.get("pointValue"));
        values.put("powerUpSound", queryValue.get("powerUpSound"));

        db.insert("powerUps", null, values);
        db.close(); //Closes database to prevent further access
    }

    public int updateInfo(HashMap<String, String> queryValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("powerUpName", queryValue.get("powerUpName"));//keys are column titles
        values.put("powerUpImage", queryValue.get("powerUpImage"));
        values.put("speedX", queryValue.get("speedX"));
        values.put("speedY", queryValue.get("speedY"));
        values.put("damage", queryValue.get("damage"));
        values.put("health", queryValue.get("health"));
        values.put("pointValue", queryValue.get("pointValue"));
        values.put("powerUpSound", queryValue.get("powerUpSound"));

        return db.update("powerUps", values, "powerUpID = ?", new String[] {queryValue.get("powerUpID")} );
    }

    public void deleteInfo(String id){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM powerUps where powerUpID = '"+id+"'";

        db.execSQL(deleteQuery);
    }

    public ArrayList<HashMap<String, String>> getAllInfo(){
        ArrayList<HashMap<String, String>> infoArrayList = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM powerUps ORDER BY powerUpID";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String, String> infoMap = new HashMap<String, String>();
                infoMap.put("powerUpID", cursor.getString(0));
                infoMap.put("powerUpName",  cursor.getString(1));
                infoMap.put("powerUpImage",  cursor.getString(2));
                infoMap.put("speedX",  cursor.getString(3));
                infoMap.put("speedY",  cursor.getString(4));
                infoMap.put("damage",  cursor.getString(5));
                infoMap.put("health",  cursor.getString(6));
                infoMap.put("pointValue",  cursor.getString(7));
                infoMap.put("powerUpSound",  cursor.getString(8));


                infoArrayList.add(infoMap);
            } while(cursor.moveToNext());
        }

        return infoArrayList;
    }

    public HashMap<String, String> getInfo(String id){
        HashMap<String, String> infoMap = new HashMap<String, String>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM powerUps WHERE powerUpID = '"+id+"'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                infoMap.put("powerUpName",  cursor.getString(1));
                infoMap.put("powerUpImage",  cursor.getString(2));
                infoMap.put("speedX",  cursor.getString(3));
                infoMap.put("speedY",  cursor.getString(4));
                infoMap.put("damage",  cursor.getString(5));
                infoMap.put("health",  cursor.getString(6));
                infoMap.put("pointValue",  cursor.getString(7));
                infoMap.put("powerUpSound",  cursor.getString(8));
            } while(cursor.moveToNext());
        }

        return infoMap;
    }
}
