package org.codehaus.aspectwerkz.hook;

import java.util.Hashtable;

/**
 * Implement to be a class PreProcessor in the AspectWerkz univeral loading architecture.
 *
 * A single instance of the class implementing this interface is build during
 * the java.lang.ClassLoader initialization or just before the first class loads,
 * bootclasspath excepted. Thus there is a single instance the of ClassPreProcessor
 * per JVM.<br/>
 * Use the <code>-Dbesee.classloader.preprocessor</code> option to specify which
 * class preprocessor to use.
 *
 * @see ProcessStarter
 * @author alex
 * @todo review the header and license - this is first commit test with cvsspam
 *
 */
public interface ClassPreProcessor {

    public abstract void initialize(Hashtable hashtable);

    public abstract byte[] preProcess(String klass, byte abyte[], ClassLoader caller);

}
