package examples.proxy.tracing;

import org.codehaus.aspectwerkz.proxy.Proxy;

public class TraceMe1 {

    public void step1() {
        step2();
    }

    protected void step2() {
        step3();
    }

    void step3() {
    }

    public static void main(String[] args) {
        TraceMe1 traceMe1 = (TraceMe1) Proxy.newInstance(TraceMe1.class);
        traceMe1.step1();
    }
}
