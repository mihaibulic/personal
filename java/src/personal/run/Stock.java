package personal.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Stock
{
    public static void main(String[] args)
    {
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);
        boolean first = true;
        String cur ="",output="";
        
        System.out.println("I will take all your stock entries and seperate them with commas after you type exit");
        System.out.println("enter stock or quit");
        while(true)
        {
            try
            {
                cur = in.readLine();
                
                if(cur.equals("quit"))
                {
                    break;
                }
                else
                {
                    if(first)
                    {
                        first = false;
                        output += cur;
                    }
                    else
                    {
                        output += ", " + cur;
                    }
                }
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        System.out.println(output);
    }
}
