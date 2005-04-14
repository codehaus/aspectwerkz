/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.aspect.AdviceInfo;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.cflow.CflowCompiler;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.JoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.AdviceMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.AspectInfo;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * FIXME doc
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AspectWerkzAspectModel implements AspectModel, Constants, TransformationConstants {

    protected final List m_customProceedMethodStructs;


    public AspectWerkzAspectModel() {
        m_customProceedMethodStructs = null;
        //prototype
    }

    private AspectWerkzAspectModel(CompilationInfo.Model compilationModel) {
        m_customProceedMethodStructs = new ArrayList(0);
        collectCustomProceedMethods(compilationModel, compilationModel.getAdviceInfoContainer());


    }

    public static final String TYPE = AspectWerkzAspectModel.class.getName();

    public AspectModel getInstance(CompilationInfo.Model compilationModel) {
        // return a new instance to handle custom proceed
        return new AspectWerkzAspectModel(compilationModel);
    }

    public String getAspectModelType() {
        return TYPE;
    }

    public void defineAspect(ClassInfo aspectClassInfo, AspectDefinition aspectDef, ClassLoader loader) {
        //FIXME - refactor there ?
    }

    public AroundClosureClassInfo getAroundClosureClassInfo() {
        if (m_customProceedMethodStructs.isEmpty()) {
            //let the compiler deal with JP / SJP interface
            return new AroundClosureClassInfo(OBJECT_CLASS_NAME, new String[0]);
        } else {
            // get the custom join point interfaces
            Set interfaces = new HashSet();
            for (Iterator it = m_customProceedMethodStructs.iterator(); it.hasNext();) {
                MethodInfo methodInfo = ((CustomProceedMethodStruct) it.next()).customProceed;
                interfaces.add(methodInfo.getDeclaringType().getName().replace('.', '/'));
            }
            return new AroundClosureClassInfo(OBJECT_CLASS_NAME, (String[]) interfaces.toArray(new String[]{}));


        }
    }

    public void createMandatoryMethods(ClassWriter cw, JoinPointCompiler compiler) {
        createCustomProceedMethods(cw, (AbstractJoinPointCompiler)compiler);
    }

    public void createInvocationOfAroundClosureSuperClass(CodeVisitor cv) {
        ;// AW model has no super class apart Object, which is handled by the compiler
    }

    /**
     * Create and initialize the aspect field for a specific aspect (qualified since it depends
     * on the param, deployment model, container etc).
     * And creates instantiation of aspects using the Aspects.aspectOf() methods which uses the AspectContainer impls.
     * We are using the THIS_CLASS classloader since the aspect can be visible from that one only f.e. for get/set/call
     *
     * @param cw
     * @param cv
     * @param aspectInfo
     * @param joinPointClassName
     */
    public void createAndStoreStaticAspectInstantiation(ClassVisitor cw, CodeVisitor cv, AspectInfo aspectInfo, String joinPointClassName) {
        String aspectClassSignature = aspectInfo.getAspectClassSignature();
        String aspectClassName = aspectInfo.getAspectClassName();
        // retrieve the aspect set it to the field
        DeploymentModel deploymentModel = aspectInfo.getDeploymentModel();
        if (CflowCompiler.isCflowClass(aspectClassName)) {
            cw.visitField(ACC_PRIVATE + ACC_STATIC, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null);
            // handle Cflow native aspectOf
            cv.visitMethodInsn(
                    INVOKESTATIC,
                    aspectClassName,
                    CflowCompiler.CFLOW_ASPECTOF_METHOD_NAME,
                    "()" + aspectClassSignature
            );
            cv.visitFieldInsn(PUTSTATIC, joinPointClassName, aspectInfo.getAspectFieldName(), aspectClassSignature);
        } else if (deploymentModel.equals(DeploymentModel.PER_JVM)) {
            cw.visitField(ACC_PRIVATE + ACC_STATIC, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null);
            // AW-355, AW-415 we need a ClassLoader here
            cv.visitFieldInsn(GETSTATIC, joinPointClassName, THIS_CLASS_FIELD_NAME_IN_JP, CLASS_CLASS_SIGNATURE);
            cv.visitMethodInsn(
                    INVOKEVIRTUAL, CLASS_CLASS, GETCLASSLOADER_METHOD_NAME,
                    CLASS_CLASS_GETCLASSLOADER_METHOD_SIGNATURE
            );
            cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
            cv.visitLdcInsn(aspectInfo.getAspectDefinition().getContainerClassName());
            cv.visitMethodInsn(
                    INVOKESTATIC,
                    ASPECTS_CLASS_NAME,
                    ASPECT_OF_METHOD_NAME,
                    ASPECT_OF_PER_JVM_METHOD_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, aspectClassName);
            cv.visitFieldInsn(PUTSTATIC, joinPointClassName, aspectInfo.getAspectFieldName(), aspectClassSignature);
        } else if (deploymentModel.equals(DeploymentModel.PER_CLASS)) {
            cw.visitField(ACC_PRIVATE + ACC_STATIC, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null);
            cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
            cv.visitLdcInsn(aspectInfo.getAspectDefinition().getContainerClassName());
            cv.visitFieldInsn(GETSTATIC, joinPointClassName, THIS_CLASS_FIELD_NAME_IN_JP, CLASS_CLASS_SIGNATURE);
            cv.visitMethodInsn(
                    INVOKESTATIC,
                    ASPECTS_CLASS_NAME,
                    ASPECT_OF_METHOD_NAME,
                    ASPECT_OF_PER_CLASS_METHOD_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, aspectClassName);
            cv.visitFieldInsn(PUTSTATIC, joinPointClassName, aspectInfo.getAspectFieldName(), aspectClassSignature);
        } else if (AbstractJoinPointCompiler.requiresCallerOrCallee(deploymentModel)) {
            cw.visitField(ACC_PRIVATE, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null);
        } else {
            throw new UnsupportedOperationException(
                    "unsupported deployment model - " +
                    aspectInfo.getAspectClassName() + " " +
                    deploymentModel
            );
        }
    }

    /**
     * Initializes instance level aspects, retrieves them from the target instance through the
     * <code>HasInstanceLevelAspect</code> interfaces.
     * <p/>
     * Use by 'perInstance', 'perThis' and 'perTarget' deployment models.
     *
     * @param cv
     * @param aspectInfo
     * @param input
     */
    public void createAndStoreRuntimeAspectInstantiation(final CodeVisitor cv,
                                                         final CompilerInput input,
                                                         final AspectInfo aspectInfo) {
        // gen code: if (Aspects.hasAspect(...) { aspectField = (<TYPE>)((HasInstanceLocalAspect)CALLER).aw$getAspect(className, qualifiedName, containerClassName) }
        if (DeploymentModel.PER_INSTANCE.equals(aspectInfo.getDeploymentModel())) {//TODO && callerIndex >= 0
            storeAspectInstance(cv, input, aspectInfo, input.callerIndex);
        } else if (DeploymentModel.PER_THIS.equals(aspectInfo.getDeploymentModel())
                && input.callerIndex >= 0) {
            Label hasAspectCheck = pushPerXCondition(cv, input.callerIndex, aspectInfo.getAspectQualifiedName());
            storeAspectInstance(cv, input, aspectInfo, input.callerIndex);
            cv.visitLabel(hasAspectCheck);
        } else if (DeploymentModel.PER_TARGET.equals(aspectInfo.getDeploymentModel())
                && input.calleeIndex >= 0) {
            Label hasAspectCheck = pushPerXCondition(cv, input.calleeIndex, aspectInfo.getAspectQualifiedName());
            storeAspectInstance(cv, input, aspectInfo, input.calleeIndex);
            cv.visitLabel(hasAspectCheck);
        }

        if (aspectInfo.getDeploymentModel() == DeploymentModel.PER_INSTANCE) {//TODO refactor with previous if block
            // gen code: aspectField = (<TYPE>)((HasInstanceLocalAspect)CALLER).aw$getAspect(className, qualifiedName, containerClassName)
            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            if (input.callerIndex >= 0) {
                cv.visitVarInsn(ALOAD, input.callerIndex);
            } else {
                // caller instance not available - skipping
                //TODO clean up should not occur
            }
            cv.visitLdcInsn(aspectInfo.getAspectClassName().replace('/', '.'));
            cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
            cv.visitLdcInsn(aspectInfo.getAspectDefinition().getContainerClassName());
            cv.visitMethodInsn(
                    INVOKEINTERFACE,
                    HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME,
                    GET_INSTANCE_LEVEL_ASPECT_METHOD_NAME,
                    GET_INSTANCE_LEVEL_ASPECT_METHOD_SIGNATURE
            );
            cv.visitTypeInsn(CHECKCAST, aspectInfo.getAspectClassName());
            cv.visitFieldInsn(
                    PUTFIELD,
                    input.joinPointClassName,
                    aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
        }
    }


    /**
     * Load aspect instance on stack
     *
     * @param cv
     * @param input
     * @param aspectInfo
     */
    public void loadAspect(final CodeVisitor cv,
                           final CompilerInput input,
                           final AspectInfo aspectInfo) {
        DeploymentModel deploymentModel = aspectInfo.getDeploymentModel();
        if (DeploymentModel.PER_JVM.equals(deploymentModel)
                || DeploymentModel.PER_CLASS.equals(deploymentModel)) {
            cv.visitFieldInsn(
                    GETSTATIC, input.joinPointClassName, aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
        } else if (DeploymentModel.PER_INSTANCE.equals(deploymentModel)) {
            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            cv.visitFieldInsn(
                    GETFIELD, input.joinPointClassName, aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
        } else if (DeploymentModel.PER_THIS.equals(deploymentModel)) {
            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            cv.visitFieldInsn(
                    GETFIELD,
                    input.joinPointClassName,
                    aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );

            //FIXME see FIXME on aspect instantion
            Label nullCheck = new Label();
            cv.visitJumpInsn(IFNONNULL, nullCheck);
            storeAspectInstance(cv, input, aspectInfo, input.callerIndex);
            cv.visitLabel(nullCheck);

            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            cv.visitFieldInsn(
                    GETFIELD,
                    input.joinPointClassName,
                    aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
        } else if (DeploymentModel.PER_TARGET.equals(deploymentModel)) {
            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            cv.visitFieldInsn(
                    GETFIELD,
                    input.joinPointClassName,
                    aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
            //FIXME see FIXME on aspect instantion

            Label nullCheck = new Label();
            cv.visitJumpInsn(IFNONNULL, nullCheck);
            storeAspectInstance(cv, input, aspectInfo, input.calleeIndex);
            cv.visitLabel(nullCheck);

            AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            cv.visitFieldInsn(
                    GETFIELD,
                    input.joinPointClassName,
                    aspectInfo.getAspectFieldName(),
                    aspectInfo.getAspectClassSignature()
            );
        } else {
            throw new DefinitionException("deployment model [" + deploymentModel + "] is not supported");
        }
    }

    public void createAroundAdviceArgumentHandling(CodeVisitor cv,
                                                   CompilerInput input,
                                                   Type[] joinPointArgumentTypes,
                                                   AdviceMethodInfo adviceMethodInfo) {
        defaultCreateAroundAdviceArgumentHandling(
                cv,
                input,
                joinPointArgumentTypes,
                adviceMethodInfo
        );
    }

    public void createBeforeOrAfterAdviceArgumentHandling(CodeVisitor cv,
                                                          CompilerInput input,
                                                          Type[] joinPointArgumentTypes,
                                                          AdviceMethodInfo adviceMethodInfo,
                                                          int specialArgIndex) {
        defaultCreateBeforeOrAfterAdviceArgumentHandling(
                cv,
                input,
                joinPointArgumentTypes,
                adviceMethodInfo,
                specialArgIndex
        );
    }

    public boolean requiresReflectiveInfo() {
        // custom proceed() JoinPoint will not be recognize by the default logic
        return m_customProceedMethodStructs.size() > 0;
    }

    ///////---------- Helpers

    /**
     * Generate a "if Aspects.hasAspect(qName, instance)"
     *
     * @param cv
     * @param perInstanceIndex
     * @param aspectQName
     * @return
     */
    private Label pushPerXCondition(final CodeVisitor cv,
                                    final int perInstanceIndex,
                                    final String aspectQName) {
        Label hasAspectCheck = new Label();

        cv.visitLdcInsn(aspectQName);
        cv.visitVarInsn(ALOAD, perInstanceIndex);
        cv.visitMethodInsn(
                INVOKESTATIC,
                ASPECTS_CLASS_NAME,
                HASASPECT_METHOD_NAME,
                HASASPECT_METHOD_SIGNATURE
        );
        cv.visitJumpInsn(IFEQ, hasAspectCheck);

        return hasAspectCheck;
    }

    /**
     * Creates the instance of an aspect by invoking
     * "HasInstanceLevelAspect.aw$getAspect(String, String)" on perInstanceIndex variable
     * and stores the aspect instance in the joinpoint instance field
     */
    private void storeAspectInstance(final CodeVisitor cv,
                                     final CompilerInput input,
                                     final AspectInfo aspectInfo,
                                     final int perInstanceIndex) {
        AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);

        if (perInstanceIndex >= 0) {
            cv.visitVarInsn(ALOAD, perInstanceIndex);
        }

        cv.visitLdcInsn(aspectInfo.getAspectClassName().replace('/', '.'));
        cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
        cv.visitLdcInsn(aspectInfo.getAspectDefinition().getContainerClassName());
        cv.visitMethodInsn(
                INVOKEINTERFACE,
                HAS_INSTANCE_LEVEL_ASPECT_INTERFACE_NAME,
                GET_INSTANCE_LEVEL_ASPECT_METHOD_NAME,
                GET_INSTANCE_LEVEL_ASPECT_METHOD_SIGNATURE
        );

        cv.visitTypeInsn(CHECKCAST, aspectInfo.getAspectClassName());

        cv.visitFieldInsn(
                PUTFIELD,
                input.joinPointClassName,
                aspectInfo.getAspectFieldName(),
                aspectInfo.getAspectClassSignature()
        );
    }

    /**
     * Creates the custom proceed methods.
     */
    private void createCustomProceedMethods(ClassWriter cw, AbstractJoinPointCompiler compiler) {
        Set addedMethodSignatures = new HashSet();
        for (Iterator it = m_customProceedMethodStructs.iterator(); it.hasNext();) {
            CustomProceedMethodStruct customProceedStruct = (CustomProceedMethodStruct) it.next();
            MethodInfo methodInfo = customProceedStruct.customProceed;
            final String desc = methodInfo.getSignature();

            if (addedMethodSignatures.contains(desc)) {
                continue;
            }
            addedMethodSignatures.add(desc);

            CodeVisitor cv = cw.visitMethod(
                    ACC_PUBLIC | ACC_FINAL,
                    PROCEED_METHOD_NAME,
                    desc,
                    new String[]{
                        THROWABLE_CLASS_NAME
                    },
                    null
            );

            // update the joinpoint instance with the given values
            // starts at 1 since first arg is the custom join point by convention
            //TODO see JoinPointManage for this custom jp is first convention
            int argStackIndex = 1;
            for (int i = 1; i < customProceedStruct.adviceToTargetArgs.length; i++) {
                int targetArg = customProceedStruct.adviceToTargetArgs[i];
                if (targetArg >= 0) {
                    // regular arg
                    String fieldName = compiler.m_fieldNames[targetArg];
                    cv.visitVarInsn(ALOAD, 0);
                    Type type = compiler.m_argumentTypes[targetArg];
                    argStackIndex = AsmHelper.loadType(cv, argStackIndex, type);
                    cv.visitFieldInsn(PUTFIELD, compiler.m_joinPointClassName, fieldName, type.getDescriptor());
                } else if (targetArg == AdviceInfo.TARGET_ARG) {
                    cv.visitVarInsn(ALOAD, 0);
                    argStackIndex = AsmHelper.loadType(
                            cv, argStackIndex, Type.getType(compiler.m_calleeClassSignature)
                    );
                    cv.visitFieldInsn(
                            PUTFIELD,
                            compiler.m_joinPointClassName,
                            CALLEE_INSTANCE_FIELD_NAME,
                            compiler.m_calleeClassSignature
                    );
                } else if (targetArg == AdviceInfo.THIS_ARG) {
                    cv.visitVarInsn(ALOAD, 0);
                    argStackIndex = AsmHelper.loadType(
                            cv, argStackIndex, Type.getType(compiler.m_callerClassSignature)
                    );
                    cv.visitFieldInsn(
                            PUTFIELD,
                            compiler.m_joinPointClassName,
                            CALLER_INSTANCE_FIELD_NAME,
                            compiler.m_callerClassSignature
                    );
                } else {
                    ;//skip it
                }
            }

            // call proceed()
            // and handles unwrapping for returning primitive
            Type returnType = Type.getType(customProceedStruct.customProceed.getReturnType().getSignature());
            if (AsmHelper.isPrimitive(returnType)) {
                cv.visitVarInsn(ALOAD, 0);
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        compiler.m_joinPointClassName,
                        PROCEED_METHOD_NAME,
                        PROCEED_METHOD_SIGNATURE
                );
                AsmHelper.unwrapType(cv, returnType);
            } else {
                cv.visitVarInsn(ALOAD, 0);
                cv.visitMethodInsn(
                        INVOKESPECIAL,
                        compiler.m_joinPointClassName,
                        PROCEED_METHOD_NAME,
                        PROCEED_METHOD_SIGNATURE
                );
                if (!returnType.getClassName().equals(OBJECT_CLASS_SIGNATURE)) {
                    cv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
                }
            }
            AsmHelper.addReturnStatement(cv, returnType);
            cv.visitMaxs(0, 0);
        }
    }

    //---------- Model specific methods

    /**
     * Collects the custom proceed methods used in the advice specified.
     *
     * @param model
     * @param advices
     */
    private void collectCustomProceedMethods(final CompilationInfo.Model model,
                                             final AdviceInfoContainer advices) {
        ClassLoader loader = model.getThisClassInfo().getClassLoader();
        final AdviceInfo[] beforeAdviceInfos = advices.getBeforeAdviceInfos();
        for (int i = 0; i < beforeAdviceInfos.length; i++) {
            collectCustomProceedMethods(beforeAdviceInfos[i], loader);
        }
        final AdviceInfo[] aroundAdviceInfos = advices.getAroundAdviceInfos();
        for (int i = 0; i < aroundAdviceInfos.length; i++) {
            collectCustomProceedMethods(aroundAdviceInfos[i], loader);
        }
        final AdviceInfo[] afterFinallyAdviceInfos = advices.getAfterFinallyAdviceInfos();
        for (int i = 0; i < afterFinallyAdviceInfos.length; i++) {
            collectCustomProceedMethods(afterFinallyAdviceInfos[i], loader);
        }
        final AdviceInfo[] afterReturningAdviceInfos = advices.getAfterReturningAdviceInfos();
        for (int i = 0; i < afterReturningAdviceInfos.length; i++) {
            collectCustomProceedMethods(afterReturningAdviceInfos[i], loader);
        }
        final AdviceInfo[] afterThrowingAdviceInfos = advices.getAfterThrowingAdviceInfos();
        for (int i = 0; i < afterThrowingAdviceInfos.length; i++) {
            collectCustomProceedMethods(afterThrowingAdviceInfos[i], loader);
        }
    }

    /**
     * Collects the custom proceed methods used in the advice specified.
     *
     * @param adviceInfo
     * @param loader
     */
    private void collectCustomProceedMethods(final AdviceInfo adviceInfo, final ClassLoader loader) {
        final Type[] paramTypes = adviceInfo.getMethodParameterTypes();
        if (paramTypes.length != 0) {
            Type firstParam = paramTypes[0];
            //TODO should we support JP at other positions or lock the other advice models then so that JP..
            // ..is not there or first only ?
            // check if first param is an object but not a JP or SJP
            if (firstParam.getSort() == Type.OBJECT &&
                    !firstParam.getClassName().equals(JOIN_POINT_JAVA_CLASS_NAME) &&
                    !firstParam.getClassName().equals(STATIC_JOIN_POINT_JAVA_CLASS_NAME)) {
                ClassInfo classInfo = AsmClassInfo.getClassInfo(firstParam.getClassName(), loader);
                if (ClassInfoHelper.implementsInterface(classInfo, JOIN_POINT_JAVA_CLASS_NAME) ||
                        ClassInfoHelper.implementsInterface(classInfo, STATIC_JOIN_POINT_JAVA_CLASS_NAME)) {
                    // we have ourselves a custom joinpoint
                    MethodInfo[] methods = classInfo.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        MethodInfo method = methods[j];
                        if (method.getName().equals(PROCEED_METHOD_NAME)) {
                            // we inherit the binding from the advice that actually use us
                            // for now the first advice sets the rule
                            // it is up to the user to ensure consistency if the custom proceed
                            // is used more than once in different advices.
                            m_customProceedMethodStructs.add(
                                    new CustomProceedMethodStruct(
                                            method,
                                            adviceInfo.getMethodToArgIndexes()
                                    )
                            );
                        }
                    }
                }
            }
        }
    }

    private static class CustomProceedMethodStruct {
        MethodInfo customProceed;
        int[] adviceToTargetArgs;

        public CustomProceedMethodStruct(MethodInfo customProceed, int[] adviceToTargetArgs) {
            this.customProceed = customProceed;
            this.adviceToTargetArgs = adviceToTargetArgs;
        }
    }

    public static void defaultCreateBeforeOrAfterAdviceArgumentHandling(CodeVisitor cv,
                                                                        CompilerInput input,
                                                                        Type[] joinPointArgumentTypes,
                                                                        AdviceMethodInfo adviceMethodInfo,
                                                                        int specialArgIndex) {
        int[] argIndexes = adviceMethodInfo.getAdviceMethodArgIndexes();
        // if empty, we consider for now that we have to push JoinPoint for old advice with JoinPoint as sole arg
        for (int j = 0; j < argIndexes.length; j++) {
            int argIndex = argIndexes[j];
            if (argIndex >= 0) {
                Type argumentType = joinPointArgumentTypes[argIndex];
                int argStackIndex = AsmHelper.getRegisterIndexOf(joinPointArgumentTypes, argIndex) + input.argStartIndex;
                AsmHelper.loadType(cv, argStackIndex, argumentType);
            } else if (argIndex == AdviceInfo.JOINPOINT_ARG || argIndex == AdviceInfo.STATIC_JOINPOINT_ARG) {
                AbstractJoinPointCompiler.loadJoinPointInstance(cv, input);
            } else if (argIndex == AdviceInfo.TARGET_ARG) {
                AbstractJoinPointCompiler.loadCallee(cv, input);
                // add a cast if runtime check was used
                if (adviceMethodInfo.getAdviceInfo().hasTargetWithRuntimeCheck()) {
                    cv.visitTypeInsn(
                            CHECKCAST,
                            adviceMethodInfo.getAdviceInfo().getMethodParameterTypes()[j].getInternalName()
                    );
                }
            } else if (argIndex == AdviceInfo.THIS_ARG) {
                AbstractJoinPointCompiler.loadCaller(cv, input);
            } else if (argIndex == AdviceInfo.SPECIAL_ARGUMENT && specialArgIndex != INDEX_NOTAVAILABLE) {
                Type argumentType = adviceMethodInfo.getAdviceInfo().getMethodParameterTypes()[j];
                AsmHelper.loadType(cv, specialArgIndex, argumentType);
                if (adviceMethodInfo.getAdviceInfo().getAdviceDefinition().getType().equals(AdviceType.AFTER_THROWING)) {
                    cv.visitTypeInsn(CHECKCAST, argumentType.getInternalName());
                }
            } else {
                throw new Error("magic index is not supported: " + argIndex);
            }
        }
    }

    public static void defaultCreateAroundAdviceArgumentHandling(CodeVisitor cv,
                                                                 CompilerInput input,
                                                                 Type[] joinPointArgumentTypes,
                                                                 AdviceMethodInfo adviceMethodInfo) {
        int[] argIndexes = adviceMethodInfo.getAdviceMethodArgIndexes();
        for (int j = 0; j < argIndexes.length; j++) {
            int argIndex = argIndexes[j];
            if (argIndex >= 0) {
                Type argumentType = joinPointArgumentTypes[argIndex];
                cv.visitVarInsn(ALOAD, 0);
                cv.visitFieldInsn(
                        GETFIELD,
                        input.joinPointClassName,
                        ARGUMENT_FIELD + argIndex,
                        argumentType.getDescriptor()
                );
            } else if (argIndex == AdviceInfo.JOINPOINT_ARG ||
                    argIndex == AdviceInfo.STATIC_JOINPOINT_ARG ||
                    argIndex == AdviceInfo.VALID_NON_AW_AROUND_CLOSURE_TYPE ||
                    argIndex == AdviceInfo.CUSTOM_JOIN_POINT_ARG) {
                cv.visitVarInsn(ALOAD, 0);
            } else if (argIndex == AdviceInfo.TARGET_ARG) {
                AbstractJoinPointCompiler.loadCallee(cv, input);
                // add a cast if runtime check was used
                if (adviceMethodInfo.getAdviceInfo().hasTargetWithRuntimeCheck()) {
                    cv.visitTypeInsn(
                            CHECKCAST,
                            adviceMethodInfo.getAdviceInfo().getMethodParameterTypes()[j].getInternalName()
                    );
                }
            } else if (argIndex == AdviceInfo.THIS_ARG) {
                AbstractJoinPointCompiler.loadCaller(cv, input);
            } else {
                throw new Error("advice method argument index type is not supported: " + argIndex);
            }
        }
    }

}
