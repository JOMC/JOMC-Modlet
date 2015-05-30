/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2005-206
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *     o Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     o Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
package org.jomc.modlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default {@code ModelContext} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContextFactory
 */
public class DefaultModelContext extends ModelContext
{

    /**
     * Constant for the name of the model context attribute backing property {@code providerLocation}.
     *
     * @see #getProviderLocation()
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String PROVIDER_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModelContext.providerLocationAttribute";

    /**
     * Constant for the name of the model context attribute backing property {@code platformProviderLocation}.
     *
     * @see #getPlatformProviderLocation()
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModelContext.platformProviderLocationAttribute";

    /**
     * Supported schema name extensions.
     */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /**
     * Class path location searched for providers by default.
     *
     * @see #getDefaultProviderLocation()
     */
    private static final String DEFAULT_PROVIDER_LOCATION = "META-INF/services";

    /**
     * Location searched for platform providers by default.
     *
     * @see #getDefaultPlatformProviderLocation()
     */
    private static final String DEFAULT_PLATFORM_PROVIDER_LOCATION =
        new StringBuilder( 255 ).append( System.getProperty( "java.home" ) ).append( File.separator ).append( "lib" ).
        append( File.separator ).append( "jomc.properties" ).toString();

    /**
     * Constant for the service identifier of marshaller listener services.
     *
     * @since 1.2
     */
    private static final String MARSHALLER_LISTENER_SERVICE = "javax.xml.bind.Marshaller.Listener";

    /**
     * Constant for the service identifier of unmarshaller listener services.
     *
     * @since 1.2
     */
    private static final String UNMARSHALLER_LISTENER_SERVICE = "javax.xml.bind.Unmarshaller.Listener";

    /**
     * Default provider location.
     */
    private static volatile String defaultProviderLocation;

    /**
     * Default platform provider location.
     */
    private static volatile String defaultPlatformProviderLocation;

    /**
     * Provider location of the instance.
     */
    private String providerLocation;

    /**
     * Platform provider location of the instance.
     */
    private String platformProviderLocation;

    /**
     * Creates a new {@code DefaultModelContext} instance.
     *
     * @since 1.2
     */
    public DefaultModelContext()
    {
        super();
    }

    /**
     * Creates a new {@code DefaultModelContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     */
    public DefaultModelContext( final ClassLoader classLoader )
    {
        super( classLoader );
    }

    /**
     * Gets the default location searched for provider resources.
     * <p>
     * The default provider location is controlled by system property
     * {@code org.jomc.modlet.DefaultModelContext.defaultProviderLocation} holding the location to search
     * for provider resources by default. If that property is not set, the {@code META-INF/services} default is
     * returned.
     * </p>
     *
     * @return The location searched for provider resources by default.
     *
     * @see #setDefaultProviderLocation(java.lang.String)
     */
    public static String getDefaultProviderLocation()
    {
        if ( defaultProviderLocation == null )
        {
            defaultProviderLocation = System.getProperty(
                "org.jomc.modlet.DefaultModelContext.defaultProviderLocation", DEFAULT_PROVIDER_LOCATION );

        }

        return defaultProviderLocation;
    }

    /**
     * Sets the default location searched for provider resources.
     *
     * @param value The new default location to search for provider resources or {@code null}.
     *
     * @see #getDefaultProviderLocation()
     */
    public static void setDefaultProviderLocation( final String value )
    {
        defaultProviderLocation = value;
    }

    /**
     * Gets the location searched for provider resources.
     *
     * @return The location searched for provider resources.
     *
     * @see #getDefaultProviderLocation()
     * @see #setProviderLocation(java.lang.String)
     * @see #PROVIDER_LOCATION_ATTRIBUTE_NAME
     */
    public final String getProviderLocation()
    {
        if ( this.providerLocation == null )
        {
            this.providerLocation = getDefaultProviderLocation();

            if ( DEFAULT_PROVIDER_LOCATION.equals( this.providerLocation )
                     && this.getAttribute( PROVIDER_LOCATION_ATTRIBUTE_NAME ) instanceof String )
            {
                final String contextProviderLocation = (String) this.getAttribute( PROVIDER_LOCATION_ATTRIBUTE_NAME );

                if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "contextProviderLocationInfo",
                                                        contextProviderLocation ), null );
                }

                this.providerLocation = null;
                return contextProviderLocation;
            }
            else if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultProviderLocationInfo", this.providerLocation ), null );
            }
        }

        return this.providerLocation;
    }

    /**
     * Sets the location searched for provider resources.
     *
     * @param value The new location to search for provider resources or {@code null}.
     *
     * @see #getProviderLocation()
     */
    public final void setProviderLocation( final String value )
    {
        this.providerLocation = value;
    }

    /**
     * Gets the default location searched for platform provider resources.
     * <p>
     * The default platform provider location is controlled by system property
     * {@code org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation} holding the location to
     * search for platform provider resources by default. If that property is not set, the
     * {@code <java-home>/lib/jomc.properties} default is returned.
     * </p>
     *
     * @return The location searched for platform provider resources by default.
     *
     * @see #setDefaultPlatformProviderLocation(java.lang.String)
     */
    public static String getDefaultPlatformProviderLocation()
    {
        if ( defaultPlatformProviderLocation == null )
        {
            defaultPlatformProviderLocation = System.getProperty(
                "org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation",
                DEFAULT_PLATFORM_PROVIDER_LOCATION );

        }

        return defaultPlatformProviderLocation;
    }

    /**
     * Sets the default location searched for platform provider resources.
     *
     * @param value The new default location to search for platform provider resources or {@code null}.
     *
     * @see #getDefaultPlatformProviderLocation()
     */
    public static void setDefaultPlatformProviderLocation( final String value )
    {
        defaultPlatformProviderLocation = value;
    }

    /**
     * Gets the location searched for platform provider resources.
     *
     * @return The location searched for platform provider resources.
     *
     * @see #getDefaultPlatformProviderLocation()
     * @see #setPlatformProviderLocation(java.lang.String)
     * @see #PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME
     */
    public final String getPlatformProviderLocation()
    {
        if ( this.platformProviderLocation == null )
        {
            this.platformProviderLocation = getDefaultPlatformProviderLocation();

            if ( DEFAULT_PLATFORM_PROVIDER_LOCATION.equals( this.platformProviderLocation )
                     && this.getAttribute( PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME ) instanceof String )
            {
                final String contextPlatformProviderLocation =
                    (String) this.getAttribute( PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME );

                if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "contextPlatformProviderLocationInfo",
                                                        contextPlatformProviderLocation ), null );

                }

                this.platformProviderLocation = null;
                return contextPlatformProviderLocation;
            }
            else if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG,
                          getMessage( "defaultPlatformProviderLocationInfo", this.platformProviderLocation ), null );

            }
        }

        return this.platformProviderLocation;
    }

    /**
     * Sets the location searched for platform provider resources.
     *
     * @param value The new location to search for platform provider resources or {@code null}.
     *
     * @see #getPlatformProviderLocation()
     */
    public final void setPlatformProviderLocation( final String value )
    {
        this.platformProviderLocation = value;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ModletProvider} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ModletProvider} resources to return a list of {@code Modlets}.
     * </p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletProvider#findModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @deprecated As of JOMC 1.6, replaced by {@link #findModlets(org.jomc.modlet.Modlets)}. This method will be
     * removed in JOMC 2.0.
     */
    @Override
    @Deprecated
    public Modlets findModlets() throws ModelException
    {
        return this.findModlets( new Modlets() );
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ModletProvider} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ModletProvider} resources to return a list of {@code Modlets}.
     * </p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletProvider#findModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @since 1.6
     */
    @Override
    public Modlets findModlets( final Modlets modlets ) throws ModelException
    {
        if ( modlets == null )
        {
            throw new NullPointerException( "modlets" );
        }

        Modlets found = modlets.clone();
        final Collection<ModletProvider> providers = this.loadModletServices( ModletProvider.class );

        for ( final ModletProvider provider : providers )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "creatingModlets", provider.toString() ), null );
            }

            final Modlets provided = provider.findModlets( this, found );

            if ( provided != null )
            {
                found = provided;
            }
        }

        if ( this.isLoggable( Level.FINEST ) )
        {
            for ( final Modlet m : found.getModlet() )
            {
                this.log( Level.FINEST,
                          getMessage( "modletInfo", m.getName(), m.getModel(),
                                      m.getVendor() != null
                                          ? m.getVendor() : getMessage( "noVendor" ),
                                      m.getVersion() != null
                                          ? m.getVersion() : getMessage( "noVersion" ) ), null );

                if ( m.getSchemas() != null )
                {
                    for ( final Schema s : m.getSchemas().getSchema() )
                    {
                        this.log( Level.FINEST,
                                  getMessage( "modletSchemaInfo", m.getName(), s.getPublicId(), s.getSystemId(),
                                              s.getContextId() != null
                                                  ? s.getContextId() : getMessage( "noContext" ),
                                              s.getClasspathId() != null
                                                  ? s.getClasspathId() : getMessage( "noClasspathId" ) ), null );

                    }
                }

                if ( m.getServices() != null )
                {
                    for ( final Service s : m.getServices().getService() )
                    {
                        this.log( Level.FINEST, getMessage( "modletServiceInfo", m.getName(), s.getOrdinal(),
                                                            s.getIdentifier(), s.getClazz() ), null );

                    }
                }
            }
        }

        return found;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ModletProcessor} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ModletProcessor} resources to process a list of {@code Modlets}.
     * </p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletProcessor#processModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @since 1.6
     */
    @Override
    public Modlets processModlets( final Modlets modlets ) throws ModelException
    {
        if ( modlets == null )
        {
            throw new NullPointerException( "modlets" );
        }

        Modlets result = modlets.clone();
        final Collection<ModletProcessor> processors = this.loadModletServices( ModletProcessor.class );

        for ( final ModletProcessor processor : processors )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "processingModlets", processor.toString() ), null );
            }

            final Modlets processed = processor.processModlets( this, result );

            if ( processed != null )
            {
                result = processed;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ModletValidator} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ModletValidator} resources to validate a list of {@code Modlets}.
     * </p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletValidator#validateModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @since 1.9
     */
    @Override
    public ModelValidationReport validateModlets( final Modlets modlets ) throws ModelException
    {
        if ( modlets == null )
        {
            throw new NullPointerException( "modlets" );
        }

        final ModelValidationReport report = new ModelValidationReport();
        final Collection<ModletValidator> modletValidators = this.loadModletServices( ModletValidator.class );

        for ( final ModletValidator modletValidator : modletValidators )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "validatingModlets", modletValidator.toString() ), null );
            }

            final ModelValidationReport current = modletValidator.validateModlets( this, modlets );

            if ( current != null )
            {
                report.getDetails().addAll( current.getDetails() );
            }
        }

        return report;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method creates all {@code ModelProvider} service objects of the model identified by {@code model} to create
     * a new {@code Model} instance.
     * </p>
     *
     * @see #findModel(org.jomc.modlet.Model)
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model, ModelProvider.class )
     * @see ModelProvider#findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model findModel( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        final Model m = new Model();
        m.setIdentifier( model );

        return this.findModel( m );
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method creates all {@code ModelProvider} service objects of the given model to populate the given model
     * instance.
     * </p>
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model.getIdentifier(), ModelProvider.class )
     * @see ModelProvider#findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     *
     * @since 1.2
     */
    @Override
    public Model findModel( final Model model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        Model m = model.clone();
        final long t0 = System.currentTimeMillis();

        for ( final ModelProvider provider
                  : this.createServiceObjects( model.getIdentifier(), ModelProvider.class.getName(),
                                               ModelProvider.class ) )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "creatingModel", m.getIdentifier(), provider.toString() ), null );
            }

            final Model provided = provider.findModel( this, m );

            if ( provided != null )
            {
                m = provided;
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "findModelReport", m.getIdentifier(), System.currentTimeMillis() - t0 ),
                      null );

        }

        return m;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method creates all {@code ModelProcessor} service objects of {@code model} to process the given
     * {@code Model}.
     * </p>
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model.getIdentifier(), ModelProcessor.class )
     * @see ModelProcessor#processModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model processModel( final Model model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        Model processed = model;
        final long t0 = System.currentTimeMillis();

        for ( final ModelProcessor processor
                  : this.createServiceObjects( model.getIdentifier(), ModelProcessor.class.getName(),
                                               ModelProcessor.class ) )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "processingModel", model.getIdentifier(),
                                                   processor.toString() ), null );

            }

            final Model current = processor.processModel( this, processed );

            if ( current != null )
            {
                processed = current;
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "processModelReport", model.getIdentifier(),
                                              System.currentTimeMillis() - t0 ), null );

        }

        return processed;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method creates all {@code ModelValidator} service objects of {@code model} to validate the given
     * {@code Model}.
     * </p>
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model.getIdentifier(), ModelValidator.class )
     * @see ModelValidator#validateModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public ModelValidationReport validateModel( final Model model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        final long t0 = System.currentTimeMillis();
        final ModelValidationReport report = new ModelValidationReport();

        for ( final ModelValidator validator
                  : this.createServiceObjects( model.getIdentifier(), ModelValidator.class.getName(),
                                               ModelValidator.class ) )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "validatingModel", model.getIdentifier(),
                                                   validator.toString() ), null );

            }

            final ModelValidationReport current = validator.validateModel( this, model );

            if ( current != null )
            {
                report.getDetails().addAll( current.getDetails() );
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "validateModelReport", model.getIdentifier(),
                                              System.currentTimeMillis() - t0 ), null );

        }

        return report;
    }

    /**
     * {@inheritDoc}
     *
     * @see #createSchema(java.lang.String)
     */
    @Override
    public ModelValidationReport validateModel( final String model, final Source source ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if ( source == null )
        {
            throw new NullPointerException( "source" );
        }

        final long t0 = System.currentTimeMillis();
        final javax.xml.validation.Schema schema = this.createSchema( model );
        final Validator validator = schema.newValidator();
        final ModelErrorHandler modelErrorHandler = new ModelErrorHandler( this );
        validator.setErrorHandler( modelErrorHandler );

        try
        {
            validator.validate( source );
        }
        catch ( final SAXException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getException() != null )
            {
                message = getMessage( e.getException() );
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, message, e );
            }

            if ( modelErrorHandler.getReport().isModelValid() )
            {
                throw new ModelException( message, e );
            }
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "validateModelReport", model, System.currentTimeMillis() - t0 ), null );
        }

        return modelErrorHandler.getReport();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ServiceFactory} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ServiceFactory} resources to create a new service object.
     * </p>
     *
     * @since 1.9
     */
    @Override
    public <T> Collection<? extends T> createServiceObjects( final String model, final String service,
                                                             final Class<T> type )
        throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if ( service == null )
        {
            throw new NullPointerException( "service" );
        }
        if ( type == null )
        {
            throw new NullPointerException( "type" );
        }

        final Services modelServices = this.getModlets().getServices( model );
        final Collection<T> serviceObjects =
            new ArrayList<T>( modelServices != null ? modelServices.getService().size() : 0 );

        if ( modelServices != null )
        {
            final Collection<ServiceFactory> factories = this.loadModletServices( ServiceFactory.class );

            for ( final Service s : modelServices.getServices( service ) )
            {
                serviceObjects.add( this.createServiceObject( s, type, factories ) );
            }
        }

        return Collections.unmodifiableCollection( serviceObjects );
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ServiceFactory} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ServiceFactory} resources to create a new service object.
     * </p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ServiceFactory#createServiceObject(org.jomc.modlet.ModelContext, org.jomc.modlet.Service, java.lang.Class)
     * @since 1.2
     * @deprecated As of JOMC 1.9, please use method {@link #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class)}.
     * This method will be removed in JOMC 2.0.
     */
    @Override
    @Deprecated
    public <T> T createServiceObject( final Service service, final Class<T> type ) throws ModelException
    {
        if ( service == null )
        {
            throw new NullPointerException( "service" );
        }
        if ( type == null )
        {
            throw new NullPointerException( "type" );
        }

        return this.createServiceObject( service, type, this.loadModletServices( ServiceFactory.class ) );
    }

    /**
     * This method creates a new service object for a given service using a given collection of service factories.
     *
     * @param <T> The type of the service.
     * @param service The service to create a new object of.
     * @param type The class of the type of the service.
     * @param factories The service factories to use for creating the new service object.
     *
     * @return An new service object for {@code service}.
     *
     * @throws NullPointerException if {@code service}, {@code type} or {@code factories} is {@code null}.
     * @throws ModelException if creating the service object fails.
     * @since 1.9
     */
    private <T> T createServiceObject( final Service service, final Class<T> type,
                                       final Collection<ServiceFactory> factories ) throws ModelException
    {
        if ( service == null )
        {
            throw new NullPointerException( "service" );
        }
        if ( type == null )
        {
            throw new NullPointerException( "type" );
        }
        if ( factories == null )
        {
            throw new NullPointerException( "factories" );
        }

        T serviceObject = null;

        for ( final ServiceFactory factory : factories )
        {
            final T current = factory.createServiceObject( this, service, type );

            if ( current != null )
            {
                if ( this.isLoggable( Level.FINER ) )
                {
                    this.log( Level.FINER, getMessage( "creatingService", service.getOrdinal(), service.getIdentifier(),
                                                       service.getClazz(), factory.toString() ), null );

                }

                serviceObject = current;
                break;
            }
        }

        if ( serviceObject == null )
        {
            throw new ModelException( getMessage( "serviceNotCreated", service.getOrdinal(), service.getIdentifier(),
                                                  service.getClazz() ), null );

        }

        return serviceObject;
    }

    @Override
    public EntityResolver createEntityResolver( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createEntityResolver( this.getModlets().getSchemas( model ) );
    }

    @Override
    @Deprecated
    public EntityResolver createEntityResolver( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createEntityResolver( this.getModlets().getSchemas( publicId ) );
    }

    @Override
    public LSResourceResolver createResourceResolver( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createResourceResolver( this.createEntityResolver( model ) );
    }

    @Override
    @Deprecated
    public LSResourceResolver createResourceResolver( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createResourceResolver( this.createEntityResolver( publicId ) );
    }

    @Override
    public javax.xml.validation.Schema createSchema( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createSchema( this.getModlets().getSchemas( model ), this.createEntityResolver( model ),
                                  this.createResourceResolver( model ), model, null );

    }

    @Override
    @Deprecated
    public javax.xml.validation.Schema createSchema( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createSchema( this.getModlets().getSchemas( publicId ), this.createEntityResolver( publicId ),
                                  this.createResourceResolver( publicId ), null, publicId );

    }

    @Override
    public JAXBContext createContext( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createContext( this.getModlets().getSchemas( model ), model, null );
    }

    @Override
    @Deprecated
    public JAXBContext createContext( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createContext( this.getModlets().getSchemas( publicId ), null, publicId );
    }

    @Override
    public Marshaller createMarshaller( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createMarshaller( this.getModlets().getSchemas( model ), this.getModlets().getServices( model ),
                                      model, null );

    }

    @Override
    @Deprecated
    public Marshaller createMarshaller( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createMarshaller( this.getModlets().getSchemas( publicId ), null, null, publicId );
    }

    @Override
    public Unmarshaller createUnmarshaller( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return this.createUnmarshaller( this.getModlets().getSchemas( model ), this.getModlets().getServices( model ),
                                        model, null );

    }

    @Override
    @Deprecated
    public Unmarshaller createUnmarshaller( final URI publicId ) throws ModelException
    {
        if ( publicId == null )
        {
            throw new NullPointerException( "publicId" );
        }

        return this.createUnmarshaller( this.getModlets().getSchemas( publicId ), null, null, publicId );
    }

    private <T> Collection<T> loadModletServices( final Class<T> serviceClass ) throws ModelException
    {
        try
        {
            final String serviceNamePrefix = serviceClass.getName() + ".";
            final Map<String, T> sortedPlatformServices = new TreeMap<String, T>( new Comparator<String>()
            {

                public int compare( final String key1, final String key2 )
                {
                    return key1.compareTo( key2 );
                }

            } );

            final File platformServices = new File( this.getPlatformProviderLocation() );

            if ( platformServices.exists() )
            {
                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", platformServices.getAbsolutePath() ), null );
                }

                InputStream in = null;
                boolean suppressExceptionOnClose = true;
                final java.util.Properties p = new java.util.Properties();

                try
                {
                    in = new FileInputStream( platformServices );
                    p.load( in );
                    suppressExceptionOnClose = false;
                }
                finally
                {
                    try
                    {
                        if ( in != null )
                        {
                            in.close();
                        }
                    }
                    catch ( final IOException e )
                    {
                        if ( suppressExceptionOnClose )
                        {
                            this.log( Level.SEVERE, getMessage( e ), e );
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }

                for ( final Map.Entry<Object, Object> e : p.entrySet() )
                {
                    if ( e.getKey().toString().startsWith( serviceNamePrefix ) )
                    {
                        final String configuration = e.getValue().toString();

                        if ( this.isLoggable( Level.FINEST ) )
                        {
                            this.log( Level.FINEST, getMessage( "serviceInfo", platformServices.getAbsolutePath(),
                                                                serviceClass.getName(), configuration ), null );

                        }

                        sortedPlatformServices.put( e.getKey().toString(),
                                                    this.createModletServiceObject( serviceClass, configuration ) );

                    }
                }
            }

            final Enumeration<URL> classpathServices =
                this.findResources( this.getProviderLocation() + '/' + serviceClass.getName() );

            int count = 0;
            final long t0 = System.currentTimeMillis();
            final List<T> sortedClasspathServices = new ArrayList<T>();

            while ( classpathServices.hasMoreElements() )
            {
                count++;
                final URL url = classpathServices.nextElement();

                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", url.toExternalForm() ), null );
                }

                BufferedReader reader = null;
                boolean suppressExceptionOnClose = true;

                try
                {
                    reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) );

                    String line;
                    while ( ( line = reader.readLine() ) != null )
                    {
                        if ( line.contains( "#" ) )
                        {
                            continue;
                        }

                        if ( this.isLoggable( Level.FINEST ) )
                        {
                            this.log( Level.FINEST, getMessage( "serviceInfo", url.toExternalForm(),
                                                                serviceClass.getName(), line ), null );

                        }

                        final T serviceObject = this.createModletServiceObject( serviceClass, line );
                        sortedClasspathServices.add( serviceObject );
                    }

                    Collections.sort( sortedClasspathServices,
                                      new Comparator<Object>()
                                      {

                                          public int compare( final Object o1, final Object o2 )
                                          {
                                              return ordinalOf( o1 ) - ordinalOf( o2 );
                                          }

                                      } );

                    suppressExceptionOnClose = false;
                }
                finally
                {
                    try
                    {
                        if ( reader != null )
                        {
                            reader.close();
                        }
                    }
                    catch ( final IOException e )
                    {
                        if ( suppressExceptionOnClose )
                        {
                            this.log( Level.SEVERE, getMessage( e ), e );
                        }
                        else
                        {
                            throw new ModelException( getMessage( e ), e );
                        }
                    }
                }
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "contextReport", count,
                                                  this.getProviderLocation() + '/' + serviceClass.getName(),
                                                  System.currentTimeMillis() - t0 ), null );

            }

            final List<T> services =
                new ArrayList<T>( sortedPlatformServices.size() + sortedClasspathServices.size() );

            services.addAll( sortedPlatformServices.values() );
            services.addAll( sortedClasspathServices );

            return services;
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
    }

    private <T> T createModletServiceObject( final Class<T> serviceClass, final String configuration )
        throws ModelException
    {
        String className = configuration;
        final int i0 = configuration.indexOf( '[' );
        final int i1 = configuration.lastIndexOf( ']' );
        final Service service = new Service();
        service.setIdentifier( serviceClass.getName() );

        if ( i0 != -1 && i1 != -1 )
        {
            className = configuration.substring( 0, i0 );
            final StringTokenizer propertyTokens =
                new StringTokenizer( configuration.substring( i0 + 1, i1 ), "," );

            while ( propertyTokens.hasMoreTokens() )
            {
                final String property = propertyTokens.nextToken();
                final int d0 = property.indexOf( '=' );

                String propertyName = property;
                String propertyValue = null;

                if ( d0 != -1 )
                {
                    propertyName = property.substring( 0, d0 );
                    propertyValue = property.substring( d0 + 1, property.length() );
                }

                final Property p = new Property();
                service.getProperty().add( p );

                p.setName( propertyName );
                p.setValue( propertyValue );
            }
        }

        service.setClazz( className );

        // Need a way to exchange the service factory creating modlet service objects?
        return new DefaultServiceFactory().createServiceObject( this, service, serviceClass );
    }

    /**
     * Searches the context for {@code META-INF/MANIFEST.MF} resources and returns a set of URIs of entries whose names
     * end with a known schema extension.
     *
     * @return Set of URIs of any matching entries.
     *
     * @throws IOException if reading fails.
     * @throws URISyntaxException if parsing fails.
     * @throws ModelException if searching the context fails.
     */
    private Set<URI> getSchemaResources() throws IOException, URISyntaxException, ModelException
    {
        final Set<URI> resources = new HashSet<URI>();
        final long t0 = System.currentTimeMillis();
        int count = 0;

        for ( final Enumeration<URL> e = this.findResources( "META-INF/MANIFEST.MF" );
              e.hasMoreElements(); )
        {
            InputStream manifestStream = null;
            boolean suppressExceptionOnClose = true;

            try
            {
                count++;
                final URL manifestUrl = e.nextElement();
                final String externalForm = manifestUrl.toExternalForm();
                final String baseUrl = externalForm.substring( 0, externalForm.indexOf( "META-INF" ) );
                manifestStream = manifestUrl.openStream();
                final Manifest mf = new Manifest( manifestStream );

                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", externalForm ), null );
                }

                for ( final Map.Entry<String, Attributes> entry : mf.getEntries().entrySet() )
                {
                    for ( int i = SCHEMA_EXTENSIONS.length - 1; i >= 0; i-- )
                    {
                        if ( entry.getKey().toLowerCase().endsWith( '.' + SCHEMA_EXTENSIONS[i].toLowerCase() ) )
                        {
                            final URL schemaUrl = new URL( baseUrl + entry.getKey() );
                            resources.add( schemaUrl.toURI() );

                            if ( this.isLoggable( Level.FINEST ) )
                            {
                                this.log( Level.FINEST, getMessage( "foundSchemaCandidate",
                                                                    schemaUrl.toExternalForm() ), null );

                            }
                        }
                    }
                }

                suppressExceptionOnClose = false;
            }
            finally
            {
                try
                {
                    if ( manifestStream != null )
                    {
                        manifestStream.close();
                    }
                }
                catch ( final IOException ex )
                {
                    if ( suppressExceptionOnClose )
                    {
                        this.log( Level.SEVERE, getMessage( ex ), ex );
                    }
                    else
                    {
                        throw ex;
                    }
                }
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "contextReport", count, "META-INF/MANIFEST.MF",
                                              System.currentTimeMillis() - t0 ), null );

        }

        return resources;
    }

    private EntityResolver createEntityResolver( final Schemas schemas )
    {
        return new DefaultHandler()
        {

            @Override
            public InputSource resolveEntity( final String publicId, final String systemId )
                throws SAXException, IOException
            {
                InputSource schemaSource = null;

                try
                {
                    Schema s = null;

                    if ( schemas != null )
                    {
                        if ( systemId != null && !"".equals( systemId ) )
                        {
                            s = schemas.getSchemaBySystemId( systemId );
                        }
                        else if ( publicId != null )
                        {
                            s = schemas.getSchemaByPublicId( publicId );
                        }
                    }

                    if ( s != null )
                    {
                        schemaSource = new InputSource();
                        schemaSource.setPublicId( s.getPublicId() );
                        schemaSource.setSystemId( s.getSystemId() );

                        if ( s.getClasspathId() != null )
                        {
                            final URL resource = findResource( s.getClasspathId() );

                            if ( resource != null )
                            {
                                schemaSource.setSystemId( resource.toExternalForm() );
                            }
                            else if ( isLoggable( Level.WARNING ) )
                            {
                                log( Level.WARNING, getMessage( "resourceNotFound", s.getClasspathId() ), null );
                            }
                        }

                        if ( isLoggable( Level.FINEST ) )
                        {
                            log( Level.FINEST, getMessage( "resolutionInfo", publicId + ", " + systemId,
                                                           schemaSource.getPublicId() + ", "
                                                               + schemaSource.getSystemId() ), null );

                        }
                    }

                    if ( schemaSource == null && systemId != null && !"".equals( systemId ) )
                    {
                        final URI systemUri = new URI( systemId );
                        String schemaName = systemUri.getPath();

                        if ( schemaName != null )
                        {
                            final int lastIndexOfSlash = schemaName.lastIndexOf( '/' );
                            if ( lastIndexOfSlash != -1 && lastIndexOfSlash < schemaName.length() )
                            {
                                schemaName = schemaName.substring( lastIndexOfSlash + 1 );
                            }

                            for ( final URI uri : getSchemaResources() )
                            {
                                if ( uri.getSchemeSpecificPart() != null
                                         && uri.getSchemeSpecificPart().endsWith( schemaName ) )
                                {
                                    schemaSource = new InputSource();
                                    schemaSource.setPublicId( publicId );
                                    schemaSource.setSystemId( uri.toASCIIString() );

                                    if ( isLoggable( Level.FINEST ) )
                                    {
                                        log( Level.FINEST, getMessage( "resolutionInfo", systemUri.toASCIIString(),
                                                                       schemaSource.getSystemId() ), null );

                                    }

                                    break;
                                }
                            }
                        }
                        else
                        {
                            if ( isLoggable( Level.WARNING ) )
                            {
                                log( Level.WARNING, getMessage( "unsupportedIdUri", systemId,
                                                                systemUri.toASCIIString() ), null );

                            }

                            schemaSource = null;
                        }
                    }
                }
                catch ( final URISyntaxException e )
                {
                    if ( isLoggable( Level.WARNING ) )
                    {
                        log( Level.WARNING, getMessage( "unsupportedIdUri", systemId, getMessage( e ) ), null );
                    }

                    schemaSource = null;
                }
                catch ( final ModelException e )
                {
                    String message = getMessage( e );
                    if ( message == null )
                    {
                        message = "";
                    }
                    else if ( message.length() > 0 )
                    {
                        message = " " + message;
                    }

                    String resource = "";
                    if ( publicId != null )
                    {
                        resource = publicId + ", ";
                    }
                    resource += systemId;

                    // JDK: As of JDK 6, "new IOException( message, cause )".
                    throw (IOException) new IOException( getMessage(
                        "failedResolving", resource, message ) ).initCause( e );

                }

                return schemaSource;
            }

        };
    }

    private LSResourceResolver createResourceResolver( final EntityResolver entityResolver )
    {
        if ( entityResolver == null )
        {
            throw new NullPointerException( "entityResolver" );
        }

        return new LSResourceResolver()
        {

            public LSInput resolveResource( final String type, final String namespaceURI, final String publicId,
                                            final String systemId, final String baseURI )
            {
                final String resolvePublicId = namespaceURI == null ? publicId : namespaceURI;
                final String resolveSystemId = systemId == null ? "" : systemId;

                try
                {
                    if ( XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( type ) )
                    {
                        final InputSource schemaSource =
                            entityResolver.resolveEntity( resolvePublicId, resolveSystemId );

                        if ( schemaSource != null )
                        {
                            return new LSInput()
                            {

                                public Reader getCharacterStream()
                                {
                                    return schemaSource.getCharacterStream();
                                }

                                public void setCharacterStream( final Reader characterStream )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setCharacterStream",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public InputStream getByteStream()
                                {
                                    return schemaSource.getByteStream();
                                }

                                public void setByteStream( final InputStream byteStream )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setByteStream",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public String getStringData()
                                {
                                    return null;
                                }

                                public void setStringData( final String stringData )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setStringData",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public String getSystemId()
                                {
                                    return schemaSource.getSystemId();
                                }

                                public void setSystemId( final String systemId )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setSystemId",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public String getPublicId()
                                {
                                    return schemaSource.getPublicId();
                                }

                                public void setPublicId( final String publicId )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setPublicId",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public String getBaseURI()
                                {
                                    return baseURI;
                                }

                                public void setBaseURI( final String baseURI )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setBaseURI",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public String getEncoding()
                                {
                                    return schemaSource.getEncoding();
                                }

                                public void setEncoding( final String encoding )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setEncoding",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                                public boolean getCertifiedText()
                                {
                                    return false;
                                }

                                public void setCertifiedText( final boolean certifiedText )
                                {
                                    if ( isLoggable( Level.WARNING ) )
                                    {
                                        log( Level.WARNING, getMessage(
                                             "unsupportedOperation", "setCertifiedText",
                                             DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                    }
                                }

                            };
                        }

                    }
                    else if ( isLoggable( Level.WARNING ) )
                    {
                        log( Level.WARNING, getMessage( "unsupportedResourceType", type ), null );
                    }
                }
                catch ( final SAXException e )
                {
                    String message = getMessage( e );
                    if ( message == null && e.getException() != null )
                    {
                        message = getMessage( e.getException() );
                    }
                    if ( message == null )
                    {
                        message = "";
                    }
                    else if ( message.length() > 0 )
                    {
                        message = " " + message;
                    }

                    String resource = "";
                    if ( resolvePublicId != null )
                    {
                        resource = resolvePublicId + ", ";
                    }
                    resource += resolveSystemId;

                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", resource, message ), e );
                    }
                }
                catch ( final IOException e )
                {
                    String message = getMessage( e );
                    if ( message == null )
                    {
                        message = "";
                    }
                    else if ( message.length() > 0 )
                    {
                        message = " " + message;
                    }

                    String resource = "";
                    if ( resolvePublicId != null )
                    {
                        resource = resolvePublicId + ", ";
                    }
                    resource += resolveSystemId;

                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", resource, message ), e );
                    }
                }

                return null;
            }

        };
    }

    private javax.xml.validation.Schema createSchema( final Schemas schemas, final EntityResolver entityResolver,
                                                      final LSResourceResolver resourceResolver, final String model,
                                                      final URI publicId ) throws ModelException
    {
        if ( entityResolver == null )
        {
            throw new NullPointerException( "entityResolver" );
        }
        if ( model != null && publicId != null )
        {
            throw new IllegalArgumentException( "model=" + model + ", publicId=" + publicId.toASCIIString() );
        }

        try
        {
            final long t0 = System.currentTimeMillis();
            final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            final List<Source> sources = new ArrayList<Source>( schemas != null ? schemas.getSchema().size() : 0 );

            if ( schemas != null )
            {
                for ( final Schema s : schemas.getSchema() )
                {
                    final InputSource inputSource = entityResolver.resolveEntity( s.getPublicId(), s.getSystemId() );

                    if ( inputSource != null )
                    {
                        sources.add( new SAXSource( inputSource ) );
                    }
                }
            }

            if ( sources.isEmpty() )
            {
                if ( model != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForModel", model ) );
                }
                if ( publicId != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForPublicId", publicId ) );
                }
            }

            f.setResourceResolver( resourceResolver );
            f.setErrorHandler( new ErrorHandler()
            {
                // See http://java.net/jira/browse/JAXP-66

                public void warning( final SAXParseException e ) throws SAXException
                {
                    String message = getMessage( e );
                    if ( message == null && e.getException() != null )
                    {
                        message = getMessage( e.getException() );
                    }

                    if ( isLoggable( Level.WARNING ) )
                    {
                        log( Level.WARNING, message, e );
                    }
                }

                public void error( final SAXParseException e ) throws SAXException
                {
                    throw e;
                }

                public void fatalError( final SAXParseException e ) throws SAXException
                {
                    throw e;
                }

            } );

            final javax.xml.validation.Schema schema = f.newSchema( sources.toArray( new Source[ sources.size() ] ) );

            if ( this.isLoggable( Level.FINE ) )
            {
                final StringBuilder schemaInfo = new StringBuilder( sources.size() * 50 );

                for ( final Source s : sources )
                {
                    schemaInfo.append( ", " ).append( s.getSystemId() );
                }

                this.log( Level.FINE, getMessage( "creatingSchema", schemaInfo.substring( 2 ),
                                                  System.currentTimeMillis() - t0 ), null );

            }

            return schema;
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
        catch ( final SAXException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getException() != null )
            {
                message = getMessage( e.getException() );
            }

            throw new ModelException( message, e );
        }
    }

    private JAXBContext createContext( final Schemas schemas, final String model, final URI publicId )
        throws ModelException
    {
        if ( model != null && publicId != null )
        {
            throw new IllegalArgumentException( "model=" + model + ", publicId=" + publicId.toASCIIString() );
        }

        try
        {
            StringBuilder packageNames = null;
            final long t0 = System.currentTimeMillis();

            if ( schemas != null )
            {
                packageNames = new StringBuilder( schemas.getSchema().size() * 25 );

                for ( final Schema schema : schemas.getSchema() )
                {
                    if ( schema.getContextId() != null )
                    {
                        packageNames.append( ':' ).append( schema.getContextId() );
                    }
                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                if ( model != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForModel", model ) );
                }
                if ( publicId != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForPublicId", publicId ) );
                }
            }

            final JAXBContext context = JAXBContext.newInstance( packageNames.substring( 1 ), this.getClassLoader() );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "creatingContext", packageNames.substring( 1 ),
                                                  System.currentTimeMillis() - t0 ), null );
            }

            return context;
        }
        catch ( final JAXBException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getLinkedException() != null )
            {
                message = getMessage( e.getLinkedException() );
            }

            throw new ModelException( message, e );
        }
    }

    private Marshaller createMarshaller( final Schemas schemas, final Services services, final String model,
                                         final URI publicId ) throws ModelException
    {
        if ( model != null && publicId != null )
        {
            throw new IllegalArgumentException( "model=" + model + ", publicId=" + publicId.toASCIIString() );
        }

        try
        {
            StringBuilder packageNames = null;
            StringBuilder schemaLocation = null;
            final long t0 = System.currentTimeMillis();

            if ( schemas != null )
            {
                packageNames = new StringBuilder( schemas.getSchema().size() * 25 );
                schemaLocation = new StringBuilder( schemas.getSchema().size() * 50 );

                for ( final Schema schema : schemas.getSchema() )
                {
                    if ( schema.getContextId() != null )
                    {
                        packageNames.append( ':' ).append( schema.getContextId() );
                    }
                    if ( schema.getPublicId() != null && schema.getSystemId() != null )
                    {
                        schemaLocation.append( ' ' ).append( schema.getPublicId() ).append( ' ' ).
                            append( schema.getSystemId() );

                    }
                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                if ( model != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForModel", model ) );
                }
                if ( publicId != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForPublicId", publicId ) );
                }
            }

            final Marshaller m =
                JAXBContext.newInstance( packageNames.substring( 1 ), this.getClassLoader() ).createMarshaller();

            if ( schemaLocation != null && schemaLocation.length() != 0 )
            {
                m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.substring( 1 ) );
            }

            MarshallerListenerList listenerList = null;

            if ( services != null )
            {
                for ( final Service service : services.getServices( MARSHALLER_LISTENER_SERVICE ) )
                {
                    if ( listenerList == null )
                    {
                        listenerList = new MarshallerListenerList();
                    }

                    listenerList.getListeners().add( this.createServiceObject( service, Marshaller.Listener.class ) );
                }
            }

            if ( listenerList != null )
            {
                m.setListener( listenerList );
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                if ( listenerList == null )
                {
                    this.log( Level.FINE, getMessage( "creatingMarshaller", packageNames.substring( 1 ),
                                                      schemaLocation.substring( 1 ),
                                                      System.currentTimeMillis() - t0 ), null );

                }
                else
                {
                    final StringBuilder b = new StringBuilder( listenerList.getListeners().size() * 100 );

                    for ( int i = 0, s0 = listenerList.getListeners().size(); i < s0; i++ )
                    {
                        b.append( ',' ).append( listenerList.getListeners().get( i ) );
                    }

                    this.log( Level.FINE, getMessage( "creatingMarshallerWithListeners", packageNames.substring( 1 ),
                                                      schemaLocation.substring( 1 ), b.substring( 1 ),
                                                      System.currentTimeMillis() - t0 ), null );

                }
            }

            return m;
        }
        catch ( final JAXBException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getLinkedException() != null )
            {
                message = getMessage( e.getLinkedException() );
            }

            throw new ModelException( message, e );
        }
    }

    private Unmarshaller createUnmarshaller( final Schemas schemas, final Services services, final String model,
                                             final URI publicId ) throws ModelException
    {
        if ( model != null && publicId != null )
        {
            throw new IllegalArgumentException( "model=" + model + ", publicId=" + publicId.toASCIIString() );
        }

        try
        {
            StringBuilder packageNames = null;
            final long t0 = System.currentTimeMillis();

            if ( schemas != null )
            {
                packageNames = new StringBuilder( schemas.getSchema().size() * 25 );

                for ( final Schema schema : schemas.getSchema() )
                {
                    if ( schema.getContextId() != null )
                    {
                        packageNames.append( ':' ).append( schema.getContextId() );
                    }
                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                if ( model != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForModel", model ) );
                }
                if ( publicId != null )
                {
                    throw new ModelException( getMessage( "missingSchemasForPublicId", publicId ) );
                }
            }

            final Unmarshaller u =
                JAXBContext.newInstance( packageNames.substring( 1 ), this.getClassLoader() ).createUnmarshaller();

            UnmarshallerListenerList listenerList = null;

            if ( services != null )
            {
                for ( final Service service : services.getServices( UNMARSHALLER_LISTENER_SERVICE ) )
                {
                    if ( listenerList == null )
                    {
                        listenerList = new UnmarshallerListenerList();
                    }

                    listenerList.getListeners().add( this.createServiceObject( service, Unmarshaller.Listener.class ) );
                }
            }

            if ( listenerList != null )
            {
                u.setListener( listenerList );
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                if ( listenerList == null )
                {
                    this.log( Level.FINE, getMessage( "creatingUnmarshaller", packageNames.substring( 1 ),
                                                      System.currentTimeMillis() - t0 ), null );

                }
                else
                {
                    final StringBuilder b = new StringBuilder( listenerList.getListeners().size() * 100 );

                    for ( int i = 0, s0 = listenerList.getListeners().size(); i < s0; i++ )
                    {
                        b.append( ',' ).append( listenerList.getListeners().get( i ) );
                    }

                    this.log( Level.FINE, getMessage( "creatingUnmarshallerWithListeners",
                                                      packageNames.substring( 1 ), b.substring( 1 ),
                                                      System.currentTimeMillis() - t0 ), null );

                }
            }

            return u;
        }
        catch ( final JAXBException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getLinkedException() != null )
            {
                message = getMessage( e.getLinkedException() );
            }

            throw new ModelException( message, e );
        }
    }

    private static int ordinalOf( final Object serviceObject )
    {
        int ordinal = 0;

        if ( serviceObject instanceof ModletProvider )
        {
            ordinal = ( (ModletProvider) serviceObject ).getOrdinal();
        }
        if ( serviceObject instanceof ModletProcessor )
        {
            ordinal = ( (ModletProcessor) serviceObject ).getOrdinal();
        }
        if ( serviceObject instanceof ModletValidator )
        {
            ordinal = ( (ModletValidator) serviceObject ).getOrdinal();
        }
        if ( serviceObject instanceof ServiceFactory )
        {
            ordinal = ( (ServiceFactory) serviceObject ).getOrdinal();
        }

        return ordinal;
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelContext.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null
                   ? t.getMessage() != null && t.getMessage().trim().length() > 0
                         ? t.getMessage()
                         : getMessage( t.getCause() )
                   : null;

    }

}

/**
 * {@code ErrorHandler} collecting {@code ModelValidationReport} details.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 */
class ModelErrorHandler extends DefaultHandler
{

    /**
     * The context of the instance.
     */
    private ModelContext context;

    /**
     * The report of the instance.
     */
    private ModelValidationReport report;

    /**
     * Creates a new {@code ModelErrorHandler} instance taking a context.
     *
     * @param context The context of the instance.
     */
    ModelErrorHandler( final ModelContext context )
    {
        this( context, null );
    }

    /**
     * Creates a new {@code ModelErrorHandler} instance taking a report to use for collecting validation events.
     *
     * @param context The context of the instance.
     * @param report A report to use for collecting validation events.
     */
    ModelErrorHandler( final ModelContext context, final ModelValidationReport report )
    {
        super();
        this.context = context;
        this.report = report;
    }

    /**
     * Gets the report of the instance.
     *
     * @return The report of the instance.
     */
    public ModelValidationReport getReport()
    {
        if ( this.report == null )
        {
            this.report = new ModelValidationReport();
        }

        return this.report;
    }

    @Override
    public void warning( final SAXParseException exception ) throws SAXException
    {
        String message = getMessage( exception );
        if ( message == null && exception.getException() != null )
        {
            message = getMessage( exception.getException() );
        }

        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, message, exception );
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Warning condition", Level.WARNING, message, null ) );

    }

    @Override
    public void error( final SAXParseException exception ) throws SAXException
    {
        String message = getMessage( exception );
        if ( message == null && exception.getException() != null )
        {
            message = getMessage( exception.getException() );
        }

        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, message, exception );
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Section 1.2 - Error", Level.SEVERE, message, null ) );

    }

    @Override
    public void fatalError( final SAXParseException exception ) throws SAXException
    {
        String message = getMessage( exception );
        if ( message == null && exception.getException() != null )
        {
            message = getMessage( exception.getException() );
        }

        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, message, exception );
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Section 1.2 - Fatal Error", Level.SEVERE, message, null ) );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null
                   ? t.getMessage() != null && t.getMessage().trim().length() > 0
                         ? t.getMessage()
                         : getMessage( t.getCause() )
                   : null;

    }

}

/**
 * List of {@code Marshaller.Listener}s.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.2
 */
class MarshallerListenerList extends Marshaller.Listener
{

    /**
     * The {@code Marshaller.Listener}s of the instance.
     */
    private List<Marshaller.Listener> listeners;

    /**
     * Creates a new {@code MarshallerListenerList} instance.
     */
    MarshallerListenerList()
    {
        super();
    }

    /**
     * Gets the listeners of the instance.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.
     * </p>
     *
     * @return The list of listeners of the instance.
     */
    List<Marshaller.Listener> getListeners()
    {
        if ( this.listeners == null )
        {
            this.listeners = new ArrayList<Marshaller.Listener>();
        }

        return this.listeners;
    }

    @Override
    public void beforeMarshal( final Object source )
    {
        for ( int i = 0, s0 = this.getListeners().size(); i < s0; i++ )
        {
            this.getListeners().get( i ).beforeMarshal( source );
        }
    }

    @Override
    public void afterMarshal( final Object source )
    {
        for ( int i = 0, s0 = this.getListeners().size(); i < s0; i++ )
        {
            this.getListeners().get( i ).afterMarshal( source );
        }
    }

}

/**
 * List of {@code Unmarshaller.Listener}s.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.2
 */
class UnmarshallerListenerList extends Unmarshaller.Listener
{

    /**
     * The {@code Unmarshaller.Listener}s of the instance.
     */
    private List<Unmarshaller.Listener> listeners;

    /**
     * Creates a new {@code UnmarshallerListenerList} instance.
     */
    UnmarshallerListenerList()
    {
        super();
    }

    /**
     * Gets the listeners of the instance.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.
     * </p>
     *
     * @return The list of listeners of the instance.
     */
    List<Unmarshaller.Listener> getListeners()
    {
        if ( this.listeners == null )
        {
            this.listeners = new ArrayList<Unmarshaller.Listener>();
        }

        return this.listeners;
    }

    @Override
    public void beforeUnmarshal( final Object target, final Object parent )
    {
        for ( int i = 0, s0 = this.getListeners().size(); i < s0; i++ )
        {
            this.getListeners().get( i ).beforeUnmarshal( target, parent );
        }
    }

    @Override
    public void afterUnmarshal( final Object target, final Object parent )
    {
        for ( int i = 0, s0 = this.getListeners().size(); i < s0; i++ )
        {
            this.getListeners().get( i ).afterUnmarshal( target, parent );
        }
    }

}
