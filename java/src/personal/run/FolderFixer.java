package personal.run;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class FolderFixer {

	private String dir;
	
	public FolderFixer(File url) 
	{
		if(url.isFile()) throw new RuntimeException("arg must be directory");

		try 
		{
			dir = url.getCanonicalPath();
		} catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		dir += (dir.endsWith(File.separator) ? "" : File.separator);
		
		System.out.println("FF: " + dir);
		
		Queue<File> fileQue = new LinkedList<File>();
    	File potentialSong;
    	AudioFile f;
    	Tag tag;
    	
    	fileQue.addAll(Arrays.asList(url.listFiles()));
    	
    	System.out.println("FF: Searching...");
    	while((potentialSong = fileQue.poll()) != null)
    	{
    		if(potentialSong.isDirectory())
    		{
    			fileQue.addAll(Arrays.asList(potentialSong.listFiles()));
    			potentialSong.delete(); //will only delete empty folders
    		}
    		else
    		{
    			try 
    			{
					f = AudioFileIO.read(potentialSong);
					tag = f.getTag();
					
					String artist = reformat(tag.getFirst(FieldKey.ARTIST));
					String title = reformat(tag.getFirst(FieldKey.TITLE));
					
					String fileName = potentialSong.getName();
					int mid= fileName.lastIndexOf(".");
					String name = fileName.substring(0,mid);
					String ext = fileName.substring(mid,fileName.length());
					
					if(!(name.equals(title) && getDirName(potentialSong).equals(artist)))
					{
						File artistDir = new File(dir+artist);
						artistDir.mkdirs();
						
						if(!potentialSong.renameTo(new File(artistDir, title+ext)))
						{
							System.out.println("FF: failed to move " + artist + " - " + title + " to " + artistDir.getPath());
						}
					}
					
				} catch (CannotReadException e) 
				{
					e.printStackTrace();
				} catch (IOException e) 
				{
					e.printStackTrace();
				} catch (TagException e) 
				{
					e.printStackTrace();
				} catch (ReadOnlyFileException e) 
				{
					e.printStackTrace();
				} catch (InvalidAudioFrameException e) 
				{
					e.printStackTrace();
				}
    		}
    	}
	}
	
	private String reformat(String name)
	{
	    return name.replaceAll("[^0-9a-zA-Z~@#%&_=;, {}.^()+'-]", "").replaceAll("  "," ").trim();
	}
	
	private String getDirName(File song)
	{
		File f = new File(song.getParent());
		
		return f.getName();
	}
	
	public static void main(String[] args) throws IOException 
	{
    	if(args.length != 1)
    	{
    		throw new RuntimeException("Must run with 1 arg: \n<music director or file containing paths to songs> ");
    	}
    	
        new FolderFixer(new File(args[0]));
	}

}
