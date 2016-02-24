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
package org.livespark.formmodeler.rendering.client.shared.fields;

import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.client.widget.HasModel;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.widgets.crud.client.component.CrudHelper;
import org.livespark.widgets.crud.client.component.GenericCrud;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.uberfire.ext.widgets.common.client.tables.ColumnMeta;

/**
 * Created by pefernan on 6/18/15.
 */

public class MultipleSubForm<L extends List<M>, M, F extends FormModel> extends SimplePanel implements HasModel<L> {

    private GenericCrud crudComponent;

    private MultipleSubFormModelAdapter<L, F> multipleSubFormModelAdapter;

    private FormView<F> currentForm;

    private L model;

    public MultipleSubForm( MultipleSubFormModelAdapter<L, F> adapter ) {
        super();
        if (adapter == null) throw new IllegalArgumentException( "FormModelProvider cannot be null" );

        crudComponent = new GenericCrud( 5 );

        add( crudComponent );

        multipleSubFormModelAdapter = adapter;
    }

    @Override
    public L getModel() {
        return model;
    }

    @Override
    public void setModel( L model ) {
        this.model = model;
        initView();
    }

    protected void initView() {
        currentForm = null;
        crudComponent.config( new CrudHelper() {
            @Override
            public List<ColumnMeta<HasProperties>> getGridColumns() {
                return multipleSubFormModelAdapter.getCrudColumns();
            }

            @Override
            public IsFormView getCreateInstanceForm() {
                currentForm = multipleSubFormModelAdapter.getForm();
                return currentForm;
            }

            @Override
            public void createInstance() {
                model.add( (M) currentForm.getModel().getDataModels().get( 0 ) );
                initView();
            }

            @Override
            public IsFormView getEditInstanceForm( Integer index ) {
                currentForm = multipleSubFormModelAdapter.getForm();

                M formModel = model.get( index );

                currentForm.setModel( multipleSubFormModelAdapter.getEditionFormModel( formModel ) );

                return currentForm;
            }

            @Override
            public void editInstance() {
                currentForm = multipleSubFormModelAdapter.getForm();

                model.add( (M)currentForm.getModel().getDataModels().get( 0 ) );
                initView();
            }

            @Override
            public void deleteInstance( int index ) {

            }
        } );
    }

    public HandlerRegistration addValueChangeHandler( ValueChangeHandler<L> valueChangeHandler ) {
        return this.addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}
