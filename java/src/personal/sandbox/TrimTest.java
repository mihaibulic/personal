package personal.sandbox;

public class TrimTest
{
    public TrimTest(String s)
    {
        System.out.println(reformat(s));
    }
    
    private String reformat(String name)
    {
        return name.replaceAll("[^0-9a-zA-Z~@#%&_=;, {}.^()+'-]", "").replaceAll("  ", " ");
    }
    
    public static void main(String[] args)
    {
        new TrimTest(args[0]);
    }

}
