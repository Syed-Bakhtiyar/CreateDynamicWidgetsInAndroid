package bilal.com.createdynamicwidgets;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by BILAL on 11/28/2017.
 */

public class BackupDatabase {
    public static void BackupDatabase() throws IOException
    {
        boolean success =true;
        File file = null;
        file = new File(Environment.getExternalStorageDirectory() +"/CreateDynamic/database");

        if (file.exists()) {
            success = true;
        } else {
            success = file.mkdirs();
        }

        if (success)
        {
//            String inFileName = "/data/data/" + getPackageName() + "/databases/StorePerfectDB";
            String inFileName = "/data/data/bilal.com.createdynamicwidgets/databases/"+DBHelper.DBNAME;
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = Environment.getExternalStorageDirectory()+"/CreateDynamic/database/"+DBHelper.DBNAME;

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer))>0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fis.close();
        }
    }
}
