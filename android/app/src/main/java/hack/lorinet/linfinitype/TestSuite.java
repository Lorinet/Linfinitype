package hack.lorinet.linfinitype;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestSuite
{
    public static String currentTestFile = "";
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void newTest()
    {
        File folder = new File(Environment.getExternalStorageDirectory(), "linfinitype");
        if(!folder.exists()) folder.mkdir();
        String filename = "[test " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + "].log";
        File testFile = new File(folder, filename);
        try
        {
            testFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        currentTestFile = testFile.getAbsolutePath();
        testLog("Test started");
    }

    public static void stopTest()
    {
        if(currentTestFile.length() > 0)
        {
            currentTestFile = "";
        }
    }

    public static boolean testing()
    {
        return currentTestFile.length() > 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void testLog(String text)
    {
        String fulltext = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + "] " + text;
        Log.i("LinfinityTest", fulltext);
        if(!testing()) return;
        try
        {
            FileWriter fw = new FileWriter(currentTestFile, true);
            fw.write(fulltext + "\n");
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
