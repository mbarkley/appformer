/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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


package org.livespark.formmodeler.rendering.client.flow;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.CrudOperation;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;
import org.livespark.flow.client.local.CDIStepFactory;
import org.livespark.formmodeler.rendering.client.shared.FormModel;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.view.FormView;
import org.livespark.formmodeler.rendering.client.view.ListView;
import org.livespark.formmodeler.rendering.client.view.StandaloneFormWrapper;

public abstract class FlowProducer<MODEL,
                                   FORM_MODEL extends FormModel,
                                   FORM_VIEW extends FormView<MODEL, FORM_MODEL>,
                                   LIST_VIEW extends ListView<MODEL, FORM_MODEL>,
                                   REST_SERVICE extends LiveSparkRestService<MODEL>> {

    @Inject
    protected AppFlowFactory flowFactory;

    @Inject
    protected CDIStepFactory stepFactory;

    @Inject
    protected Caller<REST_SERVICE> restService;

    @Inject
    protected Event<IsElement> event;

    @Inject
    protected ManagedInstance<LIST_VIEW> listViewProvider;

    @Inject
    protected ManagedInstance<FORM_VIEW> formViewProvider;

    @Inject
    protected ManagedInstance<StandaloneFormWrapper<MODEL, FORM_MODEL, FORM_VIEW>> wrapperProvider;

    public abstract FORM_MODEL modelToFormModel( MODEL model );
    public abstract MODEL formModelToModel( FORM_MODEL formModel );
    public abstract MODEL newModel();

    public FORM_MODEL newFormModel() {
        return modelToFormModel( newModel() );
    }

    public Step<MODEL, MODEL> save() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( (final MODEL result) -> callback.accept( result ) )
                    .create( input );
            }

            @Override
            public String getName() {
                return "Save";
            }
        };
    }

    public Step<MODEL, MODEL> update() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( result -> callback.accept( input ) )
                    .update( input );
            }

            @Override
            public String getName() {
                return "Update";
            }
        };
    }

    public Step<MODEL, MODEL> delete() {
        return new Step<MODEL, MODEL>() {

            @Override
            public void execute( final MODEL input, final Consumer<MODEL> callback ) {
                restService
                    .call( result -> callback.accept( input ) )
                    .delete( input );
            }

            @Override
            public String getName() {
                return "Delete";
            }
        };
    }

    public Step<Unit, List<MODEL>> load() {
        return new Step<Unit, List<MODEL>>() {

            @Override
            public void execute( final Unit input,
                                 final Consumer<List<MODEL>> callback ) {
                restService
                    .call( (final List<MODEL> result) -> callback.accept( result ) )
                    .load();
            }

            @Override
            public String getName() {
                return "Load";
            }
        };
    }

    public Step<FORM_MODEL, Optional<FORM_MODEL>> formView() {
        return stepFactory
                .createCdiStep( () -> {
                                    final StandaloneFormWrapper<MODEL, FORM_MODEL, FORM_VIEW> wrapper = wrapperProvider.get();
                                    final FORM_VIEW form = formViewProvider.get();
                                    wrapper.setFormView( form );

                                    return wrapper;
                                },
                                view -> {
                                    view.init();
                                    event.fire( view );
                                },
                                view -> {
                                    view.getElement().getParentElement().removeChild( view.getElement() );
                                    wrapperProvider.destroy( view );
                                    formViewProvider.destroy( view.getFormView() );
                                },
                                "Form" );
    }

    public Step<List<MODEL>, Command<CrudOperation, MODEL>> listView( final boolean allowCreate, final boolean allowEdit, final boolean allowDelete ) {
        return stepFactory
                .createCdiStep( () -> listViewProvider.get(),
                                view -> {
                                    view.setAllowCreate( allowCreate );
                                    view.setAllowEdit( allowEdit );
                                    view.setAllowDelete( allowDelete );
                                    view.init();
                                    event.fire( view );
                                },
                                view -> {
                                    view.getElement().getParentElement().removeChild( view.getElement() );
                                    listViewProvider.destroy( view );
                                },
                                "List" );
    }

    public AppFlow<FORM_MODEL, Optional<FORM_MODEL>> createOrUpdate( final Supplier<Step<MODEL, MODEL>> persist ) {
        return flowFactory
                .buildFromStep( formView() )
                .transitionTo( (final Optional<FORM_MODEL> oModel) ->
                    oModel
                        .map( (final FORM_MODEL model) ->
                            flowFactory
                                .buildFromConstant( model )
                                .andThen( this::formModelToModel )
                                .andThen( persist.get() )
                                .andThen( this::modelToFormModel )
                                .andThen( Optional::of ) )
                        .orElseGet( () ->
                            flowFactory
                                .buildFromConstant( Optional.empty() ) ) );
    }

    public AppFlow<Unit, Optional<FORM_MODEL>> create() {
        return flowFactory
                .buildFromSupplier( this::newFormModel )
                .andThen( createOrUpdate( this::save ) );
    }

    public AppFlow<Unit, Unit> crud() {
        return flowFactory
                .buildFromStep( load() )
                .andThen( listView( true, true, true ) )
                .transitionTo( (final Command<CrudOperation, MODEL> command) -> {
                    switch ( command.commandType ) {
                        case CREATE :
                            return flowFactory
                                    .buildFromConstant( command.value )
                                    .andThen( this::modelToFormModel )
                                    .andThen( createOrUpdate( this::save ) )
                                    .transitionTo( (final Optional<FORM_MODEL> ignore) -> crud() );
                        case UPDATE :
                            return flowFactory
                                    .buildFromConstant( command.value )
                                    .andThen( this::modelToFormModel )
                                    .andThen( createOrUpdate( this::update ) )
                                    .transitionTo( (final Optional<FORM_MODEL> ignore) -> crud() );
                        case DELETE :
                            return flowFactory
                                    .buildFromConstant( command.value )
                                    .andThen( delete() )
                                    .transitionTo( (final MODEL ignore) -> crud() );
                        default :
                            throw new RuntimeException( "Unrecognized command type " + command.commandType );
                    }
                } );
        }

    public AppFlow<Unit, Unit> createAndReview() {
        return flowFactory
                .buildFromConstant( newFormModel() )
                .andThen( createOrUpdate( this::save ) )
                .transitionTo( oFormModel ->
                        oFormModel
                            .map( this::review )
                            .orElseGet( flowFactory::unitFlow ) );
    }

    public AppFlow<Unit, Unit> review( final FORM_MODEL formModel ) {
        return flowFactory
                .buildFromConstant( singletonList( formModelToModel( formModel ) ) )
                .andThen( listView( false, true, true ) )
                .transitionTo( (final Command<CrudOperation, MODEL> command) -> {
                    switch ( command.commandType ) {
                        case DELETE :
                            return flowFactory
                                    .buildFromConstant( command.value )
                                    .andThen( delete() )
                                    .toUnit();
                        case UPDATE :
                            return flowFactory
                                    .buildFromConstant( modelToFormModel( command.value ) )
                                    .andThen( createOrUpdate( this::update ) )
                                    .transitionTo( oUpdatedFormModel ->
                                            oUpdatedFormModel
                                                .map( this::review )
                                                .orElseGet( () -> flowFactory
                                                                    .unitFlow()
                                                                    .andThen( review( modelToFormModel( command.value ) ) ) )
                                            );
                        default :
                            throw new RuntimeException( "Unrecognized command type " + command.commandType );
                    }
                } );
    }

    public AppFlow<Unit, Unit> view() {
        return flowFactory
                .buildFromStep( load() )
                .andThen( listView( false, false, false ) )
                .toUnit();
    }

}
