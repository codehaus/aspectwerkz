/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.ui;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;
import org.codehaus.aspectwerkz.ide.eclipse.core.IWeaverListener;
import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointManager;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.codehaus.aspectwerkz.org.objectweb.asm.*;

/**
 * @author avasseur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WeaverListener implements IWeaverListener {
    
    private final static String JOINPOINT_MARKER = "org.codehaus.aspectwerkz.ide.eclipse.core.joinpoint";
    public final static String JOINPOINT_ATTRIBUTE = "aspectwerkz.jp";
    public final static String ADVICECONTAINER_ATTRIBUTE = "aspectwerkz.adv";
    public final static String JAVAPROJECT_ATTRIBUTE = "aspectwerkz.jproject";
    
    public void onWeaved(final IJavaProject jproject, final String className, final ClassLoader loader,
			 final EmittedJoinPoint[] emittedJoinPoints,
			 final boolean isTriggered) {
        
        AwLog.logInfo("notified for " + className + ", " + emittedJoinPoints.length + " jp(s)");

        // look for the the advised Java type 
        final IType atClass;
        final IResource resource;
        try {
            atClass = jproject.findType(className);
	        if (atClass == null) {
	            return;
	        }
	        resource= atClass.getUnderlyingResource();
	        if (resource == null) {
	            return;
	        }
        } catch (JavaModelException e) {
            AwLog.logError(e);
            return;
        }
        
        // do markers creation async
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor)
              throws CoreException {
		        try {
			        IMarker marker = null;
			        
			        if (!isTriggered)
			            deleteMarkers(resource);
			        
			        for (int i = 0; i < emittedJoinPoints.length; i++) {
			            EmittedJoinPoint jp = emittedJoinPoints[i];
			            AwLog.logTrace("jp @ " + className + ":" + jp.getLineNumber() + ":"
			                    + jp.getCalleeMemberName() + jp.getCalleeMemberDesc()
			            );
			            // Note: atClass is callee for execution and caller site for call pc
			            // since it is the weaved class.
			            if (jp.getJoinPointType() == JoinPointType.METHOD_EXECUTION_INT) {
			                IMethod atMethod = findMethod(atClass, jp.getCalleeMemberName(), jp.getCalleeMemberDesc());
			                createMarker(resource, atMethod, jp, loader, jproject);
			            } else if (jp.getJoinPointType() == JoinPointType.CONSTRUCTOR_EXECUTION_INT) {
			                IMethod atCtor = findConstructor(atClass, jp.getCalleeMemberDesc());
			                createMarker(resource, atCtor, jp, loader, jproject);
			            } else if (jp.getJoinPointType() == JoinPointType.METHOD_CALL_INT
			                    || jp.getJoinPointType() == JoinPointType.CONSTRUCTOR_CALL_INT
			                    || jp.getJoinPointType() == JoinPointType.FIELD_GET_INT
			                    || jp.getJoinPointType() == JoinPointType.FIELD_SET_INT
			                    || jp.getJoinPointType() == JoinPointType.HANDLER_INT) {
			                // rely on line number
			                createMarker(resource, jp, loader, jproject);
			            }
			        }
		        } catch (Exception e) {
		            AwLog.logError(e);
		        }
            }
        };
        
        try {
            resource.getWorkspace().run(runnable, null);
        } catch (CoreException e1) {
            AwLog.logError(e1);
            return;
        }
    }
    
    public static void deleteMarkers(IResource resource) {
        try {
          resource.deleteMarkers(
            JOINPOINT_MARKER,
            false,
            IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            AwLog.logError(e);
        }
      }
    
    public static void createMarker(IResource resource, IMethod testMethod, EmittedJoinPoint jp, ClassLoader loader, IJavaProject jproject)
            throws JavaModelException, CoreException {
          ISourceRange range= testMethod.getNameRange();
          Map map= new HashMap();
          MarkerUtilities.setCharStart(map, range.getOffset());
          MarkerUtilities.setCharEnd(map, range.getOffset() +  range.getLength());
          MarkerUtilities.setMessage(map, getMarkerMessage(jp));
          map.put(JOINPOINT_ATTRIBUTE, jp);
          map.put(ADVICECONTAINER_ATTRIBUTE, createAdviceInfoContainer(loader, jp));
          map.put(JAVAPROJECT_ATTRIBUTE, jproject);
          MarkerUtilities.createMarker(resource, map, JOINPOINT_MARKER);
    }
    
    public static void createMarker(IResource resource, EmittedJoinPoint jp, ClassLoader loader, IJavaProject jproject)
    		throws JavaModelException, CoreException {
        if (jp.getLineNumber() <= 0) {
            AwLog.logError(new RuntimeException("no line number info for join point " + getJoinPointHint(jp)));
            return;
        }
		Map map= new HashMap();
		MarkerUtilities.setLineNumber(map, jp.getLineNumber());
		MarkerUtilities.setMessage(map, getMarkerMessage(jp));
		map.put(JOINPOINT_ATTRIBUTE, jp);
        map.put(ADVICECONTAINER_ATTRIBUTE, createAdviceInfoContainer(loader, jp));
        map.put(JAVAPROJECT_ATTRIBUTE, jproject);
		MarkerUtilities.createMarker(resource, map, JOINPOINT_MARKER);
    }
    
    private static String getMarkerMessage(EmittedJoinPoint jp) {
        StringBuffer sb = new StringBuffer();
        sb.append(JoinPointType.fromInt(jp.getJoinPointType()).toString());
        sb.append("\nfor: ");
        sb.append(jp.getCalleeClassName()).append(' ');
        sb.append(jp.getCalleeMemberName()).append(' ');
        sb.append(jp.getCalleeMemberDesc());
        sb.append("\nwithin: ");
        sb.append(jp.getCallerClassName()).append(' ');
        sb.append(jp.getCallerMethodName()).append(' ');
        sb.append(jp.getCallerMethodDesc());
        return sb.toString();
    }
    
    public static IMethod findMethod(IType atClass, String methodName, String methodDesc)
    		throws JavaModelException, NoSuchMethodException {
        IMethod[] methods = atClass.getMethods();
        Type[] args = Type.getArgumentTypes(methodDesc);
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getElementName().equals(methodName) && methods[i].getParameterTypes().length == args.length) {
                String[] unresolvedArgs = methods[i].getParameterTypes();
                boolean argMatch = true;
                for (int t = 0; t < unresolvedArgs.length; t++) {
                    String javaName = JavaModelUtil.getResolvedTypeName(unresolvedArgs[t], atClass);
                    AwLog.logTrace("comparing arg " + javaName + " to desc " + args[t].getClassName() + " for " + args[t].getDescriptor());
                    if (javaName.equals(args[t].getClassName())) {
                        ;
                    } else {
                        argMatch = false;
                        break;
                    }
                }
                if (argMatch == true) {
                    return methods[i];
                }
            }
        }
        throw new NoSuchMethodException(atClass.getFullyQualifiedName()+ "." + methodName + methodDesc );
    }
    
    public static IMethod findConstructor(IType atClass, String ctorDesc)
	throws JavaModelException, NoSuchMethodException {
        return findMethod(atClass, atClass.getElementName(), ctorDesc);
	}

    private static String getJoinPointHint(EmittedJoinPoint jp) {
        StringBuffer sb = new StringBuffer();
        sb.append(getMarkerMessage(jp));
        sb.append(" , caller ");
        sb.append(jp.getCallerClassName());
        sb.append('.').append(jp.getCallerMethodName());
        sb.append(jp.getCallerMethodDesc());
        sb.append(" , callee ");
        sb.append(jp.getCalleeClassName());
        sb.append('.').append(jp.getCalleeMemberName());
        sb.append(jp.getCalleeMemberDesc());
        return sb.toString();
    }
    
    private static AdviceInfoContainer createAdviceInfoContainer(ClassLoader loader, EmittedJoinPoint jp) {
        // --- TODO: code copied from AW JoinPointManager - needs public API

        ClassInfo callerClassInfo = AsmClassInfo.getClassInfo(jp.getCallerClassName(), loader);
        ReflectionInfo withinInfo = null;
        // FIXME: refactor getMethodInfo in INFO so that we can apply it on "<init>" and that it delegates to ctor
        // instead of checking things here.
        switch (jp.getJoinPointType()) {
            case JoinPointType.CONSTRUCTOR_EXECUTION_INT:
                withinInfo = callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(jp.getCallerMethodDesc()));
                break;
            default:
                // TODO - support for withincode <clinit>
                if (TransformationConstants.INIT_METHOD_NAME.equals(jp.getCallerMethodName())) {
                    withinInfo = callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(jp.getCallerMethodDesc()));
                } else {
                    withinInfo =
                    callerClassInfo.getMethod(AsmHelper.calculateMethodHash(jp.getCallerMethodName(), jp.getCallerMethodDesc()));
                }
        }

        PointcutType pointcutType = mapJoinPointTypeToPointcutType(JoinPointType.fromInt(jp.getJoinPointType()));
        
        ReflectionInfo reflectionInfo = createCalleeInfo(loader, jp);
        
        final ExpressionContext ctx = new ExpressionContext(pointcutType, reflectionInfo, withinInfo);
        final AdviceInfoContainer adviceContainer = JoinPointManager.getAdviceInfoContainerForJoinPoint(
                ctx, loader
        );
        
        return adviceContainer;
    }
    
    private static PointcutType mapJoinPointTypeToPointcutType(JoinPointType jpt) {
        if (jpt.equals(JoinPointType.CONSTRUCTOR_CALL)
                || jpt.equals(JoinPointType.METHOD_CALL)) {
            return PointcutType.CALL;
        } else if (jpt.equals(JoinPointType.CONSTRUCTOR_EXECUTION)
                || jpt.equals(JoinPointType.METHOD_EXECUTION)) {
            return PointcutType.EXECUTION;
        } else if (jpt.equals(JoinPointType.FIELD_GET)) {
            return PointcutType.GET;
        } else if (jpt.equals(JoinPointType.FIELD_SET)) {
            return PointcutType.SET;
        } else if (jpt.equals(JoinPointType.HANDLER)) {
            return PointcutType.HANDLER;
        } else {
            throw new Error("Unsupported jp type " + jpt.toString());
        }
    }
    
    private static ReflectionInfo createCalleeInfo(ClassLoader loader, EmittedJoinPoint jp) {
        ClassInfo callee = AsmClassInfo.getClassInfo(jp.getCalleeClassName(), loader);
        final ReflectionInfo calleeInfo;
        
        int jph = -1;
        switch (jp.getJoinPointType()) {
        	case JoinPointType.FIELD_GET_INT:
        	case JoinPointType.FIELD_SET_INT:
        	    jph = AsmHelper.calculateFieldHash(jp.getCalleeMemberName(), jp.getCalleeMemberDesc());
        	    calleeInfo = callee.getField(jph);
        	    break;
        	case JoinPointType.CONSTRUCTOR_CALL_INT:
        	case JoinPointType.CONSTRUCTOR_EXECUTION_INT:
        	    jph = AsmHelper.calculateConstructorHash(jp.getCalleeMemberDesc());
        		calleeInfo = callee.getConstructor(jph);
        		break;
            case JoinPointType.METHOD_CALL_INT:
            case JoinPointType.METHOD_EXECUTION_INT:
        	    jph = AsmHelper.calculateMethodHash(jp.getCalleeMemberName(), jp.getCalleeMemberDesc());
	    		calleeInfo = callee.getMethod(jph);
	    		break;
	    	case JoinPointType.HANDLER_INT:
	    	    calleeInfo = AsmClassInfo.getClassInfo(jp.getCalleeClassName(), loader);
	    		break;
            default:
                throw new Error("Unsupported jp type " + jp.getJoinPointType());
        }
        
        if (calleeInfo == null) {
            throw new Error("Could not find callee info for " + getJoinPointHint(jp));
        }
        return calleeInfo;
    }
    

}
