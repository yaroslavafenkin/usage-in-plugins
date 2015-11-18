package org.jenkinsci.deprecatedusage;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

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
        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            // log(name + " extends " + superName + " {");
            // TODO enregistrer avant analyse superName et interfaces pour appels de méthods/fields
            // sur name
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                String[] exceptions) {
            // asm javadoc says to return a new instance each time
            return new CallersMethodVisitor();
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature,
                Object value) {
            return null;
        }

        @Override
        public void visitSource(String source, String debug) {
            // nothing
        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            // nothing
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }

        @Override
        public void visitAttribute(Attribute attr) {
            // nothing
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            // nothing
        }

        @Override
        public void visitEnd() {
            // log("}");
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc,
                boolean visible) {
            return null;
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

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            // nothing
        }

        @Override
        public void visitParameter(String name, int access) {
            // nothing
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc,
                boolean visible) {
            return null;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc,
                boolean visible) {
            return null;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath,
                String desc, boolean visible) {
            return null;
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath,
                Label[] start, Label[] end, int[] index, String desc, boolean visible) {
            return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
            return null;
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return null;
        }

        @Override
        public void visitAttribute(Attribute arg0) {
            // nothing
        }

        @Override
        public void visitCode() {
            // nothing
        }

        @Override
        public void visitEnd() {
            // nothing
        }

        @Override
        public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
            // nothing
        }

        @Override
        public void visitIincInsn(int arg0, int arg1) {
            // nothing
        }

        @Override
        public void visitInsn(int arg0) {
            // nothing
        }

        @Override
        public void visitIntInsn(int arg0, int arg1) {
            // nothing
        }

        @Override
        public void visitJumpInsn(int arg0, Label arg1) {
            // nothing
        }

        @Override
        public void visitLabel(Label arg0) {
            // nothing
        }

        @Override
        public void visitLdcInsn(Object arg0) {
            // nothing
        }

        @Override
        public void visitLineNumber(int arg0, Label arg1) {
            // nothing
        }

        @Override
        public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3,
                Label arg4, int arg5) {
            // nothing
        }

        @Override
        public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
            // nothing
        }

        @Override
        public void visitMaxs(int arg0, int arg1) {
            // nothing
        }

        @Override
        public void visitMultiANewArrayInsn(String arg0, int arg1) {
            // nothing
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
            return null;
        }

        @Override
        public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label... arg3) {
            // nothing
        }

        @Override
        public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
            // nothing
        }

        @Override
        public void visitTypeInsn(int arg0, String arg1) {
            // nothing
        }

        @Override
        public void visitVarInsn(int arg0, int arg1) {
            // nothing
        }
    }
}
