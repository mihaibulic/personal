package personal.audioMaster9000;

import java.util.Comparator;

public class IntensityComparator implements Comparator<Intensity>
{
  @Override
  public int compare(Intensity first, Intensity second)
  {
    int comparison = 0;

    if (first.getFrequency() < second.getFrequency())
    {
      comparison = -1;
    }
    else if (first.getFrequency() > second.getFrequency())
    {
      comparison = 1;
    }

    return comparison;
  }

}
