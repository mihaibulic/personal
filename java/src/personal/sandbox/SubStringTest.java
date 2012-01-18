package personal.sandbox;

public class SubStringTest {

	public SubStringTest() 
	{
		String[] input = {"poop ft. mr t (M4A1 remix)", "a feat b (real hard mix)", "b - c (dirty)", "e-j (instrumental)"};
        
		for(String in : input)
		{
			System.out.print(">"+in+"<\t");

			String parenthesis = "";
			int open = in.indexOf("(");
			int close = in.indexOf(")");
			boolean importantParenthesis = false;
			
			// open < close covers all parenthesis cases except have a ) but no (
			if(open < close && open != -1) //contains parenthesis in proper order
	        {
	            parenthesis = in.substring(open, close+1);
	            String lwrCase = parenthesis.toLowerCase();
	            
	            if(lwrCase.contains("mix") || lwrCase.contains("instrumental"))
	            {
	            	importantParenthesis = true;
	            }
	        }
			
	        String[] keyWords = {" ft", " Ft", " feat", " Feat", "; ", ";", " - ", ", "};
	        for(String word : keyWords)
	        {
	        	int index = in.indexOf(word);
	            if(index > 0)
	            {
	        		in = in.substring(0, index)+(importantParenthesis ? " "+parenthesis : "");            		
	            }
	        }
	        System.out.println(">"+in+"<");
		}
	}

	public static void main(String[] args) 
	{
		new SubStringTest();
	}

}
