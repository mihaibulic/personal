package personal.audioMaster9000;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ResultsHandler
{
  public ResultsHandler(ArrayList<Intensity> intensities, String location, SettingManager settingManager)
  {
    java.util.Date today  = new java.util.Date();
    String output         = new java.sql.Timestamp(today.getTime()) + "\t";

    IntensityComparator compare = new IntensityComparator();
    Collections.sort(intensities, compare);

    if (settingManager.getIterations() > 1)
    {
      for (int x = 0; x < intensities.size(); x++)
      {
        intensities.get(x).setLeftEar(intensities.get(x).getLeftEar() / settingManager.getIterations());
        intensities.get(x).setRightEar(intensities.get(x).getRightEar() / settingManager.getIterations());
      }
    }

    for (int x = 0; x < intensities.size(); x++)
    {
      output += Math.round(intensities.get(x).getLeftEar()) + "\t";
    }
    output += new java.sql.Timestamp(today.getTime()) + "\t";
    for (int x = 0; x < intensities.size(); x++)
    {
      output += Math.round(intensities.get(x).getRightEar());
      if(intensities.size()>x+1)
      {
    	  output += "\t";
      }
    }

    try
    {
      File file             = new File(location + "/output.txt");
      BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

      writer.write(output);
      writer.newLine();
      writer.flush();
      writer.close();
    } catch (IOException e)
    {
      System.err.println(e);
      System.exit(1);
    }
  }
}
