package personal.sandbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import april.jcam.ImageConvert;
import april.jcam.ImageSource;
import april.jcam.ImageSourceFormat;
import april.jmat.LinAlg;
import april.jmat.geom.GRay3D;
import april.util.ParameterGUI;
import april.util.ParameterListener;
import april.vis.VisCanvas;
import april.vis.VisChain;
import april.vis.VisEventAdapter;
import april.vis.VisLayer;
import april.vis.VisPixelCoordinates;
import april.vis.VisTexture;
import april.vis.VisWorld;
import april.vis.VzImage;
import april.vis.VzLines;
import april.vis.VzRectangle;
import april.vis.VzText;

public class TemplateMatch implements ParameterListener
{
    JFrame jf;

    BufferedImage template = null;
    BufferedImage lastImage;

    VisWorld vw = new VisWorld();
    VisLayer vl = new VisLayer(vw);
    VisCanvas vc = new VisCanvas(vl);

    ParameterGUI pg = new ParameterGUI();

    ImageSource isrc;

    TemplateMethod tm = new MyTemplateMethod();

    MyEventHandler eventHandler = new MyEventHandler();

    public TemplateMatch(ImageSource isrc)
    {
        this.isrc = isrc;

        pg.addIntSlider("templatesize", "display size of template", 0, 400, 200);

        jf = new JFrame("TemplateMatch");
        jf.setLayout(new BorderLayout());
        jf.add(vc, BorderLayout.CENTER);
        jf.add(pg, BorderLayout.SOUTH);

        jf.setSize(600,400);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        vl.addEventHandler(eventHandler);
        pg.addListener(this);

        new RunThread().start();
    }

    public void parameterChanged(ParameterGUI pg, String name)
    {
        if (name.equals("templatesize"))
            updateTemplatePreview();
    }

    synchronized void setTemplate(BufferedImage image)
    {
        template = image;
        updateTemplatePreview();
    }

    synchronized void updateTemplatePreview()
    {
        double scale = Math.min(1.0 * pg.gi("templatesize") / template.getWidth(),
                                1.0 * pg.gi("templatesize") / template.getHeight());

        VisWorld.Buffer vb = vw.getBuffer("template");
        vb.addBack(new VisPixelCoordinates(VisPixelCoordinates.ORIGIN.TOP_LEFT,
                                           LinAlg.scale(scale, scale, 1),
                                           LinAlg.translate(0, -template.getHeight(), 0),
                                           new VzImage(new VisTexture(template, VisTexture.NO_MIN_FILTER), VzImage.FLIP)));
        vb.swap();
    }

    class MyEventHandler extends VisEventAdapter
    {
        int x0, y0, x1, y1;

        boolean grabbing = false;

        /** Return true if you've consumed the event. **/
        public boolean mousePressed(VisCanvas vc, VisLayer vl, VisCanvas.RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            int mods = e.getModifiersEx();

            boolean ctrl = (mods & MouseEvent.CTRL_DOWN_MASK) > 0;
            if (!ctrl)
                return false;

            grabbing = true;

            double xy[] = ray.intersectPlaneXY(0);
            x0 = (int) Math.round(xy[0]);
            y0 = (int) Math.round(xy[1]);
            x1 = x0;
            y1 = y0;

            selectionRectangleUpdate();

            return true;
        }

        public boolean mouseDragged(VisCanvas vc, VisLayer vl, VisCanvas.RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            if (!grabbing)
                return false;

            double xy[] = ray.intersectPlaneXY(0);
            x1 = (int) Math.round(xy[0]);
            y1 = (int) Math.round(xy[1]);

            selectionRectangleUpdate();

            return true;
        }

        public boolean mouseReleased(VisCanvas vc, VisLayer vl, VisCanvas.RenderInfo rinfo, GRay3D ray, MouseEvent e)
        {
            if (!grabbing)
                return false;

            double xy[] = ray.intersectPlaneXY(0);
            x1 = (int) Math.round(xy[0]);
            y1 = (int) Math.round(xy[1]);

            selectionRectangleUpdate();

            if (lastImage == null)
                return false;

            if (x1 < x0) {
                int t = x0;
                x0 = x1;
                x1 = t;
            }

            y0 = lastImage.getHeight() - y0;
            y1 = lastImage.getHeight() - y1;

            if (y1 < y0) {
                int t = y0;
                y0 = y1;
                y1 = t;
            }

            x0 = Math.max(0, x0);
            y0 = Math.max(0, y0);
            x1 = Math.min(lastImage.getWidth(), x1);
            y1 = Math.min(lastImage.getHeight(), y1);

            int twidth = x1 - x0, theight = y1 - y0;

            if (twidth <= 0 || theight <= 0)
                return false;

            BufferedImage newTemplate = new BufferedImage(twidth, theight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = newTemplate.createGraphics();
            g.drawImage(lastImage,
                        0, 0, newTemplate.getWidth(), newTemplate.getHeight(),
                        x0, y0, x1, y1,
                        null);
            g.dispose();

            setTemplate(newTemplate);

            selectionRectangleClear();

            grabbing = false;

            return true;
        }

        void selectionRectangleClear()
        {
            VisWorld.Buffer vb = vw.getBuffer("template selection");
            vb.swap();
        }

        void selectionRectangleUpdate()
        {
            VisWorld.Buffer vb = vw.getBuffer("template selection");
            vb.addBack(new VisChain(LinAlg.translate(x0, y0),
                                    LinAlg.scale(x1-x0, y1-y0, 1),
                                    LinAlg.scale(.5, .5, 0),
                                    LinAlg.translate(1, 1, 0),
                                    new VzRectangle(new VzLines.Style(Color.cyan, 3))));
            vb.swap();
        }
    }

    class RunThread extends Thread
    {
        boolean firstframe = true;

        public void run()
        {
            ImageSourceFormat ifmt = isrc.getCurrentFormat();

            isrc.start();

            while (true) {
                byte imbuf[] = isrc.getFrame();
                BufferedImage im = ImageConvert.convertToImage(ifmt.format, ifmt.width, ifmt.height, imbuf);

                if (true) {
                    VisWorld.Buffer vb = vw.getBuffer("image");
                    vb.addBack(new VzImage(new VisTexture(im, VisTexture.NO_MIN_FILTER), VzImage.FLIP));
                    vb.swap();
                }

                lastImage = im;

                if (firstframe) {
                    firstframe = false;
                    vl.cameraManager.fit2D(new double[] { 0, 0 },
                                           new double[] { im.getWidth(), im.getHeight() },
                                           true);
                }

                // protect against races that affect the
                // template. (specifically, the user manually
                // specifying a new template while we learn a new one.)
                synchronized(TemplateMatch.this) {

                    if (template != null) {
                        long time0 = System.nanoTime();
                        double txy[] = tm.find(im, template);

                        long time1 = System.nanoTime();

                        if (true) {
                            VisWorld.Buffer vb = vw.getBuffer("match");
                            if (txy != null)
                                vb.addBack(new VisChain(LinAlg.translate(txy[0], im.getHeight() - txy[1]),
                                                        LinAlg.scale(template.getWidth(), -template.getHeight(), 1),
                                                        LinAlg.scale(.5, .5, 0),
                                                        LinAlg.translate(1, 1, 0),
                                                        new VzRectangle(new VzLines.Style(Color.yellow, 3))));
                            vb.swap();
                        }

                        if (true) {
                            VisWorld.Buffer vb = vw.getBuffer("timing");
                            vb.addBack(new VisPixelCoordinates(VisPixelCoordinates.ORIGIN.BOTTOM_LEFT,
                                                               new VzText(VzText.ANCHOR.BOTTOM_LEFT,
                                                                          String.format("<<cyan>>sz = (%d x %d) %d px   time = %.2f ms",
                                                                                        template.getWidth(), template.getHeight(),
                                                                                        template.getWidth() * template.getHeight(),
                                                                                        (time1 - time0) / 1000000.0))));

                            vb.swap();

                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws IOException
    {
        ArrayList<String> urls = ImageSource.getCameraURLs();
        String url = urls.get(0);

        if (args.length > 0)
            url = args[0];

        System.out.println(url);

        ImageSource isrc = ImageSource.make(url);

        new TemplateMatch(isrc);
    }

    interface TemplateMethod
    {
        public double[] find(BufferedImage src, BufferedImage template);
    }

    public static class MyTemplateMethod implements TemplateMethod
    {
        public final double[] find(BufferedImage _src, BufferedImage _template)
        {
            int src[] = ((DataBufferInt) (_src.getRaster().getDataBuffer())).getData();
            int template[] = ((DataBufferInt) (_template.getRaster().getDataBuffer())).getData();
            int twidth = _template.getWidth(), theight = _template.getHeight();
            int srcwidth = _src.getWidth(), srcheight = _src.getHeight();

            int besty = 0, bestx = 0;

            double best = Double.MAX_VALUE;
            for(int x = 0; x < srcwidth; x+=3)
            {
                for(int y = 0; y < srcheight; y+=3)
                {
                    if(x+twidth < srcwidth && y + theight < srcheight)
                    {
                        double err = compare(src, srcwidth, x, y, template, twidth, theight);
                        if(err < best && err < 3*Math.pow(10, 10))
                        {
                            best = err; 
                            besty = y;
                            bestx = x;
                        }
                    }
                }
            }
            System.out.println(best);

            return new double[] { bestx, besty };
        }
        
        double compare(int[] im, int width, int x, int y, int[] temp, int twidth, int theight)
        {
            double err = 0.0;
            
            for(int xx=0; xx < twidth; xx+=3)
            {
                for(int yy=0; yy<theight; yy+=3)
                {
                    err += compare(getRGB(im, x+xx, y+yy, width), getRGB(temp, xx, yy, twidth));
                }
            }
            
            return err/(twidth*theight);
        }
        
        int getRGB(int[] im, int x, int y, int w)
        {
            return im[y*w+x];
        }
        
        double compare(int rgb1, int rgb2)
        {
            int r1 = rgb1 & 0xff0000 << 16;
            int g1 = rgb1 & 0x00ff00 << 8;
            int b1 = rgb1 & 0x0000ff;
            int r2 = rgb2 & 0xff0000 << 16;
            int g2 = rgb2 & 0x00ff00 << 8;
            int b2 = rgb2 & 0x0000ff;
            
            return (sq(r1-r2)+sq(g1-g2)+sq(b1-b2));
        }
        
        double sq(double a)
        {
            return a*a;
        }
    }
}
