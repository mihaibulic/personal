package personal.audioMaster9000;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SettingManager
{
  private File              file;
  private BufferedReader    reader;
  private ArrayList<Double> settings = new ArrayList<Double>();
  private BufferedWriter    writer;

  public SettingManager(String location)
  {
    try
    {
      file    = new File(location + "/settings.txt");
      reader  = new BufferedReader(new FileReader(file));

      String[] raw = reader.readLine().split(" ");

      for (int x = 0; x < raw.length; x++)
      {
        settings.add(Double.parseDouble(raw[x]));
      }

      reader.close();
    } catch (IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }
  }

  public double getConfig()
  {
    return (settings.get(4));
  }

  public int getEars()
  {
    return (int) (double)(settings.get(0));
  }

  public int getFrequency()
  {
    return (int) (double)(settings.get(1));
  }

  public int getIterations()
  {
    return (int) (double)(settings.get(2));
  }

  public boolean getPrecision()
  {
    return (settings.get(3) == 1.0 ? true : false);
  }

  public ArrayList<Double> getSettings()
  {
    return settings;
  }

  public void kill()
  {
    try
    {
      reader.close();
      writer.close();
    } catch (IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }
  }

  public void setConfig(double config)
  {
    settings.set(4, config);
    setSettings(settings);
  }

  public void setSettings(ArrayList<Double> settings)
  {
    for (int x = 0; x < settings.size(); x++)
    {
      this.settings.set(x, settings.get(x));
    }
    try
    {
      writer = new BufferedWriter(new FileWriter(file));
      for (int x = 0; x < settings.size(); x++)
      {
        writer.write(settings.get(x) + " ");
      }
      writer.close();
    } catch (IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }
  }
}
