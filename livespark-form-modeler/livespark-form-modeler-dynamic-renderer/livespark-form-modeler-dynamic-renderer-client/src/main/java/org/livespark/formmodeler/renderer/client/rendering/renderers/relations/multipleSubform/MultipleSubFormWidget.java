package org.livespark.formmodeler.renderer.client.rendering.renderers.relations.multipleSubform;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.livespark.formmodeler.model.FieldDefinition;
import org.livespark.formmodeler.model.impl.relations.MultipleSubFormFieldDefinition;
import org.livespark.formmodeler.model.impl.relations.TableColumnMeta;
import org.livespark.formmodeler.renderer.client.DynamicFormRenderer;
import org.livespark.formmodeler.renderer.client.rendering.renderers.relations.multipleSubform.columns.ColumnGenerator;
import org.livespark.formmodeler.renderer.service.FormRenderingContext;
import org.livespark.widgets.crud.client.component.CrudHelper;
import org.livespark.widgets.crud.client.component.GenericCrud;
import org.livespark.widgets.crud.client.component.formDisplay.IsFormView;
import org.uberfire.ext.widgets.common.client.tables.ColumnMeta;

public class MultipleSubFormWidget extends Composite implements TakesValue<List<Object>> {
    interface MultipleSubFormWidgetBinder
            extends
            UiBinder<Widget, MultipleSubFormWidget> {

    }

    private static MultipleSubFormWidgetBinder uiBinder = GWT.create( MultipleSubFormWidgetBinder.class );

    @UiField
    FlowPanel content;

    @Inject
    protected ColumnGeneratorManager columnGeneratorManager;

    @Inject
    protected DynamicFormRenderer formRenderer;

    private GenericCrud crudComponent;

    private FormRenderingContext renderingContext;

    private AsyncDataProvider<HasProperties> dataProvider;

    private List<Object> values = new ArrayList<Object>();

    public MultipleSubFormWidget() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    protected void init( FieldDefinition field ) {
        content.clear();
        crudComponent = new GenericCrud( 5, !field.getReadonly(), !field.getReadonly(), !field.getReadonly() );
        content.add( crudComponent );
    }

    protected void initCrud() {
        dataProvider = new AsyncDataProvider<HasProperties>() {
            private List<HasProperties> tableValues = new ArrayList<>();

            {
                for ( Object value : values ) {
                    HasProperties tableValue;

                    if ( value instanceof HasProperties ) {
                        tableValue = (HasProperties) value;
                    } else {
                        tableValue =(HasProperties) DataBinder.forModel( value ).getModel();
                    }

                    tableValues.add( tableValue );
                }
            }

            @Override
            protected void onRangeChanged( HasData<HasProperties> hasData ) {
                if ( tableValues != null ) {
                    updateRowCount( tableValues.size(), true );
                    updateRowData( 0, tableValues );
                } else {
                    updateRowCount( 0, true );
                    updateRowData( 0, new ArrayList<HasProperties>(  ) );
                }
            }
        };
        crudComponent.updateCrudContent( dataProvider );
    }

    public void config( final MultipleSubFormFieldDefinition field, final FormRenderingContext renderingContext ) {
        init( field );

        this.renderingContext = renderingContext;

        final List<ColumnMeta> metas = new ArrayList<ColumnMeta>();

        BindableProxy<?> proxy = null;

        try {
            proxy = BindableProxyFactory.getBindableProxy( field.getStandaloneClassName() );
        } catch ( Exception e ) {
            GWT.log( "Unable to find proxy for type '" + field.getStandaloneClassName() + ".");
        }

        for ( TableColumnMeta meta : field.getColumnMetas() ) {

            String type = String.class.getName();

            if ( proxy != null ) {
                type = proxy.getBeanProperties().get( meta.getProperty() ).getType().getName();
            }

            ColumnGenerator generator = columnGeneratorManager.getGeneratorByType( type );

            if ( generator != null ) {

                ColumnMeta<HasProperties> columnMeta = new ColumnMeta<HasProperties>( generator.getColumn( meta.getProperty() ), meta.getLabel() );

                metas.add( columnMeta );
            }
        }


        crudComponent.config( new CrudHelper<Object>() {
            private Integer position;

            @Override
            public List<ColumnMeta> getGridColumns() {
                return metas;
            }

            @Override
            public IsFormView<Object> getCreateInstanceForm() {
                if ( field.getCreationForm() != null ) {
                    BindableProxy<?> proxy = null;
                    try {
                        proxy = BindableProxyFactory.getBindableProxy( field.getStandaloneClassName() );
                    } catch ( Exception e ) {
                        GWT.log( "Unable to find proxy for type '" + field.getStandaloneClassName() + ".");
                    }
                    formRenderer.render( renderingContext.getCopyFor( field.getCreationForm(), proxy ) );
                    return formRenderer;
                }

                return null;
            }

            @Override
            public IsFormView<Object> getEditInstanceForm( Integer position ) {
                this.position = position;
                if ( field.getEditionForm() != null ) {
                    formRenderer.render( renderingContext.getCopyFor( field.getCreationForm(), values.get( position ) ) );
                    return formRenderer;
                }

                return null;
            }

            @Override
            public void createInstance() {
                values.add( formRenderer.getModel() );
                initCrud();
            }

            @Override
            public void editInstance() {
                values.set( position, formRenderer.getModel() );
                initCrud();
            }

            @Override
            public void deleteInstance( int index ) {
                values.remove( index );
                initCrud();
            }
        } );
        initCrud();
    }

    @Override
    public void setValue( List<Object> objects ) {
        values = objects;

        initCrud();
    }

    @Override
    public List<Object> getValue() {
        return values;
    }
}
