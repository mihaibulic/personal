package personal.run;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * GUI-based approach to managing environmental variables (one at a time only ATM)
 * 
 * If run with no args, it will infer you want to change the CLASSPATH
 * If called w/ one arg it will show you all the files in it 
 * 		ie: export TEST=/etc/file1:/home/user1/file2 will show this:
 * 				file1	/etc/file1
 * 				file2	/home/user1/file2
 * from there you can change, add, or remove elements and write them back to $HOME/.bashrc
 * 
 * @author mihai
 *
 */
public class EnvVarManager extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private String variable;
	private DefaultTableModel model;
	private JTable table;
	private JButton add, delete, write;
	private JFileChooser addChooser;
	
	public EnvVarManager()
	{
		new EnvVarManager("CLASSPATH");
	}
	
	public EnvVarManager(String arg)
	{
		super("environment variable viewer");
		setLayout(new BorderLayout());
		variable = arg;

		model = new DefaultTableModel(getTable(variable), new String[]{variable, "location"});
		table = new JTable(model);
		addChooser = new JFileChooser();
		add = new JButton("Add");
		delete = new JButton("Delete");
		write = new JButton("Write");
		
		add(table, BorderLayout.NORTH);
		add(add, BorderLayout.EAST);
		add(delete, BorderLayout.WEST);
		add(write, BorderLayout.SOUTH);
		
		add.addActionListener(this);
		delete.addActionListener(this);
		write.addActionListener(this);
		
		setVisible(true);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	String[][] getTable(String variable)
	{
		String[] list = System.getenv(variable).split(":");;
		String[][] table = new String[list.length][2];
		
		for(int x = 0; x < list.length; x++)
		{
			table[x][0] = list[x].substring(list[x].lastIndexOf(File.separator)+1);
			table[x][1] = list[x];
		}
		Arrays.sort(table, new StringArrayComparator());
		
		return table;
	}

	public static void main(String[] args) 
	{
		if(args.length == 0)
		{
			new EnvVarManager();
		}
		else if(args.length == 1)
		{
			new EnvVarManager(args[0]);
		}
		else
		{
			throw new RuntimeException("Error: too many arguements (one only)");
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) 
	{
		if(event.getSource().equals(add))
		{
			addChooser.showOpenDialog(this);
			try
			{
				File file = addChooser.getSelectedFile();
                if(file.exists())
                {
                	insertRow(file.getName(), file.getCanonicalPath());
                }
                else
                {
                	throw new RuntimeException("Error: file doesn't exist");
                }
			} catch (IOException ex) 
            {
            	ex.printStackTrace();
            }
		}
		else if(event.getSource().equals(delete))
		{
			int[] indicesToDelete = table.getSelectedRows();
			
			for(int i = indicesToDelete.length-1; i >= 0; i--)
			{
				model.removeRow(indicesToDelete[i]);
			}
		}
		else if(event.getSource().equals(write))
		{
			@SuppressWarnings("unchecked")
			Vector<Vector<String>> data = model.getDataVector();
			String value = "";
			
			for(int x = 0; x < data.size(); x++)
			{
				value += data.get(x).get(1) + (x+1<data.size() ? ":" : "");
			}
			
			String home = System.getenv("HOME");
			try 
			{
				ArrayList<String> bashrc = new ArrayList<String>();
				String line = "";

				BufferedReader br = new BufferedReader(new FileReader(new File(home+File.separator+".bashrc")));
				while((line = br.readLine()) != null)
				{
					
					if(line.startsWith("export " + variable + "="))
					{
						line = "export "+variable+"="+value; 
					}
					bashrc.add(line);
				}
				br.close();

				BufferedWriter bf = new BufferedWriter(new FileWriter(new File(home+File.separator+".bashrc")));
				for(String l : bashrc)
				{
					bf.write(l+"\n");
				}
				bf.close();

                System.out.println("Successful write");
				
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

        this.pack();
	}
	
	void insertRow(String fileName, String path)
	{
		String[] toAdd = new String[]{fileName, path};
		
		if(model.getRowCount() < 250)
		{
			if(fileName.compareToIgnoreCase((String)model.getValueAt(model.getRowCount()-1, 0)) > 0)
			{
				System.out.println("*");
				model.insertRow(model.getRowCount(), toAdd);
			}
			else
			{
				for(int x = 0; x < model.getRowCount(); x++)
				{
					if(fileName.compareToIgnoreCase((String)model.getValueAt(x, 0)) < 0)
					{
						System.out.println("$");
						model.insertRow(x, toAdd);
						break;
					}
					else if(fileName.compareToIgnoreCase((String)model.getValueAt(x, 0)) == 0)
					{
						System.out.println("&");
						int result = JOptionPane.showConfirmDialog(this, "Warning: " + fileName + " is already in the list (path: " + path + "). Do you wish to add it still?");
						if(result == JOptionPane.YES_OPTION)
						{
							model.insertRow(x, toAdd);
						}
						break;
					}
				}
			}
		}
		else
		{
			model.addRow(toAdd);
		}
	}
	
	class StringArrayComparator implements Comparator<String[]> 
	{
	    @Override
	    public int compare(String[] array1, String[] array2) 
	    {
	        return array1[0].compareTo(array2[0]);
	        // or : return d2.compareTo(d1);
	    }
	}
	
}
