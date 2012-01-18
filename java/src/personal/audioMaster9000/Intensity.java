package personal.audioMaster9000;

public class Intensity
{
  private int frequency   = 0;
  private double leftEar  = 0.0;
  private double rightEar = 0.0;

  public double getFrequency()
  {
    return frequency;
  }

  public double getLeftEar()
  {
    return leftEar;
  }

  public double getRightEar()
  {
    return rightEar;
  }

  public void setFrequency(int frequency)
  {
    this.frequency = frequency;
  }

  public void setLeftEar(double leftEar)
  {
    this.leftEar += leftEar;
  }

  public void setRightEar(double rightEar)
  {
    this.rightEar += rightEar;
  }
}
