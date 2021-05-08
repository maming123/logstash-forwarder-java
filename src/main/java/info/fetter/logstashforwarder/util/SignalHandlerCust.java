package info.fetter.logstashforwarder.util;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class SignalHandlerCust  implements SignalHandler{


    public static int getKILLSIGNAL() {
        return KILLSIGNAL;
    }

    private static int KILLSIGNAL=0;

    @Override
    public void handle(Signal signal) {
        System.out.println("Signal handler called for signal " + signal);
        try {
            SignalHandlerCust.KILLSIGNAL=signal.getNumber();
            System.out.println("Handling " + signal.getName());
        } catch (Exception e) {
            System.out.println("handle|Signal handler" + "failed, reason "
                    + e.getMessage());
            //e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        System.out.println("Signal handling example.");
//        SignalHandler handler = new SignalHandlerCust();
//        // kill命令
//        Signal termSignal = new Signal("TERM");
//        Signal.handle(termSignal, handler);
//        // ctrl+c命令
//        Signal intSignal = new Signal("INT");
//        Signal.handle(intSignal, handler);
//        try {
//            Thread.sleep(50000);
//        } catch (Exception e) {
//            System.out.println("Interrupted: " + e.getMessage());
//        }
//    }

}

