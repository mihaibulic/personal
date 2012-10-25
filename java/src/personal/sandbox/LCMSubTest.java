package personal.sandbox;

import java.io.IOException;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import april.lcmtypes.url_t;
import april.util.TimeUtil;

public class LCMSubTest implements LCMSubscriber
{
    public LCMSubTest()
    {
        LCM lcm = LCM.getSingleton();

        lcm.subscribeAll(this);
        
        while(true)
        {
            TimeUtil.sleep(1000);
        }
    }

    public static void main(String[] args)
    {
        new LCMSubTest();
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        if(channel.equals("myChannel"))
        {
            try
            {
                url_t test = new url_t(ins);
                System.out.println("utime:\t" + test.utime + "\nurl:\t" + test.url);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
