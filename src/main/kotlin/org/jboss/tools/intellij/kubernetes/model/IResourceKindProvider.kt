/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.kubernetes.model

import io.fabric8.kubernetes.api.model.HasMetadata

interface IResourceKindProvider<T: HasMetadata> {
    val kind: Class<out T>
    fun getAllResources(): Collection<T>
    fun hasResource(resource: HasMetadata): Boolean
    fun invalidate()
    fun add(resource: HasMetadata): Boolean
    fun remove(resource: HasMetadata): Boolean
}