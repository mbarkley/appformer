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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import org.livespark.widgets.crud.client.component.formDisplay.FormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.embedded.EmbeddedFormDisplayer;
import org.livespark.widgets.crud.client.component.formDisplay.modal.ModalFormDisplayer;

@Dependent
public class CrudComponent implements IsWidget{

    public interface CrudComponentView extends IsWidget{
        void setPresenter( CrudComponent presenter );

        void init( CrudActionsHelper helper );

        void setDataProvider( AsyncDataProvider provider );

        void refresh();

        int getCurrentPage();
    }

    private CrudComponentView view;

    private EmbeddedFormDisplayer embeddedFormDisplayer;

    private ModalFormDisplayer modalFormDisplayer;

    protected boolean embedded = true;

    @Inject
    public CrudComponent( CrudComponentView view,
                          EmbeddedFormDisplayer embeddedFormDisplayer,
                          ModalFormDisplayer modalFormDisplayer ) {
        this.view = view;
        this.embeddedFormDisplayer = embeddedFormDisplayer;
        this.modalFormDisplayer = modalFormDisplayer;
        view.setPresenter( this );
    }

    public void setDataProvider( AsyncDataProvider provider ) {
        if ( provider == null ) {
            throw new IllegalArgumentException( "Null provider" );
        }

        view.setDataProvider( provider );
    }

    public FormDisplayer getFormDisplayer() {
        if ( isEmbedded() ) {
            return embeddedFormDisplayer;
        }
        return modalFormDisplayer;
    }

    public int getCurrentPage() {
        return view.getCurrentPage();
    }

    public void init( final CrudActionsHelper helper ) {
        view.init( helper );
    }

    public void refresh() {
        view.refresh();
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded( boolean embedded ) {
        this.embedded = embedded;
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }
}
