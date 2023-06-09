/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.posix.headers.darwin;

import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CConstant;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CFieldAddress;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CIntPointer;
import org.graalvm.word.PointerBase;

import com.oracle.svm.core.posix.headers.PosixDirectives;

// Checkstyle: stop

@CContext(PosixDirectives.class)
public class DarwinThreadInfo {

    @CConstant
    public static native int THREAD_INFO_MAX();

    @CConstant
    public static native int THREAD_BASIC_INFO();

    @CStruct
    public interface time_value_t extends PointerBase {
        @CField
        int seconds();

        @CField
        int microseconds();
    }

    @CStruct
    public interface thread_basic_info_data_t extends PointerBase {
        @CFieldAddress
        time_value_t user_time();

        @CFieldAddress
        time_value_t system_time();

        @CField
        int cpu_usage();

        @CField
        int policy();

        @CField
        int run_state();

        @CField
        int flags();

        @CField
        int suspend_count();

        @CField
        int sleep_time();
    }

    @CFunction(transition = CFunction.Transition.NO_TRANSITION)
    public static native int thread_info(int machPort, int flavor, PointerBase threadInfoOut, CIntPointer threadInfoOutCnt);
}
