/*
 * Copyright 2015 JBoss Inc
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

package org.livespark.formmodeler.codegen.services.datamodeller.impl;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.text.WordUtils;
import org.kie.workbench.common.forms.editor.backend.service.util.DataModellerFieldGenerator;
import org.kie.workbench.common.forms.editor.service.VFSFormFinderService;
import org.kie.workbench.common.forms.model.DataHolder;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.model.MultipleField;
import org.kie.workbench.common.forms.model.impl.relations.EmbeddedFormField;
import org.kie.workbench.common.forms.model.impl.relations.EntityRelationField;
import org.kie.workbench.common.forms.model.impl.relations.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.model.impl.relations.SubFormFieldDefinition;
import org.kie.workbench.common.forms.model.impl.relations.TableColumnMeta;
import org.kie.workbench.common.forms.service.FieldManager;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.livespark.formmodeler.codegen.FormSourcesGenerator;
import org.livespark.formmodeler.codegen.services.datamodeller.DataModellerFormGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

public class DataModellerFormGeneratorImpl implements DataModellerFormGenerator {
    private static transient Logger log = LoggerFactory.getLogger( DataModellerFormGeneratorImpl.class );

    @Inject
    protected DataModelerService dataModelerService;

    @Inject
    protected KieProjectService projectService;

    @Inject
    protected FieldManager fieldManager;

    @Inject
    protected FormSourcesGenerator formSourcesGenerator;

    @Inject
    protected DataModellerFieldGenerator fieldGenerator;

    @Inject
    protected VFSFormFinderService vfsFormFinderService;

    @Override
    public void generateFormForDataObject( DataObject dataObject, Path path ) {

        if (dataObject.getProperties().isEmpty()) return;

        FormDefinition form = new FormDefinition();

        form.setId( dataObject.getClassName() );

        form.setName( dataObject.getName() );

        String holderName = WordUtils.uncapitalize( dataObject.getName() );

        DataHolder holder = new DataHolder( holderName, dataObject.getClassName() );

        form.addDataHolder( holder );

        List<FieldDefinition> availabeFields = fieldGenerator.getFieldsFromDataObject(holderName, dataObject);

        for (FieldDefinition field : availabeFields ) {
            if (field instanceof EmbeddedFormField) {
                if ( !loadEmbeddedFormConfig( field, path ) ) continue;
            }
            form.getFields().add( field );
        }

        if (form.getFields().isEmpty()) return;

        formSourcesGenerator.generateEntityFormSources(form, path);

    }

    protected boolean loadEmbeddedFormConfig ( FieldDefinition field, Path path ) {
        if ( !(field instanceof EmbeddedFormField) ) return false;

        List<FormDefinition> subForms = vfsFormFinderService.findFormsForType( field.getStandaloneClassName(), path );

        if ( subForms == null || subForms.isEmpty() ) {
            return false;
        }

        if ( field instanceof  MultipleField ) {
            MultipleSubFormFieldDefinition multipleSubFormFieldDefinition = (MultipleSubFormFieldDefinition) field;
            FormDefinition form = subForms.get( 0 );
            multipleSubFormFieldDefinition.setCreationForm( form.getId() );
            multipleSubFormFieldDefinition.setEditionForm( form.getId() );

            List<TableColumnMeta> columnMetas = new ArrayList<>();
            for ( FieldDefinition nestedField : form.getFields() ) {
                if ( nestedField instanceof EntityRelationField ) {
                    continue;
                }
                TableColumnMeta meta = new TableColumnMeta( nestedField.getLabel(), nestedField.getBoundPropertyName() );
                columnMetas.add( meta );
            }

            multipleSubFormFieldDefinition.setColumnMetas( columnMetas );
        } else {
            SubFormFieldDefinition subFormField = (SubFormFieldDefinition) field;
            subFormField.setNestedForm( subForms.get( 0 ).getId() );
        }

        return true;
    }
}
