package net.bytebuddy.instrumentation.method.bytecode.stack.assign.primitive;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import net.bytebuddy.instrumentation.method.bytecode.stack.assign.Assigner;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.MockitoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class PrimitiveUnboxingDelegateDirectTest {

    private final Class<?> primitiveType;
    private final Class<?> wrapperType;
    private final String unboxingMethodName;
    private final String unboxingMethodDescriptor;
    private final int sizeChange;
    @Rule
    public TestRule mockitoRule = new MockitoRule(this);
    @Mock
    private TypeDescription primitiveTypeDescription, wrapperTypeDescription;
    @Mock
    private Assigner chainedAssigner;
    @Mock
    private StackManipulation stackManipulation;
    @Mock
    private MethodVisitor methodVisitor;
    @Mock
    private Instrumentation.Context instrumentationContext;

    public PrimitiveUnboxingDelegateDirectTest(Class<?> primitiveType,
                                               Class<?> wrapperType,
                                               String unboxingMethodName,
                                               String unboxingMethodDescriptor,
                                               int sizeChange) {
        this.primitiveType = primitiveType;
        this.wrapperType = wrapperType;
        this.unboxingMethodName = unboxingMethodName;
        this.unboxingMethodDescriptor = unboxingMethodDescriptor;
        this.sizeChange = sizeChange;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {boolean.class, Boolean.class, "booleanValue", "()Z", 0},
                {byte.class, Byte.class, "byteValue", "()B", 0},
                {short.class, Short.class, "shortValue", "()S", 0},
                {char.class, Character.class, "charValue", "()C", 0},
                {int.class, Integer.class, "intValue", "()I", 0},
                {long.class, Long.class, "longValue", "()J", 1},
                {float.class, Float.class, "floatValue", "()F", 0},
                {double.class, Double.class, "doubleValue", "()D", 1},
        });
    }

    @Before
    public void setUp() throws Exception {
        when(primitiveTypeDescription.isPrimitive()).thenReturn(true);
        when(primitiveTypeDescription.represents(primitiveType)).thenReturn(true);
        when(primitiveTypeDescription.getInternalName()).thenReturn(Type.getInternalName(primitiveType));
        when(wrapperTypeDescription.isPrimitive()).thenReturn(false);
        when(wrapperTypeDescription.represents(wrapperType)).thenReturn(true);
        when(wrapperTypeDescription.getInternalName()).thenReturn(Type.getInternalName(wrapperType));
        when(chainedAssigner.assign(any(TypeDescription.class), any(TypeDescription.class), anyBoolean())).thenReturn(stackManipulation);
        when(stackManipulation.isValid()).thenReturn(true);
        when(stackManipulation.apply(any(MethodVisitor.class), any(Instrumentation.Context.class))).thenReturn(StackSize.ZERO.toIncreasingSize());
    }

    @After
    public void tearDown() throws Exception {
        verifyZeroInteractions(instrumentationContext);
    }

    @Test
    public void testTrivialBoxing() throws Exception {
        StackManipulation stackManipulation = PrimitiveUnboxingDelegate.forReferenceType(wrapperTypeDescription)
                .assignUnboxedTo(primitiveTypeDescription, chainedAssigner, false);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(sizeChange));
        assertThat(size.getMaximalSize(), is(sizeChange));
        verify(methodVisitor).visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                wrapperTypeDescription.getInternalName(),
                unboxingMethodName,
                unboxingMethodDescriptor,
                false);
        verifyNoMoreInteractions(methodVisitor);
        verifyZeroInteractions(chainedAssigner);
        verifyZeroInteractions(this.stackManipulation);
    }

    @Test
    public void testImplicitBoxing() throws Exception {
        TypeDescription referenceTypeDescription = mock(TypeDescription.class);
        StackManipulation primitiveStackManipulation = PrimitiveUnboxingDelegate.forReferenceType(referenceTypeDescription)
                .assignUnboxedTo(primitiveTypeDescription, chainedAssigner, true);
        assertThat(primitiveStackManipulation.isValid(), is(true));
        StackManipulation.Size size = primitiveStackManipulation.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(sizeChange));
        assertThat(size.getMaximalSize(), is(sizeChange));
        verify(methodVisitor).visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                wrapperTypeDescription.getInternalName(),
                unboxingMethodName,
                unboxingMethodDescriptor,
                false);
        verifyNoMoreInteractions(methodVisitor);
        verify(chainedAssigner).assign(referenceTypeDescription, new TypeDescription.ForLoadedType(wrapperType), true);
        verifyNoMoreInteractions(chainedAssigner);
        verify(stackManipulation, atLeast(1)).isValid();
        verify(stackManipulation).apply(methodVisitor, instrumentationContext);
        verifyNoMoreInteractions(stackManipulation);
    }
}
