package DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import DB.GallandaDbSchema.DataTable;
/**
 * Created by sasiroot on 5/4/18.
 */

public class GallandaBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "gallandaDataDB.db";

    public GallandaBaseHelper (Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DataTable.NAME + "(_id integer primary key autoincrement, " +
                DataTable.Cols.TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                DataTable.Cols.SPEED + " integer, " +
                DataTable.Cols.TEMPERATURE + " integer, " +
                DataTable.Cols.BATTERYCHARGE + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
