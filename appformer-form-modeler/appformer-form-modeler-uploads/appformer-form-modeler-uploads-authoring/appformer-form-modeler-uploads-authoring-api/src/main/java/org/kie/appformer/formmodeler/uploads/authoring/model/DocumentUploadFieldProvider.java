/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.uploads.authoring.model;

import javax.enterprise.context.Dependent;

import org.kie.appformer.formmodeler.uploads.shared.model.DocumentUpload;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.BasicTypeFieldProvider;
import org.kie.workbench.common.forms.model.FieldDataType;
import org.kie.workbench.common.forms.model.FieldType;

@Dependent
public class DocumentUploadFieldProvider extends BasicTypeFieldProvider<DocumentUploadFieldDefinition> {

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    protected void doRegisterFields() {
        registerPropertyType(DocumentUpload.class);
    }

    @Override
    public DocumentUploadFieldDefinition createFieldByType(FieldDataType typeInfo) {
        if (typeInfo.getType().equals(DocumentUpload.class.getName()) && !typeInfo.isList()) {
            return getDefaultField();
        }
        return null;
    }

    @Override
    public Class<? extends FieldType> getFieldType() {
        return DocumentUploadFieldType.class;
    }

    @Override
    public String getFieldTypeName() {
        return DocumentUploadFieldType.NAME;
    }

    @Override
    public DocumentUploadFieldDefinition getDefaultField() {
        DocumentUploadFieldDefinition field = new DocumentUploadFieldDefinition();
        field.setStandaloneClassName(DocumentUpload.class.getName());
        return field;
    }
}
