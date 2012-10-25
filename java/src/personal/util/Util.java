package personal.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util
{
    /**
     * @param file
     *            - string for the file to be written to (tip: use a suffix of .log and place it in /tmp)
     * @param msg
     *            - string to write to log file
     */
    public static void log(String file, String msg)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        
        write(file, dateFormat.format(date) + "\t" + msg + "\n", true);
    }
    
    /**
     * @param msg
     *            - string to write to log file
     */
    public static void log(String msg)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        
        write("/tmp/generic.log", dateFormat.format(date) + "\t" + msg + "\n", true);
    }
    
    /**
     * Does not append  
     * @param file - string is file location+name eg: /home/user/test.txt
     * @param msg - string to write to file
     */
    public static void write(String file, String msg)
    {
        write(file, msg, false);
    }

    /**
     * @param file - string is file location+name eg: /home/user/test.txt
     * @param msg - string to write to file
     * @param append - should msg be appended to the file or should it override the file
     */
    public static void write(String file, String msg, boolean append)
    {
        //try to make a filewriter, if an exception is caught the method stops
        FileWriter f =null;
        try
        {
            f = new FileWriter(file, append);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        BufferedWriter out = new BufferedWriter(f);
        
        //try to write the msg to the file and no matter what make sure to close the stream
        try
        {
            out.write(msg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
