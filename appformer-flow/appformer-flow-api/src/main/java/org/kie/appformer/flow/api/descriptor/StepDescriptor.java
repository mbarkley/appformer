/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.kie.appformer.flow.api.descriptor;

import org.kie.appformer.flow.api.AppFlow;
import org.kie.appformer.flow.api.Step;
import org.kie.appformer.flow.api.descriptor.common.FlowPartDescriptor;
import org.kie.appformer.flow.api.descriptor.conversion.DescriptorRegistry;

/**
 * <p>
 * A descriptor for a {@link Step}, used to construct an {@link AppFlowDescriptor}.
 *
 * @see DescriptorFactory
 * @see DescriptorRegistry
 * @see AppFlowDescriptor
 * @see Step
 * @see AppFlow
 */
public interface StepDescriptor extends FlowPartDescriptor {

}
