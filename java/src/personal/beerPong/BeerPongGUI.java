package personal.beerPong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import april.jmat.LinAlg;
import april.jmat.geom.GRay3D;
import april.util.ParameterGUI;
import april.util.ParameterListener;
import april.util.TimeUtil;
import april.vis.HelpOutput;
import april.vis.VisCanvas;
import april.vis.VisCanvasEventHandler;
import april.vis.VisChain;
import april.vis.VisCircle;
import april.vis.VisDataFillStyle;
import april.vis.VisDataLineStyle;
import april.vis.VisLineSegment;
import april.vis.VisRectangle;
import april.vis.VisWorld;

public class BeerPongGUI implements ParameterListener, Runnable
{
    /*
    class Template implements Runnable
    {
        private double x = 0;
        private double y = 0;
        
        public Template(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
        
        public void run()
        {
            VisWorld.Buffer   vbTemplate = vw.getBuffer("template");
            
        }
    }
     */

    class Ripples implements Runnable
    {
        private ArrayList<Double[]> points = new ArrayList<Double[]>(); // x, y, hue, sat, scale (0-maxRadius) 
        private VisWorld.Buffer   vbRipples = vw.getBuffer("Ripples");
        int maxRadius = Math.min(width, height)/4;
        
        public void run()
        {
            while(true)
            {
                TimeUtil.sleep(20);
                draw();
            }
        }
        
        /**
         * @param double x - x position (horizonal)
         * @param double y - y position (vertical)
         * @param int[] hs - hue and saturation value for ripple
         */
        public synchronized void draw(double x, double y, int[] hs)
        {
            points.add(new Double[] { x, y, (double)hs[0], (double)hs[1], 0.0, 20+55*(new Random()).nextDouble()});
        }

        /**
         * @param x - x position (horizonal)
         * @param y - y position (vertical)
         */
        public synchronized void draw(double x, double y)
        {
            points.add(new Double[] { x, y, 255*(new Random()).nextDouble(), 128+127*(new Random()).nextDouble(), 0.0, 20+55*(new Random()).nextDouble()});
        }
        
        public synchronized void draw()
        {
            VisChain chain = new VisChain();
            vbRipples.setDrawOrder(6);
            
            for(int a = 0; a < points.size(); a++)
            {
                while(points.size() > 0 && points.size() > a && points.get(a)[4] >= points.get(a)[5] )
                {
                    points.remove(a);
                }
                
                if(points.size()>0 && points.size() > a)
                {
                    Double[] point = points.get(a);
                    double thickness = (point[4] <= point[5]/10) ? (point[4]*2) : (0.22*(point[5]-(point[4])));
                    
                    int[][] rgb = {ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(90 + 100 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 100 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 90 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 80 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 95 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 125 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 155 * (thickness / 24))),
                            ColorSpaceConvert.HSVtoRGB((int)(point[2].doubleValue()), (int)(point[3].doubleValue()), (int)(100 + 135 * (thickness / 24)))} ;
                    
                    vbRipples.addBuffered(new VisChain(
                            LinAlg.translate(point[0], point[1]), 
                            new VisCircle(point[4] - (2 * (thickness / 24)),new VisDataLineStyle(new Color(rgb[0][0], rgb[0][1], rgb[0][2]), thickness / 5, true)), 
                            new VisCircle(point[4] - (thickness), new VisDataLineStyle(new Color(rgb[1][0], rgb[1][1], rgb[1][2]), thickness / 5, true)), 
                            new VisCircle(point[4], new VisDataLineStyle(new Color(rgb[2][0], rgb[2][1], rgb[2][2]), thickness / 5, true)), 
                            new VisCircle(point[4] + (thickness / 5), new VisDataLineStyle(new Color(rgb[3][0], rgb[3][1], rgb[3][2]), thickness / 5, true)), 
                            new VisCircle(point[4] + (2 * thickness / 5), new VisDataLineStyle(new Color(rgb[4][0], rgb[4][1], rgb[4][2]), thickness / 5, true)), 
                            new VisCircle(point[4] + (3 * thickness / 5), new VisDataLineStyle(new Color(rgb[5][0], rgb[5][1], rgb[5][2]), thickness / 5, true)), 
                            new VisCircle(point[4] + (4 * thickness / 5), new VisDataLineStyle(new Color(rgb[6][0], rgb[6][1], rgb[6][2]), thickness / 5, true)), 
                            new VisCircle(point[4] + (1.5 * (thickness / 24)), new VisDataLineStyle(new Color(rgb[7][0], rgb[7][1], rgb[7][2]), thickness / 5, true))));
                    
                    point[4]++;
                    points.set(a, point);
                }
            }        
            vbRipples.addBuffered(chain);
            vbRipples.switchBuffer();
        }
    }
    
    class Dots implements Runnable
    {
        private VisWorld.Buffer   vbDots = vw.getBuffer("Dots");
        private ArrayList<Double[]> points = new ArrayList<Double[]>(); 
        private int[] rgb = {0,0,0};
        public boolean connect = false;
        public boolean circles = true;
        private double rotate = 0;
        
        public void run()
        {
            while(true)
            {
                TimeUtil.sleep(100);
                draw(0.1);
            }
        }
        
        /**
         * @param x - x position (horizonal)
         * @param y - y position (vertical)
         * @param connect - true if you want points to be connected
         * @param circles - true if you want the points to be cirlces, false if you want rotating squares
         */
        public synchronized void draw(double x, double y, boolean connect, boolean circles)
        {
            this.connect = connect;
            this.circles = circles;
            points.add(new Double[] { x, y, 10. });
            draw(0.25);
        }
        
        public synchronized void draw(double subtract)
        {
            VisChain chain = new VisChain();
            vbDots.setDrawOrder(3);
            int hue = (new Random()).nextInt(255);
            rotate=(rotate+(Math.PI/10))%(2*Math.PI);
            
            for(int a = 0; a < points.size(); a++)
            {
                rgb = ColorSpaceConvert.HSVtoRGB(hue, 255, 127+a*(127/points.size()));
                
                if((int)((points.get(a)[2]-subtract)/2.) > 0)
                {
                    Double[] dub = new Double[] {points.get(a)[0], points.get(a)[1], points.get(a)[2]-subtract};
                    points.set(a, dub);
                }
                else
                {
                    points.remove(a);
                }
                
                if(points.size()>0)
                {
                    
                    if(a == 0)
                    {
                        chain.add(LinAlg.translate((points.get(a))[0], (points.get(a))[1]));
                    }
                    
    
                    if(circles)
                    {
                        chain.add(new VisCircle(points.get(a)[2], new VisDataFillStyle(new Color((int)(rgb[0]),(int)(rgb[1]),(int)(rgb[2])))));
                    }
                    else
                    {
                        chain.add(  LinAlg.rotateZ(rotate),
                                    new VisRectangle(new double[] {-points.get(a)[2],-points.get(a)[2],0}, new double[] {points.get(a)[2],points.get(a)[2],0}, 
                                        new VisDataFillStyle(new Color((int)(rgb[0]),(int)(rgb[1]),(int)(rgb[2])))),
                                    LinAlg.rotateZ(-rotate));
                    }
                    
                    if(a+1 < points.size())
                    {
                        if(connect)
                        {
                            chain.add(new VisLineSegment(0,0,0, (points.get(a+1))[0]-(points.get(a))[0], (points.get(a+1))[1]-(points.get(a))[1], 0,
                                    new Color((int)(rgb[0]),(int)(rgb[1]),(int)(rgb[2])), (int)(points.get(a)[2]/2.)), 
                                    LinAlg.translate((points.get(a+1))[0]-(points.get(a))[0], (points.get(a+1))[1]-(points.get(a))[1]));
                        }
                        else
                        {
                            chain.add(LinAlg.translate((points.get(a+1))[0]-(points.get(a))[0], (points.get(a+1))[1]-(points.get(a))[1]));
                        }
                    }
                }                
            }                
            vbDots.addBuffered(chain);
            vbDots.switchBuffer();
        }
    }
     
    class Move implements Runnable
    {
        private double x = 0;
        private double y = 0;
        
        public Move(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
        
        public void run()
        {
            VisWorld.Buffer   vbMove = vw.getBuffer("Move");
            vbMove.setDrawOrder(2);

            vbMove.addBuffered(new VisChain(
                    LinAlg.translate(x, y), new VisCircle(5, new VisDataFillStyle(Color.WHITE)), 
                    LinAlg.translate(-x, -y)));
            vbMove.switchBuffer();
        }
    }

    class Click implements VisCanvasEventHandler
    {
        public double pickQuery(VisCanvas vc, GRay3D ray) {return 1.0;}
        public double hoverQuery(VisCanvas vc, GRay3D ray) { return 1; }
        public void pickNotify(boolean winner) {}
        public void hoverNotify(boolean winner) {}
        public boolean keyTyped(VisCanvas vc, KeyEvent e) { return false; }
        public boolean keyPressed(VisCanvas vc, KeyEvent e) { return false; }
        public boolean keyReleased(VisCanvas vc, KeyEvent e) { return false; }
        public boolean mouseWheelMoved(VisCanvas vc, GRay3D ray, MouseWheelEvent e) { return true; }
        public boolean mouseClicked(VisCanvas vc, GRay3D ray, MouseEvent e) { return true; }
        public boolean mouseReleased(VisCanvas vc, GRay3D ray, MouseEvent e) { return false; }
        public boolean mouseDragged(VisCanvas vc, GRay3D ray, MouseEvent e) { return true; }
        public String getName() { return ""; }
        public void doHelp(HelpOutput houts) {}
        
        public boolean mousePressed(VisCanvas vc, GRay3D ray, MouseEvent e)
        {
            double x = ray.intersectPlaneXY(0)[0];
            double y = ray.intersectPlaneXY(0)[1];
            
            if(pg.gb("ripples"))
            {
                if(pg.gb("random"))
                {
                    ripples.draw(x,y);
                }
                else
                {
                    ripples.draw(x,y,hsColors[pg.gi("background")]);
                }
            }
            if(pg.gb("dots"))
            {
                dots.draw(x, y, pg.gb("connect"), pg.gb("circles"));
            }
            
            return true;
        }
        
        public boolean mouseMoved(VisCanvas vc, GRay3D ray, MouseEvent e)
        {
            double x = ray.intersectPlaneXY(0)[0];
            double y = ray.intersectPlaneXY(0)[1];
            
            (new Thread(new Move(x, y))).start();
            
            return true;
        }
    }
    
    // GUI
    private ParameterGUI      pg        = null;
    
    // Image
    private int               width     = 752;
    private int               height    = (int)(752*(1.5/7.5));
    
    // Vis
    private VisWorld                 vw = new VisWorld();
    private VisCanvas                vc = new VisCanvas(vw);
    private Color[]              colors = {Color.black, Color.white, new Color(100,0,0), new Color(0,100,0), new Color(0,0,100), Color.black};
    private int[][]            hsColors = {{0,0},{0,0},{0,255},{85,255},{170,255},{0,0}};
    
    // Visualizations
    private Dots dots = new Dots();
    private Ripples ripples = new Ripples();
    
    public BeerPongGUI()
    {
        /* add parameter GUI compentents below */
        pg = new ParameterGUI();
        pg.addChoice("background", "Background color", new String[] {"Black", "White", "Red", "Green", "UM Blue", "Custom"}, 4);
        pg.addBoolean("ripples","Ripples",true);
        pg.addBoolean("random","Random Ripple colors?",false);
        pg.addBoolean("dots","Dots",false);
        pg.addBoolean("connect","Connect Dots?",false);
        pg.addBoolean("circles","Draw Circles?",false);
        pg.addIntSlider("customRed", "Red", 0, 255, 0);
        pg.addIntSlider("customGreen", "Green", 0, 255, 0);
        pg.addIntSlider("customBlue", "Blue", 0, 255, 0);
        pg.addListener(this);
        /* add parameter GUI compentents above */
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        
        /* add JFrame compentents below */
        JFrame displayFrame = new JFrame(gs[0].getDefaultConfiguration());
        displayFrame.setLayout(new BorderLayout());
        displayFrame.add(vc, BorderLayout.CENTER);
        displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        displayFrame.setSize(10000, 1000);
        displayFrame.setVisible(true);
        
        JFrame guiFrame = new JFrame("Beer Pong GUI");
        guiFrame.setLayout(new BorderLayout());
        guiFrame.add(pg, BorderLayout.CENTER);
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.pack();
        guiFrame.setVisible(true);
        /* add JFrame compentents above */
        
        /* add vis components below */
        vc.addEventHandler(new Click());
        vc.getViewManager().viewGoal.fit2D(new double[] { 0, 0 }, new double[] { width, height });
        vc.setBackground(colors[pg.gi("background")]);
        
        /* add vis components above */
     
        run();
    }
    
    public void parameterChanged(ParameterGUI pg, String name)
    {
        if(name.equals("background"))
        {
            vc.setBackground(colors[pg.gi("background")]);
        }
        if((name.equals("customRed") || name.equals("customGreen") || name.equals("customBlue") ) && pg.gi("background") == 5)
        {
            vc.setBackground(new Color(pg.gi("customRed"), pg.gi("customGreen"), pg.gi("customBlue")));
            
            int hsv[] = ColorSpaceConvert.RGBtoHSV( (pg.gi("customRed") <= 235 ? pg.gi("customRed") + 20 : 0),
                                                    (pg.gi("customGreen") <= 235 ? pg.gi("customGreen") + 20 : 0),
                                                    (pg.gi("customBlue") <= 235 ? pg.gi("customBlue") + 20 : 0) );
            
            hsColors[5][0] = hsv[0];
            hsColors[5][1] = hsv[1];
        }
    }
    
    public void run()
    {
        (new Thread(dots)).start();
        (new Thread(ripples)).start();
     
        VisWorld.Buffer vbBackground = vw.getBuffer("Background");
        vbBackground.setDrawOrder(10);
        vbBackground.addBuffered(new VisChain (
              new VisRectangle(new double[] { -600, -600}, new double[] { 0, height+600 }, new VisDataFillStyle(Color.white)) ,
              new VisRectangle(new double[] { 0, height }, new double[] { width+600, height+600 }, new VisDataFillStyle(Color.white)),
              new VisRectangle(new double[] { 0, -600 }, new double[] { width+600, 0 }, new VisDataFillStyle(Color.white)),
              new VisRectangle(new double[] { width, -600 }, new double[] { width+600, height }, new VisDataFillStyle(Color.white))
        ));
        vbBackground.switchBuffer();
    }
    
    public static void main(String args[])
    {
        new BeerPongGUI();
    }
}
