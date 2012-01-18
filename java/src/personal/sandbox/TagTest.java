package personal.sandbox;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class TagTest {

	public TagTest(File in) 
	{
		try
		{
			 AudioFile f = AudioFileIO.read(in);
	         Tag tag = f.getTag();
	         
	         tag.setField(FieldKey.ARTIST, "test");
	         f.commit();
	         
	         tag.setField(FieldKey.ARTIST, "crew"); // rem
	         f.commit();
	         
	         AudioFile f2 = AudioFileIO.read(in);
	         Tag tag2 = f2.getTag();
	         System.out.println(tag2.getFirst(FieldKey.ARTIST));
	         
	         
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		new TagTest(new File(args[0]));
	}

}
