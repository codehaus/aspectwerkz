/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms 8of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.WeavingStrategy;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.weaver.ConstructorBodyVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.ConstructorCallVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.FieldSetFieldGetVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.FieldWrapperVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AddSerialVersionUidVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.JoinPointInitVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.MethodCallVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.MethodExecutionVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.MethodWrapperVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AlreadyAddedMethodAdapter;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AddInterfaceVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AddMixinMethodsVisitor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.attrs.Attributes;
import gnu.trove.TLongObjectHashMap;

/**
 * A weaving strategy implementing a weaving scheme based on statical compilation, and no reflection.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class InliningWeavingStrategy implements WeavingStrategy {

    /**
     * Performs the weaving of the target class.
     *
     * @param className
     * @param context
     */
    public void transform(String className, final Context context) {
        try {
            if (className.endsWith(TransformationConstants.JOIN_POINT_CLASS_SUFFIX) ||
                className.startsWith(TransformationConstants.ASPECTWERKZ_PACKAGE_NAME)) {
                return;
            }

            final byte[] bytecode = context.getInitialBytecode();
            final ClassLoader loader = context.getLoader();

            ClassInfo classInfo = AsmClassInfo.getClassInfo(bytecode, loader);

            final Set definitions = context.getDefinitions();
            final ExpressionContext[] ctxs = new ExpressionContext[]{
                new ExpressionContext(PointcutType.EXECUTION, classInfo, classInfo),
                new ExpressionContext(PointcutType.CALL, null, classInfo),
                new ExpressionContext(PointcutType.GET, classInfo, classInfo),
                new ExpressionContext(PointcutType.SET, classInfo, classInfo),
                new ExpressionContext(PointcutType.HANDLER, classInfo, classInfo),
                new ExpressionContext(PointcutType.STATIC_INITIALIZATION, classInfo, classInfo),
                new ExpressionContext(PointcutType.WITHIN, classInfo, classInfo)
            };

            if (classFilter(definitions, ctxs, classInfo)) {
                return;
            }

            // build the ClassInfo from the bytecode to avoid loading it from the loader resource stream later
            // to support stub weaving
            AsmClassInfo.getClassInfo(bytecode, loader);

            // compute CALL + GET/SET early matching results to avoid registering useless visitors
            final boolean filterForCall = classFilterFor(
                    definitions, new ExpressionContext[]{
                        new ExpressionContext(PointcutType.CALL, null, classInfo),
                        new ExpressionContext(PointcutType.WITHIN, classInfo, classInfo)
                    }
            );//FIXME - within make match all
            final boolean filterForGetSet = classFilterFor(
                    definitions, new ExpressionContext[]{
                        new ExpressionContext(PointcutType.GET, classInfo, classInfo),
                        new ExpressionContext(PointcutType.SET, classInfo, classInfo),
                        new ExpressionContext(PointcutType.WITHIN, classInfo, classInfo)
                    }
            );//FIXME - within make match all

            final ClassReader crLookahead = new ClassReader(bytecode);

            // prepare ctor call jp
            TLongObjectHashMap newInvocationsByCallerMemberHash = null;
            if (!filterForCall) {
                newInvocationsByCallerMemberHash = new TLongObjectHashMap();
                crLookahead.accept(
                        new ConstructorCallVisitor.LookaheadNewDupInvokeSpecialInstructionClassAdapter(
                                newInvocationsByCallerMemberHash
                        ),
                        true
                );
            }

            // gather wrapper methods to support multi-weaving
            // skip annotations visit and debug info by using the lookahead read-only classreader
            Set addedMethods = new HashSet();
            crLookahead.accept(new AlreadyAddedMethodAdapter(addedMethods), true);

            // -- Phase 1 --
            final ClassWriter writerPhase1 = AsmHelper.newClassWriter(true);
            final ClassReader readerPhase1 = new ClassReader(bytecode);
            ClassVisitor reversedChainPhase1 = writerPhase1;
            reversedChainPhase1 = new AddMixinMethodsVisitor(reversedChainPhase1, classInfo, context, addedMethods);
            reversedChainPhase1 = new AddInterfaceVisitor(reversedChainPhase1, classInfo, context);
            reversedChainPhase1 = new AddSerialVersionUidVisitor(reversedChainPhase1, classInfo, context);
            readerPhase1.accept(reversedChainPhase1, Attributes.getDefaultAttributes(), false);
            final byte[] bytesPhase1 = writerPhase1.toByteArray();

            // update the class info
            classInfo = AsmClassInfo.newClassInfo(bytesPhase1, loader);

            // -- Phase 2 --
            final ClassWriter writerPhase2 = AsmHelper.newClassWriter(true);
            final ClassReader readerPhase2 = new ClassReader(bytesPhase1);
            ClassVisitor reversedChainPhase2 = writerPhase2;
            reversedChainPhase2 = new JoinPointInitVisitor(reversedChainPhase2, context);
            reversedChainPhase2 = new MethodExecutionVisitor(reversedChainPhase2, classInfo, context, addedMethods);
            reversedChainPhase2 = new ConstructorBodyVisitor(reversedChainPhase2, classInfo, context, addedMethods);
            //reversedChainPhase2 = new HandlerVisitor(reversedChainPhase2, loader, classInfo, context, addedMethods); // TODO fix handler impl
            if (!filterForCall) {
                reversedChainPhase2 = new MethodCallVisitor(reversedChainPhase2, loader, classInfo, context);
                reversedChainPhase2 = new ConstructorCallVisitor(
                        reversedChainPhase2, loader, classInfo, context, newInvocationsByCallerMemberHash
                );
            }
            if (!filterForGetSet) {
                reversedChainPhase2 = new FieldSetFieldGetVisitor(reversedChainPhase2, loader, classInfo, context);
                reversedChainPhase2 = new FieldWrapperVisitor(reversedChainPhase2, classInfo, context, addedMethods);
            }
            reversedChainPhase2 = new MethodWrapperVisitor(reversedChainPhase2, classInfo, context, addedMethods);
            readerPhase2.accept(reversedChainPhase2, Attributes.getDefaultAttributes(), false);
            final byte[] bytesPhase2 = writerPhase2.toByteArray();

            // TODO: INNER CLASS OR NOT?
            // loop over emitted jp and flag them as inner classes
//            for (Iterator iterator = ((ContextImpl) context).getEmittedInlinedJoinPoint().iterator(); iterator.hasNext();) {
//                String joinPointClassName = ((ContextImpl.EmittedJoinPoint) iterator.next()).joinPointClassName;
//                int innerIndex = joinPointClassName.lastIndexOf('$');
//                cw.visitInnerClass(joinPointClassName,
//                        joinPointClassName.substring(0, innerIndex),
//                        joinPointClassName.substring(innerIndex + 1, joinPointClassName.length()),
//                        Constants.ACC_PUBLIC + Constants.ACC_STATIC);
//            }

            context.setCurrentBytecode(bytesPhase2);

            // NOTE: remove when in release time or in debugging trouble (;-) - Alex)
            // FAKE multiweaving - which is a requirement
            //            Object multi = context.getMetaData("FAKE");
            //            if (multi == null) {
            //                context.addMetaData("FAKE", "FAKE");
            //                transform(className, context);
            //            }

        } catch (Throwable t) {
            t.printStackTrace();
            throw new WrappedRuntimeException(t);
        }
    }

    /**
     * Creates a new transformation context.
     *
     * @param name
     * @param bytecode
     * @param loader
     * @return
     */
    public Context newContext(final String name, final byte[] bytecode, final ClassLoader loader) {
        return new ContextImpl(name, bytecode, loader);
    }

    /**
     * Filters out the classes that are not eligible for transformation.
     *
     * @param definitions the definitions
     * @param ctxs        an array with the contexts
     * @param classInfo   the class to filter
     * @return boolean true if the class should be filtered out
     */
    private static boolean classFilter(final Set definitions,
                                       final ExpressionContext[] ctxs,
                                       final ClassInfo classInfo) {
        if (classInfo.isInterface()) {
            return true;
        }
        for (Iterator defs = definitions.iterator(); defs.hasNext();) {
            if (classFilter((SystemDefinition) defs.next(), ctxs, classInfo)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Filters out the classes that are not eligible for transformation.
     *
     * @param definition the definition
     * @param ctxs       an array with the contexts
     * @param classInfo  the class to filter
     * @return boolean true if the class should be filtered out
     * @TODO: when a class had execution pointcut that were removed it must be unweaved, thus not filtered out How to
     * handle that? cache lookup? or custom class level attribute ?
     */
    private static boolean classFilter(final SystemDefinition definition,
                                       final ExpressionContext[] ctxs,
                                       final ClassInfo classInfo) {
        if (classInfo.isInterface()) {
            return true;
        }
        String className = classInfo.getName();
        if (definition.inExcludePackage(className)) {
            return true;
        }
        if (!definition.inIncludePackage(className)) {
            return true;
        }
        if (definition.isAdvised(ctxs)) {
            return false;
        }
        if (definition.hasMixin(ctxs)) {
            return false;
        }
        if (definition.hasIntroducedInterface(ctxs)) {
            return false;
        }
        if (definition.inPreparePackage(className)) {
            return false;
        }
        return true;
    }

    private static boolean classFilterFor(final Set definitions,
                                          final ExpressionContext[] ctxs) {
        for (Iterator defs = definitions.iterator(); defs.hasNext();) {
            if (classFilterFor((SystemDefinition) defs.next(), ctxs)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean classFilterFor(final SystemDefinition definition,
                                          final ExpressionContext[] ctxs) {
        if (definition.isAdvised(ctxs)) {
            return false;
        }
        return true;
    }

}