/*
 * Created on Jul 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test.inlining.proxymethod;

/**
 * @author jboner
 */
public class Target {
    public String toString() {
        return "called toString";
    }

    public void memberMethod() {
    }

    public static void staticMethod() {
    }

    public static void main(String[] args) {
        System.out.println("new Test().toString() = " + new Target().toString());
    }
}
