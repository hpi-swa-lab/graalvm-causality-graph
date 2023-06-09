/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, 2020, Red Hat Inc. All rights reserved.
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

package com.oracle.objectfile.pecoff.cv;

public abstract class CVConstants {

    /* The names of relevant CodeView sections. */
    static final String CV_SECTION_NAME_PREFIX = ".debug$";
    static final String CV_SYMBOL_SECTION_NAME = CV_SECTION_NAME_PREFIX + "S";
    static final String CV_TYPE_SECTION_NAME = CV_SECTION_NAME_PREFIX + "T";

    /* CodeView section header signature */
    static final int CV_SIGNATURE_C13 = 4;

    static final int CV_AMD64_R8 = 336;
    static final int CV_AMD64_R9 = 337;
    static final int CV_AMD64_R10 = 338;
    static final int CV_AMD64_R11 = 339;
    static final int CV_AMD64_R12 = 340;
    static final int CV_AMD64_R13 = 341;
    static final int CV_AMD64_R14 = 342;
    static final int CV_AMD64_R15 = 343;
}
