/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.intercept;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

/**
 * Implementation of the <code>Advisable</code> mixin.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdvisableImpl implements Advisable {

    private static final String EXPRESSION_NAMESPACE = "___AW_ADVISABLE_AW___";

    public static final ClassInfo CLASS_INFO;
    public static final AroundAdvice[] EMPTY_AROUND_ADVICE_ARRAY = new AroundAdvice[0];
    public static final BeforeAdvice[] EMPTY_BEFORE_ADVICE_ARRAY = new BeforeAdvice[0];
    public static final AfterAdvice[] EMPTY_AFTER_ADVICE_ARRAY = new AfterAdvice[0];
    public static final AfterReturningAdvice[] EMPTY_AFTER_RETURNING_ADVICE_ARRAY = new AfterReturningAdvice[0];
    public static final AfterThrowingAdvice[] EMPTY_AFTER_THROWING_ADVICE_ARRAY = new AfterThrowingAdvice[0];

    static {
        final Class clazz = AdvisableImpl.class;
        try {
            CLASS_INFO = AsmClassInfo.getClassInfo(clazz.getName(), clazz.getClassLoader());
        } catch (Exception e) {
            throw new Error("could not create class info for [" + clazz.getName() + ']');
        }
    }

    private final Advisable m_targetInstance;

    private final TIntObjectHashMap m_aroundAdvice = new TIntObjectHashMap();
    private final TIntObjectHashMap m_beforeAdvice = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterAdvice = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterReturningAdvice = new TIntObjectHashMap();
    private final TIntObjectHashMap m_afterThrowingAdvice = new TIntObjectHashMap();

    /**
     * Creates a new mixin impl.
     *
     * @param targetInstance the target for this mixin instance (perInstance deployed)
     */
    public AdvisableImpl(final Object targetInstance) {
        if (!(targetInstance instanceof Advisable)) {
            throw new RuntimeException(
                    "advisable mixin applied to target class that does not implement the Advisable interface"
            );
        }
        m_targetInstance = (Advisable) targetInstance;
    }

    /**
     * @param memberPattern
     * @param advice
     */
    public void aw$addAdvice(final String memberPattern, final Advice advice) {
        ClassInfo classInfo = JavaClassInfo.getClassInfo(m_targetInstance.getClass());
        if (memberPattern.endsWith(")")) {
            addAdviceToMethods(memberPattern, advice, classInfo.getMethods());
        } else {
            addAdviceToFields(memberPattern, advice, classInfo.getFields());
        }
    }

    /**
     * @param memberPattern
     * @param adviceClass
     */
    public void aw$removeAdvice(final String memberPattern, final Class adviceClass) {
        ClassInfo classInfo = JavaClassInfo.getClassInfo(m_targetInstance.getClass());
        if (memberPattern.endsWith(")")) {
            removeAdviceFromMethods(memberPattern, adviceClass, classInfo.getMethods());
        } else {
            removeAdviceFromFields(memberPattern, adviceClass, classInfo.getFields());
        }
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AroundAdvice[] aw$getAroundAdvice(final int joinPointIndex) {
        Object advice = m_aroundAdvice.get(joinPointIndex);
        if (advice == null) {
            return EMPTY_AROUND_ADVICE_ARRAY;
        } else {
            return (AroundAdvice[]) advice;
        }
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public BeforeAdvice[] aw$getBeforeAdvice(final int joinPointIndex) {
        Object advice = m_beforeAdvice.get(joinPointIndex);
        if (advice == null) {
            return EMPTY_BEFORE_ADVICE_ARRAY;
        } else {
            return (BeforeAdvice[]) advice;
        }
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterAdvice[] aw$getAfterAdvice(final int joinPointIndex) {
        Object advice = m_afterAdvice.get(joinPointIndex);
        if (advice == null) {
            return EMPTY_AFTER_ADVICE_ARRAY;
        } else {
            return (AfterAdvice[]) advice;
        }
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterReturningAdvice[] aw$getAfterReturningAdvice(final int joinPointIndex) {
        Object advice = m_afterReturningAdvice.get(joinPointIndex);
        if (advice == null) {
            return EMPTY_AFTER_RETURNING_ADVICE_ARRAY;
        } else {
            return (AfterReturningAdvice[]) advice;
        }
    }

    /**
     * @param joinPointIndex
     * @return
     */
    public AfterThrowingAdvice[] aw$getAfterThrowingAdvice(final int joinPointIndex) {
        Object advice = m_afterThrowingAdvice.get(joinPointIndex);
        if (advice == null) {
            return EMPTY_AFTER_THROWING_ADVICE_ARRAY;
        } else {
            return (AfterThrowingAdvice[]) advice;
        }
    }

    /**
     * @param methodPattern
     * @param advice
     * @param methods
     */
    private void addAdviceToMethods(final String methodPattern,
                                    final Advice advice,
                                    final MethodInfo[] methods) {
        ExpressionInfo expressionInfo = new ExpressionInfo("execution(" + methodPattern + ')', EXPRESSION_NAMESPACE);
        ExpressionVisitor expression = expressionInfo.getExpression();
        for (int i = 0; i < methods.length; i++) {
            MethodInfo method = methods[i];
            if (method.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)) {
                continue;
            }
            if (expression.match(new ExpressionContext(PointcutType.EXECUTION, method, null))) {
                int joinPointHash = AsmHelper.calculateMethodHash(method.getName(), method.getSignature());
                addAroundAdvice(advice, joinPointHash);
                addBeforeAdvice(advice, joinPointHash);
                addAfterAdvice(advice, joinPointHash);
                addAfterReturningAdvice(advice, joinPointHash);
                addAfterThrowingAdvice(advice, joinPointHash);
            }
        }
    }

    /**
     * @param fieldPattern
     * @param advice
     * @param fields
     */
    private void addAdviceToFields(final String fieldPattern,
                                   final Advice advice,
                                   final FieldInfo[] fields) {
        ExpressionInfo expressionInfo = new ExpressionInfo("set(" + fieldPattern + ')', EXPRESSION_NAMESPACE);
        ExpressionVisitor expression = expressionInfo.getExpression();
        for (int i = 0; i < fields.length; i++) {
            FieldInfo field = fields[i];
            if (field.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)) {
                continue;
            }
            if (expression.match(new ExpressionContext(PointcutType.SET, field, null))) {
                int joinPointHash = AsmHelper.calculateFieldHash(field.getName(), field.getSignature());
                addAroundAdvice(advice, joinPointHash);
                addBeforeAdvice(advice, joinPointHash);
                addAfterAdvice(advice, joinPointHash);
                addAfterReturningAdvice(advice, joinPointHash);
                addAfterThrowingAdvice(advice, joinPointHash);
            }
        }
    }

    /**
     * @param methodPattern
     * @param adviceClass
     * @param methods
     */
    private void removeAdviceFromMethods(final String methodPattern,
                                         final Class adviceClass,
                                         final MethodInfo[] methods) {
        ExpressionInfo expressionInfo = new ExpressionInfo("execution(" + methodPattern + ')', EXPRESSION_NAMESPACE);
        ExpressionVisitor expression = expressionInfo.getExpression();
        for (int i = 0; i < methods.length; i++) {
            MethodInfo method = methods[i];
            if (method.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)) {
                continue;
            }
            if (expression.match(new ExpressionContext(PointcutType.EXECUTION, method, null))) {
                int joinPointHash = AsmHelper.calculateMethodHash(method.getName(), method.getSignature());
                removeAroundAdvice(adviceClass, joinPointHash);
                removeBeforeAdvice(adviceClass, joinPointHash);
                removeAfterAdvice(adviceClass, joinPointHash);
                removeAfterReturningAdvice(adviceClass, joinPointHash);
                removeAfterThrowingAdvice(adviceClass, joinPointHash);
            }
        }
    }

    /**
     * @param fieldPattern
     * @param adviceClass
     * @param fields
     */
    private void removeAdviceFromFields(final String fieldPattern,
                                        final Class adviceClass,
                                        final FieldInfo[] fields) {
        ExpressionInfo expressionInfo = new ExpressionInfo("set(" + fieldPattern + ')', EXPRESSION_NAMESPACE);
        ExpressionVisitor expression = expressionInfo.getExpression();
        for (int i = 0; i < fields.length; i++) {
            FieldInfo field = fields[i];
            if (field.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)) {
                continue;
            }
            if (expression.match(new ExpressionContext(PointcutType.SET, field, null))) {
                int joinPointHash = AsmHelper.calculateFieldHash(field.getName(), field.getSignature());
                removeAroundAdvice(adviceClass, joinPointHash);
                removeBeforeAdvice(adviceClass, joinPointHash);
                removeAfterAdvice(adviceClass, joinPointHash);
                removeAfterReturningAdvice(adviceClass, joinPointHash);
                removeAfterThrowingAdvice(adviceClass, joinPointHash);
            }
        }
    }

    /**
     * @param advice
     * @param joinPointHash
     */
    private void addAroundAdvice(final Advice advice, int joinPointHash) {
        if (advice instanceof AroundAdvice) {
            AroundAdvice aroundAdvice = (AroundAdvice) advice;
            AroundAdvice[] advices;
            AroundAdvice[] olds = aw$getAroundAdvice(joinPointHash);
            if (olds != null) {
                advices = new AroundAdvice[olds.length + 1];
                System.arraycopy(olds, 0, advices, 0, olds.length);
                advices[advices.length - 1] = aroundAdvice;
            } else {
                advices = new AroundAdvice[]{aroundAdvice};
            }
            m_aroundAdvice.put(joinPointHash, advices);
        }
    }

    /**
     * @param advice
     * @param joinPointHash
     */
    private void addBeforeAdvice(final Advice advice, int joinPointHash) {
        if (advice instanceof BeforeAdvice) {
            BeforeAdvice beforeAdvice = (BeforeAdvice) advice;
            BeforeAdvice[] advices;
            BeforeAdvice[] olds = aw$getBeforeAdvice(joinPointHash);
            if (olds != null) {
                advices = new BeforeAdvice[olds.length + 1];
                System.arraycopy(olds, 0, advices, 0, olds.length);
                advices[advices.length - 1] = beforeAdvice;
            } else {
                advices = new BeforeAdvice[]{beforeAdvice};
            }
            m_beforeAdvice.put(joinPointHash, advices);
        }
    }

    /**
     * @param advice
     * @param joinPointHash
     */
    private void addAfterAdvice(final Advice advice, int joinPointHash) {
        if (advice instanceof AfterAdvice) {
            AfterAdvice afterFinallyAdvice = (AfterAdvice) advice;
            AfterAdvice[] advices;
            AfterAdvice[] olds = aw$getAfterAdvice(joinPointHash);
            if (olds != null) {
                advices = new AfterAdvice[olds.length + 1];
                System.arraycopy(olds, 0, advices, 0, olds.length);
                advices[advices.length - 1] = afterFinallyAdvice;
            } else {
                advices = new AfterAdvice[]{afterFinallyAdvice};
            }
            m_afterAdvice.put(joinPointHash, advices);
        }
    }

    /**
     * @param advice
     * @param joinPointHash
     */
    private void addAfterReturningAdvice(final Advice advice, int joinPointHash) {
        if (advice instanceof AfterReturningAdvice) {
            AfterReturningAdvice afterReturningAdvice = (AfterReturningAdvice) advice;
            AfterReturningAdvice[] advices;
            AfterReturningAdvice[] olds = aw$getAfterReturningAdvice(joinPointHash);
            if (olds != null) {
                advices = new AfterReturningAdvice[olds.length + 1];
                System.arraycopy(olds, 0, advices, 0, olds.length);
                advices[advices.length - 1] = afterReturningAdvice;
            } else {
                advices = new AfterReturningAdvice[]{afterReturningAdvice};
            }
            m_afterReturningAdvice.put(joinPointHash, advices);
        }
    }

    /**
     * @param advice
     * @param joinPointHash
     */
    private void addAfterThrowingAdvice(final Advice advice, int joinPointHash) {
        if (advice instanceof AfterThrowingAdvice) {
            AfterThrowingAdvice afterThrowingAdvice = (AfterThrowingAdvice) advice;
            AfterThrowingAdvice[] advices;
            AfterThrowingAdvice[] olds = aw$getAfterThrowingAdvice(joinPointHash);
            if (olds != null) {
                advices = new AfterThrowingAdvice[olds.length + 1];
                System.arraycopy(olds, 0, advices, 0, olds.length);
                advices[advices.length - 1] = afterThrowingAdvice;
            } else {
                advices = new AfterThrowingAdvice[]{afterThrowingAdvice};
            }
            m_afterThrowingAdvice.put(joinPointHash, advices);
        }
    }

    /**
     * @param adviceClass
     * @param joinPointHash
     */
    private void removeAroundAdvice(final Class adviceClass, int joinPointHash) {
        if (isAroundAdvice(adviceClass)) {
            AroundAdvice[] oldArray = aw$getAroundAdvice(joinPointHash);
            if (oldArray.length == 0) {
            } else if (oldArray.length == 1) {
                m_aroundAdvice.put(joinPointHash, EMPTY_AROUND_ADVICE_ARRAY);
            } else {
                AroundAdvice[] newArray = new AroundAdvice[oldArray.length - 1];
                System.arraycopy(oldArray, 0, newArray, 0, newArray.length);
                m_aroundAdvice.put(joinPointHash, newArray);
            }
        }
    }

    /**
     * @param adviceClass
     * @param joinPointHash
     */
    private void removeBeforeAdvice(final Class adviceClass, int joinPointHash) {
        if (isBeforeAdvice(adviceClass)) {
            BeforeAdvice[] oldArray = aw$getBeforeAdvice(joinPointHash);
            if (oldArray.length == 0) {
            } else if (oldArray.length == 1) {
                m_beforeAdvice.put(joinPointHash, EMPTY_BEFORE_ADVICE_ARRAY);
            } else {
                BeforeAdvice[] newArray = new BeforeAdvice[oldArray.length - 1];
                System.arraycopy(oldArray, 0, newArray, 0, newArray.length);
                m_beforeAdvice.put(joinPointHash, newArray);
            }
        }
    }

    /**
     * @param adviceClass
     * @param joinPointHash
     */
    private void removeAfterAdvice(final Class adviceClass, int joinPointHash) {
        if (isAfterAdvice(adviceClass)) {
            AfterAdvice[] oldArray = aw$getAfterAdvice(joinPointHash);
            if (oldArray.length == 0) {
            } else if (oldArray.length == 1) {
                m_afterAdvice.put(joinPointHash, EMPTY_AFTER_ADVICE_ARRAY);
            } else {
                AfterAdvice[] newArray = new AfterAdvice[oldArray.length - 1];
                System.arraycopy(oldArray, 0, newArray, 0, newArray.length);
                m_afterAdvice.put(joinPointHash, newArray);
            }
        }
    }

    /**
     * @param adviceClass
     * @param joinPointHash
     */
    private void removeAfterReturningAdvice(final Class adviceClass, int joinPointHash) {
        if (isAfterReturningAdvice(adviceClass)) {
            AfterReturningAdvice[] oldArray = aw$getAfterReturningAdvice(joinPointHash);
            if (oldArray.length == 0) {
            } else if (oldArray.length == 1) {
                m_afterReturningAdvice.put(joinPointHash, EMPTY_AFTER_RETURNING_ADVICE_ARRAY);
            } else {
                AfterReturningAdvice[] newArray = new AfterReturningAdvice[oldArray.length - 1];
                System.arraycopy(oldArray, 0, newArray, 0, newArray.length);
                m_afterReturningAdvice.put(joinPointHash, newArray);
            }
        }
    }

    /**
     * @param adviceClass
     * @param joinPointHash
     */
    private void removeAfterThrowingAdvice(final Class adviceClass, int joinPointHash) {
        if (isAfterThrowingAdvice(adviceClass)) {
            AfterThrowingAdvice[] oldArray = aw$getAfterThrowingAdvice(joinPointHash);
            if (oldArray.length == 0) {
            } else if (oldArray.length == 1) {
                m_afterThrowingAdvice.put(joinPointHash, EMPTY_AFTER_THROWING_ADVICE_ARRAY);
            } else {
                AfterThrowingAdvice[] newArray = new AfterThrowingAdvice[oldArray.length - 1];
                System.arraycopy(oldArray, 0, newArray, 0, newArray.length);
                m_afterThrowingAdvice.put(joinPointHash, newArray);
            }
        }
    }

    private boolean isAroundAdvice(final Class adviceClass) {
        if (adviceClass == AroundAdvice.class) {
            return true;
        }
        Class[] interfaces = adviceClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface == AroundAdvice.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isBeforeAdvice(final Class adviceClass) {
        if (adviceClass == BeforeAdvice.class) {
            return true;
        }
        Class[] interfaces = adviceClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface == BeforeAdvice.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isAfterAdvice(final Class adviceClass) {
        if (adviceClass == AfterAdvice.class) {
            return true;
        }
        Class[] interfaces = adviceClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface == AfterAdvice.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isAfterReturningAdvice(final Class adviceClass) {
        if (adviceClass == AfterReturningAdvice.class) {
            return true;
        }
        Class[] interfaces = adviceClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface == AfterReturningAdvice.class) {
                return true;
            }
        }
        return false;
    }

    private boolean isAfterThrowingAdvice(final Class adviceClass) {
        if (adviceClass == AfterThrowingAdvice.class) {
            return true;
        }
        Class[] interfaces = adviceClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            if (anInterface == AfterThrowingAdvice.class) {
                return true;
            }
        }
        return false;
    }
}
