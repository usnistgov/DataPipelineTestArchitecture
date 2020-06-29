package gov.nist.mtconnect;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MockAdapterService implements Runnable {

    //@Test
    public void run(){
        try {
            Process process = Runtime.getRuntime().exec("/usr/bin/ruby /etc/mtconnect/adapter/run_scenario.rb -l /home/tim/agent/cppagent/simulator/VMC-3Axis-Log.txt");
            sleep(10000);
            process.destroy();
            sleep(10000);
            process = Runtime.getRuntime().exec("/usr/bin/ruby /etc/mtconnect/adapter/run_scenario.rb -l /home/tim/agent/cppagent/simulator/VMC-3Axis-Log.txt");
            sleep(10000);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }



    };
}
