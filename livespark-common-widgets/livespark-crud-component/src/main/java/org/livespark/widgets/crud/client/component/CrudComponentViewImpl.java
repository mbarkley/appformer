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

package org.livespark.widgets.crud.client.component;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.resources.i18n.CrudConstants;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.tables.ColumnMeta;
import org.uberfire.ext.widgets.common.client.tables.PagedTable;
import org.uberfire.mvp.Command;

@Dependent
@Templated
public class CrudComponentViewImpl extends Composite implements CrudComponent.CrudComponentView {

    private CrudComponent presenter;

    private CrudActionsHelper helper;

    private AsyncDataProvider<Object> dataProvider;

    private PagedTable<Object> table;

    protected FormDisplayer displayer;

    @DataField
    protected FlowPanel content = new FlowPanel();

    @Override
    public void setPresenter( CrudComponent presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void init( final CrudActionsHelper helper ) {
        this.helper = helper;

        content.clear();

        table = new PagedTable<>( helper.getPageSize() );
        table.setcolumnPickerButtonVisibe( false );

        if ( helper.isAllowCreate() ) {
            Button createButton = new Button( CrudConstants.INSTANCE.newInstanceButton() );
            createButton.setType( ButtonType.PRIMARY );
            createButton.setIcon( IconType.PLUS );
            table.getLeftToolbar().add( createButton );
            createButton.addClickHandler( new ClickHandler() {
                @Override
                public void onClick( ClickEvent clickEvent ) {
                    renderNestedForm( CrudConstants.INSTANCE.newInstanceTitle(), helper.getCreateInstanceForm(), new FormDisplayer.FormDisplayerCallback() {
                        @Override
                        public void onAccept() {
                            doCreate();
                        }

                        @Override
                        public void onCancel() {
                            doCancel();
                        }
                    } );
                }
            } );
        }

        List<ColumnMeta<Object>> columns = new ArrayList<>( helper.getGridColumns() );

        if ( helper.isAllowEdit() ) {
            Column<Object, String> column = new Column<Object, String>( new ButtonCell( IconType.EDIT, ButtonType.PRIMARY, ButtonSize.SMALL ) ) {
                @Override
                public String getValue( Object model ) {
                    return CrudConstants.INSTANCE.editInstanceButton();
                }
            };
            column.setFieldUpdater( new FieldUpdater<Object, String>() {
                @Override
                public void update( int i, Object model, String s ) {
                    renderNestedForm( CrudConstants.INSTANCE.editInstanceTitle(), helper.getEditInstanceForm( i ), new FormDisplayer.FormDisplayerCallback() {
                        @Override
                        public void onAccept() {
                            doEdit();
                        }

                        @Override
                        public void onCancel() {
                            doCancel();
                        }
                    } );

                }
            } );
            columns.add( new ColumnMeta( column, "" ) );
        }

        if ( helper.isAllowDelete() ) {
            Column<Object, String> column = new Column<Object, String>( new ButtonCell( IconType.TRASH, ButtonType.DANGER, ButtonSize.SMALL ) ) {
                @Override
                public String getValue( Object model ) {
                    return CrudConstants.INSTANCE.deleteInstance();
                }
            };
            column.setFieldUpdater( new FieldUpdater<Object, String>() {
                @Override
                public void update( final int i, Object model, String s ) {
                    YesNoCancelPopup.newYesNoCancelPopup( CrudConstants.INSTANCE.deleteTitle(), CrudConstants.INSTANCE.deleteBody(),
                            new Command() {
                                @Override
                                public void execute() {
                                    helper.deleteInstance( i );
                                }
                            }, new Command() {
                                @Override
                                public void execute() {

                                }
                            }, null ).show();
                }
            } );
            columns.add( new ColumnMeta( column, "" ) );
        }
        table.addColumns( columns );

        dataProvider = helper.getDataProvider();

        table.setDataProvider( helper.getDataProvider() );

        refresh();

        content.add( table );
    }

    @Override
    public int getCurrentPage() {
        return table.getPageStart();
    }

    protected void renderNestedForm( String title, IsFormView formView, FormDisplayer.FormDisplayerCallback callback ) {
        displayer = presenter.getFormDisplayer();

        content.clear();
        content.add( displayer );

        displayer.display( title, formView, callback );
    }

    protected void doCreate() {
        restoreTable();
        helper.createInstance();
    }

    protected void doEdit() {
        restoreTable();
        helper.editInstance();
    }

    protected void doCancel() {
        restoreTable();
    }

    protected void restoreTable() {
        content.remove( displayer );
        content.add( table );
    }

    @Override
    public void setDataProvider( AsyncDataProvider provider ) {
        if ( dataProvider == null ) {
            if ( dataProvider != null && dataProvider.getDataDisplays().contains( table ) ) {
                dataProvider.removeDataDisplay( table );
            }
            this.dataProvider = provider;
            table.setDataProvider( dataProvider );
        }
        refresh();
    }

    @Override
    public void refresh() {
        HasData next = dataProvider.getDataDisplays().iterator().next();
        next.setVisibleRangeAndClearData( next.getVisibleRange(), true );
    }
}
