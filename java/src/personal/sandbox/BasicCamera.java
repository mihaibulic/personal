package personal.sandbox;

import java.awt.image.BufferedImage;
import java.io.IOException;

import april.jcam.ImageConvert;
import april.jcam.ImageSource;
import april.jcam.ImageSourceFormat;

public class BasicCamera
{
    public BasicCamera() throws IOException
    {
        for(String url : ImageSource.getCameraURLs())
        {
            if(url.startsWith("dc1394://"))
            {
                ImageSource isrc = ImageSource.make(url);
                ImageSourceFormat ifmt= isrc.getCurrentFormat();
                
                isrc.start();
                
                while(true)
                {
                    BufferedImage bf = ImageConvert.convertToImage(ifmt.format, ifmt.width, ifmt.height, isrc.getFrame());
                    System.out.print("*");
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        new BasicCamera();
    }

}
