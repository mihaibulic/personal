package personal.audioMaster9000;

import java.util.Comparator;
import java.util.Random;

public class ToneComparator implements Comparator<Tone>
{
  private int     earSort   = 0;
  private int     freqSort  = 0;
  
  private Random  rand      = new Random();

  public ToneComparator(int earSort, int freqSort)
  {
    this.earSort  = earSort;  // 0: random   1: left->right  2: right->left
    this.freqSort = freqSort; // 0: random   1: low->high    2: high->low
  }

  @Override
  public int compare(Tone first, Tone second)
  {
    int comparison = 0;

    if (first.getLeftEar() != second.getLeftEar() && earSort > 0)
    {
      comparison = 1;
      if (first.getLeftEar() && earSort == 1 || second.getLeftEar() && earSort == 2)
      {
        comparison = -1;
      }
    }
    else
    {
      if (freqSort == 1)
      {
        comparison = first.getFrequency() > second.getFrequency() ? 1 : -1;
      }
      else if (freqSort == 2)
      {
        comparison = first.getFrequency() < second.getFrequency() ? 1 : -1;
      }
      else
      {
        comparison = rand.nextBoolean() ? 1 : -1;
      }
    }

    return comparison;
  }
}
