package youyihj.probezs.bracket;

import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.zenscript.GlobalRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
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
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.natives.JavaMethod;
import stanhebben.zenscript.util.MethodOutput;
import stanhebben.zenscript.util.ZenPosition;
import youyihj.probezs.ProbeZS;
import youyihj.probezs.api.BracketHandlerService;
import youyihj.probezs.core.BytecodeClassLoader;
import youyihj.probezs.render.RenderHelper;
import youyihj.probezs.render.RenderTaskDispatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author youyihj
 */
public class BracketHandlerCaller implements BracketHandlerService {
    public static final BracketHandlerCaller INSTANCE = new BracketHandlerCaller();
    public static final BracketHandlerResult EMPTY_RESULT = new BracketHandlerResult(null, ZenType.ANY);

    private static final IEnvironmentGlobal ENVIRONMENT_GLOBAL = GlobalRegistry.makeGlobalEnvironment(new HashMap<>());
    private static final ZenPosition POSITION = new ZenPosition(null, 1, 0, "BracketHandlerCaller.java");
    private static final Map<Method, Class<?>> CACHED_SUPPLIER_CLASSES = new HashMap<>();
    private static final Field CALL_STATIC_METHOD_FIELD;
    private static final Field CALL_STATIC_ARGUMENTS_FILED;
    private static final Field EXPRESSION_INT_VALUE;
    private static final Field EXPRESSION_FLOAT_VALUE;
    private static final Field EXPRESSION_STRING_VALUE;
    private static final BytecodeClassLoader CLASS_LOADER = new BytecodeClassLoader(ProbeZS.class.getClassLoader());

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

    public BracketHandlerResult query(String content) {
        String className = "bh$" + content.replaceAll("\\W", "_");
        IPartialExpression expression = getZenExpression(content);
        BracketHandlerResult result;
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

    private static BracketHandlerResult getCached(ExpressionCallStatic expressionCallStatic, String className) {
        Method method = ((JavaMethod) getFieldUnchecked(CALL_STATIC_METHOD_FIELD, expressionCallStatic)).getMethod();
        Expression[] expressions = getFieldUnchecked(CALL_STATIC_ARGUMENTS_FILED, expressionCallStatic);
        Class<?> clazz = CACHED_SUPPLIER_CLASSES.computeIfAbsent(method, (method1 -> {
            byte[] bytecode = defineArgumentSupplierClass(method1, expressions, className);
            CLASS_LOADER.putBytecode(className, bytecode);
//            try {
//                FileUtils.writeByteArrayToFile(new File("bhClasses/" + className + ".class"), bytecode);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            try {
                return Class.forName(className, true, CLASS_LOADER);
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
                long value = 0;
                try {
                    value = EXPRESSION_INT_VALUE.getLong(expression);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (parameterType == int.class) {
                    argumentsForConstructor[i] = ((int) value);
                } else if (parameterType == long.class) {
                    argumentsForConstructor[i] = value;
                } else if (parameterType == short.class) {
                    argumentsForConstructor[i] = ((short) value);
                } else if (parameterType == byte.class) {
                    argumentsForConstructor[i] = ((byte) value);
                } else {
                    return getDirectly(expressionCallStatic, className);
                }
            } else if (expression instanceof ExpressionFloat) {
                double value = 0;
                try {
                    value = EXPRESSION_FLOAT_VALUE.getDouble(expression);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (parameterType == double.class) {
                    argumentsForConstructor[i] = value;
                } else if (parameterType == float.class) {
                    argumentsForConstructor[i] = ((float) value);
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
            return new BracketHandlerResult(obj, expressionCallStatic.getType());
        } catch (ReflectiveOperationException e) {
            return getDirectly(expressionCallStatic, className);
        }
    }

    private static BracketHandlerResult getDirectly(IPartialExpression expression, String className) {
        Class<?> supplierClass;
        try {
            supplierClass = Class.forName(className, true, CLASS_LOADER);
        } catch (ClassNotFoundException e) {
            byte[] bytecode = defineDirectSupplierClass(expression, className);
            CLASS_LOADER.putBytecode(className, bytecode);
//            try {
//                FileUtils.writeByteArrayToFile(new File("bhClasses/" + className + ".class"), bytecode);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            return getDirectly(expression, className);
        }
        try {
            return new BracketHandlerResult(((Supplier<?>) supplierClass.newInstance()).get(), expression.getType());
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

    @SuppressWarnings("unchecked")
    private static <T> T getFieldUnchecked(Field field, Object obj) {
        try {
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public String getLocalizedName(String expr) {
        BracketHandlerResult result = query(expr);
        Object object = result.getObject();
        if (object instanceof IItemStack) {
            return ((IItemStack) object).getDisplayName();
        }
        if (object instanceof ILiquidStack) {
            return ((ILiquidStack) object).getDisplayName();
        }
        if (object instanceof IOreDictEntry) {
            return ((IOreDictEntry) object).getFirstItem().getDisplayName();
        }
        return null;
    }

    @Nullable
    @Override
    public String getIcon(String expr) {
        BracketHandlerResult result = query(expr);
        Object object = result.getObject();
        if (object instanceof IItemStack) {
            try {
                return RenderTaskDispatcher.submit(() -> {
                    RenderHelper.setupRenderState(32);
                    GlStateManager.pushMatrix();
                    GlStateManager.clearColor(0, 0, 0, 0);
                    GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(CraftTweakerMC.getItemStack((IItemStack) object), 0, 0);
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
            } catch (Exception e) {
                ProbeZS.logger.error(e);
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public String getTypeName(String expr) {
        return query(expr).getZenType().getName();
    }
}
