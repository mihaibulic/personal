package personal.sandbox;

import lcm.lcm.LCM;
import april.lcmtypes.url_t;
import april.util.TimeUtil;

public class LCMPubTest
{

    public LCMPubTest()
    {
        LCM lcm = LCM.getSingleton();
        
        url_t test = new url_t();
        test.utime = TimeUtil.utime();
        test.url = "This is a test";
        
        while(true)
        {
            TimeUtil.sleep(1000);
            lcm.publish("myChannel", test);
        }

    }

    public static void main(String[] args)
    {
        new LCMPubTest();
    }

}
