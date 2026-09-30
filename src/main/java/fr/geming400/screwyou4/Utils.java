package fr.geming400.screwyou4;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Utils {
    private Utils() {}

    public static boolean isPackagePrivate(Method method) {
        int modifiers = method.getModifiers();
        return !Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers)
                && !Modifier.isPrivate(modifiers);
    }

    public static boolean isPackagePrivate(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        return !Modifier.isPublic(modifiers)
                && !Modifier.isProtected(modifiers)
                && !Modifier.isPrivate(modifiers);
    }

    public static boolean hasDefaultAccessibleConstructor(Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isPrimitive())
            return false;

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0)
                return true;
        }

        return false;
    }

    public static long getUniqueClassID(Class<?> clazz) {
        return Objects.hash(clazz.getName(), clazz.getPackageName(), clazz.getModifiers());
    }

    public static String getSafeUniqueClassID(Class<?> clazz) {
        String id = String.valueOf(getUniqueClassID(clazz));
        return id.replace("-", "_");
    }

    public static long getUniqueMethodID(Method method) {
        return Objects.hash(getMixinSignature(method), method.getModifiers(), getUniqueClassID(method.getDeclaringClass()));
    }

    public static String getSafeUniqueMethodID(Method method) {
        String id = String.valueOf(getUniqueMethodID(method));
        return id.replace("-", "_");
    }

    public static String getMixinSignature(Method method) {
        return method.getName() + Type.getMethodDescriptor(method);
    }

    public static boolean isVoid(Class<?> clazz) {
        return clazz.equals(Void.TYPE);
    }

    public static boolean isLambda(Method method) {
        // Hacky way to check if a given method
        // is a lambda, but it works
        return getMixinSignature(method).contains("lambda$");
    }

    public static boolean isPrivateOrHasPrivateEnclosingClass(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return false;
        } else {
            return !Modifier.isPublic(clazz.getModifiers()) || isPrivateOrHasPrivateEnclosingClass(clazz.getEnclosingClass());
        }
    }

    public static <K, V> Map<K, V> linkedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <K, V> Map<K, V> linkedMapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> Map<K, V> linkedMapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> linkedMapOf(K k1, V v1) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        return map;
    }
}
