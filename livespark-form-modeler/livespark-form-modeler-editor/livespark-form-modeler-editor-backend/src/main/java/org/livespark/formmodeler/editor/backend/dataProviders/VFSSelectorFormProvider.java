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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.model.Project;
import org.kie.workbench.common.services.datamodeller.util.FileUtils;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.template.FormDefinitionSerializer;
import org.livespark.formmodeler.editor.service.FormEditorRenderingContext;
import org.livespark.formmodeler.editor.type.FormResourceTypeDefinition;
import org.livespark.formmodeler.model.DataHolder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.FormDefinition;
import org.livespark.formmodeler.model.config.SelectorData;
import org.livespark.formmodeler.model.config.SystemSelectorDataProvider;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;

public class VFSSelectorFormProvider implements SystemSelectorDataProvider {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private FormDefinitionSerializer serializer;

    @Override
    public String getProviderName() {
        return getClass().getSimpleName();
    }

    @Override
    public SelectorData getSelectorData( FormRenderingContext context ) {
        Map<String, String> values = new TreeMap<>();

        if ( context.getModel() instanceof MultipleSubFormFieldDefinition ) {
            FormEditorRenderingContext editorContext = (FormEditorRenderingContext) context;

            Project project = projectService.resolveProject( editorContext.getFormPath() );

            FileUtils utils = FileUtils.getInstance();

            List<Path> nioPaths = new ArrayList<Path>();

            nioPaths.add( Paths.convert( project.getRootPath() ) );

            Collection<FileUtils.ScanResult> forms = utils.scan( ioService, nioPaths, FormResourceTypeDefinition.EXTENSION, true );

            for ( FileUtils.ScanResult form : forms ) {
                org.uberfire.java.nio.file.Path formPath = form.getFile();

                FormDefinition formDefinition = serializer.deserialize( ioService.readAllString( formPath ).trim() );

                FieldDefinition field = (FieldDefinition) context.getModel();

                if ( formDefinition.getDataHolders() == null ||
                        formDefinition.getDataHolders().size() != 1 ) {
                    continue;
                }

                if ( field != null ) {

                    DataHolder holder = formDefinition.getDataHolders().get( 0 );

                    if ( !field.getStandaloneClassName().isEmpty() && !holder.getType().equals( field.getStandaloneClassName() ) ) {
                        continue;
                    }
                }
                values.put( formDefinition.getId(), formDefinition.getName() );
            }
        }
        return  new SelectorData( values, null );
    }
}
