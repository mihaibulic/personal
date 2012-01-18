package personal.sandbox;

public class CapitalizationTest {

	public CapitalizationTest(String in) 
	{
		System.out.println("in: " + in);
		System.out.println("out: " + capitalize(in));
		
	}
	
	String capitalize(String in)
	{
    	boolean foundSpace = true;
    	String out = "";
    	for(char a : in.toCharArray())
    	{
    		if(a == ' ' || a == '_')
    		{
    			if(!foundSpace)
    			{
    				out += ' ';
    			}
    			foundSpace = true;
    		}
    		else if(foundSpace)
    		{
    			out += Character.toUpperCase(a);
    			foundSpace = false;
    		}
    		else
    		{
    			out += Character.toLowerCase(a);
    		}
    	}
    	
    	return out.trim();
	}

	public static void main(String[] args) 
	{
		new CapitalizationTest(args[0]);
	}

}
