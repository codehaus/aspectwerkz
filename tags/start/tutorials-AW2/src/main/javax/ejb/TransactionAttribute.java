/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package javax.ejb;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * From EJB 3 specification
 *
 *
 * The TransactionAttribute annotation specifies whether the container is to invoke a business
 * method within a transaction context. The semantics of transaction attributes are defined in Chapter 17 of
 * the EJB 2.1 specification.
 * <p/>
 * The TransactionAttribute annotation can only be specified if container managed transaction
 * demarcation is used. The annotation can be specified on the bean class and/or it can be specified on
 * methods of the class that are methods of the business interface. Specifying the TransactionAttribute
 * annotation on the bean class means that it applies to all applicable business interface methods
 * of the class. Specifying the annotation on a method applies it to that method only. If the annotation
 * is applied at both the class and the method level, the method value overrides if the two disagree.
 * The values of the TransactionAttribute annotation are defined by the enum Transaction-
 * AttributeType.
 * <p/>
 * If a TransactionAttribute annotation is not specified, and the bean uses container managed
 * transaction demarcation, the semantics of the REQUIRED transaction attribute are assumed.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Target({ElementType.METHOD, ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME)
public @interface TransactionAttribute {
    TransactionAttributeType value() default TransactionAttributeType.REQUIRED;
}
