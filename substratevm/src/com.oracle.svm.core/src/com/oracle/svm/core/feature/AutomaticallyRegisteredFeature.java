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
package com.oracle.svm.core.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

/**
 * {@link InternalFeature} classes with this annotation are automatically registered using an
 * annotation processor. If both a class and a subclass are annotated with
 * {@link AutomaticallyRegisteredFeature}, only the subclass will be registered as a feature.
 *
 * Note that this requires the `SVM_PROCESSOR` to be defined as an annotation processor in the
 * suite.py file.
 *
 * Only classes that are part of the image builder can use this annotation. Even parts of GraalVM
 * that are not on the image builder class path, but on the application class path, cannot use this
 * annotation. For example, the "driver" or our JVMTI agent implementations are part of the
 * application class path and therefore need to use explicit feature registration via a
 * native-image.properties file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Platforms(Platform.HOSTED_ONLY.class)
public @interface AutomaticallyRegisteredFeature {

}
