package net.bytebuddy.instrumentation.type.generic;

import net.bytebuddy.instrumentation.ByteCodeElement;
import net.bytebuddy.instrumentation.DeclaredBy;
import net.bytebuddy.instrumentation.NamedElement;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.type.TypeDescription;

import java.lang.reflect.*;

public interface GenericType<T extends ByteCodeElement> extends NamedElement, DeclaredBy<T> {

    String toSymbol();

    GenericTypeList getUpperBounds();

    GenericTypeList getLowerBounds();

    GenericTypeList getParameters();

    GenericType<?> getComponentType();

    TypeDescription asRawType();

    static final class Resolver {

        public static GenericType<?> resolve(Type type) {
            if (type instanceof Class<?>) {
                return new TypeDescription.ForLoadedType((Class<?>) type);
            } else if (type instanceof TypeVariable<?>) {
                return new Variable.ForLoadedType<ByteCodeElement>((TypeVariable<?>) type);
            } else if (type instanceof WildcardType) {
                return Wildcard.ForLoadedWildcard.of((WildcardType) type);
            } else if (type instanceof ParameterizedType) {
                return new Parameterized.ForLoadedType((ParameterizedType) type);
            } else if (type instanceof GenericArrayType) {
                return new OfArray.ForLoadedType((GenericArrayType) type);
            } else if (type == null) {
                throw new NullPointerException();
            } else {
                throw new IllegalArgumentException("Unknown type: " + type);
            }
        }

        private Resolver() {
            throw new UnsupportedOperationException();
        }
    }

    abstract static class Variable<T extends ByteCodeElement> implements GenericType<T> {

        private static final int RAW_TYPE = 0;

        @Override
        public String toSymbol() {
            return null; // TODO
        }

        @Override
        public GenericTypeList getLowerBounds() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericTypeList getParameters() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericType<?> getComponentType() {
            return null;
        }

        @Override
        public TypeDescription asRawType() {
            return getUpperBounds().get(RAW_TYPE).asRawType();
        }

        public static class ForLoadedType<S extends ByteCodeElement> extends Variable<S> {

            private final TypeVariable<?> typeVariable;

            protected ForLoadedType(TypeVariable<?> typeVariable) {
                this.typeVariable = typeVariable;
            }

            @Override
            public GenericTypeList getUpperBounds() {
                return new GenericTypeList.Loaded(typeVariable.getBounds());
            }

            @Override
            @SuppressWarnings("unchecked")
            public S getDeclaringElement() {
                GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
                ByteCodeElement byteCodeElement;
                if (genericDeclaration instanceof Class<?>) {
                    byteCodeElement = new TypeDescription.ForLoadedType((Class<?>) genericDeclaration);
                } else if (genericDeclaration instanceof Constructor<?>) {
                    byteCodeElement = new MethodDescription.ForLoadedConstructor((Constructor<?>) genericDeclaration);
                } else if (genericDeclaration instanceof Method) {
                    byteCodeElement = new MethodDescription.ForLoadedMethod((Method) genericDeclaration);
                } else {
                    throw new IllegalStateException();
                }
                return (S) byteCodeElement;
            }

            @Override
            public String getName() {
                return typeVariable.getName();
            }
        }
    }

    abstract static class Wildcard implements GenericType<ByteCodeElement> {

        private static final int RAW_TYPE = 0;

        private final Dispatcher dispatcher;

        protected Wildcard(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public String toSymbol() {
            StringBuilder stringBuilder = new StringBuilder("? ").append(dispatcher.getExtensionSymbol()).append(" ");
            for (GenericType<?> genericType : getAppliedBounds()) {
                stringBuilder.append(genericType.toSymbol());
            }
            return stringBuilder.toString();
        }

        protected abstract GenericTypeList getAppliedBounds();

        @Override
        public GenericTypeList getUpperBounds() {
            return dispatcher.isBoundAbove()
                    ? getAppliedBounds()
                    : new GenericTypeList.Empty();
        }

        @Override
        public GenericTypeList getLowerBounds() {
            return dispatcher.isBoundAbove()
                    ? new GenericTypeList.Empty()
                    : getAppliedBounds();
        }

        @Override
        public GenericTypeList getParameters() {
            return new GenericTypeList.Empty();
        }

        @Override
        public TypeDescription asRawType() {
            return getUpperBounds().get(RAW_TYPE).asRawType();
        }

        @Override
        public ByteCodeElement getDeclaringElement() {
            return null;
        }

        @Override
        public GenericType<?> getComponentType() {
            return null;
        }

        @Override
        public String getName() {
            return toSymbol();
        }

        public static enum Dispatcher {

            BOUNDED_ABOVE("extends", true),

            BOUNDED_BELOW("super", false);

            private final String extensionSymbol;

            private final boolean boundAbove;

            private Dispatcher(String extensionSymbol, boolean boundAbove) {
                this.extensionSymbol = extensionSymbol;
                this.boundAbove = boundAbove;
            }

            protected String getExtensionSymbol() {
                return extensionSymbol;
            }

            protected boolean isBoundAbove() {
                return boundAbove;
            }
        }

        public static class ForLoadedWildcard extends Wildcard {

            private final Type[] bound;

            public static GenericType<?> of(WildcardType wildcardType) {
                Type[] upperBound = wildcardType.getUpperBounds();
                return upperBound.length == 0
                        ? new ForLoadedWildcard(Dispatcher.BOUNDED_ABOVE, upperBound)
                        : new ForLoadedWildcard(Dispatcher.BOUNDED_BELOW, wildcardType.getLowerBounds());
            }

            protected ForLoadedWildcard(Dispatcher dispatcher, Type[] bound) {
                super(dispatcher);
                this.bound = bound;
            }

            @Override
            protected GenericTypeList getAppliedBounds() {
                return new GenericTypeList.Loaded(bound);
            }
        }
    }

    abstract static class Parameterized implements GenericType<TypeDescription> {

        @Override
        public GenericTypeList getUpperBounds() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericTypeList getLowerBounds() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericType<?> getComponentType() {
            return null;
        }

        public static class ForLoadedType extends Parameterized {

            private final ParameterizedType parameterizedType;

            public ForLoadedType(ParameterizedType parameterizedType) {
                this.parameterizedType = parameterizedType;
            }

            @Override
            public String toSymbol() {
                return null; // TODO
            }

            @Override
            public GenericTypeList getParameters() {
                return new GenericTypeList.Loaded(parameterizedType.getActualTypeArguments());
            }

            @Override
            public TypeDescription asRawType() {
                return new TypeDescription.ForLoadedType((Class<?>) parameterizedType.getRawType());
            }

            @Override
            public TypeDescription getDeclaringElement() {
                return null;
            }

            @Override
            public String getName() {
                return parameterizedType.getTypeName();
            }
        }
    }

    abstract static class OfArray implements GenericType<ByteCodeElement> {

        @Override
        public String toSymbol() {
            return null; // TODO
        }

        @Override
        public GenericTypeList getUpperBounds() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericTypeList getLowerBounds() {
            return new GenericTypeList.Empty();
        }

        @Override
        public GenericTypeList getParameters() {
            return new GenericTypeList.Empty();
        }

        @Override
        public TypeDescription asRawType() {
            return TypeDescription.ArrayProjection.of(getComponentType().asRawType(), 1);
        }

        @Override
        public ByteCodeElement getDeclaringElement() {
            return null;
        }

        @Override
        public String getName() {
            return getComponentType().getName() + "[]";
        }

        public static class ForLoadedType extends OfArray {

            private final GenericArrayType genericArrayType;

            public ForLoadedType(GenericArrayType genericArrayType) {
                this.genericArrayType = genericArrayType;
            }

            @Override
            public GenericType<?> getComponentType() {
                return Resolver.resolve(genericArrayType.getGenericComponentType());
            }
        }
    }
}
