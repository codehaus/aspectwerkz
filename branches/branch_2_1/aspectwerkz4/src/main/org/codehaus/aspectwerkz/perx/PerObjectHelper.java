/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.perx;


import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.aspect.management.HasInstanceLevelAspect;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;

/**
 * Utility class for perX behavior.
 * 
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class PerObjectHelper {
	private static final String PEROBJECT_ASPECT_NAME = PerObjectAspect.class.getName();
	private static final ClassInfo PEROBJECT_CLASSINFO = JavaClassInfo.getClassInfo(PerObjectAspect.class);
	private static MethodInfo BEFORE_ADVICE_METHOD_INFO;
	private static int s_aspectCount;
	
	static {
		MethodInfo[] methods = PEROBJECT_CLASSINFO.getMethods();
		for(int i = 0; i < methods.length; i++) {
			if(PerObjectAspect.BEFORE_ADVICE_NAME.equals(methods[i].getName())) {
				BEFORE_ADVICE_METHOD_INFO = methods[i];
				break;
			}
		}
	}

	/**
	 * Creates the generic AspectDefinition for
	 * @param systemDefinition
	 * @param aspectDefinition
	 * @param perThis
	 * @return
	 */
	public static AspectDefinition getAspectDefinition(SystemDefinition systemDefinition,
	                                                   AspectDefinition aspectDefinition,
	                                                   boolean perThis) {
		AspectDefinition aspectDef = new AspectDefinition(
				getAspectName(),
				PEROBJECT_CLASSINFO,
				systemDefinition);

		aspectDef.setDeploymentModel(DeploymentModel.PER_JVM);
		aspectDef.addParameter(PerObjectAspect.ASPECT_QNAME_PARAM, 
                               aspectDefinition.getQualifiedName());
        aspectDef.addParameter(PerObjectAspect.CONTAINER_CLASSNAME_PARAM,
                               aspectDefinition.getContainerClassName());
		
		DeploymentModel deploymentModel = aspectDefinition.getDeploymentModel();
		
        ExpressionInfo expressionInfo = getExpressionInfo(deploymentModel,
                                                          aspectDefinition.getQualifiedName(),
                                                          PEROBJECT_CLASSINFO.getClassLoader()
        );

        aspectDef.addBeforeAdviceDefinition(
				new AdviceDefinition(
						PerObjectAspect.ADVICE_SIGNATURE,
						AdviceType.BEFORE, 
						null, 
						PEROBJECT_ASPECT_NAME, 
						PEROBJECT_ASPECT_NAME, 
						expressionInfo,
						BEFORE_ADVICE_METHOD_INFO,
						aspectDef
				)
		);
		
		return aspectDef;
	}
	
	private static String getAspectName() {
		s_aspectCount++;
		
		return PEROBJECT_ASPECT_NAME + "_" + s_aspectCount;
	}
	
	public static boolean hasAspect(Object targetInstance, String aspectQName) {
		if (null == targetInstance) {
//			System.out.println("return FALSE for [" + aspectQName + "] cause null");
			return false;
		}
		
		if (!(targetInstance instanceof HasInstanceLevelAspect)) {
//			System.out.println("return FALSE for [" + aspectQName +"] in " 
//					+ targetInstance.getClass().getName()
//					+ "["
//					+ targetInstance.hashCode() 
//					+ "]");
			
			return false;
		}
		
		boolean result = ((HasInstanceLevelAspect) targetInstance).aw$hasAspect(aspectQName);
		
//		System.out.println("return " + result + " for [" + aspectQName +"] in " 
//							+ targetInstance.getClass().getName()
//							+ "["
//							+ targetInstance.hashCode() 
//							+ "]");

		
		return result; 
	}

	/**
	 * @param deployModel
	 * @param qualifiedName
	 * @return
	 */
	public static ExpressionInfo getExpressionInfo(final DeploymentModel deployModel, 
	                                               final String qualifiedName,
	                                               final ClassLoader cl) {
		ExpressionInfo expressionInfo = new ExpressionInfo(deployModel.getDeploymentExpression(),
		                                                   qualifiedName
		);
		
		expressionInfo.addArgument(PerObjectAspect.ADVICE_ARGUMENT_NAME, 
		                           PerObjectAspect.ADVICE_ARGUMENT_TYPE, 
		                           cl);
		
		return expressionInfo;
	}
}
