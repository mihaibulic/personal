package personal.audioMaster9000;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.lang.Object;

public class AudioMaster extends JPanel implements ActionListener
{
  static final long serialVersionUID  = -1L;
  private static final double VERSION = 0.5;
  private static final String LOCATION= System.getProperty("user.dir");
  
  private JFrame      frame       = new JFrame("AudioMaster 9000 v" + VERSION);
  private JPanel      cards       = new JPanel(new CardLayout());
  private CardLayout  cardHolder  = (CardLayout) (cards.getLayout());

  private JPanel    panelWelcome  = new JPanel(new GridBagLayout());
  private JPanel    panelMain     = new JPanel(new GridBagLayout());
  private JPanel    panelSettings = new JPanel(new GridBagLayout());
  private JPanel    panelConfigure= new JPanel(new GridBagLayout());
  private JPanel    panelFinished = new JPanel(new GridBagLayout());

  private JMenuBar  menu          = new JMenuBar();
  private JMenu     menuTools     = new JMenu("Tools");
  private JMenuItem menuItemSet   = new JMenuItem("Settings");
  private JMenuItem menuItemConfig= new JMenuItem("Configure audio");
  private JMenu     menuHelp      = new JMenu("Help");
  private JMenuItem menuItemHowTo = new JMenuItem("How to Use Program");
  private JMenuItem menuItemAbout = new JMenuItem("About");

  private JButton   buttonStart   = new JButton("Start");
  private JButton   buttonRepeat  = new JButton("Repeat");
  private JButton   buttonMain    = new JButton("I Hear it!");
  private JButton   buttonPause   = new JButton("Pause");
  private JButton   buttonApply   = new JButton("Apply");
  private JButton   buttonCancel  = new JButton("Cancel");
  private JButton   buttonCMain   = new JButton("I Heard it!");
  private String[]  earChoices    = { "Random", "Left then right", "Right then left" };
  private JComboBox ears          = new JComboBox(earChoices);
  private String[]  freqChoices   = { "Random", "Lowest to highest", "Highest to lowest" };
  private JComboBox freq          = new JComboBox(freqChoices);
  private JTextArea iterations    = new JTextArea("1");
  private JRadioButton precision  = new JRadioButton("");
  
  private ArrayList<Intensity> intensities= new ArrayList<Intensity>();
  
  private SettingManager settingManager   = new SettingManager(LOCATION);
  private TonePlayer     tonePlayer       = new TonePlayer(LOCATION, settingManager);
  private Thread         tonePlayerThread = new Thread(tonePlayer);

  public AudioMaster()
  {
    ears.setSelectedIndex(settingManager.getEars());
    freq.setSelectedIndex(settingManager.getFrequency());
    iterations.setText(String.valueOf(settingManager.getIterations()));
    precision.setSelected(settingManager.getPrecision());

    frame.setJMenuBar(menu);
    menuTools.add(menuItemSet);
    menuTools.add(menuItemConfig);
    menuHelp.add(menuItemHowTo);
    menuHelp.add(menuItemAbout);
    menu.add(menuTools);
    menu.add(menuHelp);
    menuItemSet.addActionListener(this);
    menuItemConfig.addActionListener(this);
    menuItemHowTo.addActionListener(this);
    menuItemAbout.addActionListener(this);

    GridBagConstraints constraints = new GridBagConstraints();

    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridx = 0;
    constraints.gridwidth = 1;
    constraints.gridy = 0;
    constraints.gridheight = 1;
    panelWelcome.add(new JLabel("Welcome to the AudioMaster 9000", 0), constraints);
    constraints.gridy = 1;
    panelWelcome.add(buttonStart, constraints);

    constraints.gridy = 0;
    constraints.gridwidth = 2;
    panelMain.add(new JLabel("Press the button when you hear a tone", 0), constraints);
    constraints.gridwidth = 1;
    constraints.gridy = 1;
    panelMain.add(buttonMain, constraints);
    constraints.gridx = 1;
    panelMain.add(buttonPause, constraints);

    ears.setPreferredSize(new Dimension(135, 20));
    freq.setPreferredSize(new Dimension(135, 20));
    iterations.setPreferredSize(new Dimension(135, 20));
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridy = 0;
    constraints.gridx = 0;
    panelSettings.add(new JLabel("Order to test ears: "), constraints);
    constraints.gridy++;
    panelSettings.add(new JLabel("Order to test frequencies: "), constraints);
    constraints.gridy++;
    panelSettings.add(new JLabel("Tests/frequency/ear: "), constraints);
    constraints.gridy++;
    panelSettings.add(new JLabel("Precision Mode: "), constraints);
    constraints.gridx++;
    panelSettings.add(precision, constraints);
    constraints.gridy--;
    constraints.anchor = GridBagConstraints.EAST;
    panelSettings.add(iterations, constraints);
    constraints.gridy--;
    panelSettings.add(freq, constraints);
    constraints.gridy--;
    panelSettings.add(ears, constraints);
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridy = 4;
    panelSettings.add(buttonCancel, constraints);
    constraints.gridx = 0;
    panelSettings.add(buttonApply, constraints);

    constraints.gridy = 0;
    panelConfigure.add(new JLabel("To Configure: press button when you hear the tone", 0), constraints);
    constraints.gridy = 1;
    panelConfigure.add(buttonCMain, constraints);

    constraints.gridy = 0;
    panelFinished.add(new JLabel("You have finished the test.", 0), constraints);
    constraints.gridy = 1;
    panelFinished.add(new JLabel("Your results have been recorded.", 0), constraints);
    constraints.gridy = 2;
    panelFinished.add(buttonRepeat, constraints);

    buttonStart.addActionListener(this);
    buttonRepeat.addActionListener(this);
    buttonMain.addActionListener(this);
    buttonPause.addActionListener(this);
    buttonApply.addActionListener(this);
    buttonCancel.addActionListener(this);
    buttonCMain.addActionListener(this);

    cards.add(panelWelcome, "Welcome");
    cards.add(panelMain, "Main");
    cards.add(panelConfigure, "Configure");
    cards.add(panelSettings, "Settings");
    cards.add(panelFinished, "Finished");
    frame.add(cards);
  }

  public void actionPerformed(ActionEvent event)
  {
    Object command = event.getSource();

    if (command == buttonStart)
      startButton();
    else if (command == buttonRepeat)
      repeatButton();
    else if (command == buttonMain)
      normalButton();
    else if (command == buttonPause)
      pauseButton();
    else if (command == buttonApply)
      applyButton();
    else if (command == buttonCancel)
      cancelButton();
    else if (command == buttonCMain)
      configureButton();
    else if (command == menuItemSet)
      settingsMenuItem();
    else if (command == menuItemConfig)
      configureMenuItem();
    else if (command == menuItemHowTo)
      howToMenuItem();
    else if (command == menuItemAbout)
      aboutMenuItem();
    else
    {
    }
  }

  private void launch()
  {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setMinimumSize(new Dimension(300, 100));
    frame.setResizable(false);
    frame.setLocation(500, 350);
    frame.setIconImage(new ImageIcon(LOCATION + "/imageIcon.png").getImage());
    frame.setVisible(true);
    tonePlayerThread.start();
  }

  public static void main(String[] args)
  {
    AudioMaster gui = new AudioMaster();
    gui.launch();
  }
  
  private void configureMenuItem()
  {
	tonePlayer.soundWasHeard();
    cardHolder.show(cards, "Configure");
    tonePlayer.selectTone(1000, true);
    tonePlayer.setTone(true);
  }
  
  private void configureButton()
  {
	double config = 0.0;
	try
	{
	  tonePlayer.soundWasHeard();
	  config = calculateConfigConstant(Double.parseDouble(JOptionPane.showInputDialog(frame, "What is the intensity of this 1kHz sound (in dB)?")));
	}catch(Exception e)
	{
	  config = settingManager.getConfig();
	}
	
	settingManager.setConfig(config);
	tonePlayer.setConfigConstant(config);
    cardHolder.show(cards, "Welcome");
  }

  private void settingsMenuItem()
  {
    tonePlayer.soundWasHeard();
    cardHolder.show(cards, "Settings");
  }
  
  private void applyButton()
  {
    ArrayList<Double> settings = getSettings();
    settingManager.setSettings(settings);

    tonePlayer.reset();
    cardHolder.show(cards, "Welcome");
  }

  private void cancelButton()
  {
    tonePlayer.reset();
    cardHolder.show(cards, "Welcome");
  }

  private void howToMenuItem()
  {
	String output = "-Set your settings (click on 'Settings', under Tools)\n" +
					"-Plug in headphones\n" +
					"-Configure the program (click on 'Configure Audio', under Tools)\n" +
					"-Click 'Start'\n" +
					"-Everytime you hear a tone, click 'I Heard It!'\n" +
					"-Once you're done, you will be notified.  You may click 'Repeat' to retake the test.";
    JOptionPane.showMessageDialog(frame, output);
  }

  private void aboutMenuItem()
  {
    JOptionPane.showMessageDialog(frame, "Writen by: Mihai Bulic\nVersion: " + VERSION + "v" + "\n(c) Copyright 2009.  All rights reserved." + "\nThis product includes software developed by " + "Mihai Bulic");
  }
  
  private void startButton()
  {
    tonePlayer.addTones();
    tonePlayer.setConfigConstant(settingManager.getConfig());
    
    cardHolder.show(cards, "Main");

    tonePlayer.selectTone();
    tonePlayer.setTone(true);
  }

  private void normalButton()
  {
    try
    {
      if (!tonePlayer.precision())
      {
        tonePlayer.soundWasHeard();
        addIntensity();
      }
      else
      {
        return;
      }

      if (tonePlayer.isFinished())
      {
        new ResultsHandler(intensities, LOCATION, settingManager);
        cardHolder.show(cards, "Finished");
      }
      else
      {
        tonePlayer.selectTone();
        tonePlayer.setTone(true);
      }
    } catch (Exception e)
    {
      System.err.println(e);
      System.out.println("line: AudioMaster 233");
    }
  }

  private void pauseButton()
  {
    tonePlayer.setTone(false);
    JOptionPane.showMessageDialog(frame, "Press OK to resume");
    tonePlayer.setTone(true);
  }

  private void repeatButton()
  {
    tonePlayer.reset();
    intensities.clear();
  
    startButton();
  }
  
  private void addIntensity()
  {
    int frequency = tonePlayer.getFrequency();
    double volume = tonePlayer.getActualIntensity(tonePlayer.getVolume());
    boolean found = false;

    for (int x = 0; x < intensities.size(); x++)
    {
      if (frequency == intensities.get(x).getFrequency())
      {
        found = true;
        if (tonePlayer.getLeftEar())
          intensities.get(x).setLeftEar(volume);
        else
          intensities.get(x).setRightEar(volume);
      }
    }

    if (!found)
    {
      Intensity intensity = new Intensity();
      intensity.setFrequency(tonePlayer.getFrequency());

      if (tonePlayer.getLeftEar())
      {
        intensity.setLeftEar(volume);
      }
      else
      {
        intensity.setRightEar(volume);
      }
      intensities.add(intensity);
    }
  }
  
  private double calculateConfigConstant(double actualDecibel)
  {
	return Math.pow(10, actualDecibel-tonePlayer.getVolume());
  }

  private ArrayList<Double> getSettings()
  {
    ArrayList<Double> settings = new ArrayList<Double>();

    settings.add((double) (ears.getSelectedIndex()));
    settings.add((double) (freq.getSelectedIndex()));
    settings.add((Double.parseDouble(iterations.getText())));
    settings.add((precision.isSelected() ? 1.0 : 0.0));
    settings.add(settingManager.getConfig());

    return settings;
  }
}
