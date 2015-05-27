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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 * Model context interface.
 * <p>
 * <b>Use Cases:</b><br/><ul>
 * <li>{@link #createContext(java.lang.String) }</li>
 * <li>{@link #createEntityResolver(java.lang.String) }</li>
 * <li>{@link #createMarshaller(java.lang.String) }</li>
 * <li>{@link #createResourceResolver(java.lang.String) }</li>
 * <li>{@link #createSchema(java.lang.String) }</li>
 * <li>{@link #createUnmarshaller(java.lang.String) }</li>
 * <li>{@link #findModel(java.lang.String) }</li>
 * <li>{@link #findModel(org.jomc.modlet.Model) }</li>
 * <li>{@link #processModel(org.jomc.modlet.Model) }</li>
 * <li>{@link #validateModel(org.jomc.modlet.Model) }</li>
 * <li>{@link #validateModel(java.lang.String, javax.xml.transform.Source) }</li>
 * </ul>
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 *
 * @see ModelContextFactory
 */
public abstract class ModelContext
{

    /**
     * Listener interface.
     */
    public abstract static class Listener
    {

        /**
         * Creates a new {@code Listener} instance.
         */
        public Listener()
        {
            super();
        }

        /**
         * Gets called on logging.
         *
         * @param level The level of the event.
         * @param message The message of the event or {@code null}.
         * @param t The throwable of the event or {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        public void onLog( final Level level, final String message, final Throwable t )
        {
            if ( level == null )
            {
                throw new NullPointerException( "level" );
            }
        }

    }

    /**
     * Default {@code http://jomc.org/modlet} namespace schema system id.
     *
     * @see #getDefaultModletSchemaSystemId()
     */
    private static final String DEFAULT_MODLET_SCHEMA_SYSTEM_ID =
        "http://xml.jomc.org/modlet/jomc-modlet-1.9.xsd";

    /**
     * Log level events are logged at by default.
     *
     * @see #getDefaultLogLevel()
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

    /**
     * Default log level.
     */
    private static volatile Level defaultLogLevel;

    /**
     * Default {@code http://jomc.org/model/modlet} namespace schema system id.
     */
    private static volatile String defaultModletSchemaSystemId;

    /**
     * Class name of the {@code ModelContext} implementation.
     */
    @Deprecated
    private static volatile String modelContextClassName;

    /**
     * The attributes of the instance.
     */
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * The class loader of the instance.
     */
    private ClassLoader classLoader;

    /**
     * Flag indicating the {@code classLoader} field is initialized.
     *
     * @since 1.2
     */
    private boolean classLoaderSet;

    /**
     * The listeners of the instance.
     */
    private List<Listener> listeners;

    /**
     * Log level of the instance.
     */
    private Level logLevel;

    /**
     * The {@code Modlets} of the instance.
     */
    private Modlets modlets;

    /**
     * Modlet namespace schema system id of the instance.
     */
    private String modletSchemaSystemId;

    /**
     * Creates a new {@code ModelContext} instance.
     *
     * @since 1.2
     */
    public ModelContext()
    {
        super();
        this.classLoader = null;
        this.classLoaderSet = false;
    }

    /**
     * Creates a new {@code ModelContext} instance taking a class loader.
     *
     * @param classLoader The class loader of the context.
     *
     * @see #getClassLoader()
     */
    public ModelContext( final ClassLoader classLoader )
    {
        super();
        this.classLoader = classLoader;
        this.classLoaderSet = true;
    }

    /**
     * Gets a set holding the names of all attributes of the context.
     *
     * @return An unmodifiable set holding the names of all attributes of the context.
     *
     * @see #clearAttribute(java.lang.String)
     * @see #getAttribute(java.lang.String)
     * @see #getAttribute(java.lang.String, java.lang.Object)
     * @see #setAttribute(java.lang.String, java.lang.Object)
     * @since 1.2
     */
    public Set<String> getAttributeNames()
    {
        return Collections.unmodifiableSet( this.attributes.keySet() );
    }

    /**
     * Gets an attribute of the context.
     *
     * @param name The name of the attribute to get.
     *
     * @return The value of the attribute with name {@code name}; {@code null} if no attribute matching {@code name} is
     * found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     *
     * @see #getAttribute(java.lang.String, java.lang.Object)
     * @see #setAttribute(java.lang.String, java.lang.Object)
     * @see #clearAttribute(java.lang.String)
     */
    public Object getAttribute( final String name )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        return this.attributes.get( name );
    }

    /**
     * Gets an attribute of the context.
     *
     * @param name The name of the attribute to get.
     * @param def The value to return if no attribute matching {@code name} is found.
     *
     * @return The value of the attribute with name {@code name}; {@code def} if no such attribute is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     *
     * @see #getAttribute(java.lang.String)
     * @see #setAttribute(java.lang.String, java.lang.Object)
     * @see #clearAttribute(java.lang.String)
     */
    public Object getAttribute( final String name, final Object def )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        Object value = this.getAttribute( name );

        if ( value == null )
        {
            value = def;
        }

        return value;
    }

    /**
     * Sets an attribute in the context.
     *
     * @param name The name of the attribute to set.
     * @param value The value of the attribute to set.
     *
     * @return The previous value of the attribute with name {@code name}; {@code null} if no such value is found.
     *
     * @throws NullPointerException if {@code name} or {@code value} is {@code null}.
     *
     * @see #getAttribute(java.lang.String)
     * @see #getAttribute(java.lang.String, java.lang.Object)
     * @see #clearAttribute(java.lang.String)
     */
    public Object setAttribute( final String name, final Object value )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }
        if ( value == null )
        {
            throw new NullPointerException( "value" );
        }

        return this.attributes.put( name, value );
    }

    /**
     * Removes an attribute from the context.
     *
     * @param name The name of the attribute to remove.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     *
     * @see #getAttribute(java.lang.String)
     * @see #getAttribute(java.lang.String, java.lang.Object)
     * @see #setAttribute(java.lang.String, java.lang.Object)
     */
    public void clearAttribute( final String name )
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        this.attributes.remove( name );
    }

    /**
     * Gets the class loader of the context.
     *
     * @return The class loader of the context or {@code null}, indicating the bootstrap class loader.
     *
     * @see #findClass(java.lang.String)
     * @see #findResource(java.lang.String)
     * @see #findResources(java.lang.String)
     */
    public ClassLoader getClassLoader()
    {
        if ( !this.classLoaderSet )
        {
            this.classLoader = this.getClass().getClassLoader();
            this.classLoaderSet = true;
        }

        return this.classLoader;
    }

    /**
     * Gets the listeners of the context.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.
     * </p>
     *
     * @return The list of listeners of the context.
     *
     * @see #log(java.util.logging.Level, java.lang.String, java.lang.Throwable)
     */
    public List<Listener> getListeners()
    {
        if ( this.listeners == null )
        {
            this.listeners = new LinkedList<Listener>();
        }

        return this.listeners;
    }

    /**
     * Gets the default {@code http://jomc.org/modlet} namespace schema system id.
     * <p>
     * The default {@code http://jomc.org/modlet} namespace schema system id is controlled by system property
     * {@code org.jomc.modlet.ModelContext.defaultModletSchemaSystemId} holding a system id URI.
     * If that property is not set, the {@code http://xml.jomc.org/modlet/jomc-modlet-1.9.xsd} default is
     * returned.
     * </p>
     *
     * @return The default system id of the {@code http://jomc.org/modlet} namespace schema.
     *
     * @see #setDefaultModletSchemaSystemId(java.lang.String)
     */
    public static String getDefaultModletSchemaSystemId()
    {
        if ( defaultModletSchemaSystemId == null )
        {
            defaultModletSchemaSystemId = System.getProperty(
                "org.jomc.modlet.ModelContext.defaultModletSchemaSystemId", DEFAULT_MODLET_SCHEMA_SYSTEM_ID );

        }

        return defaultModletSchemaSystemId;
    }

    /**
     * Sets the default {@code http://jomc.org/modlet} namespace schema system id.
     *
     * @param value The new default {@code http://jomc.org/modlet} namespace schema system id or {@code null}.
     *
     * @see #getDefaultModletSchemaSystemId()
     */
    public static void setDefaultModletSchemaSystemId( final String value )
    {
        defaultModletSchemaSystemId = value;
    }

    /**
     * Gets the {@code http://jomc.org/modlet} namespace schema system id of the context.
     *
     * @return The {@code http://jomc.org/modlet} namespace schema system id of the context.
     *
     * @see #getDefaultModletSchemaSystemId()
     * @see #setModletSchemaSystemId(java.lang.String)
     */
    public final String getModletSchemaSystemId()
    {
        if ( this.modletSchemaSystemId == null )
        {
            this.modletSchemaSystemId = getDefaultModletSchemaSystemId();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG,
                          getMessage( "defaultModletSchemaSystemIdInfo", this.modletSchemaSystemId ), null );

            }
        }

        return this.modletSchemaSystemId;
    }

    /**
     * Sets the {@code http://jomc.org/modlet} namespace schema system id of the context.
     *
     * @param value The new {@code http://jomc.org/modlet} namespace schema system id or {@code null}.
     *
     * @see #getModletSchemaSystemId()
     */
    public final void setModletSchemaSystemId( final String value )
    {
        final String oldModletSchemaSystemId = this.getModletSchemaSystemId();
        this.modletSchemaSystemId = value;

        if ( this.modlets != null )
        {
            for ( int i = 0, s0 = this.modlets.getModlet().size(); i < s0; i++ )
            {
                final Modlet m = this.modlets.getModlet().get( i );

                if ( m.getSchemas() != null )
                {
                    final Schema s = m.getSchemas().getSchemaBySystemId( oldModletSchemaSystemId );

                    if ( s != null )
                    {
                        s.setSystemId( value );
                    }
                }
            }
        }
    }

    /**
     * Gets the default log level events are logged at.
     * <p>
     * The default log level is controlled by system property
     * {@code org.jomc.modlet.ModelContext.defaultLogLevel} holding the log level to log events at by default.
     * If that property is not set, the {@code WARNING} default is returned.
     * </p>
     *
     * @return The log level events are logged at by default.
     *
     * @see #getLogLevel()
     * @see Level#parse(java.lang.String)
     */
    public static Level getDefaultLogLevel()
    {
        if ( defaultLogLevel == null )
        {
            defaultLogLevel = Level.parse( System.getProperty(
                "org.jomc.modlet.ModelContext.defaultLogLevel", DEFAULT_LOG_LEVEL.getName() ) );

        }

        return defaultLogLevel;
    }

    /**
     * Sets the default log level events are logged at.
     *
     * @param value The new default level events are logged at or {@code null}.
     *
     * @see #getDefaultLogLevel()
     */
    public static void setDefaultLogLevel( final Level value )
    {
        defaultLogLevel = value;
    }

    /**
     * Gets the log level of the context.
     *
     * @return The log level of the context.
     *
     * @see #getDefaultLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     * @see #isLoggable(java.util.logging.Level)
     */
    public final Level getLogLevel()
    {
        if ( this.logLevel == null )
        {
            this.logLevel = getDefaultLogLevel();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultLogLevelInfo", this.logLevel.getLocalizedName() ), null );
            }
        }

        return this.logLevel;
    }

    /**
     * Sets the log level of the context.
     *
     * @param value The new log level of the context or {@code null}.
     *
     * @see #getLogLevel()
     * @see #isLoggable(java.util.logging.Level)
     */
    public final void setLogLevel( final Level value )
    {
        this.logLevel = value;
    }

    /**
     * Checks if a message at a given level is provided to the listeners of the context.
     *
     * @param level The level to test.
     *
     * @return {@code true}, if messages at {@code level} are provided to the listeners of the context; {@code false},
     * if messages at {@code level} are not provided to the listeners of the context.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     */
    public boolean isLoggable( final Level level )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        return level.intValue() >= this.getLogLevel().intValue();
    }

    /**
     * Notifies all listeners of the context.
     *
     * @param level The level of the event.
     * @param message The message of the event or {@code null}.
     * @param throwable The throwable of the event {@code null}.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getListeners()
     * @see #isLoggable(java.util.logging.Level)
     */
    public void log( final Level level, final String message, final Throwable throwable )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        if ( this.isLoggable( level ) )
        {
            for ( final Listener l : this.getListeners() )
            {
                l.onLog( level, message, throwable );
            }
        }
    }

    /**
     * Gets the {@code Modlets} of the context.
     * <p>
     * If no {@code Modlets} have been set using the {@code setModlets} method, this method calls the
     * {@code findModlets} method and the {@code processModlets} method to initialize the {@code Modlets} of the
     * context.
     * </p>
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object.
     * </p>
     *
     * @return The {@code Modlets} of the context.
     *
     * @throws ModelException if getting the {@code Modlets} of the context fails.
     *
     * @see #setModlets(org.jomc.modlet.Modlets)
     * @see #findModlets(org.jomc.modlet.Modlets)
     * @see #processModlets(org.jomc.modlet.Modlets)
     * @see #validateModlets(org.jomc.modlet.Modlets)
     */
    public final Modlets getModlets() throws ModelException
    {
        if ( this.modlets == null )
        {
            final Modlet modlet = new Modlet();
            modlet.setModel( ModletObject.MODEL_PUBLIC_ID );
            modlet.setName( getMessage( "projectName" ) );
            modlet.setVendor( getMessage( "projectVendor" ) );
            modlet.setVersion( getMessage( "projectVersion" ) );
            modlet.setSchemas( new Schemas() );

            final Schema schema = new Schema();
            schema.setPublicId( ModletObject.MODEL_PUBLIC_ID );
            schema.setSystemId( this.getModletSchemaSystemId() );
            schema.setContextId( ModletObject.class.getPackage().getName() );
            schema.setClasspathId( ModletObject.class.getPackage().getName().replace( '.', '/' )
                                       + "/jomc-modlet-1.9.xsd" );

            modlet.getSchemas().getSchema().add( schema );

            this.modlets = new Modlets();
            this.modlets.getModlet().add( modlet );

            long t0 = System.currentTimeMillis();
            final Modlets provided = this.findModlets( this.modlets );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "findModletsReport",
                                                  provided != null ? provided.getModlet().size() : 0,
                                                  System.currentTimeMillis() - t0 ), null );

            }

            if ( provided != null )
            {
                this.modlets = provided;
            }

            t0 = System.currentTimeMillis();
            final Modlets processed = this.processModlets( this.modlets );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "processModletsReport",
                                                  processed != null ? processed.getModlet().size() : 0,
                                                  System.currentTimeMillis() - t0 ), null );
            }

            if ( processed != null )
            {
                this.modlets = processed;
            }

            t0 = System.currentTimeMillis();
            final ModelValidationReport report = this.validateModlets( this.modlets );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "validateModletsReport",
                                                  this.modlets.getModlet().size(),
                                                  System.currentTimeMillis() - t0 ), null );
            }

            for ( final ModelValidationReport.Detail detail : report.getDetails() )
            {
                if ( this.isLoggable( detail.getLevel() ) )
                {
                    this.log( detail.getLevel(), detail.getMessage(), null );
                }
            }

            if ( !report.isModelValid() )
            {
                this.modlets = null;
                throw new ModelException( getMessage( "invalidModlets" ) );
            }
        }

        return this.modlets;
    }

    /**
     * Sets the {@code Modlets} of the context.
     *
     * @param value The new {@code Modlets} of the context or {@code null}.
     *
     * @see #getModlets()
     */
    public final void setModlets( final Modlets value )
    {
        this.modlets = value;
    }

    /**
     * Searches the context for a class with a given name.
     *
     * @param name The name of the class to search.
     *
     * @return A class object of the class with name {@code name} or {@code null}, if no such class is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws ModelException if searching fails.
     *
     * @see #getClassLoader()
     */
    public Class<?> findClass( final String name ) throws ModelException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        try
        {
            return Class.forName( name, false, this.getClassLoader() );
        }
        catch ( final ClassNotFoundException e )
        {
            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( e ), e );
            }

            return null;
        }
    }

    /**
     * Searches the context for a resource with a given name.
     *
     * @param name The name of the resource to search.
     *
     * @return An URL object for reading the resource or {@code null}, if no such resource is found.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws ModelException if searching fails.
     *
     * @see #getClassLoader()
     */
    public URL findResource( final String name ) throws ModelException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        final long t0 = System.currentTimeMillis();
        final URL resource = this.getClassLoader() == null
                                 ? ClassLoader.getSystemResource( name )
                                 : this.getClassLoader().getResource( name );

        if ( this.isLoggable( Level.FINE ) )
        {
            this.log( Level.FINE, getMessage( "resourcesReport", name, System.currentTimeMillis() - t0 ), null );
        }

        return resource;
    }

    /**
     * Searches the context for resources with a given name.
     *
     * @param name The name of the resources to search.
     *
     * @return An enumeration of URL objects for reading the resources. If no resources are found, the enumeration will
     * be empty.
     *
     * @throws NullPointerException if {@code name} is {@code null}.
     * @throws ModelException if searching fails.
     *
     * @see #getClassLoader()
     */
    public Enumeration<URL> findResources( final String name ) throws ModelException
    {
        if ( name == null )
        {
            throw new NullPointerException( "name" );
        }

        try
        {
            final long t0 = System.currentTimeMillis();
            final Enumeration<URL> resources = this.getClassLoader() == null
                                                   ? ClassLoader.getSystemResources( name )
                                                   : this.getClassLoader().getResources( name );

            if ( this.isLoggable( Level.FINE ) )
            {
                this.log( Level.FINE, getMessage( "resourcesReport", name, System.currentTimeMillis() - t0 ), null );
            }

            return resources;
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
    }

    /**
     * Searches the context for {@code Modlets}.
     *
     * @return The {@code Modlets} found in the context or {@code null}.
     *
     * @throws ModelException if searching {@code Modlets} fails.
     *
     * @see ModletProvider META-INF/services/org.jomc.modlet.ModletProvider
     * @see #getModlets()
     * @deprecated As of JOMC 1.6, replaced by {@link #findModlets(org.jomc.modlet.Modlets)}. This method will be
     * removed in JOMC 2.0.
     */
    @Deprecated
    public abstract Modlets findModlets() throws ModelException;

    /**
     * Searches the context for {@code Modlets}.
     *
     * @param modlets The {@code Modlets} currently being searched.
     *
     * @return The {@code Modlets} found in the context or {@code null}.
     *
     * @throws NullPointerException if {@code modlets} is {@code null}.
     * @throws ModelException if searching {@code Modlets} fails.
     *
     * @see ModletProvider META-INF/services/org.jomc.modlet.ModletProvider
     * @see #getModlets()
     * @since 1.6
     */
    public abstract Modlets findModlets( Modlets modlets ) throws ModelException;

    /**
     * Processes a list of {@code Modlet}s.
     *
     * @param modlets The {@code Modlets} currently being processed.
     *
     * @return The processed {@code Modlets} or {@code null}.
     *
     * @throws NullPointerException if {@code modlets} is {@code null}.
     * @throws ModelException if processing {@code Modlets} fails.
     *
     * @see ModletProcessor META-INF/services/org.jomc.modlet.ModletProcessor
     * @see #getModlets()
     * @since 1.6
     */
    public abstract Modlets processModlets( Modlets modlets ) throws ModelException;

    /**
     * Validates a list of {@code Modlet}s.
     *
     * @param modlets The {@code Modlets} to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code modlets} is {@code null}.
     * @throws ModelException if validating {@code modlets} fails.
     *
     * @see ModletValidator META-INF/services/org.jomc.modlet.ModletValidator
     * @see #getModlets()
     * @since 1.9
     */
    public abstract ModelValidationReport validateModlets( Modlets modlets ) throws ModelException;

    /**
     * Creates a new {@code Model} instance.
     *
     * @param model The identifier of the {@code Model} to create.
     *
     * @return A new instance of the {@code Model} identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new {@code Model} instance fails.
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model, ModelProvider.class )
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract Model findModel( String model ) throws ModelException;

    /**
     * Populates a given {@code Model} instance.
     *
     * @param model The {@code Model} to populate.
     *
     * @return The populated model.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if populating {@code model} fails.
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model, ModelProvider.class )
     *
     * @since 1.2
     */
    public abstract Model findModel( Model model ) throws ModelException;

    /**
     * Gets the name of the class providing the default {@code ModelContext} implementation.
     * <p>
     * The name of the class providing the default {@code ModelContext} implementation returned by method
     * {@link #createModelContext(java.lang.ClassLoader)} is controlled by system property
     * {@code org.jomc.modlet.ModelContext.className}. If that property is not set, the name of the
     * {@link org.jomc.modlet.DefaultModelContext} class is returned.
     * </p>
     *
     * @return The name of the class providing the default {@code ModelContext} implementation.
     *
     * @see #setModelContextClassName(java.lang.String)
     *
     * @deprecated As of JOMC 1.2, replaced by class {@link ModelContextFactory}. This method will be removed in version
     * 2.0.
     */
    @Deprecated
    public static String getModelContextClassName()
    {
        if ( modelContextClassName == null )
        {
            modelContextClassName = System.getProperty( "org.jomc.modlet.ModelContext.className",
                                                        DefaultModelContext.class.getName() );

        }

        return modelContextClassName;
    }

    /**
     * Sets the name of the class providing the default {@code ModelContext} implementation.
     *
     * @param value The new name of the class providing the default {@code ModelContext} implementation or {@code null}.
     *
     * @see #getModelContextClassName()
     *
     * @deprecated As of JOMC 1.2, replaced by class {@link ModelContextFactory}. This method will be removed in version
     * 2.0.
     */
    @Deprecated
    public static void setModelContextClassName( final String value )
    {
        modelContextClassName = value;
    }

    /**
     * Creates a new default {@code ModelContext} instance.
     *
     * @param classLoader The class loader to create a new default {@code ModelContext} instance with or {@code null},
     * to create a new context using the platform's bootstrap class loader.
     *
     * @return A new {@code ModelContext} instance.
     *
     * @throws ModelException if creating a new {@code ModelContext} instance fails.
     *
     * @see #getModelContextClassName()
     *
     * @deprecated As of JOMC 1.2, replaced by method {@link ModelContextFactory#newModelContext(java.lang.ClassLoader)}.
     * This method will be removed in version 2.0.
     */
    public static ModelContext createModelContext( final ClassLoader classLoader ) throws ModelException
    {
        if ( getModelContextClassName().equals( DefaultModelContext.class.getName() ) )
        {
            return new DefaultModelContext( classLoader );
        }

        try
        {
            final Class<?> clazz = Class.forName( getModelContextClassName(), false, classLoader );

            if ( !ModelContext.class.isAssignableFrom( clazz ) )
            {
                throw new ModelException( getMessage( "illegalContextImplementation", getModelContextClassName(),
                                                      ModelContext.class.getName() ) );

            }

            final Constructor<? extends ModelContext> ctor =
                clazz.asSubclass( ModelContext.class ).getDeclaredConstructor( ClassLoader.class );

            return ctor.newInstance( classLoader );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ModelException( getMessage( "contextClassNotFound", getModelContextClassName() ), e );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new ModelException( getMessage( "contextConstructorNotFound", getModelContextClassName() ), e );
        }
        catch ( final InstantiationException e )
        {
            final String message = getMessage( e );
            throw new ModelException( getMessage( "contextInstantiationException", getModelContextClassName(),
                                                  message != null ? " " + message : "" ), e );

        }
        catch ( final IllegalAccessException e )
        {
            final String message = getMessage( e );
            throw new ModelException( getMessage( "contextConstructorAccessDenied", getModelContextClassName(),
                                                  message != null ? " " + message : "" ), e );

        }
        catch ( final InvocationTargetException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getTargetException() != null )
            {
                message = getMessage( e.getTargetException() );
            }

            throw new ModelException( getMessage( "contextConstructorException", getModelContextClassName(),
                                                  message != null ? " " + message : "" ), e );

        }
    }

    /**
     * Processes a {@code Model}.
     *
     * @param model The {@code Model} to process.
     *
     * @return The processed {@code Model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if processing {@code model} fails.
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model, ModelProcessor.class )
     */
    public abstract Model processModel( Model model ) throws ModelException;

    /**
     * Validates a given {@code Model}.
     *
     * @param model The {@code Model} to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if validating {@code model} fails.
     *
     * @see #createServiceObjects(java.lang.String, java.lang.Class) createServiceObjects( model, ModelValidator.class )
     * @see ModelValidationReport#isModelValid()
     */
    public abstract ModelValidationReport validateModel( Model model ) throws ModelException;

    /**
     * Validates a given model.
     *
     * @param model The identifier of the {@code Model} to use for validating {@code source}.
     * @param source A source providing the model to validate.
     *
     * @return Validation report.
     *
     * @throws NullPointerException if {@code model} or {@code source} is {@code null}.
     * @throws ModelException if validating the model fails.
     *
     * @see #createSchema(java.lang.String)
     * @see ModelValidationReport#isModelValid()
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract ModelValidationReport validateModel( String model, Source source ) throws ModelException;

    /**
     * Creates service objects of a model.
     * <p>
     * Calling this method is the same as calling method<blockquote>
     * {@link #createServiceObjects(java.lang.String, java.lang.String, java.lang.Class) createServiceObjects( model, type.getName(), type )}
     * </blockquote>
     * </p>
     *
     * @param <T> The type of the service.
     * @param model The identifier of the {@code Model} to create service objects of.
     * @param type The class of the type of the service.
     *
     * @return An ordered, unmodifiable collection of new service objects identified by {@code type} of the model
     * identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} or {@code type} is {@code null}.
     * @throws ModelException if creating service objects fails.
     *
     * @see ModelProvider
     * @see ModelProcessor
     * @see ModelValidator
     *
     * @since 1.9
     */
    public abstract <T> Collection<? extends T> createServiceObjects( final String model, final Class<T> type )
        throws ModelException;

    /**
     * Creates service objects of a model.
     *
     * @param <T> The type of the service.
     * @param model The identifier of the {@code Model} to create service objects of.
     * @param service The identifier of the service to create objects of.
     * @param type The class of the type of the service.
     *
     * @return An ordered, unmodifiable collection of new service objects identified by {@code service} of the model
     * identified by {@code model}.
     *
     * @throws NullPointerException if {@code model}, {@code service} or {@code type} is {@code null}.
     * @throws ModelException if creating service objects fails.
     *
     * @see ModelProvider
     * @see ModelProcessor
     * @see ModelValidator
     *
     * @since 1.9
     */
    public abstract <T> Collection<? extends T> createServiceObjects( final String model, final String service,
                                                                      final Class<T> type )
        throws ModelException;

    /**
     * Creates a new service object.
     *
     * @param <T> The type of the service.
     * @param service The service to create a new object of.
     * @param type The class of the type of the service.
     *
     * @return An new service object for {@code service}.
     *
     * @throws NullPointerException if {@code service} or {@code type} is {@code null}.
     * @throws ModelException if creating the service object fails.
     *
     * @see ModletProvider
     * @see ModletProcessor
     * @see ModletValidator
     * @see ServiceFactory
     *
     * @since 1.2
     */
    public abstract <T> T createServiceObject( final Service service, final Class<T> type ) throws ModelException;

    /**
     * Creates a new SAX entity resolver instance of a given model.
     *
     * @param model The identifier of the model to create a new SAX entity resolver of.
     *
     * @return A new SAX entity resolver instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new SAX entity resolver instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract EntityResolver createEntityResolver( String model ) throws ModelException;

    /**
     * Creates a new SAX entity resolver instance for a given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new SAX entity resolver for.
     *
     * @return A new SAX entity resolver instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new SAX entity resolver instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract EntityResolver createEntityResolver( URI publicId ) throws ModelException;

    /**
     * Creates a new L/S resource resolver instance of a given model.
     *
     * @param model The identifier of the model to create a new L/S resource resolver of.
     *
     * @return A new L/S resource resolver instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new L/S resource resolver instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract LSResourceResolver createResourceResolver( String model ) throws ModelException;

    /**
     * Creates a new L/S resource resolver instance for a given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new L/S resource resolver for.
     *
     * @return A new L/S resource resolver instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new L/S resource resolver instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract LSResourceResolver createResourceResolver( URI publicId ) throws ModelException;

    /**
     * Creates a new JAXP schema instance of a given model.
     *
     * @param model The identifier of the model to create a new JAXP schema instance of.
     *
     * @return A new JAXP schema instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new JAXP schema instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract javax.xml.validation.Schema createSchema( String model ) throws ModelException;

    /**
     * Creates a new JAXP schema instance for a given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new JAXP schema instance for.
     *
     * @return A new JAXP schema instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new JAXP schema instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract javax.xml.validation.Schema createSchema( URI publicId ) throws ModelException;

    /**
     * Creates a new JAXB context instance of a given model.
     *
     * @param model The identifier of the model to create a new JAXB context instance of.
     *
     * @return A new JAXB context instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new JAXB context instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract JAXBContext createContext( String model ) throws ModelException;

    /**
     * Creates a new JAXB context instance for a given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new JAXB context instance for.
     *
     * @return A new JAXB context instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new JAXB context instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract JAXBContext createContext( URI publicId ) throws ModelException;

    /**
     * Creates a new JAXB marshaller instance of a given model.
     *
     * @param model The identifier of the model to create a new JAXB marshaller instance of.
     *
     * @return A new JAXB marshaller instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new JAXB marshaller instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract Marshaller createMarshaller( String model ) throws ModelException;

    /**
     * Creates a new JAXB marshaller instance for a given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new JAXB marshaller instance for.
     *
     * @return A new JAXB marshaller instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new JAXB marshaller instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract Marshaller createMarshaller( URI publicId ) throws ModelException;

    /**
     * Creates a new JAXB unmarshaller instance of a given model.
     *
     * @param model The identifier of the model to create a new JAXB unmarshaller instance of.
     *
     * @return A new JAXB unmarshaller instance of the model identified by {@code model}.
     *
     * @throws NullPointerException if {@code model} is {@code null}.
     * @throws ModelException if creating a new JAXB unmarshaller instance fails.
     *
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract Unmarshaller createUnmarshaller( String model ) throws ModelException;

    /**
     * Creates a new JAXB unmarshaller instance for a given given public identifier URI.
     *
     * @param publicId The public identifier URI to create a new JAXB unmarshaller instance for.
     *
     * @return A new JAXB unmarshaller instance for the public identifier URI {@code publicId}.
     *
     * @throws NullPointerException if {@code publicId} is {@code null}.
     * @throws ModelException if creating a new JAXB unmarshaller instance fails.
     *
     * @see ModletObject#PUBLIC_ID
     * @since 1.2
     * @deprecated As of JOMC 1.8, removed without replacement. This method will be removed in JOMC 2.0.
     */
    @Deprecated
    public abstract Unmarshaller createUnmarshaller( URI publicId ) throws ModelException;

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            ModelContext.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

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
