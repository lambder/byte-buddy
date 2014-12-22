package net.bytebuddy.instrumentation.type.generic;

import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.instrumentation.type.TypeList;
import net.bytebuddy.matcher.FilterableList;
import sun.net.www.content.text.Generic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface GenericTypeList<T extends GenericType<?>, S extends FilterableList<T, S>> extends FilterableList<T, S> {

    TypeList toRawTypes();

    String[] toInternalNames();

    int getStackSize();

    abstract static class AbstractBase extends FilterableList.AbstractBase<GenericType<?>, AbstractBase>
            implements GenericTypeList<GenericType<?>, AbstractBase> {

        @Override
        public String[] toInternalNames() {
            return toRawTypes().toInternalNames();
        }

        @Override
        public int getStackSize() {
            return toRawTypes().getStackSize();
        }

        @Override
        public TypeList toRawTypes() {
            List<TypeDescription> rawTypes = new ArrayList<TypeDescription>(size());
            for (GenericType<?> genericType : this) {
                rawTypes.add(genericType.asRawType());
            }
            return new TypeList.Explicit(rawTypes);
        }
    }

    static class Loaded extends AbstractBase {

        private final Type[] type;

        public Loaded(Type... type) {
            this.type = type;
        }

        @Override
        public GenericType<?> get(int index) {
            return GenericType.Resolver.resolve(this.type[index]);
        }

        @Override
        public int size() {
            return type.length;
        }

        @Override
        protected Explicit wrap(List<GenericType<?>> values) {
            return new Explicit(values);
        }
    }

    static class Explicit extends AbstractBase {

        private final List<? extends GenericType<?>> genericTypes;

        public Explicit(List<? extends GenericType<?>> genericTypes) {
            this.genericTypes = Collections.unmodifiableList(genericTypes);
        }

        @Override
        public GenericType<?> get(int index) {
            return genericTypes.get(index);
        }

        @Override
        public int size() {
            return genericTypes.size();
        }

        @Override
        protected Explicit wrap(List<GenericType<?>> values) {
            return new Explicit(values);
        }

    }

    static class Empty extends FilterableList.Empty<GenericType<?>, Empty> implements GenericTypeList<GenericType<?, Empty>> {

        @Override
        public String[] toInternalNames() {
            return null;
        }

        @Override
        public int getStackSize() {
            return 0;
        }

        @Override
        public TypeList toRawTypes() {
            return new TypeList.Empty();
        }
    }
}
