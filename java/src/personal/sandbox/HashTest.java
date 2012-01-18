package personal.sandbox;

import java.util.HashMap;

public class HashTest {

	public HashTest() 
	{
		HashMap<String, String> test = new HashMap<String, String>();
		
		test.put("A", "a");
		test.put("B", "b");
		test.put("C", "c");
		test.put("D", "d");
		test.put("A", "aa");
		test.put("A", "aaa");
		
		Object[] out = test.values().toArray();
				
		System.out.println("size: " + out.length);
		for(Object a : out)
		{
			System.out.println((String)a);
		}
		
	}

	public static void main(String[] args) 
	{
		new HashTest();
	}

}
