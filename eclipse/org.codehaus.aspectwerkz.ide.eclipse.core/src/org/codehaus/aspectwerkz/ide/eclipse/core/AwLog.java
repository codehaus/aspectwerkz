/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Log service
 * From the book Building quality eclipse plugin.
 * http://www.qualityeclipse.com/
 * 
 * @author avasseur
 */
public class AwLog {

    /**
     * Log the specified information.
     * 
     * @param message
     *            a human-readable message, localized to the current locale
     */
    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
        logTrace(message);
    }

    /**
     * Log the specified information.
     * 
     * @param message
     *            a human-readable message, localized to the current locale
     */
    public static void logTrace(String message) {
        System.out.println("- AW - "  + message);
    }

    /**
     * Log the specified error.
     * 
     * @param exception
     *            a low-level exception
     */
    public static void logError(Throwable exception) {
        logError("Unexpected Exception", exception);
    }

    /**
     * Log the specified error.
     * 
     * @param message
     *            a human-readable message, localized to the current locale
     * @param exception
     *            a low-level exception, or <code>null</code> if not
     *            applicable
     */
    public static void logError(String message, Throwable exception) {
        log(IStatus.ERROR, IStatus.OK, message, exception);
    }

    /**
     * Log the specified information.
     * 
     * @param severity
     *            the severity; one of <code>IStatus.OK</code>,
     *            <code>IStatus.ERROR</code>,<code>IStatus.INFO</code>,
     *            or <code>IStatus.WARNING</code>
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param code
     *            the plug-in-specific status code, or <code>OK</code>
     * @param message
     *            a human-readable message, localized to the current locale
     * @param exception
     *            a low-level exception, or <code>null</code> if not
     *            applicable
     */
    public static void log(int severity, int code, String message,
            Throwable exception) {

        log(createStatus(severity, code, message, exception));
    }

    /**
     * Create a status object representing the specified information.
     * 
     * @param severity
     *            the severity; one of <code>IStatus.OK</code>,
     *            <code>IStatus.ERROR</code>,<code>IStatus.INFO</code>,
     *            or <code>IStatus.WARNING</code>
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param code
     *            the plug-in-specific status code, or <code>OK</code>
     * @param message
     *            a human-readable message, localized to the current locale
     * @param exception
     *            a low-level exception, or <code>null</code> if not
     *            applicable
     * @return the status object (not <code>null</code>)
     */
    public static IStatus createStatus(int severity, int code, String message,
            Throwable exception) {

        return new Status(severity,
        //FIXME
                //AwCorePlugin.getDefault().getDescriptor().getUniqueIdentifier(),
                "AspectWerkz", code, message, exception);
    }

    /**
     * Log the given status.
     * 
     * @param status
     *            the status to log
     */
    public static void log(IStatus status) {
        AwCorePlugin.getDefault().getLog().log(status);
    }

}