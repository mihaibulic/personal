package personal.sandbox;

public class StringTest {

	public StringTest(String a) 
	{
	    System.out.println("*" + capitalize(a));
	}

   private String capitalize(String in)
    {
        boolean useCap = true;
        String out = "";
        
        for(char a : in.toCharArray())
        {
            if(useCap)
            {
                if(a != ' ' && a != '_') out += Character.toUpperCase(a);
                useCap = false;
            }
            else
            {
                out += Character.toLowerCase(a);
            }
            
            useCap = (a == ' ' || a == '_' || a == '(');
        }
        
        return out.trim();
    }
	
	public static void main(String[] args) 
	{
		new StringTest(args[0]);
	}

}
