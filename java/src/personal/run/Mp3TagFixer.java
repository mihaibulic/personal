package personal.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import personal.util.Levenshtein;

public class Mp3TagFixer
{
	private class Song
	{
		boolean changed = false;
		String artist;
		String title;
		String genre;
		AudioFile af;
		
		public Song(String artist, String title, String genre, AudioFile af, boolean changed)
		{
			this.artist = artist;
			this.title = title;
			this.genre = genre;
			this.af = af;
			this.changed = changed;
		}
		
		public void setField(FieldKey key, String value)
		{
			switch((FieldKey)key)
			{
				case ARTIST:
				{
					if(changed || !value.equals(artist)) changed = true;
					artist = value;
					break;
				}
				case TITLE:
				{
					if(changed || !value.equals(title)) changed = true;
					title = value;
					break;
				}
				case GENRE:
				{
					if(changed || !value.equals(genre)) changed = true;
					genre = value;
					break;
				}
			}
		}
		
		public void commit()
		{
			try 
			{
				Tag tag = af.getTag();
				
				tag.setField(FieldKey.ARTIST, artist);
				tag.setField(FieldKey.ALBUM, artist);
				tag.setField(FieldKey.TITLE, title);
				tag.setField(FieldKey.GENRE, genre);
				
				af.commit();
			} catch (TagException e)
			{
				e.printStackTrace();
			} catch (CannotWriteException e) 
			{
				e.printStackTrace();
			}
		}
		
	}
	
	private class Occurance
	{
		String value = null;
		List<Integer> files = new LinkedList<Integer>();
		List<Integer> changed = new LinkedList<Integer>();
		
		public Occurance(String value, int file)
		{
			this.value = value;
			files.add(file);
		}
		
		void add(int file)
		{
			files.add(file);
		}
		
		public void join(Occurance a)
		{
			changed.addAll(a.files);
			changed.addAll(a.changed);
			a.value = "";
			a.files.clear();
			a.changed.clear();
		}
		
		@Override
		public boolean equals(Object a)
		{
			return value.equals(((Occurance)a).value);
		}
	}

	ArrayList<Song> songs = new ArrayList<Song>();
	HashMap<String, Occurance> artists = new HashMap<String, Occurance>();
	HashMap<String, Occurance> genres = new HashMap<String, Occurance>();
	
    public Mp3TagFixer(File url) throws IOException
    {
    	System.out.println("MTF: hashing");
    	hashTags(getInitialFileList(url));
    	
    	System.out.println("MTF: finding joins");
    	ArrayList<Occurance> a = findJoins(artists.values().toArray());
    	ArrayList<Occurance> g = findJoins(genres.values().toArray());
    	
    	System.out.println("MTF: updating tags");
    	updateTags(a, FieldKey.ARTIST); // join similar artists together
    	updateTags(g, FieldKey.GENRE); // join similar genres together
    	
    	System.out.println("MTF: writing tags to file (this may take a moment)");
    	writeTags();
    	
    	System.out.println("MTF: Done, running folder fixer");
    	new FolderFixer(url);
    }
    
    private Queue<File> getInitialFileList(File url)
    {
    	Queue<File> fileQue = new LinkedList<File>();
    	
    	if(url.isFile())
    	{
    		try
    		{
	    		BufferedReader br = new BufferedReader(new FileReader(url));
	    		String line = "";
	    		
	    		while((line = br.readLine()) != null)
	    		{
	    			fileQue.add(new File(line));
	    		}
	    		
	    		br.close();
    		} catch(IOException e)
    		{
    			e.printStackTrace();
    		}
    	}
    	else
    	{
    		fileQue.addAll(Arrays.asList(url.listFiles()));
    	}

    	return fileQue;
    }
    
    private void hashTags(Queue<File> fileQue)
    {
        File potentialSong;
        AudioFile f;
        Tag tag;
        while((potentialSong = fileQue.poll()) != null)
        {
            if(potentialSong.isDirectory())
            {
                fileQue.addAll(Arrays.asList(potentialSong.listFiles()));
            }
            else if(potentialSong.getName().endsWith(".mp3"))
            {
                try 
                {
					f = AudioFileIO.read(potentialSong);
					tag = f.getTag();
					
					String artist = tag.getFirst(FieldKey.ARTIST);
					String album = tag.getFirst(FieldKey.ALBUM);
					String newArtist = fixArtist(artist);
					String title = tag.getFirst(FieldKey.TITLE);
					String newTitle = capitalize(title);
					String genre = tag.getFirst(FieldKey.GENRE);
					String newGenre = fixGenre(genre);
					
					boolean changed = !newArtist.equals(artist) || !newArtist.equals(album) || !newTitle.equals(title) || !newGenre.equals(genre);
					
					add(newArtist, newGenre, songs.size());
					songs.add(new Song(newArtist, newTitle, newGenre, f, changed));
				} catch (CannotReadException e) 
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
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
            }
        }
    }

    private void add(String artist, String genre, int file)
    {
		Occurance artistOcc = artists.get(artist);
		Occurance genreOcc = genres.get(genre);
		
		if(artistOcc == null)
		{
			artistOcc  = new Occurance(artist, file);
		}
		else
		{
			artistOcc.add(file);
		}
		artists.put(artist, artistOcc);
		
		if(genreOcc == null)
		{
			genreOcc  = new Occurance(genre, file);
		}
		else
		{
			genreOcc.add(file);
		}
		genres.put(genre, genreOcc);
    }
    
    private ArrayList<Occurance> findJoins(Object[] in)
    {
    	ArrayList<Occurance> joined = new ArrayList<Occurance>();

    	for(int x = 0; x < in.length; x++)
    	{
    		for(int y = x+1; y < in.length; y++)
    		{
    			if(isJoinable((Occurance)in[x], (Occurance)in[y]))
    			{
    				joined.add(join(((Occurance)in[x]), ((Occurance)in[y])));
    			}
    		}
    	}
    	
    	return joined;
    }

    private boolean isJoinable(Occurance a, Occurance b)
    {
    	return a.files.size() > 0 && b.files.size() > 0 && Levenshtein.fuzzyEquals(a.value, b.value);
    }
    
    private Occurance join(Occurance a, Occurance b)
    {
    	Occurance joiner = a;
    	Occurance joinee = b;

    	if(b.files.size() > a.files.size())
    	{
    		joiner = b;
    		joinee = a;
    	}
    	joiner.join(joinee);
    	
    	return joiner;
    }
    
    private void updateTags(ArrayList<Occurance> occs, FieldKey field)
    {
    	for(Occurance occ : occs)
    	{
    		for(int f : occ.changed)
    		{
    			try 
    			{
    				songs.get(f).setField(field, occ.value);
				} catch (KeyNotFoundException e) 
				{
					e.printStackTrace();
				}
    		}
    	}
    }
    
    private void writeTags()
    {
    	for(Song s : songs)
    	{
    		if(s.changed)
    		{
    			s.commit();
    		}
    	}
    }
	
    private String fixArtist(String in)
    {
        if(in.isEmpty())
        {
            in = "Unknown";
        }
        else
        {
	        String[] keyWords = {" ft", " Ft", " feat", " Feat", "/", ";", " - ", ", ", " ("};
	        for(String word : keyWords)
	        {
	        	int index = in.indexOf(word);
	            if(index > 0)
	            {
	        		in = in.substring(0, index);            		
	            }
	        }
        }
        
        return in.replaceAll("  ", " ").trim();
    }
    
    
    String[] tagGenres = new String[]{  "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", 
							    		"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", 
							    		"Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", 
							    		"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", 
							    		"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", 
							    		"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", 
							    		"AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", 
							    		"Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", 
							    		"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", 
							    		"Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", 
							    		"Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", 
							    		"Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", 
							    		"Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", 
							    		"Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", 
							    		"Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", 
							    		"Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", 
							    		"Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", 
							    		"Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", 
							    		"Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", 
							    		"Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", 
							    		"Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", 
							    		"Dance Hall" };
    
    Pattern p = Pattern.compile("\\([0-9]*\\)");
    
    /**
     * Maps String to potential genre id. If not available, return genre name 
     * @param in
     * @return
     */
    private String fixGenre(String in)
    {
    	if(!p.matcher(in).matches())
    	{
    		boolean found = false;
    		
    		for(int x = 0; x < tagGenres.length && !found; x++)
    		{
    			if(Levenshtein.fuzzyEquals(in, tagGenres[x]))
    			{
    				found = true;
    				in = "(" + x + ")";
    			}
    		}

    		if(!found) in = capitalize(in);
    	}
    	
    	return in.replaceAll("  ", " ").trim();
    }
    
    private String capitalize(String in)
    {
        boolean useCap = true;
        String out = "";
        
        for(char a : in.toCharArray())
        {
            if(useCap)
            {
                if(a != ' ' && a != '_') out += Character.toUpperCase(a);
                useCap = false;
            }
            else
            {
                out += Character.toLowerCase(a);
            }
            
            useCap = (a == ' ' || a == '_' || a == '(');
        }
        
        return out.replaceAll("  ", " ").trim();
    }

    public static void main(String[] args) throws IOException
    {
    	if(args.length != 1)
    	{
    		throw new RuntimeException("Must run with 1 arg: \n<music director or file containing paths to songs> ");
    	}
    	
        new Mp3TagFixer(new File(args[0]));
    }

}
