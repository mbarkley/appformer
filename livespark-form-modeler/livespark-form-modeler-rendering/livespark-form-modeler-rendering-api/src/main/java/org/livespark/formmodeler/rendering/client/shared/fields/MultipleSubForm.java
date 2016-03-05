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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.ioc.client.container.IOC;
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

public class MultipleSubForm<L extends List<M>, M, C extends FormModel, E extends FormModel> extends SimplePanel implements HasModel<L> {

    private GenericCrud crudComponent;

    private MultipleSubFormModelAdapter<L, M, C, E> multipleSubFormModelAdapter;

    private L model;

    public MultipleSubForm( MultipleSubFormModelAdapter<L, M, C, E> adapter ) {
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

    protected void initCrudComponent() {
        AsyncDataProvider<M> dataProvider = new AsyncDataProvider<M>() {
            @Override
            protected void onRangeChanged( HasData<M> hasData ) {
                if ( model != null ) {
                    updateRowCount( model.size(), true );
                    updateRowData( 0, model );
                } else {
                    updateRowCount( 0, true );
                    updateRowData( 0, new ArrayList<M>() );
                }
            }
        };

        crudComponent.setDataProvider( dataProvider );
    }

    protected void initView() {
        crudComponent.config( new CrudHelper() {

            private FormView<C> creationForm;
            private FormView<E> editionForm;

            private int editionIndex;


            @Override
            public List<ColumnMeta> getGridColumns() {
                return multipleSubFormModelAdapter.getCrudColumns();
            }

            @Override
            public IsFormView getCreateInstanceForm() {
                creationForm = IOC.getBeanManager().lookupBean( multipleSubFormModelAdapter.getCreationForm() ).newInstance();
                return creationForm;
            }

            @Override
            public void createInstance() {
                model.add( (M) creationForm.getModel().getDataModels().get( 0 ) );
                editionForm = null;
                crudComponent.refresh();
            }

            @Override
            public IsFormView getEditInstanceForm( Integer index ) {
                editionIndex = index;
                editionForm = IOC.getBeanManager().lookupBean( multipleSubFormModelAdapter.getEditionForm() ).newInstance();

                M model = MultipleSubForm.this.model.get( index );

                editionForm.setModel( multipleSubFormModelAdapter.getEditionFormModel( model ) );

                return editionForm;
            }

            @Override
            public void editInstance() {
                crudComponent.refresh();
                editionForm = null;
            }

            @Override
            public void deleteInstance( int index ) {
                model.remove( index );
                crudComponent.refresh();
            }
        } );
        initCrudComponent();
    }

    public HandlerRegistration addValueChangeHandler( ValueChangeHandler<L> valueChangeHandler ) {
        return this.addHandler(valueChangeHandler, ValueChangeEvent.getType());
    }
}
