package bilal.com.createdynamicwidgets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by BILAL on 11/28/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static String DBNAME = "todo.db";

    private static final int version = 1;

    private static String SERVEY_TABLE = "survey";

    private static String QUESTION_TABLE = "survey_question";

    private static String CREATE_SERVEY_TABLE = "CREATE TABLE IF NOT EXISTS "+SERVEY_TABLE+" (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, server_id TEXT NOT NULL, title TEXT DEFAULT '', publish_date TEXT DEFAULT '', expiry_date TEXT DEFAULT '', created_at TEXT DEFAULT '', status TEXT DEFAULT  '')";

    private static String CREATE_SURVEY_QUESTION = "CREATE TABLE IF NOT EXISTS "+QUESTION_TABLE+" (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,server_id TEXT NOT NULL, servey_table_id TEXT NOT NULL,question_title TEXT NOT NULL, question_type TEXT NOT NULL, answer_type TEXT NOT NULL, question_image TEXT DEFAULT '', options TEXT NOT NULL)";

    private static String TAG = "application";

    public DBHelper(Context context) {
        super(context, DBNAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_SERVEY_TABLE);
        sqLiteDatabase.execSQL(CREATE_SURVEY_QUESTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+SERVEY_TABLE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+QUESTION_TABLE);

    }

    public void emptyTable(String tablename)
            throws SQLException {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tablename, null, null);
//        db.execSQL("delete from "+ DB_TABLE);
        db.close();
    }

    public void add_servey(String server_id, String title, String publish_date, String expiry_date, String created_at, String status){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();

            contentValues.put("server_id",server_id);
            contentValues.put("title",title);
            contentValues.put("publish_date",publish_date);
            contentValues.put("expiry_date",expiry_date);
            contentValues.put("created_at",created_at);
            contentValues.put("status",status);

        long flag = db.insert(SERVEY_TABLE,null,contentValues);

        if(flag == -1){

            Log.d(TAG, "Not inserted servey: ");

        }
        else {

            Log.d(TAG, "Inserted Survey");

        }

    }

    public void add_servey_question(String server_id, String servey_table_id, String question_type, String answer_type, String question_image, String options,String question_title){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put("server_id",server_id);
        contentValues.put("servey_table_id",servey_table_id);
        contentValues.put("question_type",question_type);
        contentValues.put("answer_type",answer_type);
        contentValues.put("question_image",question_image);
        contentValues.put("question_title",question_title);
        contentValues.put("options",options);

        long flag = db.insert(QUESTION_TABLE,null,contentValues);

        if(flag == -1){

            Log.d(TAG, "Not inserted Question: ");

        }
        else {

            Log.d(TAG, "Inserted Question");

        }


    }

    public ArrayList<ServeyModel> getServeyQuestion(){

        ArrayList<ServeyModel> arrayList = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT s.server_id, s.title, s.created_at, sq.server_id, sq.question_title, sq.question_type, sq.answer_type, sq.question_image,sq.options from "+SERVEY_TABLE+" s " +
                "inner join "+QUESTION_TABLE+" sq on s.server_id = sq.servey_table_id";

        int q_num = 1;

        Cursor cursor = db.rawQuery(query,null);

        if(cursor != null && cursor.moveToNext()){
            do {

                arrayList.add(new ServeyModel(
                        cursor.getString(1),
                        q_num+": "+cursor.getString(4),
                        cursor.getString(0),
                        "",
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(2),
                        cursor.getString(8),
                        cursor.getString(7),
                        ""
                ));

                q_num += 1;

            }while (cursor.moveToNext());


        }

        return arrayList;

    }


}
