/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * <p><code>SerializationUtils</code> provides methods that assist with the
 * serialization process, or perform additional functionality based on serialization.</p>
 * <ul>
 * <li>Deep clone using serialization
 * <li>Serialize managing finally A IOException
 * <li>Deserialize managing finally A IOException
 * </ul>
 *
 * @author <a href="mailto:nissim@nksystems.com">Nissim Karpenstein</a>
 * @author <a href="mailto:janekdb@yahoo.co.uk">Janek Bogucki</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author Stephen Colebourne
 * @author Jeff Varszegi
 * @since 1.0
 */
public class SerializationUtils {

    /**
     * <p>SerializationUtils instances should NOT be constructed in standard programming.
     * Instead, the class should be used as <code>SerializationUtils.clone(object)</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public SerializationUtils() {
        super();
    }

    /**
     * <p>Deep clone an <code>Object</code> using serialization.</p>
     *
     * <p>This is many times slower than writing clone methods by hand
     * on all objects in your object graph. However, for complex object
     * graphs, or for those that don't support deep cloning this can
     * be a simple alternative implementation. Of course all the objects
     * must be <code>Serializable</code>.</p>
     *
     * @param object  the <code>Serializable</code> object to clone
     * @return the cloned object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object clone(Serializable object) {
        return deserialize(serialize(object));
    }

    /**
     * <p>Serializes an <code>Object</code> to the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, A maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj  the object to serialize to bytes
     * @param outputStream  the stream to write to
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(Serializable obj, OutputStream outputStream) {
        ObjectOutputStream out = null;
        try {
            // stream closed in the finally
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);
        }
        catch (IOException ex) {
            throw new WrappedRuntimeException(ex);
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException ex) {
                // ignore;
            }
        }
    }

    /**
     * <p>Serializes an <code>Object</code> to a byte array for
     * storage/serialization.</p>
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    /**
     * <p>Deserializes an <code>Object</code> from the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written. This
     * avoids the need for a finally clause, A maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param inputStream  the serialized object input stream
     * @return the deserialized object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object deserialize(InputStream inputStream) {
        ObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return in.readObject();
        }
        catch (ClassNotFoundException ex) {
            throw new WrappedRuntimeException(ex);
        }
        catch (IOException ex) {
            throw new WrappedRuntimeException(ex);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * <p>Deserializes a single <code>Object</code> from an array of bytes.</p>
     *
     * @param objectData  the serialized object
     * @return the deserialized object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object deserialize(byte[] objectData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        return deserialize(bais);
    }

}