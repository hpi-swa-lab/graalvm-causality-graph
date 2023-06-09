/*
 * Copyright (c) 2019, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.wasm;

import static java.lang.Integer.compareUnsigned;
import static org.graalvm.wasm.constants.Sizes.MAX_TABLE_DECLARATION_SIZE;
import static org.graalvm.wasm.constants.Sizes.MAX_TABLE_INSTANCE_SIZE;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.graalvm.wasm.constants.Sizes;

import com.oracle.truffle.api.interop.TruffleObject;

public final class WasmTable extends EmbedderDataHolder implements TruffleObject {
    /**
     * @see #declaredMinSize()
     */
    private final int declaredMinSize;

    /**
     * @see #declaredMaxSize()
     */
    private final int declaredMaxSize;

    /**
     * @see #elemType()
     */
    private final byte elemType;

    /**
     * @see #minSize()
     */
    private int currentMinSize;

    /**
     * The maximum practical size of this table instance.
     * <p>
     * It is the minimum between {@link #declaredMaxSize the limit defined in the module binary},
     * {@link Sizes#MAX_TABLE_INSTANCE_SIZE the GraalWasm limit} and any additional limit (the JS
     * API for example has lower limits).
     * <p>
     * This is different from {@link #declaredMaxSize()}, which can be higher.
     */
    private final int maxAllowedSize;

    private Object[] elements;

    @TruffleBoundary
    private WasmTable(int declaredMinSize, int declaredMaxSize, int initialSize, int maxAllowedSize, byte elemType, Object initialValue) {
        assert compareUnsigned(declaredMinSize, initialSize) <= 0;
        assert compareUnsigned(initialSize, maxAllowedSize) <= 0;
        assert compareUnsigned(maxAllowedSize, declaredMaxSize) <= 0;
        assert compareUnsigned(maxAllowedSize, MAX_TABLE_INSTANCE_SIZE) <= 0;
        assert compareUnsigned(declaredMaxSize, MAX_TABLE_DECLARATION_SIZE) <= 0;
        assert elemType == WasmType.FUNCREF_TYPE || elemType == WasmType.EXTERNREF_TYPE;

        this.declaredMinSize = declaredMinSize;
        this.declaredMaxSize = declaredMaxSize;
        this.maxAllowedSize = maxAllowedSize;
        this.currentMinSize = declaredMinSize;
        this.elements = new Object[declaredMinSize];
        Arrays.fill(this.elements, initialValue);
        this.elemType = elemType;
    }

    public WasmTable(int declaredMinSize, int declaredMaxSize, int maxAllowedSize, byte elemType) {
        this(declaredMinSize, declaredMaxSize, declaredMinSize, maxAllowedSize, elemType, WasmConstant.NULL);
    }

    public WasmTable(int declaredMinSize, int declaredMaxSize, int maxAllowedSize, byte elemType, Object initialValue) {
        this(declaredMinSize, declaredMaxSize, declaredMinSize, maxAllowedSize, elemType, initialValue);
    }

    /**
     * Shrinks this table's size to its {@link #declaredMinSize()} initial size, and sets all
     * elements to {@code null}.
     * <p>
     * Note: this does not restore content from elements section. For this, use
     * {@link org.graalvm.wasm.parser.bytecode.BytecodeParser#resetTableState}.
     */
    @TruffleBoundary
    public void reset() {
        elements = new Object[declaredMinSize];
        Arrays.fill(elements, WasmConstant.NULL);
        currentMinSize = declaredMinSize;
    }

    /**
     * The current size of this table instance.
     */
    public int size() {
        return elements.length;
    }

    /**
     * The minimum size of this table as declared in the binary.
     */
    public int declaredMinSize() {
        return declaredMinSize;
    }

    /**
     * The maximum size of this table as declared in the binary.
     * <p>
     * This is an upper bound on this table's size. This table can only be imported with a greater
     * or equal maximum size.
     * <p>
     * This is different from internal max allowed size, which can be lower.
     */
    public int declaredMaxSize() {
        return declaredMaxSize;
    }

    /**
     * The type of the elements in the table.
     * <p>
     * This table can only be imported with an equivalent elem type.
     *
     * @return Either {@link WasmType#FUNCREF_TYPE} or {@link WasmType#EXTERNREF_TYPE}.
     */
    public byte elemType() {
        return elemType;
    }

    /**
     * The current minimum size of the table. The size can change based on calls to
     * {@link #grow(int, Object)}.
     * <p>
     * This is a lower bound on this table's size. This table can only be imported with a lower or
     * equal minimum size.
     */
    public int minSize() {
        return currentMinSize;
    }

    public Object[] elements() {
        return elements;
    }

    /**
     * Gets element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if the index is negative or greater or equal to table size
     */
    public Object get(int index) {
        return elements[index];
    }

    /**
     * Sets element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if the index is negative or greater or equal to table size
     */
    public void set(int index, Object element) {
        elements[index] = element;
    }

    /**
     * Initializes the content of the table based on the given elem instance.
     * 
     * @param elemInstance The source elem instance that should be copied to the table
     * @param sourceOffset The offset in the source elem segment
     * @param destinationOffset The offset in the table
     * @param length The number of elements that should be copied
     */
    public void initialize(Object[] elemInstance, int sourceOffset, int destinationOffset, int length) {
        System.arraycopy(elemInstance, sourceOffset, elements, destinationOffset, length);
    }

    /**
     * Fills the table with the given value.
     * 
     * @param offset The offset in the table
     * @param length The number of elements that should be filled
     * @param value The value that should be used for filling the table
     */
    @TruffleBoundary
    public void fill(int offset, int length, Object value) {
        assert offset + length <= size();
        Arrays.fill(elements, offset, offset + length, value);
    }

    /**
     * Copies elements from another table into this table.
     * 
     * @param source The source table
     * @param sourceOffset The offset in the source table
     * @param destinationOffset The offset in this table
     * @param length The number of elements that should be copied
     */
    public void copyFrom(WasmTable source, int sourceOffset, int destinationOffset, int length) {
        System.arraycopy(source.elements, sourceOffset, elements, destinationOffset, length);
    }

    /**
     * Grows the table so that it can contain {@code delta} more elements.
     * 
     * @param delta The expected additional number of elements
     * @param value The value of the newly added elements
     * @return The previous size if the growing succeeded, -1 otherwise.
     */
    @TruffleBoundary
    public int grow(int delta, Object value) {
        final int size = size();
        final int targetSize = size + delta;
        if (compareUnsigned(delta, maxAllowedSize) <= 0 && compareUnsigned(targetSize, maxAllowedSize) <= 0) {
            elements = Arrays.copyOf(elements, targetSize);
            Arrays.fill(elements, size, targetSize, value);
            currentMinSize += targetSize;
            return size;
        }
        return -1;
    }
}
