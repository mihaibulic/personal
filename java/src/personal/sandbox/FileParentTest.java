package personal.sandbox;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class FileParentTest {

	public FileParentTest(File url) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
	{
		AudioFile f = AudioFileIO.read(url);
		Tag t = f.getTag();
		
		System.out.println("*" + t.getFirst(FieldKey.TITLE));
	}
	
	public static void main(String[] args) throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException 
	{
		new FileParentTest(new File(args[0]));
	}

}
