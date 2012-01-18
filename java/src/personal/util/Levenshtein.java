package personal.util;

import java.util.ArrayList;

public class Levenshtein
{
	private static final double DEFAULT_THRESH = 0.75; 
	
	public static boolean fuzzyEquals(String s1, String s2)
	{
		return (compare(s1,s2) >= DEFAULT_THRESH);
	}
	
	public static double compare(String s1, String s2)
	{
		return compare(s1,s2,false);
	}
	
	public static String[] getClose(final String in, ArrayList<String> options)
	{
		return getClose(in, options, DEFAULT_THRESH);
	}
	
	public static String[] getClose(final String in, ArrayList<String> options, double thresh)
	{
		double max = 0;
		ArrayList<String> candidate = new ArrayList<String>();
		
		for(String s : options)
		{
			double val = compare(in, s);
			if(val > max)
			{
				max = val;
				candidate.clear();
				candidate.add(s);
			}
			else if(val == max)
			{
				candidate.add(s);
			}
		}

		System.out.print("* " + candidate.get(0));
		
		if(max < thresh)
		{
			candidate.clear();
		}
		
		System.out.println(" " + max);
		
		return candidate.toArray(new String[candidate.size()]);
	}
	
    public static double compare(final String s1Orig, final String s2Orig, boolean caseSens)
    {
    	final String s1 = caseSens ? s1Orig : s1Orig.toLowerCase();
    	final String s2 = caseSens ? s2Orig : s2Orig.toLowerCase();
    	
        double retval = 0.0;
        final int n = s1.length();
        final int m = s2.length();
        if (0 == n)
        {
            retval = m;
        }
        else if (0 == m)
        {
            retval = n;
        }
        else
        {
            retval = 1.0 - (compare(s1, n, s2, m) / (Math.max(n, m)));
        }
        return retval;
    }

    private static double compare(final String s1, final int n, 
                           final String s2, final int m)
    {
        int matrix[][] = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++)
        {
            matrix[i][0] = i;
        }
        for (int i = 0; i <= m; i++)
        {
            matrix[0][i] = i;
        }

        for (int i = 1; i <= n; i++)
        {
            int s1i = s1.codePointAt(i - 1);
            for (int j = 1; j <= m; j++)
            {
                int s2j = s2.codePointAt(j - 1);
                final int cost = s1i == s2j ? 0 : 1;
                matrix[i][j] = min3(matrix[i - 1][j] + 1, 
                                    matrix[i][j - 1] + 1, 
                                    matrix[i - 1][j - 1] + cost);
            }
        }
        return matrix[n][m];
    }

    private static int min3(final int a, final int b, final int c)
    {
        return Math.min(Math.min(a, b), c);
    }
}
