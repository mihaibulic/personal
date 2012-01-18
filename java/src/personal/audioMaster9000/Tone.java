package personal.audioMaster9000;

public class Tone
{
  private int frequency   = 0;
  private boolean leftEar = true;

  public Tone()
  {
  }

  public Tone(int frequency, boolean leftEar)
  {
    this.frequency = frequency;
    this.leftEar = leftEar;
  }

  public int getFrequency()
  {
    return frequency;
  }

  public boolean getLeftEar()
  {
    return leftEar;
  }

  public Tone getTone()
  {
    return new Tone(frequency, leftEar);
  }

  public void setFrequency(int frequency)
  {
    this.frequency = frequency;
  }

  public void setLeftEar(boolean leftEar)
  {
    this.leftEar = leftEar;
  }

  public void setTone(Tone tone)
  {
    frequency = tone.getFrequency();
    leftEar = tone.getLeftEar();
  }
}
