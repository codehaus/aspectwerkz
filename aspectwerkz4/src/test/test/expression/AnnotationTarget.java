/*
 * $Id: AnnotationTarget.java,v 1.1 2004-12-17 15:52:10 avasseur Exp $
 * $Date: 2004-12-17 15:52:10 $
 */
package test.expression;


/**
 * Target test class for annotations unit.
 * 
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
 * @version $Revision: 1.1 $
 */
public class AnnotationTarget {
	/**
	 * @Service
	 */
	public static class ClassLevelAnnotation {
		/**
		 * @DefaultConstructor
		 */
		public ClassLevelAnnotation() {
		}
		
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
	
	
	public static interface IConstructorAnnotation {
		String value();
	}
	
	public static interface IServiceAnnotation {
		String value();
	}
	
	public static interface IPersistableAnnotation {
		String value();
	}
	
	public static interface IAsynchronousAnnotation {
		String value();
	}
}
