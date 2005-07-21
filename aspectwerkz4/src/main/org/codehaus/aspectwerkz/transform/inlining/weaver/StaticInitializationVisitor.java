/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;


import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Set;

/**
 * Adds a "proxy method" to the <tt>&lt;clinit&gt;</tt> that matches an 
 * <tt>staticinitialization</tt> pointcut as well as prefixing the "original method" 
 * (see {@link org.codehaus.aspectwerkz.transform.TransformationUtil#getPrefixedOriginalClinitName(String)}).
 * <br/>
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
 */
public class StaticInitializationVisitor extends ClassAdapter implements TransformationConstants {

	private final ContextImpl m_ctx;
	private String m_declaringTypeName;
    private final Set m_addedMethods;

	/**
	 * Creates a new class adapter.
	 * 
	 * @param cv
	 * @param ctx
     * @param addedMethods already added methods by AW
	 */
	public StaticInitializationVisitor(	final ClassVisitor cv,
										final Context ctx,
                                        final Set addedMethods) {
		super(cv);
		m_ctx = (ContextImpl) ctx;
        m_addedMethods = addedMethods;
	}

	/**
	 * Visits the class.
	 * 
	 * @param access
	 * @param name
     * @param signature
	 * @param superName
	 * @param interfaces
	 */
	public void visit(	final int version,
						final int access,
						final String name,
                        final String signature,
						final String superName,
						final String[] interfaces) {
		m_declaringTypeName = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Visits the methods.
	 * 
	 * @param access
	 * @param name
	 * @param desc
     * @param signature
	 * @param exceptions
	 * @return
	 */
	public MethodVisitor visitMethod(final int access,
                                     final String name,
									 final String desc,
                                     final String signature,
									 final String[] exceptions) {
		if(!CLINIT_METHOD_NAME.equals(name)) {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

		String prefixedOriginalName = TransformationUtil.getPrefixedOriginalClinitName(m_declaringTypeName);
        if (m_addedMethods.contains(AlreadyAddedMethodAdapter.getMethodKey(prefixedOriginalName, CLINIT_METHOD_SIGNATURE))) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

		m_ctx.markAsAdvised();

		// create the proxy for the original method
		createProxyMethod(access, name, desc, signature, exceptions);

		// prefix the original method
		return cv.visitMethod(access + ACC_PUBLIC, prefixedOriginalName, desc, signature, exceptions);
	}

	/**
	 * Creates the "proxy method", e.g. the method that has the same name and
	 * signature as the original method but a completely other implementation.
	 * 
	 * @param access
	 * @param name
	 * @param desc
     * @param signature
	 * @param exceptions
	 */
	private void createProxyMethod(final int access,
								   final String name,
                                   final String desc,
                                   final String signature,
								   final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        //caller instance is null
		mv.visitInsn(ACONST_NULL);

		int joinPointHash = AsmHelper.calculateMethodHash(name, desc);
		String joinPointClassName = TransformationUtil
				.getJoinPointClassName(	m_declaringTypeName,
										name,
										desc,
										m_declaringTypeName,
										JoinPointType.STATIC_INITIALIZATION_INT,
										joinPointHash);

		mv.visitMethodInsn(INVOKESTATIC,
		        joinPointClassName, 
		        INVOKE_METHOD_NAME, 
		        TransformationUtil.getInvokeSignatureForCodeJoinPoints(	access,
																		desc,
																		m_declaringTypeName,
																		m_declaringTypeName));

		AsmHelper.addReturnStatement(mv, Type.VOID_TYPE);
		mv.visitMaxs(0, 0);

		// emit the joinpoint
		m_ctx.addEmittedJoinPoint(
		        new EmittedJoinPoint(JoinPointType.STATIC_INITIALIZATION_INT,
		                             m_declaringTypeName,
		                             name,
		                             desc,
		                             access,
		                             m_declaringTypeName,
		                             name,
		                             desc,
		                             access,
		                             joinPointHash,
		                             joinPointClassName,
		                             EmittedJoinPoint.NO_LINE_NUMBER
		        )
		);
	}
}
