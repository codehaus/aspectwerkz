/************************************************************************************** * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 * * http://aspectwerkz.codehaus.org                                                    * * ---------------------------------------------------------------------------------- * * The software in this package is published under the terms of the LGPL license      * * a copy of which has been included with this distribution in the license.txt file.  * **************************************************************************************/package test.expression;
/** * Target test class for annotations unit. *  * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a> */public class AnnotationTarget {
	/**	 * @Service	 */	public static class ClassLevelAnnotation {
		/**		 * @DefaultConstructor		 */		public ClassLevelAnnotation() {		}
		/**
		 * @Asynch
		 */
		public void innerMethodAsynch() {
		}
		/**
		 * @Persistable
		 */
		Object m_innerField;
	}
	
	/**
	 * @Asynch
	 */
	public void methodOneAsynch() {
	}
	/**
	 * @DefaultConstructor
	 */
	public AnnotationTarget() {
	}
	/**
	 * @Persistable
	 */
	private Object m_annotatedField;

	public static interface IConstructorAnnotation {		String value();	}
	public static interface IServiceAnnotation {		String value();	}
	public static interface IPersistableAnnotation {		String value();	}
	public static interface IAsynchronousAnnotation {		String value();	}}