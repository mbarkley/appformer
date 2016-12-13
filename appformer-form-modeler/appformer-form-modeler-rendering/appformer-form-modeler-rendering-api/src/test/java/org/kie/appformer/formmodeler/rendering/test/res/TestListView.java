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
package org.kie.appformer.formmodeler.rendering.test.res;

import java.util.ArrayList;
import java.util.List;

import org.kie.appformer.formmodeler.rendering.client.view.FormView;
import org.kie.appformer.formmodeler.rendering.client.view.ListView;
import org.kie.workbench.common.forms.crud.client.component.CrudActionsHelper;
import org.kie.workbench.common.forms.crud.client.component.mock.CrudModel;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import com.google.gwt.user.cellview.client.TextColumn;

public class TestListView extends ListView<CrudModel, TestFormModel> {

    @Override
    public List<ColumnMeta<CrudModel>> getCrudColumns() {

        final List<ColumnMeta<CrudModel>> metas = new ArrayList<>();

        ColumnMeta<CrudModel> columnMeta = new ColumnMeta<>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
                if ( model.getName() == null ) {
                    return "";
                }
                return String.valueOf( model.getName() );
            }
        }, "Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
                if ( model.getLastName() == null ) {
                    return "";
                }
                return String.valueOf( model.getLastName() );
            }
        }, "Last Name" );

        metas.add( columnMeta );

        columnMeta = new ColumnMeta<>( new TextColumn<CrudModel>() {
            @Override
            public String getValue( final CrudModel model ) {
                if ( model.getBirthday() == null ) {
                    return "";
                }
                return String.valueOf( model.getBirthday() );
            }
        }, "Birthday" );

        metas.add( columnMeta );

        return metas;
    }

    @Override
    public TestFormModel createFormModel( final CrudModel model ) {
        return new TestFormModel( model );
    }

    @Override
    public String getFormTitle() {
        return "Test Form";
    }

    @Override
    protected String getFormId() {
        return "TestFormId";
    }

    @Override
    public String getListTitle() {
        return "List Title";
    }

    public CrudActionsHelper<CrudModel> getCrudActionsHelper() {
        return crudActionsHelper;
    }

    @Override
    public CrudModel newModel() {
        return new CrudModel();
    }
}
