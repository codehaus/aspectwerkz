/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

/**
 * Adds a "proxy method" to the <tt>&lt;clinit&gt;</tt> that matches an 
 * <tt>staticinitialization</tt> pointcut as well as prefixing the "original method" 
 * (see {@link org.codehaus.aspectwerkz.transform.TransformationUtil#getPrefixedOriginalClinitName(String)}).
 * <br/>
 *
 * TODO multiweaving checks
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
 */
public class StaticInitializationVisitor extends ClassAdapter implements TransformationConstants {

	private final ClassInfo m_classInfo;
	private final ContextImpl m_ctx;
	private String m_declaringTypeName;

	/**
	 * Creates a new class adapter.
	 * 
	 * @param cv
	 * @param classInfo
	 * @param ctx
	 */
	public StaticInitializationVisitor(	final ClassVisitor cv,
										final ClassInfo classInfo,
										final Context ctx) {
		super(cv);
		m_classInfo = classInfo;
		m_ctx = (ContextImpl) ctx;
	}

	/**
	 * Visits the class.
	 * 
	 * @param access
	 * @param name
	 * @param superName
	 * @param interfaces
	 * @param sourceFile
	 */
	public void visit(	final int version,
						final int access,
						final String name,
						final String superName,
						final String[] interfaces,
						final String sourceFile) {
		m_declaringTypeName = name;
		super.visit(version, access, name, superName, interfaces, sourceFile);
	}

	/**
	 * Visits the methods.
	 * 
	 * @param access
	 * @param name
	 * @param desc
	 * @param exceptions
	 * @param attrs
	 * @return
	 */
	public CodeVisitor visitMethod(	final int access,
									final String name,
									final String desc,
									final String[] exceptions,
									final Attribute attrs) {
		if(!CLINIT_METHOD_NAME.equals(name)) {
			return cv.visitMethod(access, name, desc, exceptions, attrs);
		}

		String prefixedOriginalName = TransformationUtil.getPrefixedOriginalClinitName(m_declaringTypeName);

		m_ctx.markAsAdvised();

		// create the proxy for the original method
		createProxyMethod(access, name, desc, exceptions, attrs);

		// prefix the original method
		return cv.visitMethod(access + ACC_PUBLIC, prefixedOriginalName, desc, exceptions, attrs);
	}

	/**
	 * Creates the "proxy method", e.g. the method that has the same name and
	 * signature as the original method but a completely other implementation.
	 * 
	 * @param access
	 * @param name
	 * @param desc
	 * @param exceptions
	 * @param attrs
	 */
	private void createProxyMethod(	final int access,
									final String name,
									final String desc,
									final String[] exceptions,
									final Attribute attrs) {
		CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions, attrs);

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
