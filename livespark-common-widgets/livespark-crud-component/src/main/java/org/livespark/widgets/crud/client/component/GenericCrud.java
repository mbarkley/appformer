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

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ioc.client.container.IOC;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.tables.ColumnMeta;
import org.uberfire.ext.widgets.common.client.tables.PagedTable;
import org.livespark.widgets.crud.client.resources.i18n.CrudConstants;
import org.uberfire.mvp.Command;

public class GenericCrud extends Composite implements CrudUpdater {
    interface GenericCrudBinder
            extends
            UiBinder<Widget, GenericCrud> {

    }

    private static GenericCrudBinder uiBinder = GWT.create( GenericCrudBinder.class );

    private EmbeddedFormDisplayer embeddedDisplayer;

    private ModalFormDisplayer modalDisplayer;

    @UiField
    FlowPanel content;

    private AsyncDataProvider<HasProperties> dataProvider;

    private PagedTable<HasProperties> table = new PagedTable<>( 5 );

    private boolean allowCreate = true;
    private boolean allowEdit = true;
    private boolean allowDelete = true;

    private boolean embedded = true;

    private CrudHelper helper;

    public GenericCrud( int pageSize ) {
        this( pageSize, true, true, true );
    }

    public GenericCrud( int pageSize, boolean allowCreate, boolean allowEdit, boolean allowDelete ) {
        initWidget( uiBinder.createAndBindUi( this ) );

        this.allowCreate = allowCreate;
        this.allowEdit = allowEdit;
        this.allowDelete = allowDelete;

        initGrid( pageSize );

        embeddedDisplayer = IOC.getBeanManager().lookupBean( EmbeddedFormDisplayer.class ).newInstance();

        modalDisplayer = IOC.getBeanManager().lookupBean( ModalFormDisplayer.class ).newInstance();


        content.add( table );
    }

    public void config( final CrudHelper helper ) {
        if ( helper != null ) {
            if ( this.helper != null ) {
                initGrid( table.getPageSize() );
            }
            this.helper = helper;

            List<ColumnMeta<HasProperties>> columns = new ArrayList<>( helper.getGridColumns() );

            if ( allowEdit ) {
                Column<HasProperties, String> column = new Column<HasProperties, String>( new ButtonCell( IconType.EDIT, ButtonType.PRIMARY, ButtonSize.SMALL ) ) {
                    @Override
                    public String getValue( HasProperties properties ) {
                        return CrudConstants.INSTANCE.editInstanceButton();
                    }
                };
                column.setFieldUpdater( new FieldUpdater<HasProperties, String>() {
                    @Override
                    public void update( int i, HasProperties hasProperties, String s ) {
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

            if ( allowDelete ) {
                Column<HasProperties, String> column = new Column<HasProperties, String>( new ButtonCell( IconType.TRASH, ButtonType.DANGER, ButtonSize.SMALL ) ) {
                    @Override
                    public String getValue( HasProperties properties ) {
                        return CrudConstants.INSTANCE.deleteInstance();
                    }
                };
                column.setFieldUpdater( new FieldUpdater<HasProperties, String>() {
                    @Override
                    public void update( final int i, HasProperties hasProperties, String s ) {
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
        }
    }

    protected void initGrid( int pageSize ) {
        table = new PagedTable<HasProperties>( pageSize );
        table.setcolumnPickerButtonVisibe( false );
        drawHeader();
    }

    protected void drawHeader() {
        if ( allowCreate ) {
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
    }

    protected void renderNestedForm( String title, IsFormView formView, FormDisplayer.FormDisplayerCallback callback ) {
        FormDisplayer displayer;

        if ( embedded ) {
            displayer = embeddedDisplayer;
            content.clear();
            content.add( embeddedDisplayer );
        } else {
            displayer = modalDisplayer;
        }
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
        if ( embedded ) {
            content.remove( embeddedDisplayer );
            content.add( table );
        }
    }

    public boolean isAllowCreate() {
        return allowCreate;
    }

    public void setAllowCreate( boolean allowCreate ) {
        this.allowCreate = allowCreate;
    }

    public boolean isAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit( boolean allowEdit ) {
        this.allowEdit = allowEdit;
    }

    public boolean isAllowDelete() {
        return allowDelete;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded( boolean embedded ) {
        this.embedded = embedded;
    }

    public void setAllowDelete( boolean allowDelete ) {
        this.allowDelete = allowDelete;
    }

    @Override
    public void updateCrudContent( AsyncDataProvider<HasProperties> provider ) {
        if ( provider == null ) {
            return;
        }
        this.dataProvider = provider;
        table.setDataProvider( dataProvider );
        table.redraw();
    }
}
