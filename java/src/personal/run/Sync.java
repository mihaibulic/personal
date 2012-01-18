package personal.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Sync 
{
	private HashSet<String> alreadySynced;
	private ArrayList<String> newFiles;
	
	public Sync(File src, File dest, File hashFile, File newlyCopiedFile) throws IOException
	{
		System.out.println("Starting sync");
		
		if(!src.exists())
		{
			throw new RuntimeException("Source (" + src.getPath() + ") does not exist");
		}
		if(!dest.exists())
		{
			dest.mkdirs();
		}
		
		ArrayList<File> fileList = new ArrayList<File>(); //get list of files recursively (equiv of ls -r)
		fileList.addAll(Arrays.asList(src.listFiles()));
		System.out.println("Initial file import");
		
		alreadySynced = getHash(hashFile);
		newFiles = new ArrayList<String>();
		System.out.println("Hash imported");
		
		System.out.println("Begining copying");
        for(int x = 0; x < fileList.size(); x++)
        {
        	copy(fileList.get(x), dest);
        }
        System.out.println("Files copied");
        
        System.out.println("Outputing hash");
        writeHash(hashFile, newlyCopiedFile);
	}
	
	enum STATUS {OK, DNE, CANT_OVERWRITE, IOEXCEPTION};
	
	STATUS copy(File toCopy, File dest)
	{
		if(!toCopy.exists())
		{
			System.out.println("No such file or directory to copy");
			return STATUS.DNE;
		}
		
		if(toCopy.isDirectory())
		{
			File newDest = new File(dest, toCopy.getName());
			newDest.mkdirs();

			for(File f : toCopy.listFiles())
			{
				copy(f, newDest);
			}
		}
		else
		{
			FileChannel source = null;
			FileChannel destination = null;
			
			if(!alreadySynced.contains(toCopy.getPath()))
			{			
				if(dest.isDirectory())
				{
					dest = new File(dest, toCopy.getName());
				}
				if(!dest.exists()) 
				{
					try 
					{
						dest.createNewFile();
					} catch (IOException e) 
					{
						System.err.println("Cannot create regular file " + dest.getName() + ": Permission denied");
						return STATUS.CANT_OVERWRITE;
					}
				}
				
				if(!dest.canWrite())
				{
					System.err.println("Cannot create regular file " + dest.getName() + ": Permission denied");
					return STATUS.CANT_OVERWRITE;
				}
				
				try 
				{
					source = new FileInputStream(toCopy).getChannel();
					destination = new FileOutputStream(dest).getChannel();
					destination.transferFrom(source, 0, source.size());
					
					source.close();
					destination.close();
					
					alreadySynced.add(toCopy.getPath());
					newFiles.add(dest.getPath());
					
					System.out.println("Copied " + toCopy.getName());
				}
				catch(IOException e)
				{
					System.out.println("Ommiting " + toCopy.getName());
					return STATUS.IOEXCEPTION;
				}
				finally
				{
					try
					{
						if(source != null && source.isOpen()) source.close();
						if(destination != null && destination.isOpen()) destination.close();
					} catch(IOException e)
					{
						System.out.println("Unable to close stream on " + toCopy.getName());
						return STATUS.IOEXCEPTION;
					}
				}
			}
		}
		
		return STATUS.OK;
	}
	
	HashSet<String> getHash(File hashFile) throws IOException
	{
		if(!hashFile.exists())
		{
			hashFile.createNewFile();
		}
		
		BufferedReader br = new BufferedReader(new FileReader(hashFile));
		String line = "";
		alreadySynced = new HashSet<String>();
		
		while((line = br.readLine()) != null)
		{
			alreadySynced.add(line);
		}
		
		return alreadySynced;
	}
	
	void writeHash(File hashFile, File newlyCopiedFile) throws IOException
	{
		String[] toWrite = alreadySynced.toArray(new String[alreadySynced.size()]);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(hashFile, false));
		for(String s : toWrite)
		{
			bw.write(s);
			bw.newLine();
		}
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(newlyCopiedFile, true));
		for(String s : newFiles)
		{
			bw.write(s);
			bw.newLine();
		}
		bw.close();
		
	}
	
	public static void main(String[] args) throws IOException 
	{
		if(args.length != 4)
		{
			throw new RuntimeException("Sync must be run with 3 arguements:\n\t"+
											"<src dir> <dest dir> <hash> <copied>\n" +
										"src dir: directory to sync from\n" +
										"dest dir: directory to synce to\n" +
										"hash: file containing src path to each synced file (each entry on new line)\n" +
										"copied: file containing dest path to each file copied this time");
		}
		
		new Sync(new File(args[0]), new File(args[1]), new File(args[2]), new File(args[3]));
	}

}
