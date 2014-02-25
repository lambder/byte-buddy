package com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.constant;

import com.blogspot.mydailyjava.bytebuddy.instrumentation.Instrumentation;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public enum FloatConstant implements StackManipulation {

    ZERO(Opcodes.FCONST_0),
    ONE(Opcodes.FCONST_1),
    TWO(Opcodes.FCONST_2);

    private static final Size SIZE = StackSize.SINGLE.toIncreasingSize();

    private static class ConstantPool implements StackManipulation {

        private final float value;

        private ConstantPool(float value) {
            this.value = value;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Instrumentation.Context instrumentationContext) {
            methodVisitor.visitLdcInsn(value);
            return SIZE;
        }
    }

    public static StackManipulation forValue(float value) {
        if (value == 0f) {
            return ZERO;
        } else if (value == 1f) {
            return ONE;
        } else if (value == 2f) {
            return TWO;
        } else {
            return new ConstantPool(value);
        }
    }

    private final int opcode;

    private FloatConstant(int opcode) {
        this.opcode = opcode;
    }


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Size apply(MethodVisitor methodVisitor, Instrumentation.Context instrumentationContext) {
        methodVisitor.visitInsn(opcode);
        return SIZE;
    }
}