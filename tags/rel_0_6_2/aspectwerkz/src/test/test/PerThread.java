package test;

import java.io.Serializable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PerThread.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public interface PerThread extends Serializable {
    void runPerThread();
}
