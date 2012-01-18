package personal.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Sort 
{
	public Sort(File in, File out) throws IOException
	{
		ArrayList<String> artists = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(in));
		
		String line = "";
		while((line = br.readLine()) != null)
		{
			if(!artists.contains(line)) artists.add(line);
		}
		br.close();
		
		Collections.sort(artists);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		
		for(int x = 0; x < artists.size(); x++)
		{
			boolean write = true;
			String cur = artists.get(x);
			if(x+1 < artists.size())
			{
				String next = artists.get(x+1);
				if(next.startsWith(cur))
				{
					write = false;
				}
			}
				
			if(write)
			{
				bw.write(cur);
				bw.newLine();
			}
		}
		
		bw.flush();
		bw.close();
	}
	
	public static void main(String[] args) throws IOException 
	{
		new Sort(new File(args[0]), new File(args[1]));
	}

}
