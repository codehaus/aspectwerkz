package test.inlining;

import java.util.Hashtable;
import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;
import org.codehaus.aspectwerkz.transform.delegation.Klass;
import org.codehaus.aspectwerkz.transform.inlining.ProxyMethodClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class InliningPreProcessor implements ClassPreProcessor {

    public void initialize(Hashtable m) {
    }

    public byte[] preProcess(String name, byte[] bs, ClassLoader loader) {
        System.out.println("pre processing " + name);
        try {
            name.replace('/', '.');

            //            List m_definitions = SystemDefinitionContainer.getSystemDefinitions(loader);
            //            System.out.println("nr of systems: " + m_definitions.size());

            if (!name.startsWith("test.inlining.")) {
                return bs;
            }

            Klass klass = new Klass(name, bs, loader);
            if (!name.startsWith("test.inlining.")) {
                //System.out.println("\t\t\t\t skip " + name);
                return bs;
            }
            System.out.println("weaving class = " + name);

            ClassReader cr = new ClassReader(bs);
            ClassWriter cw = new ClassWriter(true);
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(klass.getCtClass(), loader);
            ProxyMethodClassAdapter proxyMethodClassAdapter = new ProxyMethodClassAdapter(
                cw,
                loader,
                classInfo);
            cr.accept(proxyMethodClassAdapter, false);
            return cw.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
            return bs;
        }
    }
}