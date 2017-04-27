/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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


package org.kie.appformer.flowset.backend;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;

import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorDataProvider;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;

@Dependent
public class RootStepProvider implements SelectorDataProvider {

    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public SelectorData getSelectorData( final FormRenderingContext context ) {
        final Map<String, String> components = new LinkedHashMap<>();

        Stream.of( "New",
                   "Edit",
                   "View Table"
                   ).forEach( name -> components.put( name, name ) );

        return new SelectorData<>( components, null );
    }

}
