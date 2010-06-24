/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
 *   THIS SOFTWARE IS PROVIDED BY THE JOMC PROJECT AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JOMC PROJECT OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $Id$
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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default {@code ModelContext} implementation.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 * @see ModelContext#createModelContext(java.lang.ClassLoader)
 */
public class DefaultModelContext extends ModelContext
{

    /** Supported schema name extensions. */
    private static final String[] SCHEMA_EXTENSIONS = new String[]
    {
        "xsd"
    };

    /**
     * Classpath location searched for providers by default.
     * @see #getDefaultProviderLocation()
     */
    private static final String DEFAULT_PROVIDER_LOCATION = "META-INF/services";

    /**
     * Location searched for platform providers by default.
     * @see #getDefaultPlatformProviderLocation()
     */
    private static final String DEFAULT_PLATFORM_PROVIDER_LOCATION =
        new StringBuilder().append( System.getProperty( "java.home" ) ).
        append( File.separator ).append( "jre" ).append( File.separator ).append( "lib" ).
        append( File.separator ).append( "jomc.properties" ).toString();

    /** Default provider location. */
    private static volatile String defaultProviderLocation;

    /** Default platform provider location. */
    private static volatile String defaultPlatformProviderLocation;

    /** Cached schema resources. */
    private Reference<Set<URI>> cachedSchemaResources = new SoftReference<Set<URI>>( null );

    /** Provider location of the instance. */
    private String providerLocation;

    /** Platform provider location of the instance. */
    private String platformProviderLocation;

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
     * <p>The default provider location is controlled by system property
     * {@code org.jomc.modlet.DefaultModelContext.defaultProviderLocation} holding the location to search
     * for provider resources by default. If that property is not set, the {@code META-INF/services} default is
     * returned.</p>
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
     */
    public String getProviderLocation()
    {
        if ( this.providerLocation == null )
        {
            this.providerLocation = getDefaultProviderLocation();
            this.log( Level.CONFIG, getMessage( "defaultProviderLocationInfo", this.getClass().getName(),
                                                this.providerLocation ), null );

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
    public void setProviderLocation( final String value )
    {
        this.providerLocation = value;
    }

    /**
     * Gets the default location searched for platform provider resources.
     * <p>The default platform provider location is controlled by system property
     * {@code org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation} holding the location to
     * search for platform provider resources by default. If that property is not set, the
     * {@code <java-home>/jre/lib/jomc.properties} default is returned.</p>
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
     */
    public String getPlatformProviderLocation()
    {
        if ( this.platformProviderLocation == null )
        {
            this.platformProviderLocation = getDefaultPlatformProviderLocation();
            this.log( Level.CONFIG, getMessage( "defaultPlatformProviderLocationInfo", this.getClass().getName(),
                                                this.platformProviderLocation ), null );

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
    public void setPlatformProviderLocation( final String value )
    {
        this.platformProviderLocation = value;
    }

    /**
     * {@inheritDoc}
     * <p>This method loads {@code ModletProvider} classes setup via the platform provider configuration file and
     * {@code <provider-location>/org.jomc.modlet.ModletProvider} resources to return a list of {@code Modlets}.</p>
     *
     * @see #getProviderLocation()
     * @see #getPlatformProviderLocation()
     * @see ModletProvider#findModlets(org.jomc.modlet.ModelContext)
     */
    @Override
    public Modlets findModlets() throws ModelException
    {
        try
        {
            final long t0 = System.currentTimeMillis();
            final Modlets modlets = new Modlets();
            final Collection<Class<? extends ModletProvider>> providers = this.loadProviders( ModletProvider.class );

            for ( Class<? extends ModletProvider> provider : providers )
            {
                final ModletProvider modletProvider = provider.newInstance();
                final Modlets provided = modletProvider.findModlets( this );
                if ( provided != null )
                {
                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        for ( Modlet m : provided.getModlet() )
                        {
                            this.log( Level.CONFIG, getMessage(
                                "modletInfo", this.getClass().getName(), modletProvider.getClass().getName(),
                                m.getName(), m.getModel(),
                                m.getVendor() != null ? m.getVendor() : getMessage( "noVendor" ),
                                m.getVersion() != null ? m.getVersion() : getMessage( "noVersion" ) ), null );

                            if ( this.isLoggable( Level.FINE ) )
                            {
                                if ( m.getSchemas() != null )
                                {
                                    for ( Schema s : m.getSchemas().getSchema() )
                                    {
                                        this.log( Level.FINE, getMessage( "modletSchemaInfo", this.getClass().getName(),
                                                                          m.getName(), s.getPublicId(), s.getSystemId(),
                                                                          s.getContextId() != null
                                                                          ? s.getContextId()
                                                                          : getMessage( "noContext" ),
                                                                          s.getClasspathId() != null
                                                                          ? s.getClasspathId()
                                                                          : getMessage( "noClasspathId" ) ), null );

                                    }
                                }

                                if ( m.getServices() != null )
                                {
                                    for ( Service s : m.getServices().getService() )
                                    {
                                        this.log( Level.FINE, getMessage( "modletServiceInfo", this.getClass().getName(),
                                                                          m.getName(), s.getOrdinal(), s.getIdentifier(),
                                                                          s.getClazz() ), null );

                                    }
                                }
                            }
                        }
                    }

                    modlets.getModlet().addAll( provided.getModlet() );
                }
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "modletReport", this.getClass().getName(), modlets.getModlet().size(),
                                                  System.currentTimeMillis() - t0 ), null );

            }

            return modlets;
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    /**
     * {@inheritDoc}
     * <p>This method loads all {@code ModelProvider} services of the model identified by {@code model} to create a new
     * {@code Model} instance.</p>
     *
     * @see #getModlets()
     * @see ModelProvider#findModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model findModel( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            Model m = new Model();
            m.setIdentifier( model );

            final Services services = this.getModlets().getServices( model );
            if ( services != null )
            {
                for ( Service provider : services.getServices( ModelProvider.class ) )
                {
                    final Class<?> clazz = this.findClass( provider.getClazz() );

                    if ( clazz == null )
                    {
                        throw new ModelException( getMessage( "serviceNotFound", provider.getOrdinal(),
                                                              provider.getIdentifier(), provider.getClazz() ) );

                    }

                    if ( !ModelProvider.class.isAssignableFrom( clazz ) )
                    {
                        throw new ModelException( getMessage( "illegalService", provider.getOrdinal(),
                                                              provider.getIdentifier(), provider.getClazz(),
                                                              ModelProvider.class.getName() ) );

                    }

                    final Class<? extends ModelProvider> modelProviderClass = clazz.asSubclass( ModelProvider.class );
                    final ModelProvider modelProvider = modelProviderClass.newInstance();
                    final Model provided = modelProvider.findModel( this, m );
                    if ( provided != null )
                    {
                        m = provided;
                    }
                }
            }

            return m;
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    @Override
    public EntityResolver createEntityResolver( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        return new DefaultHandler()
        {

            @Override
            public InputSource resolveEntity( final String publicId, final String systemId )
                throws SAXException, IOException
            {
                if ( systemId == null )
                {
                    throw new NullPointerException( "systemId" );
                }

                InputSource schemaSource = null;

                try
                {
                    Schema s = null;
                    final Schemas classpathSchemas = getModlets().getSchemas( model );

                    if ( classpathSchemas != null )
                    {
                        if ( publicId != null )
                        {
                            s = classpathSchemas.getSchemaByPublicId( publicId );
                        }
                        if ( s == null )
                        {
                            s = classpathSchemas.getSchemaBySystemId( systemId );
                        }
                    }

                    if ( s != null )
                    {
                        schemaSource = new InputSource();
                        schemaSource.setPublicId( s.getPublicId() != null ? s.getPublicId() : publicId );
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

                        if ( isLoggable( Level.FINE ) )
                        {
                            log( Level.FINE, getMessage(
                                "resolutionInfo", publicId + ", " + systemId,
                                schemaSource.getPublicId() + ", " + schemaSource.getSystemId() ), null );

                        }
                    }

                    if ( schemaSource == null )
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

                            for ( URI uri : getSchemaResources() )
                            {
                                if ( uri.getPath().endsWith( schemaName ) )
                                {
                                    schemaSource = new InputSource();
                                    schemaSource.setPublicId( publicId );
                                    schemaSource.setSystemId( uri.toASCIIString() );

                                    if ( isLoggable( Level.FINE ) )
                                    {
                                        log( Level.FINE, getMessage( "resolutionInfo", systemUri.toASCIIString(),
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
                                log( Level.WARNING, getMessage( "unsupportedSystemIdUri", systemId,
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
                        log( Level.WARNING, getMessage( "unsupportedSystemIdUri", systemId, e.getMessage() ), null );
                    }

                    schemaSource = null;
                }
                catch ( final ModelException e )
                {
                    throw (IOException) new IOException( getMessage(
                        "failedResolving", publicId, systemId, e.getMessage() ) ).initCause( e );

                }

                return schemaSource;
            }

        };
    }

    @Override
    public LSResourceResolver createResourceResolver( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
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
                            createEntityResolver( model ).resolveEntity( resolvePublicId, resolveSystemId );

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
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setCharacterStream",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public InputStream getByteStream()
                                {
                                    return schemaSource.getByteStream();
                                }

                                public void setByteStream( final InputStream byteStream )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setByteStream",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public String getStringData()
                                {
                                    return null;
                                }

                                public void setStringData( final String stringData )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setStringData",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public String getSystemId()
                                {
                                    return schemaSource.getSystemId();
                                }

                                public void setSystemId( final String systemId )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setSystemId",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public String getPublicId()
                                {
                                    return schemaSource.getPublicId();
                                }

                                public void setPublicId( final String publicId )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setPublicId",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public String getBaseURI()
                                {
                                    return baseURI;
                                }

                                public void setBaseURI( final String baseURI )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setBaseURI",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public String getEncoding()
                                {
                                    return schemaSource.getEncoding();
                                }

                                public void setEncoding( final String encoding )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setEncoding",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

                                }

                                public boolean getCertifiedText()
                                {
                                    return false;
                                }

                                public void setCertifiedText( final boolean certifiedText )
                                {
                                    log( Level.WARNING, getMessage(
                                        "unsupportedOperation", "setCertifiedText",
                                        DefaultModelContext.class.getName() + ".LSResourceResolver" ), null );

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
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", resolvePublicId, resolveSystemId,
                                                       e.getMessage() ), e );

                    }
                }
                catch ( final IOException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", resolvePublicId, resolveSystemId,
                                                       e.getMessage() ), e );

                    }
                }
                catch ( final ModelException e )
                {
                    if ( isLoggable( Level.SEVERE ) )
                    {
                        log( Level.SEVERE, getMessage( "failedResolving", resolvePublicId, resolveSystemId,
                                                       e.getMessage() ), e );

                    }
                }

                return null;
            }

        };

    }

    @Override
    public javax.xml.validation.Schema createSchema( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            final SchemaFactory f = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            final Schemas schemas = this.getModlets().getSchemas( model );
            final EntityResolver entityResolver = this.createEntityResolver( model );
            final List<Source> sources = new ArrayList<Source>( schemas != null ? schemas.getSchema().size() : 0 );

            if ( schemas != null )
            {
                for ( Schema s : schemas.getSchema() )
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
                throw new ModelException( getMessage( "missingSchemas", model ) );
            }

            f.setResourceResolver( this.createResourceResolver( model ) );
            return f.newSchema( sources.toArray( new Source[ sources.size() ] ) );
        }
        catch ( final IOException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final SAXException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    @Override
    public JAXBContext createContext( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            final StringBuilder packageNames = new StringBuilder();
            final Schemas schemas = this.getModlets().getSchemas( model );

            if ( schemas != null )
            {
                for ( final Iterator<Schema> s = schemas.getSchema().iterator(); s.hasNext(); )
                {
                    final Schema schema = s.next();
                    if ( schema.getContextId() != null )
                    {
                        packageNames.append( ':' ).append( schema.getContextId() );
                    }
                }
            }

            if ( packageNames.length() == 0 )
            {
                throw new ModelException( getMessage( "missingSchemas", model ) );
            }

            return JAXBContext.newInstance( packageNames.toString().substring( 1 ), this.getClassLoader() );
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModelException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModelException( e.getMessage(), e );
            }
        }
    }

    @Override
    public Marshaller createMarshaller( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            final StringBuilder packageNames = new StringBuilder();
            final StringBuilder schemaLocation = new StringBuilder();
            final Schemas schemas = this.getModlets().getSchemas( model );

            if ( schemas != null )
            {
                for ( final Iterator<Schema> s = schemas.getSchema().iterator(); s.hasNext(); )
                {
                    final Schema schema = s.next();

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

            if ( packageNames.length() == 0 )
            {
                throw new ModelException( getMessage( "missingSchemas", model ) );
            }

            final Marshaller m =
                JAXBContext.newInstance( packageNames.toString().substring( 1 ), this.getClassLoader() ).
                createMarshaller();

            if ( schemaLocation.length() != 0 )
            {
                m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation.toString().substring( 1 ) );
            }

            return m;
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModelException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModelException( e.getMessage(), e );
            }
        }
    }

    @Override
    public Unmarshaller createUnmarshaller( final String model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            return this.createContext( model ).createUnmarshaller();
        }
        catch ( final JAXBException e )
        {
            if ( e.getLinkedException() != null )
            {
                throw new ModelException( e.getLinkedException().getMessage(), e.getLinkedException() );
            }
            else
            {
                throw new ModelException( e.getMessage(), e );
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This method loads all {@code ModelProcessor} services of {@code model} to process the given
     * {@code Model}.</p>
     *
     * @see #getModlets()
     * @see ModelProcessor#processModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public Model processModel( final Model model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            Model processed = model;
            final Services services = this.getModlets().getServices( model.getIdentifier() );
            final List<Service> processors = services != null ? services.getServices( ModelProcessor.class ) : null;

            if ( processors != null )
            {
                for ( Service processor : processors )
                {
                    final Class<?> clazz = this.findClass( processor.getClazz() );

                    if ( clazz == null )
                    {
                        throw new ModelException( getMessage( "serviceNotFound", processor.getOrdinal(),
                                                              processor.getIdentifier(), processor.getClazz() ) );

                    }

                    if ( !ModelProcessor.class.isAssignableFrom( clazz ) )
                    {
                        throw new ModelException( getMessage( "illegalService", processor.getOrdinal(),
                                                              processor.getIdentifier(), processor.getClazz(),
                                                              ModelProcessor.class.getName() ) );

                    }

                    final Class<? extends ModelProcessor> modelProcessorClass = clazz.asSubclass( ModelProcessor.class );
                    final ModelProcessor modelProcessor = modelProcessorClass.newInstance();
                    final Model current = modelProcessor.processModel( this, processed );
                    if ( current != null )
                    {
                        processed = current;
                    }
                }
            }

            return processed;
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    /**
     * {@inheritDoc}
     * <p>This method loads all {@code ModelValidator} services of {@code model} to validate the given
     * {@code Model}.</p>
     *
     * @see #getModlets()
     * @see ModelValidator#validateModel(org.jomc.modlet.ModelContext, org.jomc.modlet.Model)
     */
    @Override
    public ModelValidationReport validateModel( final Model model ) throws ModelException
    {
        if ( model == null )
        {
            throw new NullPointerException( "model" );
        }

        try
        {
            final Services services = this.getModlets().getServices( model.getIdentifier() );
            final List<Service> validators = services != null ? services.getServices( ModelValidator.class ) : null;
            final ModelValidationReport report = new ModelValidationReport();

            if ( validators != null )
            {
                for ( Service validator : validators )
                {
                    final Class<?> clazz = this.findClass( validator.getClazz() );

                    if ( clazz == null )
                    {
                        throw new ModelException( getMessage( "serviceNotFound", validator.getOrdinal(),
                                                              validator.getIdentifier(), validator.getClazz() ) );

                    }

                    if ( !ModelValidator.class.isAssignableFrom( clazz ) )
                    {
                        throw new ModelException( getMessage( "illegalService", validator.getOrdinal(),
                                                              validator.getIdentifier(), validator.getClazz(),
                                                              ModelValidator.class.getName() ) );

                    }

                    final Class<? extends ModelValidator> modelValidatorClass = clazz.asSubclass( ModelValidator.class );
                    final ModelValidator modelValidator = modelValidatorClass.newInstance();
                    final ModelValidationReport current = modelValidator.validateModel( this, model );
                    if ( current != null )
                    {
                        report.getDetails().addAll( current.getDetails() );
                    }
                }
            }

            return report;
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
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
            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, e.getMessage(), e );
            }

            if ( modelErrorHandler.getReport().isModelValid() )
            {
                throw new ModelException( e.getMessage(), e );
            }
        }
        catch ( final IOException e )
        {
            throw new ModelException( e.getMessage(), e );
        }

        return modelErrorHandler.getReport();
    }

    private <T> Collection<Class<? extends T>> loadProviders( final Class<T> providerClass ) throws ModelException
    {
        try
        {
            final String providerNamePrefix = providerClass.getName() + ".";
            final Map<String, Class<? extends T>> providers =
                new TreeMap<String, Class<? extends T>>( new Comparator<String>()
            {

                public int compare( final String key1, final String key2 )
                {
                    return key1.compareTo( key2 );
                }

            } );

            final File platformProviders = new File( this.getPlatformProviderLocation() );

            if ( platformProviders.exists() )
            {
                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, getMessage( "processing", this.getClass().getName(),
                                                      platformProviders.getAbsolutePath() ), null );

                }

                InputStream in = null;
                final java.util.Properties p = new java.util.Properties();

                try
                {
                    in = new FileInputStream( platformProviders );
                    p.load( in );
                }
                finally
                {
                    if ( in != null )
                    {
                        in.close();
                    }
                }

                for ( Map.Entry e : p.entrySet() )
                {
                    if ( e.getKey().toString().startsWith( providerNamePrefix ) )
                    {
                        final Class<?> provider = this.findClass( e.getValue().toString() );

                        if ( provider == null )
                        {
                            throw new ModelException( getMessage( "implementationNotFound", providerClass.getName(),
                                                                  e.getValue().toString(),
                                                                  platformProviders.getAbsolutePath() ) );

                        }

                        if ( !providerClass.isAssignableFrom( provider ) )
                        {
                            throw new ModelException( getMessage( "illegalImplementation", providerClass.getName(),
                                                                  e.getValue().toString(),
                                                                  platformProviders.getAbsolutePath() ) );

                        }

                        if ( this.isLoggable( Level.CONFIG ) )
                        {
                            this.log( Level.CONFIG, getMessage( "providerInfo", this.getClass().getName(),
                                                                platformProviders.getAbsolutePath(),
                                                                providerClass.getName(), provider.getName() ), null );

                        }

                        providers.put( e.getKey().toString(), provider.asSubclass( providerClass ) );
                    }
                }
            }

            final Enumeration<URL> classpathProviders =
                this.findResources( this.getProviderLocation() + '/' + providerClass.getName() );

            while ( classpathProviders.hasMoreElements() )
            {
                final URL url = classpathProviders.nextElement();

                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, getMessage( "processing", this.getClass().getName(),
                                                      url.toExternalForm() ), null );

                }

                final BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) );

                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( line.contains( "#" ) )
                    {
                        continue;
                    }

                    final Class<?> provider = this.findClass( line );

                    if ( provider == null )
                    {
                        throw new ModelException( getMessage(
                            "implementationNotFound", providerClass.getName(), line, url.toExternalForm() ) );

                    }

                    if ( !providerClass.isAssignableFrom( provider ) )
                    {
                        throw new ModelException( getMessage(
                            "illegalImplementation", providerClass.getName(), line, url.toExternalForm() ) );

                    }

                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "providerInfo", this.getClass().getName(),
                                                            url.toExternalForm(), providerClass.getName(),
                                                            provider.getName() ), null );

                    }

                    providers.put( providerNamePrefix + providers.size(), provider.asSubclass( providerClass ) );
                }

                reader.close();
            }

            return providers.values();
        }
        catch ( final IOException e )
        {
            throw new ModelException( e.getMessage(), e );
        }
    }

    /**
     * Searches the context for {@code META-INF/MANIFEST.MF} resources and returns a set of URIs of entries whose names
     * end with a known schema extension.
     *
     * @return Set of URIs of any matching entries.
     *
     * @throws IOException if reading fails.
     * @throws URISyntaxException if parsing fails.
     */
    private Set<URI> getSchemaResources() throws IOException, URISyntaxException
    {
        Set<URI> resources = this.cachedSchemaResources.get();

        if ( resources == null )
        {
            resources = new HashSet<URI>();
            final long t0 = System.currentTimeMillis();
            int count = 0;

            for ( final Enumeration<URL> e = this.getClassLoader().getResources( "META-INF/MANIFEST.MF" );
                  e.hasMoreElements(); )
            {
                count++;
                final URL manifestUrl = e.nextElement();
                final String externalForm = manifestUrl.toExternalForm();
                final String baseUrl = externalForm.substring( 0, externalForm.indexOf( "META-INF" ) );
                final InputStream manifestStream = manifestUrl.openStream();
                final Manifest mf = new Manifest( manifestStream );
                manifestStream.close();

                if ( this.isLoggable( Level.FINE ) )
                {
                    this.log( Level.FINE, getMessage( "processing", this.getClass().getName(), externalForm ), null );
                }

                for ( Map.Entry<String, Attributes> entry : mf.getEntries().entrySet() )
                {
                    for ( int i = SCHEMA_EXTENSIONS.length - 1; i >= 0; i-- )
                    {
                        if ( entry.getKey().toLowerCase().endsWith( '.' + SCHEMA_EXTENSIONS[i].toLowerCase() ) )
                        {
                            final URL schemaUrl = new URL( baseUrl + entry.getKey() );
                            resources.add( schemaUrl.toURI() );

                            if ( this.isLoggable( Level.CONFIG ) )
                            {
                                this.log( Level.CONFIG, getMessage( "foundSchemaCandidate", this.getClass().getName(),
                                                                    schemaUrl.toExternalForm() ), null );

                            }
                        }
                    }
                }
            }

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "contextReport", this.getClass().getName(), count,
                                                  "META-INF/MANIFEST.MF",
                                                  Long.valueOf( System.currentTimeMillis() - t0 ) ), null );

            }

            this.cachedSchemaResources = new SoftReference<Set<URI>>( resources );
        }

        return resources;
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModelContext.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

    // SECTION-END
}

/**
 * {@code ErrorHandler} collecting {@code ModelValidationReport} details.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $Id$
 */
class ModelErrorHandler extends DefaultHandler
{

    /** The context of the instance. */
    private ModelContext context;

    /** The report of the instance. */
    private ModelValidationReport report;

    /**
     * Creates a new {@code ModelErrorHandler} instance taking a context.
     *
     * @param context The context of the instance.
     */
    public ModelErrorHandler( final ModelContext context )
    {
        this( context, null );
    }

    /**
     * Creates a new {@code ModelErrorHandler} instance taking a report to use for collecting validation events.
     *
     * @param context The context of the instance.
     * @param report A report to use for collecting validation events.
     */
    public ModelErrorHandler( final ModelContext context, final ModelValidationReport report )
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
        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, exception.getMessage(), exception );
        }

        String message = exception.getMessage();
        if ( message == null && exception.getException() != null )
        {
            message = exception.getException().getMessage();
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Warning condition", Level.WARNING, message, null ) );

    }

    @Override
    public void error( final SAXParseException exception ) throws SAXException
    {
        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, exception.getMessage(), exception );
        }

        String message = exception.getMessage();
        if ( message == null && exception.getException() != null )
        {
            message = exception.getException().getMessage();
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Section 1.2 - Error", Level.SEVERE, message, null ) );

    }

    @Override
    public void fatalError( final SAXParseException exception ) throws SAXException
    {
        if ( this.context != null && this.context.isLoggable( Level.FINE ) )
        {
            this.context.log( Level.FINE, exception.getMessage(), exception );
        }

        String message = exception.getMessage();
        if ( message == null && exception.getException() != null )
        {
            message = exception.getException().getMessage();
        }

        this.getReport().getDetails().add( new ModelValidationReport.Detail(
            "W3C XML 1.0 Recommendation - Section 1.2 - Fatal Error", Level.SEVERE, message, null ) );

    }

}
