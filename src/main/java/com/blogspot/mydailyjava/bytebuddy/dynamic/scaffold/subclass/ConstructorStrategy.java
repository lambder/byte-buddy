package com.blogspot.mydailyjava.bytebuddy.dynamic.scaffold.subclass;

import com.blogspot.mydailyjava.bytebuddy.dynamic.scaffold.MethodRegistry;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.SuperMethodCall;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.attribute.MethodAttributeAppender;

import java.lang.reflect.Constructor;

import static com.blogspot.mydailyjava.bytebuddy.instrumentation.method.matcher.MethodMatchers.isConstructor;

public enum ConstructorStrategy {

    NO_CONSTRUCTORS,
    IMITATE_SUPER_TYPE;

    protected Constructor<?>[] extractConstructors(Class<?> type) {
        switch (this) {
            case NO_CONSTRUCTORS:
                return new Constructor<?>[0];
            case IMITATE_SUPER_TYPE:
                return type.getDeclaredConstructors();
            default:
                throw new AssertionError();
        }
    }

    protected MethodRegistry inject(MethodRegistry methodRegistry,
                                    MethodAttributeAppender.Factory defaultMethodAttributeAppenderFactory) {
        switch (this) {
            case NO_CONSTRUCTORS:
                return methodRegistry;
            case IMITATE_SUPER_TYPE:
                return methodRegistry.prepend(new MethodRegistry.LatentMethodMatcher.Simple(isConstructor()),
                        SuperMethodCall.INSTANCE,
                        defaultMethodAttributeAppenderFactory);
            default:
                throw new AssertionError();
        }
    }
}