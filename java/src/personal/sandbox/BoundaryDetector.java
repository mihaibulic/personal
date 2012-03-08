package personal.sandbox;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import april.jmat.LinAlg;
import april.jmat.geom.GRay3D;
import april.util.TimeUtil;
import april.vis.VisCanvas;
import april.vis.VisCanvas.RenderInfo;
import april.vis.VisChain;
import april.vis.VisEventHandler;
import april.vis.VisLayer;
import april.vis.VisWorld;
import april.vis.VzImage;

public class BoundaryDetector extends JFrame
{
    private final int framerate = 10;
    
    private volatile boolean imageSet = false;
    private volatile boolean colorSet = false;
    private BufferedImage im;
    private int color[];
    
    public BoundaryDetector(String loc) throws IOException
    {
        super("ARL: Boundry Detector");
        this.setLayout(new BorderLayout());
        this.setVisible(true);
        this.setSize(800, 600);

        JPanel jp = new JPanel(new BorderLayout());
        VisWorld vw = new VisWorld();
        VisLayer vl = new VisLayer(vw);
        VisCanvas vc = new VisCanvas(vl);
        VisWorld.Buffer vb = vw.getBuffer("iamges");
        
        vl.addEventHandler(new Click());
        
        jp.add(vc);
        this.add(jp);
        
        File dir = new File(loc);
        if(!dir.exists() || !dir.isDirectory()) throw new RuntimeException("Err: directory does not exist");
        
        File imFiles[] = dir.listFiles();
        Arrays.sort(imFiles);
        
        while(true)
        {
            for(int a = 0; a < imFiles.length; a++)
            {
                long time = System.currentTimeMillis();
                BufferedImage bufIm = ImageIO.read(imFiles[a]);
                im = bufIm;
                Pixel pxIm[][] = convert(bufIm);
                
                if(colorSet)
                {
                    boolean closed[][] = new boolean[bufIm.getWidth()][bufIm.getHeight()];
                    ArrayList< ArrayList<double[]> > blobs = new ArrayList< ArrayList<double[]> >();
                    
                    int w = bufIm.getWidth();
                    int h = bufIm.getHeight();
                    
                    for(short x = 0; x < w; x++)
                    {
                        for(short y = 0; y < h; y++)
                        {
                            if(!closed[x][y])
                            {
                                closed[x][y] = true;
                                if(pxIm[x][y].isGood(false))
                                {
                                    Queue<Pixel> pixels = new LinkedList<Pixel>();
                                    ArrayList<double[]> blob = new ArrayList<double[]>();
                                    pixels.add(pxIm[x][y]);
                                    blob.add(new double[] {x,y});
                                    
                                    while(!pixels.isEmpty())
                                    {
                                        Pixel p = pixels.remove();
                                        
                                        if(valid(p.x, p.y-1)) enque(pxIm[p.x][p.y-1], p, closed, pixels, blob);
                                        if(valid(p.x-1, p.y)) enque(pxIm[p.x-1][p.y], p, closed, pixels, blob);
                                        if(valid(p.x+1, p.y)) enque(pxIm[p.x+1][p.y], p, closed, pixels, blob);
                                        if(valid(p.x, p.y+1)) enque(pxIm[p.x][p.y+1], p, closed, pixels, blob);
                                    }

                                    if(blob.size() > 200)
                                        blobs.add(blob);
                                }
                            }
                        }
                    }
                    
                    for(ArrayList<double[]> b: blobs)
                    {
                        double line[] = LinAlg.fitLine(b);
                        
                        double start = b.get(0)[0];
                        double end = b.get(0)[0];
                        for(double[] p : b)
                        {
                            if(p[0] < start) start = p[0];
                            if(p[0] > end) end = p[0];
                        }
                        drawLine(start, end, line[0], line[1], bufIm);
                    }
                }

                vb.addBack(new VisChain(LinAlg.rotateX(Math.PI), new VzImage(bufIm)));
                vb.swap();
                
                int pause = (int)((1000.0/framerate) - (System.currentTimeMillis() - time));  
                if(pause > 0)
                {
                    TimeUtil.sleep(pause);
                }
                
                if(!imageSet)
                {
                    imageSet = true;
                    vl.cameraManager.fit2D(new double[] {0,-bufIm.getHeight()}, new double[] {bufIm.getWidth(), 0}, true);
                }
            }
        }
    }
    
    public void drawLine(double start, double end, double slope, double off, BufferedImage bufIm)
    {
        int c = (255 << 16) & 0xff0000;
        
        for(double x = start; x < end; x++)
        {
            bufIm.setRGB((int)x, (int)(x*slope+off)-1, c);
            bufIm.setRGB((int)x, (int)(x*slope+off), c);
            bufIm.setRGB((int)x, (int)(x*slope+off)+1, c);
        }
    }
    
    class Pixel
    {
        int c, x, y;
        int rgb[] = new int[3];
        int predRgb[] = new int[3];
        
        public Pixel(int x, int y, int c)
        {
            this.x = x;
            this.y = y;
            this.c = c;

            rgb[0] = ((c & 0xFF0000) >> 16);
            rgb[1] = ((c & 0x00FF00) >> 8);
            rgb[2] = (c & 0x0000FF);
        }
        
        boolean isGood(boolean compareWithPred)
        {
            int t1 = 15;
            int t2 = 25;
            
            if(compareWithPred)
            {
                return (Math.abs(rgb[0]-color[0])<= t1 && 
                        Math.abs(rgb[1]-color[1])<= t1 && 
                        Math.abs(rgb[2]-color[2])<= t1 &&
                        Math.abs(rgb[0]-predRgb[0])<= t2 && 
                        Math.abs(rgb[1]-predRgb[1])<= t2 && 
                        Math.abs(rgb[2]-predRgb[2])<= t2);
            }
            else
            {
                return (Math.abs(rgb[0]-color[0])<= t1 && 
                        Math.abs(rgb[1]-color[1])<= t1 && 
                        Math.abs(rgb[2]-color[2])<= t1);
            }
        }
        
        @Override
        public boolean equals(Object px)
        {
            return (x == ((Pixel)(px)).x && y == ((Pixel)(px)).y);
        }
        
        @Override
        public int hashCode()
        {
            return x*im.getWidth() + y;
        }
        
    }
    
    boolean valid(int x, int y)
    {
        return (x >=0 && x < im.getWidth() && y >= 0 && y < im.getHeight());
    }
    
    void enque(Pixel p, Pixel pred, boolean closed[][], Queue<Pixel> pixels, ArrayList<double[]> blob)
    {
        if(!closed[p.x][p.y])
        {
            p.predRgb = pred.rgb;
            if(p.isGood(true))
            {
                closed[p.x][p.y] = true;
                pixels.add(p);
                blob.add(new double[] {p.x, p.y});
            }
        }
    }
    
    BufferedImage convert(Pixel[][] i)
    {
        int w = i.length;
        int h = i[0].length;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        
        for(int x = 0; x < w; x++)
        {
            for(int y = 0; y < h; y++)
            {
                image.setRGB(x, y, i[x][y].c);
            }
        }
        
        return image;
    }
    
    Pixel[][] convert(BufferedImage i)
    {
        int w = i.getWidth();
        int h = i.getHeight();
        Pixel pxIm[][] = new Pixel[w][h]; 
        
        for(short x = 0; x < w; x++)
        {
            for(short y = 0; y < h; y++)
            {
                pxIm[x][y] = new Pixel(x, y, i.getRGB(x, y));
            }
        }
        
        return pxIm;
    }

    class Click implements VisEventHandler
    {
        @Override
        public int getDispatchOrder()
        {
            return 0;
        }

        @Override
        public boolean mousePressed(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            int loc[] = new int[] {(int)ray.intersectPlaneXY(0)[0], (int)(-ray.intersectPlaneXY(0)[1])};
            if(imageSet && loc[0] >= 0 && loc[0] < im.getWidth() && loc[1] >= 0 && loc[1] < im.getHeight())
            {
                int c = im.getRGB(loc[0], loc[1]);
                color = new int[] {(c & 0xFF0000) >> 16, (c & 0x00FF00) >> 8, (c & 0x0000FF)};
                colorSet = true;
            }
            
            return false;
        }

        @Override
        public boolean mouseReleased(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            return false;
        }

        @Override
        public boolean mouseClicked(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            return false;
        }

        @Override
        public boolean mouseDragged(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            return false;
        }

        @Override
        public boolean mouseMoved(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            return false;
        }

        @Override
        public boolean mouseWheel(VisCanvas vc, VisLayer vl, RenderInfo rinfo, GRay3D ray, MouseWheelEvent e)
        {
            return false;
        }

        @Override
        public boolean keyPressed(VisCanvas vc, VisLayer vl, RenderInfo rinfo, KeyEvent e)
        {
            return false;
        }

        @Override
        public boolean keyTyped(VisCanvas vc, VisLayer vl, RenderInfo rinfo, KeyEvent e)
        {
            return false;
        }

        @Override
        public boolean keyReleased(VisCanvas vc, VisLayer vl, RenderInfo rinfo, KeyEvent e)
        {
            return false;
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        if(args.length != 1) throw new RuntimeException("Err: enter directory for images");
        
        new BoundaryDetector(args[0]);
    }
}
