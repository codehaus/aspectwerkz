/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.transform.Compiler;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.AdviceMethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AspectInfo;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModel;
import org.codehaus.aspectwerkz.transform.inlining.spi.AspectModelManager;
import org.codehaus.aspectwerkz.transform.inlining.weaver.RuntimeCheckVisitor;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.joinpoint.management.AdviceInfoContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for the different join point compiler implementations.
 * <p/>
 * Compiles/generates a class that represents a specific join point, a class which invokes the advices
 * and the target join point statically.
 * <p/>
 * FIXME: depending on hotswap needs, remove the implements StaticJP or JP decision
 * FIXME: remove isOptimizedJP and put it global
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public abstract class AbstractJoinPointCompiler implements Compiler, Constants, TransformationConstants {

    protected static final String TARGET_CLASS_FIELD_NAME = "TARGET_CLASS";

    // FIXME define these two using VM option - if dump dir specified then dump
    protected static final boolean DUMP_CLASSES = true;
    protected static final String DUMP_DIR = "_dump/jp";

    protected final String m_callerClassName;
    protected final String m_calleeClassName;
    protected final String m_callerClassSignature;
    protected final String m_calleeClassSignature;
    protected final String m_joinPointClassName;
    protected final int m_joinPointType;
    protected final int m_joinPointHash;
    protected final String m_callerMethodName;
    protected final String m_callerMethodDesc;
    protected final int m_callerMethodModifiers;
    protected final String m_calleeMemberName;
    protected final String m_calleeMemberDesc;
    protected final int m_calleeMemberModifiers;

    protected ClassWriter m_cw;
    protected AspectInfo[] m_aspectInfos;
    protected AspectModel[] m_aspectModels;
    protected AdviceMethodInfo[] m_aroundAdviceMethodInfos;
    protected AdviceMethodInfo[] m_beforeAdviceMethodInfos;
    protected AdviceMethodInfo[] m_afterFinallyAdviceMethodInfos;
    protected AdviceMethodInfo[] m_afterReturningAdviceMethodInfos;
    protected AdviceMethodInfo[] m_afterThrowingAdviceMethodInfos;

    protected boolean m_hasAroundAdvices = false;
    protected boolean m_requiresThisOrTarget = false;
    protected boolean m_requiresJoinPoint = false;

    protected String[] m_fieldNames;
    protected Type[] m_argumentTypes;
    protected Type m_returnType;

    /**
     * Creates a new join point compiler instance.
     *
     * @param model
     */
    public AbstractJoinPointCompiler(final CompilationInfo.Model model) {

        m_joinPointClassName = model.getJoinPointClassName();

        final EmittedJoinPoint emittedJoinPoint = model.getEmittedJoinPoint();
        final AdviceInfoContainer advices = model.getAdviceInfoContainer();

        m_joinPointHash = emittedJoinPoint.getJoinPointHash();
        m_joinPointType = emittedJoinPoint.getJoinPointType();

        m_callerMethodName = emittedJoinPoint.getCallerMethodName();
        m_callerMethodDesc = emittedJoinPoint.getCallerMethodDesc();
        m_callerMethodModifiers = emittedJoinPoint.getCallerMethodModifiers();

        m_calleeMemberName = emittedJoinPoint.getCalleeMemberName();
        m_calleeMemberDesc = emittedJoinPoint.getCalleeMemberDesc();
        m_calleeMemberModifiers = emittedJoinPoint.getCalleeMemberModifiers();

        // NOTE: internal compiler class name format is ALWAYS using '/'
        m_callerClassName = emittedJoinPoint.getCallerClassName().replace('.', '/');
        m_calleeClassName = emittedJoinPoint.getCalleeClassName().replace('.', '/');
        m_callerClassSignature = L + emittedJoinPoint.getCallerClassName().replace('.', '/') + SEMICOLON;
        m_calleeClassSignature = L + emittedJoinPoint.getCalleeClassName().replace('.', '/') + SEMICOLON;

        m_argumentTypes = getJoinPointArgumentTypes();
        m_returnType = getJoinPointReturnType();

        initialize(advices);
    }

    /**
     * Initializes the the join point compiler.
     *
     * @param advices the new set of advice to be invoked at this join point
     */
    private synchronized void initialize(final AdviceInfoContainer advices) {

        // create the aspect fields
        List aspectQualifiedNames = new ArrayList();// in fact a Set but we need indexOf
        Set aspectInfos = new HashSet();
        m_beforeAdviceMethodInfos = getAdviceMethodInfos(
                aspectQualifiedNames, aspectInfos, advices.getBeforeAdviceInfos()
        );
        m_aroundAdviceMethodInfos = getAdviceMethodInfos(
                aspectQualifiedNames, aspectInfos, advices.getAroundAdviceInfos()
        );
        m_afterReturningAdviceMethodInfos = getAdviceMethodInfos(
                aspectQualifiedNames, aspectInfos, advices.getAfterReturningAdviceInfos()
        );
        m_afterFinallyAdviceMethodInfos = getAdviceMethodInfos(
                aspectQualifiedNames, aspectInfos, advices.getAfterFinallyAdviceInfos()
        );
        m_afterThrowingAdviceMethodInfos = getAdviceMethodInfos(
                aspectQualifiedNames, aspectInfos, advices.getAfterThrowingAdviceInfos()
        );

        m_aspectInfos = (AspectInfo[]) aspectInfos.toArray(new AspectInfo[aspectInfos.size()]);

        m_cw = AsmHelper.newClassWriter(true);

        // compute the optimization we can use
        m_hasAroundAdvices = m_aroundAdviceMethodInfos.length > 0;
        m_requiresThisOrTarget = requiresThisOrTarget();
        m_requiresJoinPoint = requiresJoinPoint();

        setupReferencedAspectModels();

        createClassHeader();

        for (int i = 0; i < m_aspectModels.length; i++) {
            m_aspectModels[i].createMandatoryMethods(m_cw, m_joinPointClassName);
        }
    }

    /**
     * Retrives and sets the aspect models that are referenced in this compilation phase.
     */
    private void setupReferencedAspectModels() {
        Map aspectModelMap = new HashMap();
        for (int i = 0; i < m_aspectInfos.length; i++) {
            AspectDefinition aspectDef = m_aspectInfos[i].getAspectDefinition();
            if (aspectDef.isAspectWerkzAspect()) {
                continue; // AW Aspect Model not managed by AspectModelManager
            }
            String type = aspectDef.getAspectModel();
            AspectModel aspectModel = AspectModelManager.getModelFor(type);
            aspectModelMap.put(type, aspectModel);
            if (aspectModel.requiresRttiInfo()) {
                m_requiresJoinPoint = true; // if at least one model requries RTTI then build it
            }
        }
        m_aspectModels = (AspectModel[])aspectModelMap.values().toArray(new AspectModel[aspectModelMap.size()]);
    }

    /**
     * Creates the class header for the join point.
     */
    private void createClassHeader() {

        List interfaces = new ArrayList();
        String baseClass = OBJECT_CLASS_NAME;

        for (int i = 0; i < m_aspectModels.length; i++) {
            AspectModel aspectModel = m_aspectModels[i];
            AspectModel.AroundClosureClassInfo closureClassInfo = aspectModel.getAroundClosureClassInfo();
            if (closureClassInfo.isClass()) {
                if (!baseClass.equals(OBJECT_CLASS_NAME)) {
                    throw new RuntimeException(
                            "compiled join point can only subclass one around closure base class but more than registered aspect model requires a closure base class"
                    );
                }
                baseClass = closureClassInfo.getClassName();
            } else if (closureClassInfo.isInterface()) {
                interfaces.add(closureClassInfo.getClassName());
            }
        }
        int i = 1;
        String[] interfaceArr = new String[interfaces.size() + 1];
        interfaceArr[0] = getJoinPointInterface();
        for (Iterator it = interfaces.iterator(); it.hasNext(); i++) {
            interfaceArr[i] = (String) it.next();
        }

        m_cw.visit(
                AsmHelper.JAVA_VERSION,
                ACC_PUBLIC + ACC_SUPER,
                m_joinPointClassName,
                baseClass,
                interfaceArr,
                null
        );
    }

    /**
     * Returns the join point interface class name.
     *
     * @return
     */
    private String getJoinPointInterface() {
        //FIXME for Hotswap JP we need one single interface
        String joinPointInterface;
        if (m_hasAroundAdvices || m_requiresJoinPoint) {
            joinPointInterface = JOIN_POINT_CLASS_NAME;
        } else {
            joinPointInterface = STATIC_JOIN_POINT_CLASS_NAME;
        }
        return joinPointInterface;
    }

    /**
     * Retrieves the advice method infos.
     *
     * @param aspectQualifiedNames
     * @param aspectInfos
     * @param adviceInfos
     * @return
     */
    protected AdviceMethodInfo[] getAdviceMethodInfos(final List aspectQualifiedNames,
                                                      final Set aspectInfos,
                                                      final AdviceInfo[] adviceInfos) {
        final AdviceMethodInfo[] adviceMethodInfos = new AdviceMethodInfo[adviceInfos.length];
        for (int i = 0; i < adviceInfos.length; i++) {
            AdviceInfo adviceInfo = adviceInfos[i];
            final String aspectClassName = adviceInfo.getAspectClassName().replace('.', '/');

            if (!aspectQualifiedNames.contains(adviceInfo.getAspectQualifiedName())) {
                aspectQualifiedNames.add(adviceInfo.getAspectQualifiedName());
            }
            int aspectIndex = aspectQualifiedNames.indexOf(adviceInfo.getAspectQualifiedName());
            AdviceMethodInfo adviceMethodInfo = new AdviceMethodInfo(
                    adviceInfo,
                    ASPECT_FIELD_PREFIX + aspectIndex,
                    aspectClassName,
                    L + aspectClassName + SEMICOLON
            );
            adviceMethodInfos[i] = adviceMethodInfo;
            aspectInfos.add(adviceMethodInfo.getAspectInfo());
        }
        return adviceMethodInfos;
    }

    /**
     * Creates fields common for all join point classes.
     */
    protected abstract void createFieldsCommonToAllJoinPoints();

    /**
     * Creates join point specific fields.
     */
    protected abstract void createJoinPointSpecificFields();

    /**
     * Creates the signature for the join point.
     * <p/>
     * FIXME signature field should NOT be of type Signature but of the specific type (update all refs as well)
     *
     * @param cv
     */
    protected abstract void createSignature(final CodeVisitor cv);

    /**
     * Optimized implementation that does not retrieve the parameters from the join point instance but is passed
     * directly to the method from the input parameters in the 'invoke' method. Can only be used if no around advice
     * exists.
     *
     * @param cv
     * @param argStartIndex index on stack of first target method arg (0 or 1, depends of static target or not)
     */
    protected abstract void createInlinedJoinPointInvocation(final CodeVisitor cv,
                                                             final boolean isOptimizedJoinPoint,
                                                             final int argStartIndex,
                                                             final int joinPointIndex);

    /**
     * Creates a call to the target join point, the parameter(s) to the join point are retrieved from the invocation
     * local join point instance.
     *
     * @param cv
     */
    protected abstract void createJoinPointInvocation(final CodeVisitor cv);

    /**
     * Returns the join points return type.
     *
     * @return
     */
    protected abstract Type getJoinPointReturnType();

    /**
     * Returns the join points argument type(s).
     *
     * @return
     */
    protected abstract Type[] getJoinPointArgumentTypes();

    /**
     * Creates the getRtti method
     */
    protected abstract void createGetRttiMethod();

    /**
     * Creates the getSignature method
     */
    protected abstract void createGetSignatureMethod();

    /**
     * Compiles a join point class, one specific class for each distinct join point. The compiled join point class
     * inherits the base join point class.
     *
     * @return the generated, compiled and loaded join point class
     */
    public byte[] compile() {
        try {
            // TODO: INNER CLASS OR NOT?
            // flag it as a public static inner class
            // Note: if <init> changes, we will need to pass the containing instance as arg0 and add a syntetic field
//            int innerIndex = m_joinPointClassName.lastIndexOf('$');
//            m_cw.visitInnerClass(m_joinPointClassName,
//                    m_joinPointClassName.substring(0, innerIndex),
//                    m_joinPointClassName.substring(innerIndex + 1, m_joinPointClassName.length()),
//                    ACC_PUBLIC + ACC_STATIC);

            createFieldsCommonToAllJoinPoints();
            //
            if (m_returnType.getSort() != Type.VOID) {
                m_cw.visitField(ACC_PRIVATE, "RETURNED", m_returnType.getDescriptor(), null, null);
            }


            createJoinPointSpecificFields();
            createStaticInitializer();
            createClinit();
            createInit();

            createUtilityMethods();
            createCopyMethod();
            createGetSignatureMethod();
            if (m_requiresJoinPoint) {
                createGetRttiMethod();
            }

            createInvokeMethod();
            if (m_hasAroundAdvices) {
                createProceedMethod();
            }

            m_cw.visitEnd();

            if (DUMP_CLASSES) {
                AsmHelper.dumpClass(DUMP_DIR, m_joinPointClassName, m_cw);
            }
            return m_cw.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            StringBuffer buf = new StringBuffer();
            buf.append("could not compile join point instance for join point with hash [");
            buf.append(m_joinPointHash);
            buf.append("] and declaring class [");
            buf.append(m_callerClassName);
            buf.append("] due to: ");
            if (e instanceof InvocationTargetException) {
                buf.append(((InvocationTargetException) e).getTargetException().toString());
            } else {
                buf.append(e.toString());
            }
            throw new RuntimeException(buf.toString());
        }
    }

    /**
     * Creates the clinit method for the join point.
     */
    protected void createClinit() {
        CodeVisitor cv = m_cw.visitMethod(ACC_STATIC, CLINIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE, null, null);
        cv.visitMethodInsn(
                INVOKESTATIC, m_joinPointClassName,
                STATIC_INITIALIZATION_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE
        );
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates the init method for the join point.
     */
    protected void createInit() {
        CodeVisitor cv = m_cw.visitMethod(ACC_PRIVATE, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE, null, null);
        cv.visitVarInsn(ALOAD, 0);

        boolean hasAroundClosureBaseClass = false;
        AspectModel aspectModel = null;

        for (int i = 0; i < m_aspectModels.length; i++) {
            aspectModel = m_aspectModels[i];
            if (aspectModel.getAroundClosureClassInfo().isClass()) {
                hasAroundClosureBaseClass = true;
                break;
            }
        }

        if (hasAroundClosureBaseClass) {
            // invoke the super class constructor
            aspectModel.createInitAroundClosureSuperClass(cv);
        } else {
            // invoke the constructor of java.lang.Object
            cv.visitMethodInsn(INVOKESPECIAL, OBJECT_CLASS_NAME, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        }

        cv.visitVarInsn(ALOAD, 0);
        resetStackFrameCounter(cv);
        cv.visitInsn(RETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates the static initialization method (not clinit) for the join point.
     */
    protected void createStaticInitializer() {
        CodeVisitor cv = m_cw.visitMethod(
                ACC_STATIC | ACC_PUBLIC,
                STATIC_INITIALIZATION_METHOD_NAME,
                NO_PARAM_RETURN_VOID_SIGNATURE,
                null, null
        );

        Label tryLabel = new Label();
        cv.visitLabel(tryLabel);
        cv.visitLdcInsn(m_calleeClassName.replace('/', '.'));
        cv.visitMethodInsn(INVOKESTATIC, CLASS_CLASS, FOR_NAME_METHOD_NAME, FOR_NAME_METHOD_SIGNATURE);
        cv.visitFieldInsn(PUTSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);

        Label finallyLabel = new Label();
        cv.visitLabel(finallyLabel);

        Label gotoFinallyLabel = new Label();
        cv.visitJumpInsn(GOTO, gotoFinallyLabel);

        Label catchLabel = new Label();
        cv.visitLabel(catchLabel);
        cv.visitVarInsn(ASTORE, 0);
        cv.visitTypeInsn(NEW, RUNTIME_EXCEPTION_CLASS_NAME);
        cv.visitInsn(DUP);
        cv.visitLdcInsn("could not load target class using Class.forName() in generated join point base class");

        cv.visitMethodInsn(
                INVOKESPECIAL,
                RUNTIME_EXCEPTION_CLASS_NAME,
                INIT_METHOD_NAME,
                RUNTIME_EXCEPTION_INIT_METHOD_SIGNATURE
        );

        cv.visitInsn(ATHROW);
        cv.visitLabel(gotoFinallyLabel);

        // create the metadata map
        cv.visitTypeInsn(NEW, HASH_MAP_CLASS_NAME);
        cv.visitInsn(DUP);
        cv.visitMethodInsn(INVOKESPECIAL, HASH_MAP_CLASS_NAME, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        cv.visitFieldInsn(PUTSTATIC, m_joinPointClassName, META_DATA_FIELD_NAME, MAP_CLASS_SIGNATURE);

        // create the Signature instance
        createSignature(cv);

        // create the static JoinPoint instance
        cv.visitTypeInsn(NEW, m_joinPointClassName);
        cv.visitInsn(DUP);
        cv.visitMethodInsn(INVOKESPECIAL, m_joinPointClassName, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        cv.visitFieldInsn(
                PUTSTATIC,
                m_joinPointClassName,
                OPTIMIZED_JOIN_POINT_INSTANCE_FIELD_NAME,
                L + m_joinPointClassName + SEMICOLON
        );

        // create and initialize the aspect fields
        for (int i = 0; i < m_aspectInfos.length; i++) {
            createAndInitializeAspectField(m_aspectInfos[i], cv);
        }

        cv.visitInsn(RETURN);
        cv.visitTryCatchBlock(tryLabel, finallyLabel, catchLabel, CLASS_NOT_FOUND_EXCEPTION_CLASS_NAME);
        cv.visitMaxs(0, 0);
    }

    /**
     * Create and initialize the aspect field for a specific aspect (qualified since it depends
     * on the param, deployment model, container etc).
     *
     * @param aspectInfo
     * @param cv
     */
    protected boolean createAndInitializeAspectField(final AspectInfo aspectInfo, final CodeVisitor cv) {
        final String aspectClassName = aspectInfo.getAspectClassName().replace('.', '/');
        final String aspectClassSignature = L + aspectClassName + SEMICOLON;

        if (aspectInfo.getAspectDefinition().isAspectWerkzAspect()) {
            // AW aspect
            // create the field to host the aspect and retrieve the aspect to set it to the field
            createAspectHost(m_cw, aspectInfo, m_joinPointClassName);
            createAspectInstantiation(cv, aspectInfo, m_joinPointClassName);
        } else {
            // non-AW aspect
            AspectModelManager.getModelFor(aspectInfo.getAspectDefinition().getAspectModel()).createAspectHost(
                    m_cw, aspectInfo, m_joinPointClassName
            );
            AspectModelManager.getModelFor(aspectInfo.getAspectDefinition().getAspectModel()).createAspectInstantiation(
                    cv, aspectInfo, m_joinPointClassName
            );
        }

        return false;
    }

    /**
     * Creates aspect host (static or non static field)
     *
     * @param cw
     * @param aspectInfo
     * @param joinPointClassName
     */
    public static void createAspectHost(ClassWriter cw, final AspectInfo aspectInfo, final String joinPointClassName) {
        String aspectClassSignature = aspectInfo.getAspectClassSignature();
        String aspectClassName = aspectInfo.getAspectClassName();
        // create a field depending on the aspect deployment model
        switch (aspectInfo.getDeploymentModel()) {
            case DeploymentModel.PER_JVM:
            case DeploymentModel.PER_CLASS:
                // add the aspect static field
                cw.visitField(
                        ACC_PRIVATE + ACC_STATIC, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null
                );
                break;
            case DeploymentModel.PER_INSTANCE:
                // add the aspect field as a non static field
                //TODO - may bee skip the aspect and all its advice is target is static, or ctor call
                //that is no instance available
                cw.visitField(
                        ACC_PRIVATE, aspectInfo.getAspectFieldName(), aspectClassSignature, null, null
                );
                break;
            default:
                throw new UnsupportedOperationException(
                        "unsupported deployment model - " +
                        aspectInfo.getAspectClassName() + " " +
                        DeploymentModel.getDeploymentModelAsString(aspectInfo.getDeploymentModel())
                );
        }
    }

    /**
     * Creates instantiation of aspects using the Aspects.aspectOf() methods which uses the AspectContainer impls.
     *
     * @param cv
     * @param aspectInfo
     * @param joinPointClassName
     */
    public static void createAspectInstantiation(CodeVisitor cv, final AspectInfo aspectInfo,
                                                 final String joinPointClassName) {
        String aspectClassSignature = aspectInfo.getAspectClassSignature();
        String aspectClassName = aspectInfo.getAspectClassName();
        // retrieve the aspect set it to the field
        switch (aspectInfo.getDeploymentModel()) {
            case DeploymentModel.PER_JVM:
                cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
                cv.visitMethodInsn(
                        INVOKESTATIC,
                        ASPECTS_CLASS_NAME,
                        ASPECT_OF_METHOD_NAME,
                        ASPECT_OF_PER_JVM_METHOD_SIGNATURE
                );
                cv.visitTypeInsn(CHECKCAST, aspectClassName);
                cv.visitFieldInsn(
                        PUTSTATIC, joinPointClassName, aspectInfo.getAspectFieldName(), aspectClassSignature
                );
                break;
            case DeploymentModel.PER_CLASS:
                cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
                cv.visitFieldInsn(GETSTATIC, joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
                cv.visitMethodInsn(
                        INVOKESTATIC,
                        ASPECTS_CLASS_NAME,
                        ASPECT_OF_METHOD_NAME,
                        ASPECT_OF_PER_CLASS_METHOD_SIGNATURE
                );
                cv.visitTypeInsn(CHECKCAST, aspectClassName);
                cv.visitFieldInsn(
                        PUTSTATIC, joinPointClassName, aspectInfo.getAspectFieldName(), aspectClassSignature
                );
                break;
            case DeploymentModel.PER_INSTANCE:
                break;
            default:
                throw new UnsupportedOperationException(
                        "unsupported deployment model - " +
                        aspectInfo.getAspectClassName() + " " +
                        DeploymentModel.getDeploymentModelAsString(aspectInfo.getDeploymentModel())
                );
        }
    }

    /**
     * Creates the 'invoke' method. If possible delegates to the target join point directly, e.g. does not invoke the
     * 'proceed' method (Used when a join point has zero around advice).
     */
    protected void createInvokeMethod() {

        final String invokeDesc = buildInvokeMethodSignature();

        // create the method
        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
                INVOKE_METHOD_NAME,
                invokeDesc,
                new String[]{
                    THROWABLE_CLASS_NAME
                },
                null
        );

        // compute the callee and caller index from the invoke(..) signature
        int calleeIndex = INDEX_NOTAVAILABLE;
        int argStartIndex = 0;
        if (!Modifier.isStatic(m_calleeMemberModifiers) && m_joinPointType != JoinPointType.CONSTRUCTOR_CALL) {
            calleeIndex = 0;
            argStartIndex++;
        } else {
            calleeIndex = INDEX_NOTAVAILABLE;// no callee in the invoke(..) parameters
        }
        final int callerIndex = argStartIndex + AsmHelper.getRegisterDepth(m_argumentTypes);//always there, can be "null"

        // do we need to keep track of CALLEE, ARGS etc, if not then completely skip it
        // and make use of the optimized join point instance
        // while not using its fields (does not support reentrancy and thread safety)
        final boolean isOptimizedJoinPoint = !m_requiresJoinPoint && !m_hasAroundAdvices;
        int joinPointIndex = INDEX_NOTAVAILABLE;

        if (!isOptimizedJoinPoint) {
            // create a new JP and makes use of it
            joinPointIndex = callerIndex + 1;
            createInvocationLocalJoinPointInstance(cv, argStartIndex, joinPointIndex);
        }

        // initialize the perTarget aspects
        for (int i = 0; i < m_aspectInfos.length; i++) {
            createInvocationToAspectOf(
                    cv, isOptimizedJoinPoint, joinPointIndex, callerIndex, calleeIndex, m_aspectInfos[i]);
        }

        // before advices
        createBeforeAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointIndex,
                callerIndex, calleeIndex
        );

        // handle different combinations of after advice (finally/throwing/returning)
        if (m_afterFinallyAdviceMethodInfos.length == 0 && m_afterThrowingAdviceMethodInfos.length == 0) {
            createPartOfInvokeMethodWithoutAfterFinallyAndAfterThrowingAdviceTypes(
                    cv, isOptimizedJoinPoint, joinPointIndex, argStartIndex, callerIndex, calleeIndex
            );
        } else if (m_afterThrowingAdviceMethodInfos.length == 0) {
            createPartOfInvokeMethodWithoutAfterThrowingAdviceTypes(
                    cv, isOptimizedJoinPoint, joinPointIndex, argStartIndex, callerIndex, calleeIndex
            );
        } else {
            createPartOfInvokeMethodWithAllAdviceTypes(
                    cv, OPTIMIZED_JOIN_POINT, joinPointIndex, argStartIndex, callerIndex, calleeIndex
            );
        }
        cv.visitMaxs(0, 0);
    }

    /**
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointInstanceIndex
     * @param argStartIndex
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createPartOfInvokeMethodWithAllAdviceTypes(final CodeVisitor cv,
                                                              final boolean isOptimizedJoinPoint,
                                                              final int joinPointInstanceIndex,
                                                              final int argStartIndex,
                                                              final int callerIndex,
                                                              final int calleeIndex) {
        final int returnValueIndex = (joinPointInstanceIndex != INDEX_NOTAVAILABLE) ?
                                     (joinPointInstanceIndex + 1) : callerIndex + 1;
        final int exceptionIndex1 = returnValueIndex + 1;
        final int exceptionIndex2 = returnValueIndex + 2;

        cv.visitInsn(ACONST_NULL);
        cv.visitVarInsn(ASTORE, returnValueIndex);

        Label tryLabel = new Label();
        cv.visitLabel(tryLabel);
        if (!m_hasAroundAdvices) {
            // if no around advice then optimize by invoking the target JP directly and no call to proceed()
            createInlinedJoinPointInvocation(cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex);
            int stackIndex = returnValueIndex;//use another int since storeType will update it
            AsmHelper.storeType(cv, stackIndex, m_returnType);
            addReturnedValueToJoinPoint(cv, returnValueIndex, joinPointInstanceIndex, false);
        } else {
            createInvocationToProceedMethod(cv, joinPointInstanceIndex, returnValueIndex);
        }

        createAfterReturningAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        Label finallyLabel1 = new Label();
        cv.visitLabel(finallyLabel1);

        createAfterFinallyAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        Label gotoFinallyLabel = new Label();
        cv.visitJumpInsn(GOTO, gotoFinallyLabel);

        Label catchLabel = new Label();
        cv.visitLabel(catchLabel);

        // store the exception
        cv.visitVarInsn(ASTORE, exceptionIndex1);

        // loop over the after throwing advices
        for (int i = m_afterThrowingAdviceMethodInfos.length - 1; i >= 0; i--) {
            AdviceMethodInfo advice = m_afterThrowingAdviceMethodInfos[i];

            // set the exception argument index
            advice.setSpecialArgumentIndex(exceptionIndex1);

            // if (e instanceof TYPE) {...}
            cv.visitVarInsn(ALOAD, exceptionIndex1);

            // FIXME use classname, not type desc - needs test coverage !!
            final String specialArgDesc = advice.getSpecialArgumentTypeDesc();
            if (specialArgDesc != null) {
                // after throwing <TYPE>
                cv.visitTypeInsn(INSTANCEOF, specialArgDesc);

                Label ifInstanceOfLabel = new Label();
                cv.visitJumpInsn(IFEQ, ifInstanceOfLabel);

                // after throwing advice invocation
                createAfterAdviceInvocation(
                        cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex,
                        argStartIndex, callerIndex, calleeIndex
                );

                cv.visitLabel(ifInstanceOfLabel);
            } else {
                // after throwing
                createAfterAdviceInvocation(
                        cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex,
                        argStartIndex, callerIndex, calleeIndex
                );
            }
        }

        // rethrow exception
        cv.visitVarInsn(ALOAD, exceptionIndex1);
        cv.visitInsn(ATHROW);

        // store exception
        Label exceptionLabel = new Label();
        cv.visitLabel(exceptionLabel);
        cv.visitVarInsn(ASTORE, exceptionIndex2);

        // after finally advice invocation
        Label finallyLabel2 = new Label();
        cv.visitLabel(finallyLabel2);

        createAfterFinallyAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        // rethrow exception
        cv.visitVarInsn(ALOAD, exceptionIndex2);
        cv.visitInsn(ATHROW);
        cv.visitLabel(gotoFinallyLabel);

        // unwrap if around advice and return in all cases
        if (m_returnType.getSort() != Type.VOID) {
            if (m_hasAroundAdvices) {
                cv.visitVarInsn(ALOAD, returnValueIndex);
                AsmHelper.unwrapType(cv, m_returnType);
            } else {
                AsmHelper.loadType(cv, returnValueIndex, m_returnType);
            }
        }

        AsmHelper.addReturnStatement(cv, m_returnType);

        // build up the exception table
        cv.visitTryCatchBlock(tryLabel, finallyLabel1, catchLabel, THROWABLE_CLASS_NAME);
        cv.visitTryCatchBlock(tryLabel, finallyLabel1, exceptionLabel, null);
        cv.visitTryCatchBlock(catchLabel, finallyLabel2, exceptionLabel, null);
    }

    /**
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointInstanceIndex
     * @param argStartIndex
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createPartOfInvokeMethodWithoutAfterThrowingAdviceTypes(final CodeVisitor cv,
                                                                           final boolean isOptimizedJoinPoint,
                                                                           final int joinPointInstanceIndex,
                                                                           final int argStartIndex,
                                                                           final int callerIndex,
                                                                           final int calleeIndex) {
        final int returnValueIndex = (joinPointInstanceIndex != INDEX_NOTAVAILABLE) ?
                                     (joinPointInstanceIndex + 1) : callerIndex + 1;
        final int exceptionIndex = returnValueIndex + 1;

        cv.visitInsn(ACONST_NULL);
        cv.visitVarInsn(ASTORE, returnValueIndex);

        Label tryLabel = new Label();
        cv.visitLabel(tryLabel);
        if (!m_hasAroundAdvices) {
            // if no around advice then optimize by invoking the target JP directly and no call to proceed()
            createInlinedJoinPointInvocation(cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex);
            int stackIndex = returnValueIndex;//use another int since storeType will update it
            AsmHelper.storeType(cv, stackIndex, m_returnType);
            addReturnedValueToJoinPoint(cv, returnValueIndex, joinPointInstanceIndex, false);
        } else {
            createInvocationToProceedMethod(cv, joinPointInstanceIndex, returnValueIndex);
        }

        createAfterReturningAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        Label finallyLabel1 = new Label();
        cv.visitLabel(finallyLabel1);

        createAfterFinallyAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        Label gotoFinallyLabel = new Label();
        cv.visitJumpInsn(GOTO, gotoFinallyLabel);

        Label exceptionLabel = new Label();
        cv.visitLabel(exceptionLabel);
        cv.visitVarInsn(ASTORE, exceptionIndex);

        Label finallyLabel2 = new Label();
        cv.visitLabel(finallyLabel2);

        createAfterFinallyAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        cv.visitVarInsn(ALOAD, exceptionIndex);
        cv.visitInsn(ATHROW);

        cv.visitLabel(gotoFinallyLabel);

        // unwrap if around advice and return in all cases
        if (m_returnType.getSort() != Type.VOID) {
            if (m_hasAroundAdvices) {
                cv.visitVarInsn(ALOAD, returnValueIndex);
                AsmHelper.unwrapType(cv, m_returnType);
            } else {
                AsmHelper.loadType(cv, returnValueIndex, m_returnType);
            }
        }

        AsmHelper.addReturnStatement(cv, m_returnType);

        cv.visitTryCatchBlock(tryLabel, finallyLabel1, exceptionLabel, null);
        cv.visitTryCatchBlock(exceptionLabel, finallyLabel2, exceptionLabel, null);
    }

    /**
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointInstanceIndex
     * @param argStartIndex
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createPartOfInvokeMethodWithoutAfterFinallyAndAfterThrowingAdviceTypes(final CodeVisitor cv,
                                                                                          final boolean isOptimizedJoinPoint,
                                                                                          final int joinPointInstanceIndex,
                                                                                          final int argStartIndex,
                                                                                          final int callerIndex,
                                                                                          final int calleeIndex) {

        final int returnValueIndex = (joinPointInstanceIndex != INDEX_NOTAVAILABLE) ?
                                     (joinPointInstanceIndex + 1) : callerIndex + 1;
        if (!m_hasAroundAdvices) {
            // if no around advice then optimize by invoking the target JP directly and no call to proceed()
            createInlinedJoinPointInvocation(cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex);
            int stackIndex = returnValueIndex;//use another int since storeType will update it
            AsmHelper.storeType(cv, stackIndex, m_returnType);
            addReturnedValueToJoinPoint(cv, returnValueIndex, joinPointInstanceIndex, false);
        } else {
            createInvocationToProceedMethod(cv, joinPointInstanceIndex, returnValueIndex);
        }


        // after returning advice invocations
        createAfterReturningAdviceInvocations(
                cv, isOptimizedJoinPoint, argStartIndex, joinPointInstanceIndex,
                callerIndex, calleeIndex
        );

        // unwrap if around advice and return in all cases
        if (m_returnType.getSort() != Type.VOID) {
            if (m_hasAroundAdvices) {
                cv.visitVarInsn(ALOAD, returnValueIndex);
                AsmHelper.unwrapType(cv, m_returnType);
            } else {
                AsmHelper.loadType(cv, returnValueIndex, m_returnType);
            }
        }

        AsmHelper.addReturnStatement(cv, m_returnType);
    }

    /**
     * Creates an invocation to the proceed method.
     *
     * @param cv
     * @param joinPointInstanceIndex
     * @param returnValueIndex
     */
    protected void createInvocationToProceedMethod(final CodeVisitor cv,
                                                   final int joinPointInstanceIndex,
                                                   final int returnValueIndex) {
        cv.visitVarInsn(ALOAD, joinPointInstanceIndex);
        cv.visitMethodInsn(INVOKEVIRTUAL, m_joinPointClassName, PROCEED_METHOD_NAME, PROCEED_METHOD_SIGNATURE);
        cv.visitVarInsn(ASTORE, returnValueIndex);
    }

    /**
     * Creates an "invocation local" join point instance, e.g. one join point per invocation. Needed for thread-safety
     * when invoking around advice.
     *
     * @param cv
     * @param argStartIndex          index on stack of first target method arg (0 or 1, depends of static target or
     *                               not)
     * @param joinPointInstanceIndex
     */
    protected void createInvocationLocalJoinPointInstance(final CodeVisitor cv,
                                                          final int argStartIndex,
                                                          final int joinPointInstanceIndex) {
        // create the join point instance
        cv.visitTypeInsn(NEW, m_joinPointClassName);
        cv.visitInsn(DUP);
        cv.visitMethodInsn(INVOKESPECIAL, m_joinPointClassName, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);

        // store the jp on the stack
        cv.visitVarInsn(ASTORE, joinPointInstanceIndex);

        // affect the target method arg to the jp (jp.m_arg<i> = <arg_i>)
        int argStackIndex = argStartIndex;
        for (int i = 0; i < m_fieldNames.length; i++) {
            String fieldName = m_fieldNames[i];
            cv.visitVarInsn(ALOAD, joinPointInstanceIndex);
            Type type = m_argumentTypes[i];
            argStackIndex = AsmHelper.loadType(cv, argStackIndex, type);
            cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, fieldName, type.getDescriptor());
        }

        // caller is arg<last>
        cv.visitVarInsn(ALOAD, joinPointInstanceIndex);
        cv.visitVarInsn(ALOAD, argStackIndex++);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);

        // callee is arg0 or null
        cv.visitVarInsn(ALOAD, joinPointInstanceIndex);
        if (argStartIndex > 0) {
            cv.visitVarInsn(ALOAD, 0);
        } else {
            cv.visitInsn(ACONST_NULL);
        }
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
    }

    /**
     * Create the proceed() method.
     */
    protected void createProceedMethod() {

        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC | ACC_FINAL,
                PROCEED_METHOD_NAME,
                PROCEED_METHOD_SIGNATURE,
                new String[]{
                    THROWABLE_CLASS_NAME
                },
                null
        );
        incrementStackFrameCounter(cv);

        // set up the labels
        Label tryLabel = new Label();
        Label defaultCaseLabel = new Label();
        Label gotoLabel = new Label();
        Label handlerLabel = new Label();
        Label endLabel = new Label();
        int nrOfCases = m_aroundAdviceMethodInfos.length;
        Label[] caseLabels = new Label[nrOfCases];
        Label[] returnLabels = new Label[nrOfCases];
        int[] caseNumbers = new int[nrOfCases];
        for (int i = 0; i < caseLabels.length; i++) {
            caseLabels[i] = new Label();
            caseNumbers[i] = i;
        }
        for (int i = 0; i < returnLabels.length; i++) {
            returnLabels[i] = new Label();
        }

        // start try-catch block
        cv.visitLabel(tryLabel);

        // start the switch block and set the stackframe as the param to the switch
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);
        cv.visitLookupSwitchInsn(defaultCaseLabel, caseNumbers, caseLabels);

        // add one case for each around advice invocation
        for (int i = 0; i < m_aroundAdviceMethodInfos.length; i++) {
            cv.visitLabel(caseLabels[i]);

            // gather advice info
            AdviceMethodInfo adviceInfo = m_aroundAdviceMethodInfos[i];

            Label endInstanceOflabel = beginRuntimeCheck(cv, false, 0, adviceInfo.getAdviceInfo(), -1);

            // get the aspect instance
            loadAspect(cv, NON_OPTIMIZED_JOIN_POINT, 0, adviceInfo.getAspectInfo());

            // load the arguments to the advice from the join point instance plus build up the advice method signature
            int[] argIndexes = adviceInfo.getAdviceMethodArgIndexes();
            for (int j = 0; j < argIndexes.length; j++) {
                int argIndex = argIndexes[j];
                if (argIndex >= 0) {
                    Type argumentType = m_argumentTypes[argIndex];
                    cv.visitVarInsn(ALOAD, 0);
                    cv.visitFieldInsn(
                            GETFIELD,
                            m_joinPointClassName,
                            ARGUMENT_FIELD + argIndex,
                            argumentType.getDescriptor()
                    );
                } else if (argIndex == AdviceInfo.JOINPOINT_ARG ||
                           argIndex == AdviceInfo.STATIC_JOINPOINT_ARG ||
                           argIndex == AdviceInfo.VALID_NON_AW_AROUND_CLOSURE_TYPE) {
                    cv.visitVarInsn(ALOAD, 0);
                } else if (argIndex == AdviceInfo.TARGET_ARG) {
                    loadCallee(cv, NON_OPTIMIZED_JOIN_POINT, 0, INDEX_NOTAVAILABLE);
                    // add a cast if runtime check was used
                    if (adviceInfo.getAdviceInfo().hasTargetWithRuntimeCheck()) {
                        cv.visitTypeInsn(
                                CHECKCAST, adviceInfo.getAdviceInfo().getMethodParameterTypes()[j].getInternalName()
                        );
                    }
                } else if (argIndex == AdviceInfo.THIS_ARG) {
                    loadCaller(cv, NON_OPTIMIZED_JOIN_POINT, 0, INDEX_NOTAVAILABLE);
                } else if (argIndex == AdviceInfo.SPECIAL_ARGUMENT) {
                    // TODO support special argument handling to proceed(..) ??
                } else {
                    throw new Error("advice method argument index type is not supported: " + argIndex);
                }
            }

            // invoke the advice method
            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    adviceInfo.getAspectInfo().getAspectClassName(),
                    adviceInfo.getAdviceInfo().getMethodName(),
                    adviceInfo.getAdviceInfo().getMethodSignature()
            );
            cv.visitVarInsn(ASTORE, 1);

            // we need to handle the case when the advice was skept due to runtime check
            // that is : if (runtimeCheck) { ret = advice() } else { ret = proceed() }
            if (endInstanceOflabel != null) {
                Label elseInstanceOfLabel = new Label();
                cv.visitJumpInsn(GOTO, elseInstanceOfLabel);
                endRuntimeCheck(cv, adviceInfo.getAdviceInfo(), endInstanceOflabel);
                cv.visitVarInsn(ALOAD, 0);
                cv.visitMethodInsn(INVOKESPECIAL, m_joinPointClassName, PROCEED_METHOD_NAME, PROCEED_METHOD_SIGNATURE);
                cv.visitVarInsn(ASTORE, 1);
                cv.visitLabel(elseInstanceOfLabel);
            }

            cv.visitLabel(returnLabels[i]);

            cv.visitVarInsn(ALOAD, 1);
            cv.visitInsn(ARETURN);
        }

        // invoke the target join point in the default case
        cv.visitLabel(defaultCaseLabel);

        AsmHelper.prepareWrappingOfPrimitiveType(cv, Type.getReturnType(m_calleeMemberDesc));

        createJoinPointInvocation(cv);

        Type m_returnType = null;
        if (m_joinPointType != JoinPointType.CONSTRUCTOR_CALL) {
            m_returnType = Type.getReturnType(m_calleeMemberDesc);
        } else {
            m_returnType = Type.getType(m_calleeClassSignature);
        }
        AsmHelper.wrapPrimitiveType(cv, m_returnType);
        cv.visitVarInsn(ASTORE, 1);

        // store it in Rtti return value
        addReturnedValueToJoinPoint(cv, 1, 0, true);

        // set it as the CALLEE instance for ctor call - TODO refactor somewhere else
        if (m_joinPointType == JoinPointType.CONSTRUCTOR_CALL) {
            cv.visitVarInsn(ALOAD, 0);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        }

        cv.visitLabel(gotoLabel);

        cv.visitVarInsn(ALOAD, 1);
        cv.visitInsn(ARETURN);

        // finally clause
        cv.visitLabel(handlerLabel);
        cv.visitVarInsn(ASTORE, 2);
        cv.visitLabel(endLabel);

        cv.visitVarInsn(ALOAD, 2);
        cv.visitInsn(ATHROW);

        // set up the label table
        cv.visitTryCatchBlock(tryLabel, returnLabels[0], handlerLabel, null);
        for (int i = 1; i < caseLabels.length; i++) {
            Label caseLabel = caseLabels[i];
            Label returnLabel = returnLabels[i];
            cv.visitTryCatchBlock(caseLabel, returnLabel, handlerLabel, null);
        }
        cv.visitTryCatchBlock(defaultCaseLabel, gotoLabel, handlerLabel, null);
        cv.visitTryCatchBlock(handlerLabel, endLabel, handlerLabel, null);
        cv.visitMaxs(0, 0);
    }

    /**
     * Adds before advice invocations.
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param argStartIndex          index on stack of first target method arg (0 or 1, depends of static target or
     *                               not)
     * @param joinPointInstanceIndex >= 0 if STATIC_JOIN_POINT is NOT to be used (around advice)
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createBeforeAdviceInvocations(final CodeVisitor cv,
                                                 final boolean isOptimizedJoinPoint,
                                                 final int argStartIndex,
                                                 final int joinPointInstanceIndex, //FIXME redundant -1 with isStaticJP
                                                 final int callerIndex,
                                                 final int calleeIndex) {
        for (int i = 0; i < m_beforeAdviceMethodInfos.length; i++) {
            AdviceMethodInfo adviceMethodInfo = m_beforeAdviceMethodInfos[i];

            // runtime check for target() etc
            Label endInstanceOflabel = beginRuntimeCheck(
                    cv, isOptimizedJoinPoint, joinPointInstanceIndex, adviceMethodInfo.getAdviceInfo(), calleeIndex
            );

            //get the aspect instance
            loadAspect(cv, isOptimizedJoinPoint, joinPointInstanceIndex, adviceMethodInfo.getAspectInfo());

            AspectDefinition aspectDef = adviceMethodInfo.getAdviceInfo().getAdviceDefinition().getAspectDefinition();
            if (aspectDef.isAspectWerkzAspect()) {
                // AW aspect
                int[] argIndexes = adviceMethodInfo.getAdviceMethodArgIndexes();
                // if empty, we consider for now that we have to push JoinPoint for old advice with JoinPoint as sole arg
                for (int j = 0; j < argIndexes.length; j++) {
                    int argIndex = argIndexes[j];
                    if (argIndex >= 0) {
                        Type argumentType = m_argumentTypes[argIndex];
                        int argStackIndex = AsmHelper.getRegisterIndexOf(m_argumentTypes, argIndex) + argStartIndex;
                        AsmHelper.loadType(cv, argStackIndex, argumentType);
                    } else if (argIndex == AdviceInfo.JOINPOINT_ARG || argIndex == AdviceInfo.STATIC_JOINPOINT_ARG) {
                        loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointInstanceIndex);
                    } else if (argIndex == AdviceInfo.TARGET_ARG) {
                        loadCallee(cv, isOptimizedJoinPoint, joinPointInstanceIndex, calleeIndex);
                        // add a cast if runtime check was used
                        if (adviceMethodInfo.getAdviceInfo().hasTargetWithRuntimeCheck()) {
                            cv.visitTypeInsn(
                                    CHECKCAST,
                                    adviceMethodInfo.getAdviceInfo().getMethodParameterTypes()[j].getInternalName()
                            );
                        }
                    } else if (argIndex == AdviceInfo.THIS_ARG) {
                        loadCaller(cv, isOptimizedJoinPoint, joinPointInstanceIndex, callerIndex);
                    } else {
                        throw new Error("AdviceMethodArgIndexes not supported: " + argIndex);
                    }
                }
            } else {
                // non-AW aspect
                for (int j = 0; j < m_aspectModels.length; j++) {
                    AspectModel aspectModel = m_aspectModels[j];
                    if (aspectDef.getAspectModel().equals(aspectModel.getAspectModelType())) {
                        aspectModel.createBeforeAdviceArgumentHandling(cv, adviceMethodInfo);
                    }
                }
            }

            cv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    adviceMethodInfo.getAspectInfo().getAspectClassName(),
                    adviceMethodInfo.getAdviceInfo().getMethodName(),
                    adviceMethodInfo.getAdviceInfo().getMethodSignature()
            );

            // end label of runtime checks
            endRuntimeCheck(cv, adviceMethodInfo.getAdviceInfo(), endInstanceOflabel);
        }
    }

    /**
     * Adds after advice invocations.
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param argStartIndex          index on stack of first target method arg (0 or 1, depends of static target or
     *                               not)
     * @param joinPointInstanceIndex >= 0 if STATIC_JOIN_POINT is NOT to be used (around advice)
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createAfterFinallyAdviceInvocations(final CodeVisitor cv,
                                                       final boolean isOptimizedJoinPoint,
                                                       final int argStartIndex,
                                                       final int joinPointInstanceIndex,
                                                       final int callerIndex,
                                                       final int calleeIndex) {
        // add after advice in reverse order
        for (int i = m_afterFinallyAdviceMethodInfos.length - 1; i >= 0; i--) {
            AdviceMethodInfo advice = m_afterFinallyAdviceMethodInfos[i];
            createAfterAdviceInvocation(
                    cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex, argStartIndex,
                    callerIndex, calleeIndex
            );
        }
    }

    /**
     * Adds after returning advice invocations.
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param argStartIndex          index on stack of first target method arg (0 or 1, depends of static target or
     *                               not)
     * @param joinPointInstanceIndex >= 0 if STATIC_JOIN_POINT is NOT to be used (around advice)
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createAfterReturningAdviceInvocations(final CodeVisitor cv,
                                                         final boolean isOptimizedJoinPoint,
                                                         final int argStartIndex,
                                                         final int joinPointInstanceIndex,
                                                         final int callerIndex,
                                                         final int calleeIndex) {
        final int returnValueIndex = (joinPointInstanceIndex != INDEX_NOTAVAILABLE) ?
                                     (joinPointInstanceIndex + 1) : callerIndex + 1;

        boolean hasPoppedReturnValueFromStack = false;
        for (int i = m_afterReturningAdviceMethodInfos.length - 1; i >= 0; i--) {
            AdviceMethodInfo advice = m_afterReturningAdviceMethodInfos[i];

            // set the return value index that will be used as arg to advice
            advice.setSpecialArgumentIndex(returnValueIndex);

            String specialArgDesc = advice.getSpecialArgumentTypeDesc();
            if (specialArgDesc == null) {
                // after returning
                createAfterAdviceInvocation(
                        cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex, argStartIndex,
                        callerIndex, calleeIndex
                );
            } else {
                // after returning <TYPE>
                if (AsmHelper.isPrimitive(m_returnType)) {
                    if (m_returnType.getDescriptor().equals(specialArgDesc)) {
                        createAfterAdviceInvocation(
                                cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex, argStartIndex,
                                callerIndex, calleeIndex
                        );
                    }
                } else {
                    //FIXME ALEX what do you mean??
                    // need the return value in instanceof operation
//                    if (m_hasAroundAdvices && !hasPoppedReturnValueFromStack) {
//                        AsmHelper.storeType(cv, returnValueIndex, m_returnType);
//                        hasPoppedReturnValueFromStack = true;
//                    }
                    cv.visitVarInsn(ALOAD, returnValueIndex);

                    //FIXME - use className, not desc - need test coverage !!
                    cv.visitTypeInsn(INSTANCEOF, specialArgDesc);

                    Label label = new Label();
                    cv.visitJumpInsn(IFEQ, label);

                    createAfterAdviceInvocation(
                            cv, isOptimizedJoinPoint, advice, joinPointInstanceIndex, argStartIndex,
                            callerIndex, calleeIndex
                    );

                    cv.visitLabel(label);
                }
            }
        }
        // need the return value in return operation
        if (!m_hasAroundAdvices && hasPoppedReturnValueFromStack) {
            cv.visitVarInsn(ALOAD, returnValueIndex);
        }
    }

    /**
     * Adds a single generic after advice invocation.
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param adviceMethodInfo
     * @param joinPointInstanceIndex
     * @param argStartIndex
     * @param callerIndex
     * @param calleeIndex
     */
    protected void createAfterAdviceInvocation(final CodeVisitor cv,
                                               final boolean isOptimizedJoinPoint,
                                               final AdviceMethodInfo adviceMethodInfo,
                                               final int joinPointInstanceIndex,
                                               final int argStartIndex,
                                               final int callerIndex,
                                               final int calleeIndex) {
        // runtime check for target() etc
        Label endInstanceOflabel = beginRuntimeCheck(
                cv, isOptimizedJoinPoint, joinPointInstanceIndex,
                adviceMethodInfo.getAdviceInfo(), calleeIndex
        );

        // get the aspect instance
        loadAspect(cv, isOptimizedJoinPoint, joinPointInstanceIndex, adviceMethodInfo.getAspectInfo());

        AspectDefinition aspectDef = adviceMethodInfo.getAdviceInfo().getAdviceDefinition().getAspectDefinition();
        if (aspectDef.isAspectWerkzAspect()) {
            // AW aspect
            // load the arguments that should be passed to the advice
            int[] argIndexes = adviceMethodInfo.getAdviceMethodArgIndexes();
            for (int j = 0; j < argIndexes.length; j++) {
                int argIndex = argIndexes[j];
                if (argIndex >= 0) {
                    Type argumentType = m_argumentTypes[argIndex];
                    int argStackIndex = AsmHelper.getRegisterIndexOf(m_argumentTypes, argIndex) + argStartIndex;
                    AsmHelper.loadType(cv, argStackIndex, argumentType);
                } else if (argIndex == AdviceInfo.JOINPOINT_ARG || argIndex == AdviceInfo.STATIC_JOINPOINT_ARG) {
                    loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointInstanceIndex);
                } else if (argIndex == AdviceInfo.TARGET_ARG) {
                    loadCallee(cv, isOptimizedJoinPoint, joinPointInstanceIndex, calleeIndex);
                    // add a cast if runtime check was used
                    if (adviceMethodInfo.getAdviceInfo().hasTargetWithRuntimeCheck()) {
                        cv.visitTypeInsn(
                                CHECKCAST,
                                adviceMethodInfo.getAdviceInfo().getMethodParameterTypes()[j].getInternalName()
                        );
                    }
                } else if (argIndex == AdviceInfo.THIS_ARG) {
                    loadCaller(cv, isOptimizedJoinPoint, joinPointInstanceIndex, callerIndex);
                } else {
                    throw new Error("AdviceMethodArgIndexes not supported: " + argIndex);
                }
            }
        } else {
            // non-AW aspect
            for (int i = 0; i < m_aspectModels.length; i++) {
                AspectModel aspectModel = m_aspectModels[i];
                if (aspectDef.getAspectModel().equals(aspectModel.getAspectModelType())) {
                    aspectModel.createAfterAdviceArgumentHandling(cv, adviceMethodInfo);
                }
            }
        }

        cv.visitMethodInsn(
                INVOKEVIRTUAL,
                adviceMethodInfo.getAspectInfo().getAspectClassName(),
                adviceMethodInfo.getAdviceInfo().getMethodName(),
                adviceMethodInfo.getAdviceInfo().getMethodSignature()
        );

        // end label of runtime checks
        endRuntimeCheck(cv, adviceMethodInfo.getAdviceInfo(), endInstanceOflabel);
    }

    /**
     * Adds the return value to the RETURNED field.
     *
     * @param cv
     * @param returnValueIndex
     * @param joinPointInstanceIndex
     * @param unwrap                 set to true if already wrapped on the stack (within proceed() code)
     */
    protected void addReturnedValueToJoinPoint(final CodeVisitor cv,
                                                      final int returnValueIndex,
                                                      final int joinPointInstanceIndex,
                                                      final boolean unwrap) {
        if (m_requiresJoinPoint && m_returnType.getSort() != Type.VOID) {
            if (m_joinPointType == JoinPointType.METHOD_EXECUTION
                || m_joinPointType == JoinPointType.METHOD_CALL
                || m_joinPointType == JoinPointType.CONSTRUCTOR_CALL) {
                //TODO should we do something for field get / set
                loadJoinPointInstance(cv, NON_OPTIMIZED_JOIN_POINT, joinPointInstanceIndex);
                if (unwrap && AsmHelper.isPrimitive(m_returnType)) {
                    cv.visitVarInsn(ALOAD, returnValueIndex);
                    AsmHelper.unwrapType(cv, m_returnType);
                } else {
                    AsmHelper.loadType(cv, returnValueIndex, m_returnType);
                }
                cv.visitFieldInsn(
                        PUTFIELD, m_joinPointClassName,
                        RETURNED_FIELD, m_returnType.getDescriptor()
                );
            }
        }
    }

    /**
     * Loads the join point instance, takes static/non-static join point access into account.
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointInstanceIndex
     */
    protected void loadJoinPointInstance(final CodeVisitor cv,
                                         final boolean isOptimizedJoinPoint,
                                         final int joinPointInstanceIndex) {
        if (isOptimizedJoinPoint) {
            cv.visitFieldInsn(
                    GETSTATIC, m_joinPointClassName,
                    OPTIMIZED_JOIN_POINT_INSTANCE_FIELD_NAME,
                    L + m_joinPointClassName + SEMICOLON
            );
        } else {
            cv.visitVarInsn(ALOAD, joinPointInstanceIndex);
        }
    }

    /**
     * Loads the argument member fields.
     *
     * @param cv
     * @param argStartIndex
     */
    protected void loadArgumentMemberFields(final CodeVisitor cv, final int argStartIndex) {
        int argStackIndex = argStartIndex;
        for (int index = 0; index < m_argumentTypes.length; index++) {
            Type argumentType = m_argumentTypes[index];
            argStackIndex = AsmHelper.loadType(cv, argStackIndex, argumentType);
        }
    }

    /**
     * Loads the arguments.
     *
     * @param cv
     */
    protected void loadArguments(final CodeVisitor cv) {
        for (int i = 0; i < m_fieldNames.length; i++) {
            String fieldName = m_fieldNames[i];
            Type argumentType = m_argumentTypes[i];
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, fieldName, argumentType.getDescriptor());
        }
    }

    /**
     * Resets the stack frame counter.
     *
     * @param cv
     */
    protected void resetStackFrameCounter(final CodeVisitor cv) {
        cv.visitVarInsn(ALOAD, 0);
        cv.visitInsn(ICONST_M1);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);
    }

    /**
     * Handles the incrementation of the stack frame.
     *
     * @param cv
     */
    protected void incrementStackFrameCounter(final CodeVisitor cv) {
        cv.visitVarInsn(ALOAD, 0);
        cv.visitInsn(DUP);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);
        cv.visitInsn(ICONST_1);
        cv.visitInsn(IADD);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);
    }

    /**
     * Create and load a structure (f.e. array of Object) where args are stored, before setting the Rtti
     * with it (See addParametersToRttiInstance). The structure is stored at the given stackFreeIndex.
     * <p/>
     * We provide here a default implementation that is suitable for method and constructor call and execution.
     * See createParameterWrappedAt for field get/set and handler compiler (no array of argument needed)
     *
     * @param cv
     * @param stackFreeIndex
     */
    protected final void createParametersArrayAt(final CodeVisitor cv,
                                                 final int stackFreeIndex) {
        AsmHelper.loadIntegerConstant(cv, m_fieldNames.length);
        cv.visitTypeInsn(ANEWARRAY, OBJECT_CLASS_NAME);
        cv.visitVarInsn(ASTORE, stackFreeIndex);

        for (int i = 0; i < m_argumentTypes.length; i++) {
            cv.visitVarInsn(ALOAD, stackFreeIndex);
            AsmHelper.loadIntegerConstant(cv, i);
            AsmHelper.prepareWrappingOfPrimitiveType(cv, m_argumentTypes[i]);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, ARGUMENT_FIELD + i, m_argumentTypes[i].getDescriptor());
            //index = AsmHelper.loadType(cv, index, m_argumentTypes[i]);
            AsmHelper.wrapPrimitiveType(cv, m_argumentTypes[i]);
            cv.visitInsn(AASTORE);
        }
    }

    /**
     * Creates utility methods for the join point (getter, setters etc.).
     */
    protected void createUtilityMethods() {
        CodeVisitor cv;

        // addMetaData
        {
            cv = m_cw.visitMethod(ACC_PUBLIC, ADD_META_DATA_METHOD_NAME, ADD_META_DATA_METHOD_SIGNATURE, null, null);
            cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, META_DATA_FIELD_NAME, MAP_CLASS_SIGNATURE);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitVarInsn(ALOAD, 2);
            cv.visitMethodInsn(
                    INVOKEINTERFACE,
                    MAP_CLASS_NAME,
                    PUT_METHOD_NAME,
                    PUT_METHOD_SIGNATURE
            );
            cv.visitInsn(POP);
            cv.visitInsn(RETURN);
            cv.visitMaxs(0, 0);
        }

        // getMetaData
        {
            cv = m_cw.visitMethod(ACC_PUBLIC, GET_META_DATA_METHOD_NAME, GET_META_DATA_METHOD_SIGNATURE, null, null);
            cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, META_DATA_FIELD_NAME, MAP_CLASS_SIGNATURE);
            cv.visitVarInsn(ALOAD, 1);
            cv.visitMethodInsn(INVOKEINTERFACE, MAP_CLASS_NAME, GET_METHOD_NAME, GET_METHOD_SIGNATURE);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getCallee
        {
            cv = m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_CALLEE_METHOD_NAME,
                    NO_PARAMS_SIGNATURE + OBJECT_CLASS_SIGNATURE,
                    null, null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getCaller
        {
            cv = m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_CALLER_METHOD_NAME,
                    NO_PARAMS_SIGNATURE + OBJECT_CLASS_SIGNATURE,
                    null, null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getTarget
        {
            cv = m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_TARGET_METHOD_NAME,
                    NO_PARAMS_SIGNATURE + OBJECT_CLASS_SIGNATURE,
                    null, null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getThis
        {
            cv = m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_THIS_METHOD_NAME,
                    NO_PARAMS_SIGNATURE + OBJECT_CLASS_SIGNATURE,
                    null, null
            );
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getCallerClass
        {
            cv =
            m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_CALLER_CLASS_METHOD_NAME,
                    GET_CALLER_CLASS_METHOD_SIGNATURE,
                    null,
                    null
            );
            cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }

        // getTargetClass
        {
            cv =
            m_cw.visitMethod(
                    ACC_PUBLIC,
                    GET_TARGET_CLASS_METHOD_NAME,
                    GET_TARGET_CLASS_METHOD_SIGNATURE,
                    null,
                    null
            );
            cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }


        // getType
        {
            // FIXME should return Enum (type-safe pattern in 1.4 and enum in 1.5)
            cv = m_cw.visitMethod(ACC_PUBLIC, GET_TYPE_METHOD_NAME, GET_TYPE_METHOD_SIGNATURE, null, null);
            cv.visitInsn(ACONST_NULL);
            cv.visitInsn(ARETURN);
            cv.visitMaxs(0, 0);
        }
    }

    /**
     * Creates the copy method.
     * <p/>
     * TODO refactor and put in subclasses
     */
    protected void createCopyMethod() {

        CodeVisitor cv = m_cw.visitMethod(ACC_PUBLIC, COPY_METHOD_NAME, COPY_METHOD_SIGNATURE, null, null);

        // create a new join point instance
        cv.visitTypeInsn(NEW, m_joinPointClassName);
        cv.visitInsn(DUP);
        int joinPointCloneIndex = 1;
        cv.visitMethodInsn(INVOKESPECIAL, m_joinPointClassName, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
        cv.visitVarInsn(ASTORE, joinPointCloneIndex);

        // set stack frame index
        cv.visitVarInsn(ALOAD, joinPointCloneIndex);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, STACK_FRAME_COUNTER_FIELD_NAME, I);

        // set callee
        cv.visitVarInsn(ALOAD, joinPointCloneIndex);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);

        // set caller
        cv.visitVarInsn(ALOAD, joinPointCloneIndex);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
        cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);

        // set the arguments
        for (int i = 0; i < m_fieldNames.length; i++) {
            String fieldName = m_fieldNames[i];
            cv.visitVarInsn(ALOAD, joinPointCloneIndex);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, fieldName, m_argumentTypes[i].getDescriptor());
            cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, fieldName, m_argumentTypes[i].getDescriptor());
        }

        // set the returned field if any
        if (m_returnType.getSort() != Type.VOID) {
            cv.visitVarInsn(ALOAD, joinPointCloneIndex);
            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, RETURNED_FIELD, m_returnType.getDescriptor());
            cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, RETURNED_FIELD, m_returnType.getDescriptor());
        }

        cv.visitVarInsn(ALOAD, joinPointCloneIndex);
        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Build up the signature of the 'invoke' methods.
     *
     * @return
     */
    protected String buildInvokeMethodSignature() {
        StringBuffer invokeDescBuf = new StringBuffer();
        invokeDescBuf.append('(');
        if (m_joinPointType != JoinPointType.CONSTRUCTOR_CALL) {
            if (!Modifier.isStatic(m_calleeMemberModifiers)) {
                // callee
                invokeDescBuf.append(m_calleeClassSignature);
            }
        }
        // args
        for (int i = 0; i < m_argumentTypes.length; i++) {
            Type type = m_argumentTypes[i];
            invokeDescBuf.append(type.getDescriptor());
        }
        // caller
        invokeDescBuf.append(m_callerClassSignature);
        invokeDescBuf.append(')');
        invokeDescBuf.append(m_returnType.getDescriptor());
        return invokeDescBuf.toString();
    }

    /**
     * Return the number of argument the joinpoint has (excludes JoinPoint, Rtti, this / target) but is only
     * the number of argument we will have in the rtti (advised method/ctor args or 1 for field / handler)
     *
     * @return
     */
    protected final boolean hasArguments() {
        return m_argumentTypes.length > 0;
    }

    /**
     * Checks if at least one advice is using this or target (bounded or runtime check)
     *
     * @return true if so
     */
    protected boolean requiresThisOrTarget() {
        return requiresThisOrTarget(m_aroundAdviceMethodInfos) ||
               requiresThisOrTarget(m_beforeAdviceMethodInfos) ||
               requiresThisOrTarget(m_afterFinallyAdviceMethodInfos) ||
               requiresThisOrTarget(m_afterReturningAdviceMethodInfos) ||
               requiresThisOrTarget(m_afterThrowingAdviceMethodInfos);
    }

    /**
     * Checks if at least one advice is using the non static JoinPoint explicitly
     *
     * @return true if so
     */
    protected boolean requiresJoinPoint() {
        return requiresJoinPoint(m_aroundAdviceMethodInfos) ||
               requiresJoinPoint(m_beforeAdviceMethodInfos) ||
               requiresJoinPoint(m_afterFinallyAdviceMethodInfos) ||
               requiresJoinPoint(m_afterReturningAdviceMethodInfos) ||
               requiresJoinPoint(m_afterThrowingAdviceMethodInfos);
    }

    /**
     * Checks if at least one advice is using target or this (bounded or runtime check)
     *
     * @param adviceMethodInfos
     * @return true if so
     */
    protected boolean requiresThisOrTarget(final AdviceMethodInfo[] adviceMethodInfos) {
        for (int i = 0; i < adviceMethodInfos.length; i++) {
            if (adviceMethodInfos[i].requiresThisOrTarget()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if at least one advice is using non static JoinPoint explicitly
     *
     * @param adviceMethodInfos
     * @return true if so
     */
    protected boolean requiresJoinPoint(final AdviceMethodInfo[] adviceMethodInfos) {
        for (int i = 0; i < adviceMethodInfos.length; i++) {
            if (adviceMethodInfos[i].requiresJoinPoint()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the if case for runtime check (target instanceof, etc)
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointInstanceIndex
     * @param adviceInfo
     * @return the label for endIf or null if the adviceInfo did not required runtime check
     */
    protected Label beginRuntimeCheck(final CodeVisitor cv,
                                      final boolean isOptimizedJoinPoint,
                                      final int joinPointInstanceIndex,
                                      final AdviceInfo adviceInfo,
                                      final int calleeIndex) {
        Label endRuntimeCheckLabel = null;
        if (adviceInfo.hasTargetWithRuntimeCheck()) {
            endRuntimeCheckLabel = new Label();
            // create a specific visitor everytime
            RuntimeCheckVisitor runtimeCheckVisitor = new RuntimeCheckVisitor(
                    this, cv, adviceInfo.getExpressionInfo(), isOptimizedJoinPoint, joinPointInstanceIndex,
                    calleeIndex
            );
            runtimeCheckVisitor.pushCheckOnStack(adviceInfo.getExpressionContext());
            cv.visitJumpInsn(IFEQ, endRuntimeCheckLabel);
        }
        return endRuntimeCheckLabel;
    }

    /**
     * Ends the ifLabel of a runtime check
     *
     * @param cv
     * @param adviceInfo
     * @param label      if null, then do nothing (means we did not had a runtime check)
     */
    protected void endRuntimeCheck(final CodeVisitor cv, final AdviceInfo adviceInfo, final Label label) {
        if (adviceInfo.hasTargetWithRuntimeCheck()) {
            cv.visitLabel(label);
        }
    }

    /**
     * Helper method to load the callee on the stack
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointIndex
     * @param calleeIndex
     */
    public void loadCallee(final CodeVisitor cv,
                           final boolean isOptimizedJoinPoint,
                           final int joinPointIndex,
                           final int calleeIndex) {
        if (isOptimizedJoinPoint) {
            // grab the callee from the invoke parameters directly
            cv.visitVarInsn(ALOAD, calleeIndex);
        } else {
            loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointIndex);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        }
    }

    /**
     * Helper method to load the caller on the stack
     *
     * @param cv
     * @param isOptimizedJoinPoint
     * @param joinPointIndex
     * @param callerIndex
     */
    public void loadCaller(final CodeVisitor cv,
                           final boolean isOptimizedJoinPoint,
                           final int joinPointIndex,
                           final int callerIndex) {
        if (isOptimizedJoinPoint) {
            // grab the callee from the invoke parameters directly
            cv.visitVarInsn(ALOAD, callerIndex);
        } else {
            loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointIndex);
            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
        }
    }

    public void loadAspect(final CodeVisitor cv,
                           final boolean isOptimizedJoinPoint,
                           final int joinPointIndex,
                           final AspectInfo aspectInfo) {
        switch(aspectInfo.getDeploymentModel()) {
            case DeploymentModel.PER_JVM:
            case DeploymentModel.PER_CLASS:
                cv.visitFieldInsn(
                        GETSTATIC, m_joinPointClassName, aspectInfo.getAspectFieldName(),
                        aspectInfo.getAspectClassSignature()
                );
                break;
            case DeploymentModel.PER_INSTANCE:
                loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointIndex);
                cv.visitFieldInsn(
                        GETFIELD, m_joinPointClassName, aspectInfo.getAspectFieldName(),
                        aspectInfo.getAspectClassSignature()
                );

        }
        //FIXME - is that ok for other models ?
   }

   public void createInvocationToAspectOf(CodeVisitor cv, boolean isOptimizedJoinPoint, int joinPointIndex,
                                          int callerIndex, int calleeIndex, AspectInfo aspectInfo) {
       if (aspectInfo.getDeploymentModel() == DeploymentModel.PER_INSTANCE) {
           //aspectField = (cast) Aspects.aspectOf(aspectQN, callee)
           loadJoinPointInstance(cv, isOptimizedJoinPoint, joinPointIndex);
           cv.visitLdcInsn(aspectInfo.getAspectQualifiedName());
           if (calleeIndex >= 0) {
               cv.visitVarInsn(ALOAD, calleeIndex);
               cv.visitMethodInsn(
                       INVOKESTATIC,
                       ASPECTS_CLASS_NAME,
                       ASPECT_OF_METHOD_NAME,
                       ASPECT_OF_PER_INSTANCE_METHOD_SIGNATURE
               );
           } else {
               // fallback to perClass
               //aspectField = (cast) Aspects.aspectOf(aspectQN, callee)
               cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
               cv.visitMethodInsn(
                       INVOKESTATIC,
                       ASPECTS_CLASS_NAME,
                       ASPECT_OF_METHOD_NAME,
                       ASPECT_OF_PER_CLASS_METHOD_SIGNATURE
               );
           }
           cv.visitTypeInsn(CHECKCAST, aspectInfo.getAspectClassName());
           cv.visitFieldInsn(PUTFIELD, m_joinPointClassName, aspectInfo.getAspectFieldName(),
                             aspectInfo.getAspectClassSignature()
           );
       }
       //FIXME - is that ok for other models ?
   }

}