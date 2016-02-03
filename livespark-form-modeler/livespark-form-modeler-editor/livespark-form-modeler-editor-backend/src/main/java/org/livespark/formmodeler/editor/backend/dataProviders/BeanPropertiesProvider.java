/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.editor.backend.dataProviders;

import java.util.HashMap;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataModel;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.editor.service.FormEditorRenderingContext;
import org.livespark.formmodeler.model.config.SelectorData;
import org.livespark.formmodeler.model.config.SystemSelectorDataProvider;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.TableColumnMeta;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;

@Dependent
public class BeanPropertiesProvider implements SystemSelectorDataProvider {

    @Inject
    private KieProjectService projectService;

    @Inject
    private DataModelerService dataModelerService;
    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public SelectorData getSelectorData( FormRenderingContext context ) {

        HashMap<String, String> values = new HashMap<>();

        if ( context instanceof FormEditorRenderingContext && context.getParentContext() != null ) {
            if ( context.getParentContext().getModel() instanceof MultipleSubFormFieldDefinition ) {
                FormEditorRenderingContext editorContext = (FormEditorRenderingContext) context;

                MultipleSubFormFieldDefinition subForm = (MultipleSubFormFieldDefinition) context.getParentContext().getModel();

                DataModel dataModel = dataModelerService.loadModel( projectService.resolveProject( editorContext.getFormPath() ) );

                DataObject dataObject = dataModel.getDataObject( subForm.getStandaloneClassName() );

                TableColumnMeta model = (TableColumnMeta) context.getModel();

                for ( ObjectProperty property : dataObject.getProperties() ) {
                    boolean add = true;

                    for ( int i = 0; i < subForm.getColumnMetas().size() && add == true; i++ ) {
                        TableColumnMeta meta = subForm.getColumnMetas().get( i );
                        if ( model != null && model.getProperty().equals( property.getName() ) ) {
                            break;
                        }
                        if ( meta.getProperty().equals( property.getName() ) ) {
                            add = false;
                        }
                    }

                    if ( add ) {
                        values.put( property.getName(), property.getName() );
                    }
                }}
        }
        return new SelectorData( values, null );
    }
}
