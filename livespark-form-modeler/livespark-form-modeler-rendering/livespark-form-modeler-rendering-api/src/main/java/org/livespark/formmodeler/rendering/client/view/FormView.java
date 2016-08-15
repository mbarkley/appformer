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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.TemplateWidgetMapper;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.kie.workbench.common.forms.crud.client.component.formDisplay.IsFormView;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.view.validation.FormViewValidator;

import com.google.gwt.user.client.ui.Widget;

public abstract class FormView<MODEL, FORM_MODEL extends FormModel> implements IsFormView<FORM_MODEL>, IsElement {

    private boolean newModel = true;

    @Inject
    protected FormViewValidator validator;

    @Inject
    @AutoBound
    protected DataBinder<FORM_MODEL> binder;

    @Override
    public void setModel( final FORM_MODEL model ) {
        binder.setModel( model );
        newModel = false;
        validator.clearFieldErrors();
        updateNestedModels( true );
    }

    public void pauseBinding() {
        binder.pause();
    }

    public void resumeBinding( final boolean keepChanges ) {
        binder.resume( keepChanges ? StateSync.FROM_UI : StateSync.FROM_MODEL );
    }

    public boolean isNewModel() {
        return newModel;
    }

    @PostConstruct
    private void init() {
        final List<MODEL> entites = getEntities();
        if (entites == null || entites.isEmpty() || entites.size() < getEntitiesCount()) {
            initEntities();
        }
        initForm();
        beforeDisplay();
    }

    protected void updateNestedModels( final boolean init ) {
    }

    protected abstract void initForm();

    public abstract void setReadOnly( boolean readOnly );

    protected abstract int getEntitiesCount();

    protected abstract List<MODEL> getEntities();

    protected abstract void initEntities();

    public abstract boolean doExtraValidations();

    public boolean validate() {

        final boolean isValid = validator.validate( binder.getModel() );

        final boolean extraValidations = doExtraValidations();

        return isValid && extraValidations;
    }

    public abstract void beforeDisplay();

    @Override
    public FORM_MODEL getModel() {
        return binder.getModel();
    }

    @Override
    public boolean isValid() {
        return validate();
    }

    @Override
    public Widget asWidget() {
        return TemplateWidgetMapper.get( this );
    }
}
