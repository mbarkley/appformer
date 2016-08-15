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

package org.livespark.formmodeler.rendering.client.view;

import static org.livespark.flow.api.CrudOperation.CREATE;
import static org.livespark.flow.api.CrudOperation.DELETE;
import static org.livespark.flow.api.CrudOperation.UPDATE;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.kie.workbench.common.forms.crud.client.component.CrudActionsHelper;
import org.kie.workbench.common.forms.crud.client.component.CrudComponent;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.CrudOperation;
import org.livespark.flow.cdi.api.FlowInput;
import org.livespark.flow.cdi.api.FlowOutput;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public abstract class ListView<M, F extends FormModel> implements IsElement {

    @Inject
    protected SyncBeanManager beanManager;

    @DataField
    protected FlowPanel content = createFlowPanel();

    @Inject
    protected CrudComponent<M, F> crudComponent;

    @Inject
    private FlowInput<List<M>> input;

    @Inject
    private FlowOutput<Command<CrudOperation, M>> output;

    protected List<M> crudItems;

    AsyncDataProvider<M> dataProvider;

    protected CrudActionsHelper<M> crudActionsHelper = new ListViewCrudActionsHelper();

    private boolean allowCreate = true;
    private boolean allowEdit = true;
    private boolean allowDelete = true;

    public void setAllowCreate( final boolean allowCreate ) {
        this.allowCreate = allowCreate;
    }

    public void setAllowEdit( final boolean allowEdit ) {
        this.allowEdit = allowEdit;
    }

    public void setAllowDelete( final boolean allowDelete ) {
        this.allowDelete = allowDelete;
    }

    public void init() {
        crudItems = input.get();
        dataProvider = new AsyncDataProvider<M>() {
            @Override
            protected void onRangeChanged( final HasData<M> hasData ) {
                if ( crudItems != null ) {
                    updateRowCount( crudItems.size(), true );
                    updateRowData( 0, crudItems );
                } else {
                    updateRowCount( 0, true );
                    updateRowData( 0, new ArrayList<M>() );
                }
            }
        };

        crudComponent.init( crudActionsHelper );
        crudComponent.setEmbedded( false );

        content.add( crudComponent );
        loadItems( crudItems );
    }

    public void addModel( final M model ) {
        crudItems.add( model );
        crudComponent.refresh();
    }

    public void removeModel( final M model ) {
        crudItems.remove( model );
        crudComponent.refresh();
    }

    public void refresh() {
        crudComponent.refresh();
    }

    /*
     * Is overridable for testing.
     */
    protected FlowPanel createFlowPanel() {
        return new FlowPanel();
    }

    public void loadItems(final List<M> itemsToLoad) {
        this.crudItems = itemsToLoad;
        initCrud();
    }

    protected void initCrud() {
        crudComponent.refresh();
    }

    public abstract String getListTitle();

    public abstract String getFormTitle();

    protected abstract String getFormId();

    public abstract List<ColumnMeta<M>> getCrudColumns();

    public abstract M getModel( F formModel );

    public abstract F createFormModel( M model );

    public abstract M newModel();

    protected class ListViewCrudActionsHelper implements CrudActionsHelper<M> {
        @Override
        public boolean showEmbeddedForms() {
            return false;
        }

        @Override
        public int getPageSize() {
            return 10;
        }

        @Override
        public boolean isAllowCreate() {
            return allowCreate;
        }

        @Override
        public boolean isAllowEdit() {
            return allowEdit;
        }

        @Override
        public boolean isAllowDelete() {
            return allowDelete;
        }

        @Override
        public List<ColumnMeta<M>> getGridColumns() {
            return getCrudColumns();
        }

        @Override
        public AsyncDataProvider<M> getDataProvider() {
            return dataProvider;
        }

        @Override
        public void deleteInstance( final int index ) {
            output.submit( new Command<>( DELETE, crudItems.get( index ) ) );
        }

        @Override
        public void createInstance() {
            output.submit( new Command<>( CREATE, newModel() ) );
        }

        @Override
        public void editInstance( final int index ) {
            output.submit( new Command<>( UPDATE, crudItems.get( index ) ) );
        }
    };
}
