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
public class EnemySQLHelper extends SQLiteOpenHelper{





    public EnemySQLHelper(Context context){
        super(context, "databases.db", null, 1);
    } //last parameter is version number; higher number to upgrade database

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE enemies (enemyID INTEGER PRIMARY KEY, enemyName TEXT, enemyImage INTEGER, speedX INTEGER, speedY INTEGER, damage INTEGER, health INTEGER, pointValue INTEGER, destroySound INTEGER)"; //phone ID is the primary key, used as identifier (array[0])
        db.execSQL(query);

        System.out.println("DATABASE CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS enemies";
        db.execSQL(query);

        onCreate(db);
        System.out.println("DATABASE UPGRADED");

    }

    public void loadInitialData(){
        String[]nameArray = {"basic_ship", "moving_ship", "asteroid"};
        int [] imageArray = {R.drawable.enemy_ship, R.drawable.moving_ship, R.drawable.meteor_small};
        int [] speedXArray = {0, 100, 0 }; //percent of BASE_SPEED
        int [] speedYArray = {50, 100, 50 }; //percent of BASE_SPEED
        int [] damageArray = {0, 0, 0}; //not used right now
        int [] healthArray = {1, 1, 4}; //not used right now
        int [] pointArray = {100, 300, 500};
        int [] destroySoundArray = {GameActivity.destroySound, GameActivity.destroySound, GameActivity.destroySound};


        //ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < nameArray.length; i++){
            HashMap<String, String> queryValues = new HashMap<String, String>();

            queryValues.put("enemyName", nameArray[i] );
            queryValues.put("enemyImage", ""+imageArray[i]);
            queryValues.put("speedX", ""+speedXArray[i] );
            queryValues.put("speedY",""+speedYArray[i]);
            queryValues.put("damage", ""+damageArray[i]);
            queryValues.put("health", "" + healthArray[i]);
            queryValues.put("pointValue", "" +  pointArray[i]);
            queryValues.put("destroySound", "" +  destroySoundArray[i]);

            insertInfo(queryValues);
        }
    }


    public void insertInfo(HashMap<String, String> queryValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("enemyName", queryValue.get("enemyName"));//keys are column titles
        values.put("enemyImage", queryValue.get("enemyImage"));
        values.put("speedX", queryValue.get("speedX"));
        values.put("speedY", queryValue.get("speedY"));
        values.put("damage", queryValue.get("damage"));
        values.put("health", queryValue.get("health"));
        values.put("pointValue", queryValue.get("pointValue"));
        values.put("destroySound", queryValue.get("destroySound"));

        db.insert("enemies", null, values);
        db.close(); //Closes database to prevent further access
    }

    public int updateInfo(HashMap<String, String> queryValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("enemyName", queryValue.get("enemyName"));//keys are column titles
        values.put("enemyImage", queryValue.get("enemyImage"));
        values.put("speedX", queryValue.get("speedX"));
        values.put("speedY", queryValue.get("speedY"));
        values.put("damage", queryValue.get("damage"));
        values.put("health", queryValue.get("health"));
        values.put("pointValue", queryValue.get("pointValue"));
        values.put("destroySound", queryValue.get("destroySound"));

        return db.update("enemies", values, "enemyID = ?", new String[] {queryValue.get("enemyID")} );
    }

    public void deleteInfo(String id){
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteQuery = "DELETE FROM enemies where enemyID = '"+id+"'";

        db.execSQL(deleteQuery);
    }

    public ArrayList<HashMap<String, String>> getAllInfo(){
        ArrayList<HashMap<String, String>> infoArrayList = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM enemies ORDER BY enemyID";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String, String> infoMap = new HashMap<String, String>();
                infoMap.put("enemyID", cursor.getString(0));
                infoMap.put("enemyName",  cursor.getString(1));
                infoMap.put("enemyImage",  cursor.getString(2));
                infoMap.put("speedX",  cursor.getString(3));
                infoMap.put("speedY",  cursor.getString(4));
                infoMap.put("damage",  cursor.getString(5));
                infoMap.put("health",  cursor.getString(6));
                infoMap.put("pointValue",  cursor.getString(7));
                infoMap.put("destroySound",  cursor.getString(8));


                infoArrayList.add(infoMap);
            } while(cursor.moveToNext());
        }

        return infoArrayList;
    }

    public HashMap<String, String> getInfo(String id){
        HashMap<String, String> infoMap = new HashMap<String, String>();
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM enemies WHERE enemyID = '"+id+"'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                infoMap.put("enemyName",  cursor.getString(1));
                infoMap.put("enemyImage",  cursor.getString(2));
                infoMap.put("speedX",  cursor.getString(3));
                infoMap.put("speedY",  cursor.getString(4));
                infoMap.put("damage",  cursor.getString(5));
                infoMap.put("health",  cursor.getString(6));
                infoMap.put("pointValue",  cursor.getString(7));
                infoMap.put("destroySound",  cursor.getString(8));
            } while(cursor.moveToNext());
        }

        return infoMap;
    }
}
