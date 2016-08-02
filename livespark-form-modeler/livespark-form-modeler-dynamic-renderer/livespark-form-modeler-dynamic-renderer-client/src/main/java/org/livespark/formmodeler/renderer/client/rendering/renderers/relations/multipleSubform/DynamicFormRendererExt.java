/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.renderer.client.rendering.renderers.relations.multipleSubform;

import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.forms.dynamic.client.DynamicFormRenderer;
import org.kie.workbench.common.forms.dynamic.service.FormRenderingContextGeneratorService;
import org.kie.workbench.common.forms.processing.engine.handling.FormHandler;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;

// TODO: remove it when  fixed CRUD migration
@Specializes
public class DynamicFormRendererExt extends DynamicFormRenderer implements IsFormView {

    @Inject
    public DynamicFormRendererExt( DynamicFormRendererView view,
                                   Caller<FormRenderingContextGeneratorService> transformerService,
                                   FormHandler formHandler ) {
        super( view, transformerService, formHandler );
    }
}
