package personal.sandbox;

import april.util.TimeUtil;

public class MemTest
{
    class Test
    {
        byte a,b,c,d,e,f,g,h,i,j;
        
    }
    
    public MemTest()
    {
        System.out.println("*");
        TimeUtil.sleep(5000);
        System.out.println("*");
        Test a[] = new Test[10000000];
        for(int x = 0; x < 10000000; x++)
        {
            a[x] = new Test();
        }
        System.out.println("*");
        TimeUtil.sleep(10000);
        System.out.println("*");
    }
    
    public static void main(String[] args)
    {
        new MemTest();
        
    }
}
