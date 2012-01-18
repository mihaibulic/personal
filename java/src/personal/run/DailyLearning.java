package personal.run;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import april.util.GetOpt;

public class DailyLearning implements ActionListener, HyperlinkListener
{
    File source;
    int current = 0;
    int length = 0;
    ArrayList<String> definitions = new ArrayList<String>();
    JFrame frame;
    JPanel controls = new JPanel();
    JEditorPane definition = new JEditorPane();
    JButton next = new JButton(new StringBuilder("Next (").append(current).append(")").toString());
    JButton back = new JButton("Back");

    public DailyLearning(File source)
    {
        try
        {
            Thread.sleep(5000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        this.source = source;
        try
        {
            FileReader fr = new FileReader(source);
            BufferedReader reader = new BufferedReader(fr);
            while (reader.readLine() != null)
                length++;
            definition.setPage(getDefinition());
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        frame = new JFrame("DailyLearning v0.2");
        frame.setLayout(new BorderLayout());
        back.setVisible(false);
        back.setActionCommand("back");
        back.addActionListener(this);
        next.setActionCommand("next");
        next.addActionListener(this);
        definition.setContentType("text/html");
        definition.setEditable(false);
        definition.addHyperlinkListener(this);
        frame.getContentPane().add(new JScrollPane(definition), "Center");
        controls.add(back, "West");
        controls.add(next, "East");
        frame.add(controls, "South");
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private String getDefinition()
    {
        String definition = "";
        Random rand = new Random();
        try
        {
            FileReader fr = new FileReader(source);
            BufferedReader reader = new BufferedReader(fr);
            int index = rand.nextInt(length);
            for (int x = 0; x < index; x++)
                reader.readLine();
            definition = reader.readLine();
            definitions.add(current, definition);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return definition;
    }

    public static void main(String[] args)
    {
        GetOpt opts = new GetOpt();
        opts.addBoolean('h', "help", false, "See this help screen");
        opts.addString('f', "filepath", "/var/tmp/dailyLearning", "default filepath to find definitions database");
        if (!opts.parse(args))
            System.out.println(new StringBuilder("option error: ").append(opts.getReason()).toString());
        new DailyLearning(new File(opts.getString("filepath")));
    }

    public void actionPerformed(ActionEvent event)
    {
        if (next.getActionCommand().equals(event.getActionCommand()))
        {
            current++;
            try
            {
                definition.setPage(current >= definitions.size() ? getDefinition() : (String) definitions.get(current));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (back.getActionCommand().equals(event.getActionCommand()))
        {
            current--;
            try
            {
                definition.setPage((String) definitions.get(current));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        next.setText(new StringBuilder("Next (").append(current).append(")").toString());
        back.setVisible(current > 0);
    }

    public void hyperlinkUpdate(HyperlinkEvent event)
    {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED)
        {
            if (event instanceof HTMLFrameHyperlinkEvent)
            {
                HTMLFrameHyperlinkEvent linkEvent = (HTMLFrameHyperlinkEvent) event;
                HTMLDocument document = (HTMLDocument) definition.getDocument();
                document.processHTMLFrameHyperlinkEvent(linkEvent);
            }
            else
            {
                try
                {
                    definition.setPage(event.getURL());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
