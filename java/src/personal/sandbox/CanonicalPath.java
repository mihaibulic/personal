package personal.sandbox;

import java.io.File;
import java.io.IOException;

public class CanonicalPath 
{
	public CanonicalPath() throws IOException
	{
		File test = new File("/mnt/deskHai/Public/Music/NWA/Fuck The Police.mp3"); 
		if(test.exists())
		{
			System.out.println("Canonical\t" + test.getCanonicalPath());
			System.out.println("Absolute\t" + test.getAbsolutePath());
			System.out.println("Path\t" + test.getPath());
			System.out.println("Name\t" + test.getName());
		}
		else
		{
			throw new RuntimeException("DNE");
		}
		
		try
		{
			errorThrower();
		}
		catch(IOException e)
		{
			System.out.println("err");
			e.printStackTrace();
		}
		
		System.out.println("cont");
		
	}
	
	void errorThrower() throws IOException
	{
		throw new IOException();
	}
	
	public static void main(String[] args) throws IOException
	{
		new CanonicalPath();
	}
	
}
