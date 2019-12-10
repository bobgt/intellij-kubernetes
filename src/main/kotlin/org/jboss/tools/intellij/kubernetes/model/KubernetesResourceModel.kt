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
import io.fabric8.kubernetes.api.model.Namespace
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import java.util.function.Consumer

object KubernetesResourceModel {

    private val watch = KubernetesResourceWatch(
        ResourceAdded(),
        ResourceRemoved())

    private var cluster = createCluster()
    private val observable = ResourceChangedObservableImpl()

    private fun createCluster(): Cluster {
        val cluster = Cluster()
        watch.start(cluster.client)
        return cluster;
    }

    fun addListener(listener: ResourceChangedObservableImpl.ResourceChangeListener) {
        observable.addListener(listener);
    }

    fun getClient(): NamespacedKubernetesClient {
        return cluster.client
    }

    fun getAllNamespaces(): List<Namespace> {
        return cluster.getAllNamespaces()
    }

    fun getResource(namespace: String, kind: Class<out HasMetadata>): Collection<HasMetadata> {
        return cluster.getNamespaceProvider(namespace)?.getResources(kind) ?: emptyList()
    }

    fun refresh(resource: Any?) {
        when(resource) {
            is NamespacedKubernetesClient -> refresh()
            is Namespace -> refresh(resource)
            is HasMetadata -> refresh(resource)
        }
    }

    private fun refresh() {
        cluster.client.close()
        cluster = createCluster()
        observable.fireModified(listOf(cluster.client))
    }

    private fun refresh(resource: Namespace) {
        val provider = cluster.getNamespaceProvider(resource)
        if (provider != null) {
            provider.clear()
            observable.fireModified(listOf(resource))
        }
    }

    fun add(resource: HasMetadata) {
        when(resource) {
            is Namespace -> addNamespace(resource)
            else -> addNamespaceChild(resource)
        }
    }

    private fun addNamespace(namespace: Namespace) {
        if (cluster.add(namespace)) {
            observable.fireModified(listOf(cluster.client))
        }
    }

    private fun addNamespaceChild(resource: HasMetadata) {
        val provider = cluster.getNamespaceProvider(resource)
        if (provider != null
            && provider.add(resource)) {
            observable.fireModified(listOf(provider.namespace))
        }
    }

    fun remove(resource: HasMetadata) {
        when(resource) {
            is Namespace -> removeNamespace(resource)
            else -> removeNamespaceChild(resource)
        }
    }

    private fun removeNamespace(namespace: Namespace) {
        if (cluster.remove(namespace)) {
            observable.fireModified(listOf(cluster.client))
        }
    }

    private fun removeNamespaceChild(resource: HasMetadata) {
        val provider = cluster.getNamespaceProvider(resource)
        if (provider != null
            && provider.remove(resource)) {
            observable.fireModified(listOf(provider.namespace))
        }
    }

    class ResourceAdded: Consumer<HasMetadata> {
        override fun accept(resource: HasMetadata) {
            add(resource)
        }
    }

    class ResourceRemoved: Consumer<HasMetadata> {
        override fun accept(resource: HasMetadata) {
            remove(resource)
        }
    }

}