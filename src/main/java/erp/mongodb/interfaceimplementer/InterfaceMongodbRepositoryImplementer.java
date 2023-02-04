package erp.mongodb.interfaceimplementer;

import erp.repository.Mutexes;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class InterfaceMongodbRepositoryImplementer {
    public static <I> I instance(Class<I> itfType, MongoTemplate mongoTemplate) {
        String newTypeClsName = defineClass(itfType);

        Constructor constructor = null;
        try {
            constructor = Class.forName(newTypeClsName).getDeclaredConstructor(MongoTemplate.class);
        } catch (Exception e) {
            throw new RuntimeException("getDeclaredConstructor for " + newTypeClsName + " error", e);
        }
        constructor.setAccessible(true);
        try {
            return (I) constructor.newInstance(mongoTemplate);
        } catch (Exception e) {
            throw new RuntimeException("newInstance for " + newTypeClsName + " error", e);
        }
    }

    public static <I> I instance(Class<I> itfType, MongoTemplate mongoTemplate, long maxLockTime) {
        String newTypeClsName = defineClass(itfType);

        Constructor constructor = null;
        try {
            constructor = Class.forName(newTypeClsName).getDeclaredConstructor(MongoTemplate.class, long.class);
        } catch (Exception e) {
            throw new RuntimeException("getDeclaredConstructor for " + newTypeClsName + " error", e);
        }
        constructor.setAccessible(true);
        try {
            return (I) constructor.newInstance(mongoTemplate, maxLockTime);
        } catch (Exception e) {
            throw new RuntimeException("newInstance for " + newTypeClsName + " error", e);
        }
    }

    public static <I> I instance(Class<I> itfType, MongoTemplate mongoTemplate, Mutexes<Object> mutexes) {
        String newTypeClsName = defineClass(itfType);

        Constructor constructor = null;
        try {
            constructor = Class.forName(newTypeClsName).getDeclaredConstructor(MongoTemplate.class, Mutexes.class);
        } catch (Exception e) {
            throw new RuntimeException("getDeclaredConstructor for " + newTypeClsName + " error", e);
        }
        constructor.setAccessible(true);
        try {
            return (I) constructor.newInstance(mongoTemplate, mutexes);
        } catch (Exception e) {
            throw new RuntimeException("newInstance for " + newTypeClsName + " error", e);
        }
    }

    private static <I> String defineClass(Class<I> itfType) {
        byte[] newClsBytes = new byte[0];
        TypeVariable<Class<I>>[] typeVariables = itfType.getTypeParameters();
        if (typeVariables.length > 0) {
            TypeVariable<Class<I>> entityTypeVariable = typeVariables[0];
            Type[] entityTypeBounds = entityTypeVariable.getBounds();
            Class entityType = (Class) entityTypeBounds[0];
            newClsBytes = generateNewClsBytesForGeneric(itfType, entityType);
        } else {
            for (Method method : itfType.getMethods()) {
                if (method.getName().equals("find")) {
                    Class entityType = method.getReturnType();
                    Class idType = method.getParameterTypes()[0];
                    newClsBytes = generateNewClsBytes(itfType, entityType, idType);
                    break;
                }
            }
        }
        String newTypeClsName = "erp.mongodb.repository.generated." + itfType.getName();
        Object[] argArray = new Object[]{newTypeClsName, newClsBytes,
                new Integer(0), new Integer(newClsBytes.length)};
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class cls = null;
        try {
            cls = Class.forName("java.lang.ClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class for java.lang.ClassLoader error", e);
        }
        java.lang.reflect.Method method = null;
        try {
            method = cls.getDeclaredMethod(
                    "defineClass",
                    new Class[]{String.class, byte[].class, int.class, int.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("get getDeclaredMethod for defineClass error", e);
        }
        method.setAccessible(true);
        try {
            method.invoke(cl, argArray);
        } catch (Exception e) {
            throw new RuntimeException("invoke defineClass error", e);
        }
        return newTypeClsName;
    }

    private static byte[] generateNewClsBytes(Class itfType, Class entityType, Class idType) {
        String entityTypeDesc = "L" + entityType.getName().replace('.', '/') + ";";
        String templateEntityTypeDesc = "Lerp/mongodb/interfaceimplementer/TemplateEntity;";

        String idTypeDesc = "L" + idType.getName().replace('.', '/') + ";";
        String templateIdTypeDesc = "Ljava/lang/Object;";

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("erp/mongodb/interfaceimplementer/TemplateEntityRepositoryImpl.class");
        byte[] bytes = new byte[0];
        try {
            bytes = new byte[is.available()];
            is.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException("read TemplateEntityRepositoryImpl.class error", e);
        }

        String newTypeClsName = "erp.mongodb.repository.generated." + itfType.getName();
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                interfaces[0] = itfType.getName().replace('.', '/');
                name = newTypeClsName.replace('.', '/');
                signature = signature.replaceAll(templateEntityTypeDesc, entityTypeDesc);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                mthDesc = mthDesc.replaceAll(templateEntityTypeDesc, entityTypeDesc).replaceAll(templateIdTypeDesc, idTypeDesc);
                return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {
                    @Override
                    public void visitTypeInsn(final int opcode, final String type) {
                        String realType = type;
                        if (Opcodes.CHECKCAST == opcode) {
                            realType = entityType.getName().replace('.', '/');
                        }
                        super.visitTypeInsn(opcode, realType);
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static byte[] generateNewClsBytesForGeneric(Class itfType, Class entityType) {
        String entityTypeDesc = "L" + entityType.getName().replace('.', '/') + ";";
        String templateEntityTypeDesc = "Lerp/mongodb/interfaceimplementer/TemplateEntity;";

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("erp/mongodb/interfaceimplementer/GenericTemplateEntityRepositoryImpl.class");
        byte[] bytes = new byte[0];
        try {
            bytes = new byte[is.available()];
            is.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException("read GenericTemplateEntityRepositoryImpl.class error", e);
        }

        String newTypeClsName = "erp.mongodb.repository.generated." + itfType.getName();
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                interfaces[0] = itfType.getName().replace('.', '/');
                name = newTypeClsName.replace('.', '/');
                signature = signature.replaceAll(templateEntityTypeDesc, entityTypeDesc);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                mthDesc = mthDesc.replaceAll(templateEntityTypeDesc, entityTypeDesc);
                return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {
                    @Override
                    public void visitTypeInsn(final int opcode, final String type) {
                        String realType = type;
                        if (Opcodes.CHECKCAST == opcode) {
                            realType = entityType.getName().replace('.', '/');
                        }
                        super.visitTypeInsn(opcode, realType);
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
