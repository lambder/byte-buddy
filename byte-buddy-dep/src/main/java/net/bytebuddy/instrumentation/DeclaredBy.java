package net.bytebuddy.instrumentation;

import net.bytebuddy.instrumentation.ByteCodeElement;

/**
 * This interface represents all elements that can be declared within a type, i.e. other types and type members.
 */
public interface DeclaredBy<T extends ByteCodeElement> {

    /**
     * Returns the declaring type of this instance.
     *
     * @return The declaring type or {@code null} if no such type exists.
     */
    T getDeclaringElement();
}
