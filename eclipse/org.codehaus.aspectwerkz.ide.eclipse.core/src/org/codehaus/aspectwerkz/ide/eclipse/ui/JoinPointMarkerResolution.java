/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.ide.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.aspectwerkz.aspect.AdviceInfo;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwCorePlugin;
import org.codehaus.aspectwerkz.ide.eclipse.core.AwLog;
import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * @author avasseur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JoinPointMarkerResolution implements IMarkerResolutionGenerator2 {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return (marker.getAttribute(WeaverListener.JOINPOINT_ATTRIBUTE) != null
                    && marker.getAttribute(WeaverListener.ADVICECONTAINER_ATTRIBUTE) != null);
        } catch (CoreException e) {
            AwLog.logError(e);
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        try {
            EmittedJoinPoint jp = (EmittedJoinPoint) marker.getAttribute(WeaverListener.JOINPOINT_ATTRIBUTE);
            AdviceInfoContainer adc = (AdviceInfoContainer) marker.getAttribute(WeaverListener.ADVICECONTAINER_ATTRIBUTE);
            IJavaProject jproject = (IJavaProject) marker.getAttribute(WeaverListener.JAVAPROJECT_ATTRIBUTE);
            
            List resolutions = new ArrayList();
            
            for (int i = 0; i < adc.getBeforeAdviceInfos().length; i++) {
                resolutions.add(new AdviceMarkerResolution(adc.getBeforeAdviceInfos()[i], jproject));
            }
            for (int i = 0; i < adc.getAroundAdviceInfos().length; i++) {
                resolutions.add(new AdviceMarkerResolution(adc.getAroundAdviceInfos()[i], jproject));
            }
            for (int i = 0; i < adc.getAfterReturningAdviceInfos().length; i++) {
                resolutions.add(new AdviceMarkerResolution(adc.getAfterReturningAdviceInfos()[i], jproject));
            }
            for (int i = 0; i < adc.getAfterThrowingAdviceInfos().length; i++) {
                resolutions.add(new AdviceMarkerResolution(adc.getAfterThrowingAdviceInfos()[i], jproject));
            }
            for (int i = 0; i < adc.getAfterFinallyAdviceInfos().length; i++) {
                resolutions.add(new AdviceMarkerResolution(adc.getAfterFinallyAdviceInfos()[i], jproject));
            }
            
            return (IMarkerResolution[]) resolutions.toArray(new IMarkerResolution[]{});
        } catch (CoreException e) {
            AwLog.logError(e);
            return new IMarkerResolution[0];
        }
    }
    
    private static class AdviceMarkerResolution implements IMarkerResolution {
        
        private final AdviceInfo m_adviceInfo;
        
        private final String m_label;
        
        private final IJavaProject m_jproject;
        
        public AdviceMarkerResolution(AdviceInfo advice, IJavaProject jproject) {
            m_adviceInfo = advice;
            m_jproject = jproject;
            
            //label
            StringBuffer sb = new StringBuffer();
            if (m_adviceInfo.hasTargetWithRuntimeCheck() || m_adviceInfo.getAdviceDefinition().hasCflowOrCflowBelow()) {
                sb.append('?');
            }
            sb.append('[').append(m_adviceInfo.getAdviceDefinition().getType().toString()).append(']');
            sb.append(m_adviceInfo.getAspectClassName());
            sb.append('.').append(m_adviceInfo.getMethodName());
            m_label = sb.toString();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IMarkerResolution#getLabel()
         */
        public String getLabel() {
            return m_label;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker) {
            String aspectClassName = m_adviceInfo.getAspectClassName();
            String adviceMethodName = m_adviceInfo.getMethodName();
            String adviceDesc = m_adviceInfo.getMethodSignature();
            
            try {
                IType aspect = m_jproject.findType(aspectClassName.replace('/', '.'));
                if (aspect != null) {
                    IResource resource = aspect.getUnderlyingResource();
                    if (resource != null) {
                        IMethod advice = WeaverListener.findMethod(aspect, adviceMethodName, adviceDesc);
                        IEditorPart editor = JavaUI.openInEditor(advice);
                        JavaUI.revealInEditor(editor, (IJavaElement)advice);
                    }
                }
            } catch (Exception e) {
                AwLog.logError(e);
                return;
            }
        }
    }
    
    

}
