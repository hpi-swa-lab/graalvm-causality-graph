/*
 * Copyright (c) 2017, 2022, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.runtime.nodes.asm.syscall;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.CountingConditionProfile;
import com.oracle.truffle.llvm.runtime.memory.LLVMSyscallOperationNode;
import com.oracle.truffle.llvm.runtime.pointer.LLVMNativePointer;

public abstract class LLVMAMD64SyscallMmapNode extends LLVMSyscallOperationNode {

    @Override
    public final String getName() {
        return "mmap";
    }

    private final CountingConditionProfile mapAnonymousProfile = CountingConditionProfile.create();

    /**
     * @param addr
     * @param len
     * @param prot
     * @param flags
     * @param fildes
     * @param off
     * @see #executeGeneric(Object, Object, Object, Object, Object, Object)
     */
    @Specialization
    protected long doOp(LLVMNativePointer addr, long len, long prot, long flags, long fildes, long off) {
        if (mapAnonymousProfile.profile((flags & LLVMAMD64Memory.MAP_ANONYMOUS) != 0)) {
            LLVMNativePointer ptr = getLanguage().getLLVMMemory().allocateMemory(this, len);
            return ptr.asNative();
        }
        return -LLVMAMD64Error.ENOMEM;
    }

    @Specialization
    protected long doOp(long addr, long len, long prot, long flags, long fildes, long off) {
        return doOp(LLVMNativePointer.create(addr), len, prot, flags, fildes, off);
    }
}
