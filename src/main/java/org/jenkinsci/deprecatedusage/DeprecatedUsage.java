package org.jenkinsci.deprecatedusage;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DeprecatedUsage {
    private final DeprecatedApi deprecatedApi;

    private final Set<String> classes = new LinkedHashSet<>();
    private final Set<String> methods = new LinkedHashSet<>();
    private final Set<String> fields = new LinkedHashSet<>();
    private final ClassVisitor classVisitor = new CallersClassVisitor();

    public DeprecatedUsage(DeprecatedApi deprecatedApi) {
        super();
        this.deprecatedApi = deprecatedApi;
    }

    public void analyze(InputStream input) throws IOException {
        final ClassReader classReader = new ClassReader(input);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Set<String> getMethods() {
        return methods;
    }

    public Set<String> getFields() {
        return fields;
    }

    public boolean hasDeprecatedUsage() {
        return !classes.isEmpty() || !methods.isEmpty() || !fields.isEmpty();
    }

    void methodCalled(String className, String name, String desc) {
        // Calls to java and javax are ignored first
        if (!isJavaClass(className)) {
            if (deprecatedApi.getClasses().contains(className)) {
                classes.add(className);
            } else {
                final String method = DeprecatedApi.getMethodKey(className, name, desc);
                if (deprecatedApi.getMethods().contains(method)) {
                    methods.add(method);
                }
            }
        }
    }

    void fieldCalled(String className, String name, String desc) {
        // Calls to java and javax are ignored first
        if (!isJavaClass(className)) {
            if (deprecatedApi.getClasses().contains(className)) {
                classes.add(className);
            } else {
                final String field = DeprecatedApi.getFieldKey(className, name, desc);
                if (deprecatedApi.getFields().contains(field)) {
                    fields.add(field);
                }
            }
        }
    }

    private static boolean isJavaClass(String asmClassName) {
        // if starts with java/ or javax/, then it's a class of core java
        return asmClassName.startsWith("java/") || asmClassName.startsWith("javax/");
    }

    /**
     * Implements ASM ClassVisitor.
     */
    private class CallersClassVisitor extends ClassVisitor {
        CallersClassVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                String[] exceptions) {
            // asm javadoc says to return a new instance each time
            return new CallersMethodVisitor();
        }
    }

    /**
     * Implements ASM MethodVisitor.
     */
    private class CallersMethodVisitor extends MethodVisitor {
        CallersMethodVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            // log("\t" + owner + " " + name + " " + desc);
            methodCalled(owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            // log("\t" + owner + " " + name + " " + desc);
            methodCalled(owner, name, desc);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            // log("\t" + owner + " " + name + " " + desc);
            fieldCalled(owner, name, desc);
        }
    }
}
