package youyihj.probezs.bracket;

import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.zenscript.GlobalRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.*;
import stanhebben.zenscript.ZenTokener;
import stanhebben.zenscript.compiler.EnvironmentClass;
import stanhebben.zenscript.compiler.EnvironmentMethod;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.compiler.ZenClassWriter;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.JavaMethod;
import stanhebben.zenscript.util.MethodOutput;
import stanhebben.zenscript.util.ZenPosition;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.api.BracketHandlerResult;
import youyihj.probezs.api.BracketHandlerService;
import youyihj.probezs.render.RenderHelper;
import youyihj.probezs.render.RenderTaskDispatcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author youyihj
 */
public class BracketHandlerCaller implements BracketHandlerService {
    public static final BracketHandlerCaller INSTANCE = new BracketHandlerCaller();
    public static final BracketHandlerResult EMPTY_RESULT = new BracketHandlerResult("null", Collections.emptyMap());

    private static final IEnvironmentGlobal ENVIRONMENT_GLOBAL = GlobalRegistry.makeGlobalEnvironment(new HashMap<>());
    private static final ZenPosition POSITION = new ZenPosition(null, 1, 0, "BracketHandlerCaller.java");
    private static final Map<Method, Class<?>> CACHED_SUPPLIER_CLASSES = new HashMap<>();
    private static final Field CALL_STATIC_METHOD_FIELD;
    private static final Field CALL_STATIC_ARGUMENTS_FILED;
    private static final Field EXPRESSION_INT_VALUE;
    private static final Field EXPRESSION_FLOAT_VALUE;
    private static final Field EXPRESSION_STRING_VALUE;

    static {
        try {
            CALL_STATIC_METHOD_FIELD = ExpressionCallStatic.class.getDeclaredField("method");
            CALL_STATIC_METHOD_FIELD.setAccessible(true);
            CALL_STATIC_ARGUMENTS_FILED = ExpressionCallStatic.class.getDeclaredField("arguments");
            CALL_STATIC_ARGUMENTS_FILED.setAccessible(true);
            EXPRESSION_INT_VALUE = ExpressionInt.class.getDeclaredField("value");
            EXPRESSION_INT_VALUE.setAccessible(true);
            EXPRESSION_FLOAT_VALUE = ExpressionFloat.class.getDeclaredField("value");
            EXPRESSION_FLOAT_VALUE.setAccessible(true);
            EXPRESSION_STRING_VALUE = ExpressionString.class.getDeclaredField("value");
            EXPRESSION_STRING_VALUE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BracketHandlerResult query(String content, boolean requiresExtras) {
        String className = "bh$" + content.replaceAll("\\W", "_");
        IPartialExpression expression = getZenExpression(content);
        ZenBracketHandlerResult result;
        if (expression == null) {
            return EMPTY_RESULT;
        }
        if (expression instanceof ExpressionCallStatic) {
            result = getCached(((ExpressionCallStatic) expression), className);
        } else {
            result = getDirectly(expression, className);
        }
        if (result.getObject() == null) {
            return EMPTY_RESULT;
        }
        if (requiresExtras) {
            writeExtras(result);
        }
        return result;
    }

    private static IPartialExpression getZenExpression(String content) {
        try {
            ZenTokener tokener = new ZenTokener(content, GlobalRegistry.getEnvironment(), "", false);
            List<Token> tokens = new ArrayList<>();
            while (tokener.hasNext()) {
                tokens.add(tokener.next());
            }
            IZenSymbol symbol = GlobalRegistry.resolveBracket(ENVIRONMENT_GLOBAL, tokens);
            if (symbol == null) {
                return null;
            }
            return symbol.instance(POSITION);
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    private static ZenBracketHandlerResult getCached(ExpressionCallStatic expressionCallStatic, String className) {
        Method method = ((JavaMethod) getFieldUnchecked(CALL_STATIC_METHOD_FIELD, expressionCallStatic)).getMethod();
        Expression[] expressions = getFieldUnchecked(CALL_STATIC_ARGUMENTS_FILED, expressionCallStatic);
        Class<?> clazz = CACHED_SUPPLIER_CLASSES.computeIfAbsent(method, (method1 -> {
            byte[] bytecode = defineArgumentSupplierClass(method1, expressions, className);
            InternalClassLoader.INSTANCE.bytecodes.put(className, bytecode);
//            try {
//                FileUtils.writeByteArrayToFile(new File("bhClasses/" + className + ".class"), bytecode);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            try {
                return Class.forName(className, true, InternalClassLoader.INSTANCE);
            } catch (ClassNotFoundException e) {
                throw new AssertionError();
            }
        }));
        Object[] argumentsForConstructor = new Object[expressions.length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < expressions.length; i++) {
            Expression expression = expressions[i];
            Class<?> parameterType = parameterTypes[i];
            if (expression instanceof ExpressionInt) {
                Long value = getFieldUnchecked(EXPRESSION_INT_VALUE, expression);
                if (parameterType == int.class) {
                    argumentsForConstructor[i] = value.intValue();
                } else if (parameterType == long.class) {
                    argumentsForConstructor[i] = value;
                } else if (parameterType == short.class) {
                    argumentsForConstructor[i] = value.shortValue();
                } else if (parameterType == byte.class) {
                    argumentsForConstructor[i] = value.byteValue();
                } else {
                    return getDirectly(expressionCallStatic, className);
                }
            } else if (expression instanceof ExpressionFloat) {
                Double value = getFieldUnchecked(EXPRESSION_FLOAT_VALUE, expression);
                if (parameterType == double.class) {
                    argumentsForConstructor[i] = value;
                } else if (parameterType == float.class) {
                    argumentsForConstructor[i] = value.floatValue();
                } else {
                    return getDirectly(expressionCallStatic, className);
                }
            } else if (expression instanceof ExpressionString) {
                String value = getFieldUnchecked(EXPRESSION_STRING_VALUE, expression);
                if (parameterType == String.class) {
                    argumentsForConstructor[i] = value;
                } else {
                    return getDirectly(expressionCallStatic, className);
                }
            }
        }
        try {
            Object obj = ((Supplier<?>) clazz.getConstructor(parameterTypes).newInstance(argumentsForConstructor)).get();
            return new ZenBracketHandlerResult(obj, expressionCallStatic.getType());
        } catch (ReflectiveOperationException e) {
            return getDirectly(expressionCallStatic, className);
        }
    }

    private static ZenBracketHandlerResult getDirectly(IPartialExpression expression, String className) {
        Class<?> supplierClass;
        try {
            supplierClass = Class.forName(className, true, InternalClassLoader.INSTANCE);
        } catch (ClassNotFoundException e) {
            byte[] bytecode = defineDirectSupplierClass(expression, className);
            InternalClassLoader.INSTANCE.bytecodes.put(className, bytecode);
//            try {
//                FileUtils.writeByteArrayToFile(new File("bhClasses/" + className + ".class"), bytecode);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            return getDirectly(expression, className);
        }
        try {
            return new ZenBracketHandlerResult(((Supplier<?>) supplierClass.newInstance()).get(), expression.getType());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] defineArgumentSupplierClass(Method method, Expression[] expressions, String className) {
        ClassWriter classWriter = new ZenClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, Type.getInternalName(Object.class), new String[]{Type.getInternalName(Supplier.class)});
        StringBuilder constructorDesc = new StringBuilder("(");
        for (int i = 0; i < expressions.length; i++) {
            Expression expression = expressions[i];
            String typeDesc = expression.getType().toASMType().getDescriptor();
            FieldVisitor fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "arg" + i, typeDesc, null, null);
            constructorDesc.append(typeDesc);
            fieldVisitor.visitEnd();
        }
        constructorDesc.append(")V");
        MethodVisitor constructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructorDesc.toString(), null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        int paraIndex = 0;
        for (int i = 0; i < expressions.length; i++) {
            Expression expression = expressions[i];
            Type asmType = expression.getType().toASMType();
            constructor.visitVarInsn(ALOAD, 0);
            paraIndex += asmType.getSize();
            constructor.visitVarInsn(asmType.getOpcode(ILOAD), paraIndex);
            constructor.visitFieldInsn(PUTFIELD, className, "arg" + i, asmType.getDescriptor());
        }
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        MethodVisitor getMethod = classWriter.visitMethod(ACC_PUBLIC, "get", "()" + Type.getDescriptor(Object.class), null, null);
        getMethod.visitCode();
        for (int i = 0; i < expressions.length; i++) {
            Expression expression = expressions[i];
            Type asmType = expression.getType().toASMType();
            getMethod.visitVarInsn(ALOAD, 0);
            getMethod.visitFieldInsn(GETFIELD, className, "arg" + i, asmType.getDescriptor());
        }
        getMethod.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method), false);
        getMethod.visitInsn(ARETURN);
        getMethod.visitMaxs(1, 1);
        getMethod.visitEnd();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static byte[] defineDirectSupplierClass(IPartialExpression expression, String className) {
        ClassWriter classWriter = new ZenClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, Type.getInternalName(Object.class), new String[]{Type.getInternalName(Supplier.class)});
        MethodVisitor constructor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        MethodOutput methodOutput = new MethodOutput(classWriter, Opcodes.ACC_PUBLIC, "get", "()" + Type.getDescriptor(Object.class), null, null);
        EnvironmentClass environmentClass = new EnvironmentClass(classWriter, ENVIRONMENT_GLOBAL);
        EnvironmentMethod environmentMethod = new EnvironmentMethod(methodOutput, environmentClass);
        methodOutput.start();
        expression.eval(environmentMethod).compile(true, environmentMethod);
        methodOutput.returnObject();
        methodOutput.end();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static void writeExtras(ZenBracketHandlerResult result) {
        Object object = result.getObject();
        Map<String, String> extras = result.getExtras();
        if (object instanceof IItemStack) {
            writeItemInfo(CraftTweakerMC.getItemStack((IItemStack) object), extras);
        }
    }

    private static void writeItemInfo(ItemStack item, Map<String, String> extras) {
        extras.put("name", item.getDisplayName());
        try {
            String iconBase64 = RenderTaskDispatcher.submit(() -> {
                RenderHelper.setupRenderState(32);
                GlStateManager.pushMatrix();
                GlStateManager.clearColor(0, 0, 0, 0);
                GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, 0, 0);
                GlStateManager.popMatrix();
                RenderHelper.tearDownRenderState();
                BufferedImage img = RenderHelper.createFlipped(RenderHelper.readPixels(32, 32));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    ImageIO.write(img, "PNG", out);
                } catch (IOException ignored) {
                }
                return Base64.getEncoder().encodeToString(out.toByteArray());
            }).get(3, TimeUnit.SECONDS);
            extras.put("icon", iconBase64);
        } catch (Exception e) {
            ProbeZS.logger.error(e);
        }

    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldUnchecked(Field field, Object obj) {
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class InternalClassLoader extends ClassLoader {
        private static final InternalClassLoader INSTANCE = new InternalClassLoader(ProbeZS.class.getClassLoader());
        private final Map<String, Class<?>> classes = new HashMap<>();
        private final Map<String, byte[]> bytecodes = new HashMap<>();

        public InternalClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (classes.containsKey(name)) {
                return classes.get(name);
            }
            if (bytecodes.containsKey(name)) {
                byte[] bytes = bytecodes.get(name);
                Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                classes.put(name, clazz);
                return clazz;
            }
            return super.findClass(name);
        }
    }
}
