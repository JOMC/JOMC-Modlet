/*
 *   Copyright (C) 2005 Christian Schulte <cs@schulte.it>
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
    private volatile String providerLocation;

    /**
     * Platform provider location of the instance.
     */
    private volatile String platformProviderLocation;

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
        String location = this.providerLocation;

        if ( this.providerLocation == null )
        {
            this.providerLocation = getDefaultProviderLocation();
            location = this.providerLocation;

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultProviderLocationInfo", location ), null );
            }
        }

        if ( this.getAttribute( PROVIDER_LOCATION_ATTRIBUTE_NAME ) instanceof String )
        {
            location = (String) this.getAttribute( PROVIDER_LOCATION_ATTRIBUTE_NAME );

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "contextProviderLocationInfo", location ), null );
            }
        }

        return location;
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
        String location = this.platformProviderLocation;

        if ( this.platformProviderLocation == null )
        {
            this.platformProviderLocation = getDefaultPlatformProviderLocation();
            location = this.platformProviderLocation;

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultPlatformProviderLocationInfo", location ), null );
            }
        }

        if ( this.getAttribute( PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME ) instanceof String )
        {
            location = (String) this.getAttribute( PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME );

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "contextPlatformProviderLocationInfo", location ), null );
            }
        }

        return location;
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
     * @since 1.6
     */
    @Override
    public Modlets findModlets( final Modlets modlets ) throws ModelException
    {
        Modlets found = Objects.requireNonNull( modlets, "modlets" ).clone();
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
            try ( final Stream<Modlet> st0 = found.getModlet().parallelStream().unordered() )
            {
                st0.forEach( m  ->
                {
                    log( Level.FINEST,
                         getMessage( "modletInfo", m.getName(), m.getModel(),
                                     m.getVendor() != null
                                         ? m.getVendor() : getMessage( "noVendor" ),
                                     m.getVersion() != null
                                         ? m.getVersion() : getMessage( "noVersion" ) ), null );

                    if ( m.getSchemas() != null )
                    {
                        try ( final Stream<Schema> st1 = m.getSchemas().getSchema().parallelStream().unordered() )
                        {
                            st1.forEach( s  ->
                            {
                                log( Level.FINEST,
                                     getMessage( "modletSchemaInfo", m.getName(), s.getPublicId(), s.getSystemId(),
                                                 s.getContextId() != null
                                                     ? s.getContextId() : getMessage( "noContext" ),
                                                 s.getClasspathId() != null
                                                     ? s.getClasspathId() : getMessage( "noClasspathId" ) ), null );
                            } );
                        }
                    }

                    if ( m.getServices() != null )
                    {
                        try ( final Stream<Service> st1 = m.getServices().getService().parallelStream().unordered() )
                        {
                            st1.forEach( s  ->
                            {
                                log( Level.FINEST, getMessage( "modletServiceInfo", m.getName(), s.getOrdinal(),
                                                               s.getIdentifier(), s.getClazz() ), null );
                            } );
                        }
                    }
                } );
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
        Modlets result = Objects.requireNonNull( modlets, "modlets" ).clone();
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
        final Modlets cloned = Objects.requireNonNull( modlets, "modlets" ).clone();
        final ModelValidationReport report = new ModelValidationReport();

        for ( final ModletValidator modletValidator
                  : this.loadModletServices( ModletValidator.class ) )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "validatingModlets", modletValidator.toString() ), null );
            }

            final ModelValidationReport current = modletValidator.validateModlets( this, cloned );

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
     * @see #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class) createServiceObjects( model, ModelProvider.class.getName(), ModelProvider.class )
     * @see ModelProvider#findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model findModel( final String model ) throws ModelException
    {
        final Model m = new Model();
        m.setIdentifier( Objects.requireNonNull( model, "model" ) );

        return this.findModel( m );
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method creates all {@code ModelProvider} service objects of the given model to populate the given model
     * instance.
     * </p>
     *
     * @see #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class) createServiceObjects( model, ModelProvider.class.getName(), ModelProvider.class )
     * @see ModelProvider#findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     *
     * @since 1.2
     */
    @Override
    public Model findModel( final Model model ) throws ModelException
    {
        Model m = Objects.requireNonNull( model, "model" ).clone();
        final long t0 = System.nanoTime();

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
            this.log( Level.FINE, getMessage( "findModelReport", m.getIdentifier(), System.nanoTime() - t0 ), null );
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
     * @see #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class) createServiceObjects( model, ModelProcessor.class.getName(), ModelProcessor.class )
     * @see ModelProcessor#processModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model processModel( final Model model ) throws ModelException
    {
        Model processed = Objects.requireNonNull( model, "model" ).clone();
        final long t0 = System.nanoTime();

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
            this.log( Level.FINE, getMessage( "processModelReport", model.getIdentifier(), System.nanoTime() - t0 ),
                      null );

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
     * @see #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class) createServiceObjects( model, ModelValidator.class.getName(), ModelValidator.class )
     * @see ModelValidator#validateModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public ModelValidationReport validateModel( final Model model ) throws ModelException
    {
        final Model cloned = Objects.requireNonNull( model, "model" ).clone();
        final long t0 = System.nanoTime();
        final ModelValidationReport resultReport = new ModelValidationReport();
        final Collection<? extends ModelValidator> modelValidators =
            this.createServiceObjects( cloned.getIdentifier(), ModelValidator.class.getName(), ModelValidator.class );

        try ( final Stream<? extends ModelValidator> st0 = modelValidators.parallelStream().unordered() )
        {
            final class ValidateModelFailure extends RuntimeException
            {

                public ValidateModelFailure( final Throwable cause )
                {
                    super( cause );
                }

                void propagate() throws ModelException
                {
                    if ( this.getCause() instanceof ModelException )
                    {
                        throw new ModelException( this.getCause().getMessage(), this.getCause() );
                    }

                    throw new AssertionError( this );
                }

            }

            try
            {
                resultReport.getDetails().addAll( st0.map( modelValidator  ->
                {
                    try
                    {
                        if ( isLoggable( Level.FINER ) )
                        {
                            log( Level.FINER, getMessage( "validatingModel", model.getIdentifier(),
                                                          modelValidator.toString() ), null );

                        }

                        return modelValidator.validateModel( DefaultModelContext.this, cloned );
                    }
                    catch ( final ModelException e )
                    {
                        throw new ValidateModelFailure( e );
                    }
                } ).flatMap( r  -> r.getDetails().parallelStream().unordered() ).
                    collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                       {
                                           l1.addAll( l2 );
                                           return l1;
                                       }, Collector.Characteristics.CONCURRENT,
                                           Collector.Characteristics.UNORDERED ) ) );

            }
            catch ( final ValidateModelFailure e )
            {
                e.propagate();
                throw new AssertionError( e );
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "validateModelReport", model.getIdentifier(),
                                              System.nanoTime() - t0 ), null );

        }

        return resultReport;
    }

    /**
     * {@inheritDoc}
     *
     * @see #createSchema(java.lang.String)
     */
    @Override
    public ModelValidationReport validateModel( final String model, final Source source ) throws ModelException
    {
        Objects.requireNonNull( model, "model" );
        Objects.requireNonNull( source, "source" );

        final long t0 = System.nanoTime();
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
            this.log( Level.FINE, getMessage( "validateModelReport", model, System.nanoTime() - t0 ), null );
        }

        return modelErrorHandler.getReport();
    }

    @Override
    public EntityResolver createEntityResolver( final String model ) throws ModelException
    {
        final Schemas schemas = getModlets().getSchemas( Objects.requireNonNull( model, "model" ) );
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

                            try ( final Stream<URI> st0 = getSchemaResources().parallelStream().unordered() )
                            {
                                final String n = schemaName;
                                final Optional<InputSource> candidate =
                                    st0.filter( uri  -> uri.getSchemeSpecificPart() != null
                                                             && uri.getSchemeSpecificPart().endsWith( n ) ).
                                        map( uri  ->
                                        {
                                            final InputSource source = new InputSource();
                                            source.setPublicId( publicId );
                                            source.setSystemId( uri.toASCIIString() );

                                            if ( isLoggable( Level.FINEST ) )
                                            {
                                                log( Level.FINEST, getMessage( "resolutionInfo", systemUri.
                                                                               toASCIIString(),
                                                                               source.getSystemId() ), null );

                                            }

                                            return source;
                                        } ).findFirst();

                                schemaSource = candidate.orElse( null );
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

                    throw new IOException( getMessage( "failedResolving", resource, message ), e );
                }

                return schemaSource;
            }

        };
    }

    @Override
    public LSResourceResolver createResourceResolver( final String model ) throws ModelException
    {
        final EntityResolver entityResolver = this.createEntityResolver( Objects.requireNonNull( model, "model" ) );
        return ( type, namespaceURI, publicId, systemId, baseURI )  ->
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

                            @Override
                            public Reader getCharacterStream()
                            {
                                return schemaSource.getCharacterStream();
                            }

                            @Override
                            public void setCharacterStream( final Reader characterStream )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setCharacterStream",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public InputStream getByteStream()
                            {
                                return schemaSource.getByteStream();
                            }

                            @Override
                            public void setByteStream( final InputStream byteStream )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setByteStream",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public String getStringData()
                            {
                                return null;
                            }

                            @Override
                            public void setStringData( final String stringData )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setStringData",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public String getSystemId()
                            {
                                return schemaSource.getSystemId();
                            }

                            @Override
                            public void setSystemId( final String systemId )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setSystemId",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public String getPublicId()
                            {
                                return schemaSource.getPublicId();
                            }

                            @Override
                            public void setPublicId( final String publicId )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setPublicId",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public String getBaseURI()
                            {
                                return baseURI;
                            }

                            @Override
                            public void setBaseURI( final String baseURI )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setBaseURI",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public String getEncoding()
                            {
                                return schemaSource.getEncoding();
                            }

                            @Override
                            public void setEncoding( final String encoding )
                            {
                                if ( isLoggable( Level.WARNING ) )
                                {
                                    log( Level.WARNING, getMessage(
                                         "unsupportedOperation", "setEncoding",
                                         DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }
                            }

                            @Override
                            public boolean getCertifiedText()
                            {
                                return false;
                            }

                            @Override
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
        };
    }

    @Override
    public javax.xml.validation.Schema createSchema( final String model ) throws ModelException
    {
        try
        {
            final long t0 = System.nanoTime();
            final Schemas schemas = this.getModlets().getSchemas( Objects.requireNonNull( model, "model" ) );
            final EntityResolver entityResolver = this.createEntityResolver( model );
            final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            final List<Source> sources = new ArrayList<>( schemas != null ? schemas.getSchema().size() : 0 );

            if ( schemas != null )
            {
                try ( final Stream<Schema> st0 = schemas.getSchema().parallelStream().unordered() )
                {
                    final class ResolveEntityFailure extends RuntimeException
                    {

                        public ResolveEntityFailure( final Throwable cause )
                        {
                            super( cause );
                        }

                        void propagate() throws IOException, SAXException
                        {
                            if ( this.getCause() instanceof IOException )
                            {
                                throw new IOException( this.getCause().getMessage(), this.getCause() );
                            }
                            else if ( this.getCause() instanceof SAXException )
                            {
                                throw new SAXException( this.getCause().getMessage(), (Exception) this.getCause() );
                            }

                            throw new AssertionError( this );
                        }

                    }

                    try
                    {
                        sources.addAll( st0.map( s  ->
                        {
                            try
                            {
                                return new SAXSource( entityResolver.resolveEntity( s.getPublicId(),
                                                                                    s.getSystemId() ) );

                            }
                            catch ( final SAXException | IOException e )
                            {
                                throw new ResolveEntityFailure( e );
                            }
                        } ).collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                               {
                                                   l1.addAll( l2 );
                                                   return l1;
                                               }, Collector.Characteristics.CONCURRENT,
                                                   Collector.Characteristics.UNORDERED ) ) );

                    }
                    catch ( final ResolveEntityFailure e )
                    {
                        e.propagate();
                        throw new AssertionError( e );
                    }
                }
            }

            if ( sources.isEmpty() )
            {
                throw new ModelException( getMessage( "missingSchemasForModel", model ) );
            }

            f.setResourceResolver( this.createResourceResolver( model ) );
            f.setErrorHandler( new ErrorHandler()
            {
                // See http://java.net/jira/browse/JAXP-66

                @Override
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

                @Override
                public void error( final SAXParseException e ) throws SAXException
                {
                    throw e;
                }

                @Override
                public void fatalError( final SAXParseException e ) throws SAXException
                {
                    throw e;
                }

            } );

            final javax.xml.validation.Schema schema = f.newSchema( sources.toArray( new Source[ sources.size() ] ) );

            if ( this.isLoggable( Level.FINE ) )
            {
                try ( final Stream<Source> st0 = sources.parallelStream().unordered() )
                {
                    this.log( Level.FINE, getMessage( "creatingSchema", st0.map( s  -> s.getSystemId() ).
                                                      collect( Collectors.joining( ", " ) ),
                                                      System.nanoTime() - t0 ), null );

                }
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

    @Override
    public JAXBContext createContext( final String model ) throws ModelException
    {
        try
        {
            String packageNames = null;
            final long t0 = System.nanoTime();
            final Schemas schemas = this.getModlets().getSchemas( Objects.requireNonNull( model, "model" ) );

            if ( schemas != null )
            {
                try ( final Stream<Schema> st0 = schemas.getSchema().parallelStream().unordered() )
                {
                    packageNames = st0.filter( s  -> s.getContextId() != null ).
                        map( s  -> s.getContextId() ).
                        collect( Collectors.joining( ":" ) );

                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                throw new ModelException( getMessage( "missingSchemasForModel", model ) );
            }

            final JAXBContext context = JAXBContext.newInstance( packageNames, this.getClassLoader() );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "creatingContext", packageNames, System.nanoTime() - t0 ), null );
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

    @Override
    public Marshaller createMarshaller( final String model ) throws ModelException
    {
        try
        {
            String packageNames = null;
            String schemaLocation = null;
            final long t0 = System.nanoTime();
            final Schemas schemas = this.getModlets().getSchemas( Objects.requireNonNull( model, "model" ) );

            if ( schemas != null )
            {
                try ( final Stream<Schema> st0 = schemas.getSchema().parallelStream().unordered() )
                {
                    packageNames = st0.filter( s  -> s.getContextId() != null ).
                        map( s  -> s.getContextId() ).collect( Collectors.joining( ":" ) );

                }

                try ( final Stream<Schema> st0 = schemas.getSchema().parallelStream().unordered() )
                {
                    schemaLocation = st0.filter( s  -> s.getPublicId() != null && s.getSystemId() != null ).
                        map( s  -> new StringBuilder( s.getPublicId() ).append( ' ' ).
                            append( s.getSystemId() ).toString() ).
                        collect( Collectors.joining( " " ) );

                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                throw new ModelException( getMessage( "missingSchemasForModel", model ) );
            }

            final Marshaller m = JAXBContext.newInstance( packageNames, this.getClassLoader() ).createMarshaller();

            if ( schemaLocation != null && schemaLocation.length() != 0 )
            {
                m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation );
            }

            final Collection<? extends Marshaller.Listener> listeners =
                this.createServiceObjects( model, MARSHALLER_LISTENER_SERVICE, Marshaller.Listener.class );

            MarshallerListenerList listenerList = null;

            if ( !listeners.isEmpty() )
            {
                listenerList = new MarshallerListenerList();
                listenerList.getListeners().addAll( listeners );
                m.setListener( listenerList );
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                if ( listenerList == null )
                {
                    this.log( Level.FINE, getMessage( "creatingMarshaller", packageNames, schemaLocation,
                                                      System.nanoTime() - t0 ), null );

                }
                else
                {
                    try ( final Stream<Marshaller.Listener> st0 =
                        listenerList.getListeners().parallelStream().unordered() )
                    {
                        final String list = st0.map( l  -> l.toString() ).collect( Collectors.joining( "," ) );
                        this.log( Level.FINE, getMessage( "creatingMarshallerWithListeners", packageNames,
                                                          schemaLocation, list, System.nanoTime() - t0 ), null );

                    }
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

    @Override
    public Unmarshaller createUnmarshaller( final String model ) throws ModelException
    {
        try
        {
            String packageNames = null;
            final long t0 = System.nanoTime();
            final Schemas schemas = this.getModlets().getSchemas( Objects.requireNonNull( model, "model" ) );

            if ( schemas != null )
            {
                try ( final Stream<Schema> st0 = schemas.getSchema().parallelStream().unordered() )
                {
                    packageNames = st0.filter( s  -> s.getContextId() != null ).
                        map( s  -> s.getContextId() ).collect( Collectors.joining( ":" ) );

                }
            }

            if ( packageNames == null || packageNames.length() == 0 )
            {
                throw new ModelException( getMessage( "missingSchemasForModel", model ) );
            }

            final Unmarshaller u = JAXBContext.newInstance( packageNames, this.getClassLoader() ).createUnmarshaller();

            UnmarshallerListenerList listenerList = null;

            final Collection<? extends Unmarshaller.Listener> listeners =
                this.createServiceObjects( model, UNMARSHALLER_LISTENER_SERVICE, Unmarshaller.Listener.class );

            if ( !listeners.isEmpty() )
            {
                listenerList = new UnmarshallerListenerList();
                listenerList.getListeners().addAll( listeners );
                u.setListener( listenerList );
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                if ( listenerList == null )
                {
                    this.log( Level.FINE, getMessage( "creatingUnmarshaller", packageNames.substring( 1 ),
                                                      System.nanoTime() - t0 ), null );

                }
                else
                {
                    try ( final Stream<Unmarshaller.Listener> st0 =
                        listenerList.getListeners().parallelStream().unordered() )
                    {
                        final String list = st0.map( l  -> l.toString() ).collect( Collectors.joining( "," ) );
                        this.log( Level.FINE, getMessage( "creatingUnmarshallerWithListeners",
                                                          packageNames, list, System.nanoTime() - t0 ), null );

                    }
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

    /**
     * {@inheritDoc}
     * <p>
     * This method loads {@code ServiceFactory} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ServiceFactory} resources to create new service objects.
     * </p>
     *
     * @since 1.9
     */
    @Override
    public <T> Collection<? extends T> createServiceObjects( final String model, final String service,
                                                             final Class<T> type )
        throws ModelException
    {
        Objects.requireNonNull( model, "model" );
        Objects.requireNonNull( service, "service" );
        Objects.requireNonNull( type, "type" );

        final Services modelServices = this.getModlets().getServices( model );
        final Collection<T> serviceObjects =
            new ArrayList<>( modelServices != null ? modelServices.getService().size() : 0 );

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
        Objects.requireNonNull( factories, "factories" );
        Objects.requireNonNull( type, "type" );
        Objects.requireNonNull( service, "service" );

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

    private <T> Collection<T> loadModletServices( final Class<T> serviceClass ) throws ModelException
    {
        try
        {
            final String serviceNamePrefix = serviceClass.getName() + ".";
            final Map<String, T> sortedPlatformServices = new TreeMap<>( ( k1, k2 )  -> k1.compareTo( k2 ) );
            final File platformServices = new File( this.getPlatformProviderLocation() );

            if ( platformServices.exists() )
            {
                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", platformServices.getAbsolutePath() ), null );
                }

                final java.util.Properties p = new java.util.Properties();

                try ( final InputStream in = new FileInputStream( platformServices ) )
                {
                    p.load( in );
                }

                try ( final Stream<Map.Entry<Object, Object>> st0 = p.entrySet().parallelStream().unordered() )
                {
                    final class CreateModletServiceObjectFailure extends RuntimeException
                    {

                        public CreateModletServiceObjectFailure( final Throwable cause )
                        {
                            super( cause );
                        }

                        void propagate() throws ModelException
                        {
                            if ( this.getCause() instanceof ModelException )
                            {
                                throw new ModelException( this.getCause().getMessage(), this.getCause() );
                            }

                            throw new AssertionError( this );
                        }

                    }

                    final class CreateModletServiceObjectResult<ST>
                    {

                        String serviceKey;

                        ST serviceObject;

                    }

                    try
                    {
                        sortedPlatformServices.putAll(
                            st0.filter( e  -> e.getKey().toString().startsWith( serviceNamePrefix ) ).
                                map( e  ->
                                {
                                    try
                                    {
                                        final CreateModletServiceObjectResult<T> r =
                                            new CreateModletServiceObjectResult<>();

                                        final String configuration = e.getValue().toString();

                                        if ( isLoggable( Level.FINEST ) )
                                        {
                                            log( Level.FINEST,
                                                 getMessage( "serviceInfo", platformServices.getAbsolutePath(),
                                                             serviceClass.getName(), configuration ), null );

                                        }

                                        r.serviceKey = e.getKey().toString();
                                        r.serviceObject = this.createModletServiceObject( serviceClass, configuration );
                                        return r;
                                    }
                                    catch ( final ModelException ex )
                                    {
                                        throw new CreateModletServiceObjectFailure( ex );
                                    }
                                } ).collect( Collectors.toMap( r  -> r.serviceKey, r  -> r.serviceObject ) ) );
                    }
                    catch ( final CreateModletServiceObjectFailure e )
                    {
                        e.propagate();
                        throw new AssertionError( e );
                    }
                }
            }

            final Enumeration<URL> classpathServices =
                this.findResources( this.getProviderLocation() + '/' + serviceClass.getName() );

            int count = 0;
            final long t0 = System.nanoTime();
            final List<T> sortedClasspathServices = new LinkedList<>();

            while ( classpathServices.hasMoreElements() )
            {
                count++;
                final URL url = classpathServices.nextElement();

                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", url.toExternalForm() ), null );
                }

                try ( final BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(),
                                                                                               "UTF-8" ) );
                      final Stream<String> st0 = reader.lines().parallel().unordered() )
                {
                    final class CreateModletServiceObjectFailure extends RuntimeException
                    {

                        public CreateModletServiceObjectFailure( final Throwable cause )
                        {
                            super( cause );
                        }

                        void propagate() throws ModelException
                        {
                            if ( this.getCause() instanceof ModelException )
                            {
                                throw new ModelException( this.getCause().getMessage(), this.getCause() );
                            }

                            throw new AssertionError( this );
                        }

                    }

                    try
                    {
                        sortedClasspathServices.addAll(
                            st0.filter( l  -> !l.contains( "#" ) ).
                                map( l  ->
                                {
                                    try
                                    {
                                        if ( isLoggable( Level.FINEST ) )
                                        {
                                            log( Level.FINEST, getMessage( "serviceInfo", url.toExternalForm(),
                                                                           serviceClass.getName(), l ), null );

                                        }

                                        return this.createModletServiceObject( serviceClass, l );
                                    }
                                    catch ( final ModelException e )
                                    {
                                        throw new CreateModletServiceObjectFailure( e );
                                    }
                                } ).collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                                       {
                                                           l1.addAll( l2 );
                                                           return l1;
                                                       }, Collector.Characteristics.CONCURRENT,
                                                           Collector.Characteristics.UNORDERED ) ) );

                        Collections.sort( sortedClasspathServices, ( o1, o2 )  -> ordinalOf( o1 ) - ordinalOf( o2 ) );
                    }
                    catch ( final CreateModletServiceObjectFailure e )
                    {
                        e.propagate();
                        throw new AssertionError( e );
                    }
                }
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "contextReport", count,
                                                  this.getProviderLocation() + '/' + serviceClass.getName(),
                                                  System.nanoTime() - t0 ), null );

            }

            final List<T> services =
                new ArrayList<>( sortedPlatformServices.size() + sortedClasspathServices.size() );

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
        final Set<URI> resources = new HashSet<>();
        final long t0 = System.nanoTime();
        int count = 0;

        for ( final Enumeration<URL> e = this.findResources( "META-INF/MANIFEST.MF" ); e.hasMoreElements(); )
        {
            count++;
            final URL manifestUrl = e.nextElement();
            final String externalForm = manifestUrl.toExternalForm();
            final String baseUrl = externalForm.substring( 0, externalForm.indexOf( "META-INF" ) );

            try ( final InputStream manifestStream = manifestUrl.openStream() )
            {
                final Manifest mf = new Manifest( manifestStream );

                if ( this.isLoggable( Level.FINEST ) )
                {
                    this.log( Level.FINEST, getMessage( "processing", externalForm ), null );
                }

                try ( final Stream<Map.Entry<String, Attributes>> st0 =
                    mf.getEntries().entrySet().parallelStream().unordered() )
                {
                    final class CreateUriFailure extends RuntimeException
                    {

                        public CreateUriFailure( final Throwable cause )
                        {
                            super( cause );
                        }

                        void propagate() throws MalformedURLException, URISyntaxException
                        {
                            if ( this.getCause() instanceof MalformedURLException )
                            {
                                throw (MalformedURLException) new MalformedURLException( this.getCause().getMessage() ).
                                    initCause( this.getCause() );

                            }
                            else if ( this.getCause() instanceof URISyntaxException )
                            {
                                throw (URISyntaxException) new URISyntaxException(
                                    ( (URISyntaxException) this.getCause() ).getInput(),
                                    ( (URISyntaxException) this.getCause() ).getReason(),
                                    ( (URISyntaxException) this.getCause() ).getIndex() ).
                                    initCause( this.getCause() );

                            }

                            throw new AssertionError( this );
                        }

                    }

                    final Function<String, String> parallelToLowerCase = ( s  ->
                    {
                        final char[] chars = s.toCharArray();

                        try ( final IntStream st1 = IntStream.range( 0, chars.length ).parallel().unordered() )
                        {
                            st1.forEach( idx  ->
                            {
                                chars[idx] = Character.toLowerCase( chars[idx] );
                            } );
                        }

                        return String.valueOf( chars );
                    } );

                    try
                    {
                        st0.filter( entry  ->
                        {
                            try ( final Stream<String> st2 =
                                Arrays.asList( SCHEMA_EXTENSIONS ).parallelStream().unordered() )
                            {
                                final boolean found = st2.filter( ext  -> parallelToLowerCase.apply( entry.getKey() ).
                                    endsWith( '.' + parallelToLowerCase.apply( ext ) ) ).findAny().isPresent();

                                if ( found && isLoggable( Level.FINEST ) )
                                {
                                    log( Level.FINEST, getMessage( "foundSchemaCandidate", entry.getKey() ), null );
                                }

                                return found;
                            }
                        } ).map( entry  ->
                        {
                            try
                            {
                                return new URL( baseUrl + entry.getKey() ).toURI();
                            }
                            catch ( final MalformedURLException | URISyntaxException ex )
                            {
                                throw new CreateUriFailure( ex );
                            }
                        } ).collect( Collector.of( CopyOnWriteArraySet::new, Set::add, ( s1, s2 )  ->
                                               {
                                                   s1.addAll( s2 );
                                                   return s2;
                                               }, Collector.Characteristics.CONCURRENT,
                                                   Collector.Characteristics.UNORDERED ) );

                    }
                    catch ( final CreateUriFailure ex )
                    {
                        ex.propagate();
                        throw new AssertionError( ex );
                    }
                }
            }
        }

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "contextReport", count, "META-INF/MANIFEST.MF", System.nanoTime() - t0 ),
                      null );

        }

        return resources;
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
    private final ModelContext context;

    /**
     * The report of the instance.
     */
    private final ModelValidationReport report;

    /**
     * Creates a new {@code ModelErrorHandler} instance taking a context.
     *
     * @param context The context of the instance.
     */
    ModelErrorHandler( final ModelContext context )
    {
        this( context, new ModelValidationReport() );
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
    private final List<Marshaller.Listener> listeners = new CopyOnWriteArrayList<Marshaller.Listener>();

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
        return this.listeners;
    }

    @Override
    public void beforeMarshal( final Object source )
    {
        try ( final Stream<Marshaller.Listener> st0 = this.getListeners().parallelStream().unordered() )
        {
            st0.forEach( ( l )  -> l.beforeMarshal( source ) );
        }
    }

    @Override
    public void afterMarshal( final Object source )
    {
        try ( final Stream<Marshaller.Listener> st0 = this.getListeners().parallelStream().unordered() )
        {
            st0.forEach( ( l )  -> l.afterMarshal( source ) );
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
    private final List<Unmarshaller.Listener> listeners = new CopyOnWriteArrayList<Unmarshaller.Listener>();

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
        return this.listeners;
    }

    @Override
    public void beforeUnmarshal( final Object target, final Object parent )
    {
        try ( final Stream<Unmarshaller.Listener> st0 = this.getListeners().parallelStream().unordered() )
        {
            st0.forEach( ( l )  -> l.beforeUnmarshal( target, parent ) );
        }
    }

    @Override
    public void afterUnmarshal( final Object target, final Object parent )
    {
        try ( final Stream<Unmarshaller.Listener> st0 = this.getListeners().parallelStream().unordered() )
        {
            st0.forEach( ( l )  -> l.afterUnmarshal( target, parent ) );
        }
    }

}
