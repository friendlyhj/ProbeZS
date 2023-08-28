package youyihj.probezs.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author youyihj
 */
public class BytecodeClassLoader extends ClassLoader {
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final Map<String, byte[]> bytecodes = new HashMap<>();

    public BytecodeClassLoader(ClassLoader parent) {
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

    public void putBytecode(String name, byte[] bytecode) {
        bytecodes.put(name, bytecode);
    }
}
