package personal.sandbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import personal.util.Levenshtein;

public class LevenTest 
{
	public LevenTest(File in, String val) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(in));
		ArrayList<String> artists=new ArrayList<String>();
		String line = "";
		while((line = br.readLine()) != null)
		{
			artists.add(line);
		}
		
		val = fixArtist(val);

		String[] cand = Levenshtein.getClose(val, artists);
		
		System.out.println(cand.length + "\t" + ((cand.length == 1) ?cand[0]:val));
	}

	private String fixArtist(String in)
    {
        if(in.isEmpty())
        {
            in = "Unknown";
        }
        else
        {
	        String[] keyWords = {" ft", " Ft", " feat", " Feat", "; ", ";", " - ", ", ", " ("};
	        for(String word : keyWords)
	        {
	        	int index = in.indexOf(word);
	            if(index > 0)
	            {
	        		in = in.substring(0, index);            		
	            }
	        }
        }
        return in.trim();
    }
	
	public static void main(String[] args) throws IOException 
	{
		new LevenTest(new File(args[0]), args[1]);
	}

}
