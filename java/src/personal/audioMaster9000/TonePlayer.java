package personal.audioMaster9000;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.util.*;

public class TonePlayer implements Runnable
{
  private String          location              = "";
  private double          configConstant        = 0.0;
  private int[]           availableFrequencies  = { 125, 250, 500, 1000, 2000, 4000, 8000 };
  private Tone            tone                  = new Tone();
  private ArrayList<Tone> tones                 = new ArrayList<Tone>();
  private String          toneString            = "";
  private Clip            clip;
  private boolean         isStarted             = false;
  private static final int MAX_WAIT_BEFORE_TONE = 4500; // in milliseconds
  private static final int TONE_DURATION        = 1500; // in milliseconds
  private static final int PAUSE_BTWN_TONES     = 750;  // in milliseconds
  private double          volume                = -80;
  private static final int NORMAL_INCRIMENT     = 5;
  private static final int PRECISE_INCRIMENT    = 1;
  private int             incriment             = NORMAL_INCRIMENT;
  private boolean         precise               = false;
  private boolean         kill                  = false;
  
  private SettingManager  settingManager;
  private Random          rand                  = new Random();
  
  public TonePlayer(String location, SettingManager settingManager)
  {
    this.location = location;
    this.settingManager = settingManager;
  }

  public void addTones()
  {
    for (int y = 0; y < settingManager.getIterations(); y++)
    {
      for (int x = 0; x < availableFrequencies.length; x++)
      {
        tones.add(new Tone(availableFrequencies[x], true));
        tones.add(new Tone(availableFrequencies[x], false));
      }
    }

    ToneComparator compare = new ToneComparator(settingManager.getEars(), settingManager.getFrequency());
    Collections.sort(tones, compare);
  }

  public void changeVolume(double changeValue)
  {
    volume = getComputerIntensity(getActualIntensity(volume) + changeValue);
  }
  
  public double getActualIntensity(double computerVolume)
  {
    return Math.log10((configConstant) * Math.pow(10, computerVolume));
  }

  public double getComputerIntensity(double actualVolume)
  {
    return Math.log10((1./configConstant) * Math.pow(10, actualVolume));
  }

  public int getFrequency()
  {
    return tone.getFrequency();
  }

  public boolean getLeftEar()
  {
    return tone.getLeftEar();
  }

  public Tone getTone()
  {
    return tone;
  }

  public double getVolume()
  {
    return volume;
  }

  public boolean isFinished()
  {
    boolean isFinished = false;

    if (tones.isEmpty())
    {
      isFinished = true;
    }

    return isFinished;
  }

  public void kill()
  {
    reset();
    kill = true;
  }

  public void playToneTWO()
  {
    try
    {
      File soundFile = new File(location + "/frequencies/" + toneString);
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
      clip = AudioSystem.getClip();
      clip.open(audioInputStream);
      FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      gainControl.setValue((float) gainControl.getMinimum());
      clip.start();
      clip.stop();
      Thread.sleep((long) (rand.nextDouble() * 4000));

      while (clip.isOpen() && isStarted)
      {
        gainControl.setValue((float) volume);
        clip.setMicrosecondPosition(0);
        clip.start();
        Thread.sleep(2000);
        clip.stop();
        changeVolume(incriment);
        Thread.sleep(500);
      }
    } catch (Exception e)
    {
      System.err.println(e);
      System.out.println("line: TonePlayer 104");
    }
    changeVolume(-incriment);
  }

  public void playTone()
  {
    try
    {      
      File soundFile = new File(location + "/frequencies/" + toneString);
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
      clip = AudioSystem.getClip();
      clip.open(audioInputStream);
      FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      
      volume = gainControl.getMinimum();
      double pause = rand.nextDouble() * MAX_WAIT_BEFORE_TONE;
      boolean isRunning = false;
      double time = System.currentTimeMillis();
      
      while (clip.isOpen() && isStarted)
      {  
        if(System.currentTimeMillis() >= (pause + time))
        {
          if(isRunning)
          {
            isRunning=false;
            changeVolume(incriment);
            pause = PAUSE_BTWN_TONES;
            clip.stop();
          }
          else
          {
            isRunning = true;
            gainControl.setValue((float) (volume));
            pause = TONE_DURATION;
            clip.setMicrosecondPosition(0);
            clip.start();
          }
          time = System.currentTimeMillis();
        }
      }
    } catch (Exception e)
    {
      System.err.println(e);
      System.out.println("line: TonePlayer 104");
    }
    
    clip.close();
    changeVolume(-incriment);
  }

  
  public boolean precision()
  {
    precise = !precise && settingManager.getPrecision() ? true : false;

    if (precise)
    {
      changeVolume(-incriment);
      incriment = PRECISE_INCRIMENT;
    }
    else
    {
      incriment = NORMAL_INCRIMENT;
    }

    return precise;
  }

  public void reset()
  {
    precise = false;
    incriment = NORMAL_INCRIMENT;
    tones.clear();
  }
  
  public void run()
  {
    try
    {
      while (!kill)
      {
        if (isStarted)
        {
          playTone();
        }
      }
    } catch (Exception e)
    {
      System.err.println(e);
      System.out.println("line: TonePlayer 44");
    }
  }

  public Tone selectTone()
  {
    tone = tones.get(0);
    tones.remove(0);
    toneString = tone.getFrequency() + "";

    if (tone.getLeftEar())
    {
      toneString += "L.wav";
    }
    else
    {
      toneString += "R.wav";
    }

    volume = getComputerIntensity(0);
    
    return tone;
  }
  
  public void selectTone(int frequency, boolean leftEar)
  {
    toneString = frequency + "";
    configConstant = Math.pow(10, -100);
    
    if (leftEar)
    {
      toneString += "L.wav";
    }
    else
    {
      toneString += "R.wav";
    }
  }
  
  public void setConfigConstant(double constant)
  {
    configConstant = constant;
  }

  public void setTone(boolean isStarted)
  {
    if(!isStarted && clip != null)
    {
      clip.stop();
    }
    this.isStarted = isStarted;
  }
  
  public void soundWasHeard()
  {
	setTone(false);
	  
    if (clip != null)
    {
      clip.close();
    }
  }
}
