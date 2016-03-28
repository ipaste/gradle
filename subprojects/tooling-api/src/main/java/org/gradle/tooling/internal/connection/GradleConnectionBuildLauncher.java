/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.connection;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.model.BuildIdentifier;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.internal.consumer.CompositeConnectionParameters;
import org.gradle.tooling.internal.consumer.DefaultBuildLauncher;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.converters.FixedBuildIdentifierProvider;
import org.gradle.tooling.internal.gradle.ConsumerProvidedTask;
import org.gradle.tooling.model.Launchable;
import org.gradle.tooling.model.Task;

import java.io.File;

public class GradleConnectionBuildLauncher extends DefaultBuildLauncher implements BuildLauncher, CompositeBuildLauncherInternal {
    public GradleConnectionBuildLauncher(AsyncConsumerActionExecutor connection, CompositeConnectionParameters parameters) {
        super(connection, parameters);
    }

    @Override
    public void preprocessLaunchables(Iterable<? extends Launchable> launchables) {
        BuildIdentifier targetBuildIdentifier = null;
        for (Launchable launchable : launchables) {
            BuildIdentifier launchableBuildIdentifier = launchable.getGradleProjectIdentifier().getBuild();
            if (targetBuildIdentifier == null) {
                targetBuildIdentifier = launchableBuildIdentifier;
            } else if (!targetBuildIdentifier.equals(launchableBuildIdentifier)) {
                throw new IllegalArgumentException("All Launchables must originate from the same build.");
            }
        }
        operationParamsBuilder.setBuildIdentifier(targetBuildIdentifier);
    }

    @Override
    public Task targetTask(String task, File buildDirectory) {
        ConsumerProvidedTask taskObject = new ConsumerProvidedTask()
            .setName(task)
            .setPath(task)
            .setDescription("Task " + task)
            .setDisplayName("Task " + task);
        FixedBuildIdentifierProvider buildIdentifierProvider = new FixedBuildIdentifierProvider(new DefaultProjectIdentifier(new DefaultBuildIdentifier(buildDirectory), ":"));
        return new ProtocolToModelAdapter().adapt(Task.class, taskObject, buildIdentifierProvider);
    }
}