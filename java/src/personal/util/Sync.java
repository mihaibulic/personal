package personal.util;

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
		System.out.println("Hash imported");
		
		System.out.println("Begining copying");
        for(int x = 0; x < fileList.size(); x++)
        {
        	syncAll(fileList.get(x), dest);
        }
        System.out.println("Files copied");
        
        System.out.println("Outputing hash");
        writeHash(hashFile, newlyCopiedFile);
	}
	
	enum STATUS {OK, DNE, CANT_OVERWRITE, IOEXCEPTION};
	
	STATUS syncAll(File toSync, File dest)
	{
		if(!toSync.exists())
		{
			System.out.println("No such file or directory to copy");
			return STATUS.DNE;
		}
		
		if(toSync.isDirectory())
		{
			File newDest = new File(dest, toSync.getName());
			newDest.mkdirs();

			for(File f : toSync.listFiles())
			{
				syncAll(f, newDest);
			}
		}
		else
		{
			syncFile(toSync, dest);
		}
		
		return STATUS.OK;
	}
	
	STATUS syncFile(File toSync, File dest)
	{
		FileChannel source = null;
		FileChannel destination = null;
		
		if(!alreadySynced.contains(toSync.getPath()))
		{			
			if(dest.isDirectory())
			{
				dest = new File(dest, toSync.getName());
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
			
			File tmp = new File(toSync.getName());
			try 
			{
				source = new FileInputStream(toSync).getChannel();
				destination = new FileOutputStream(tmp).getChannel();
				destination.transferFrom(source, 0, source.size());
			}
			catch(IOException e)
			{
				System.out.println("Ommiting " + toSync.getName());
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
					System.out.println("Unable to close stream on " + toSync.getName());
					return STATUS.IOEXCEPTION;
				}
			}
			
			Fingerprint fp = getFingerprint(tmp);
			if(!isDuplicate(fp))
			{
				fixTags(fp, tmp);
				if(tmp.renameTo(dest))
				{
					System.out.println("Synced " + dest.getName());
				}
				else
				{
					System.out.println("Omitting " + dest.getName());
				}
			}
			
			alreadySynced.add(toSync.getPath());
		}
		
		return STATUS.OK;
	}
	
	class Fingerprint
	{
		// TODO create me
	}

	/**
	 * generates fingerprint from echonest-codegen
	 */
	Fingerprint getFingerprint(File in)
	{
		// TODO create me
		return new Fingerprint();
	}

	/**
	 * query remote DB for id3 tag info
 	 * cleanup w/ some MP3TagFixer stuff (remove ft, capitalization, etc.)
	 * @param fp
	 * @param in
	 */
	void fixTags(Fingerprint fp, File in)
	{
		// TODO create me
	}

	/**
	 * query local DB. IF matched, it's a duplicate (true). 
	 * if it's not matched, it's original (false)
	 * @param fp
	 * @return
	 */
	boolean isDuplicate(Fingerprint fp)
	{
		return false;
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
