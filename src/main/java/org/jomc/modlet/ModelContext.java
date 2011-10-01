/*
 *   Copyright (C) 2009 The JOMC Project
 *   Copyright (C) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.validation.Validator;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Model context interface.
 * <p><b>Use Cases:</b><br/><ul>
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
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JOMC$
 *
 * @see #createModelContext(java.lang.ClassLoader)
 */
public abstract class ModelContext
{

    /** Listener interface. */
    public abstract static class Listener
    {

        /** Creates a new {@code Listener} instance. */
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
     * @see #getDefaultModletSchemaSystemId()
     */
    private static final String DEFAULT_MODLET_SCHEMA_SYSTEM_ID =
        "http://jomc.sourceforge.net/modlet/jomc-modlet-1.0.xsd";

    /**
     * Log level events are logged at by default.
     * @see #getDefaultLogLevel()
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

    /** Default log level. */
    private static volatile Level defaultLogLevel;

    /** Default {@code http://jomc.org/model/modlet} namespace schema system id. */
    private static volatile String defaultModletSchemaSystemId;

    /** Class name of the {@code ModelContext} implementation. */
    private static volatile String modelContextClassName;

    /** The attributes of the instance. */
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /** The class loader of the context. */
    private ClassLoader classLoader;

    /** The listeners of the instance. */
    private List<Listener> listeners;

    /** Log level of the instance. */
    private Level logLevel;

    /** The {@code Modlets} of the instance. */
    private Modlets modlets;

    /** Modlet namespace schema system id of the instance. */
    private String modletSchemaSystemId;

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
    }

    /**
     * Gets an attribute of the context.
     *
     * @param name The name of the attribute to return.
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
     * @param name The name of the attribute to return.
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
     * Clears an attribute in the context.
     *
     * @param name The name of the attribute to clear.
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
     * @return The class loader of the context.
     *
     * @see #findClass(java.lang.String)
     * @see #findResource(java.lang.String)
     * @see #findResources(java.lang.String)
     */
    public ClassLoader getClassLoader()
    {
        if ( this.classLoader == null )
        {
            this.classLoader = new ClassLoader( null )
            {

                @Override
                public String toString()
                {
                    return ModelContext.class.getName() + ".BootstrapClassLoader@"
                           + Integer.toHexString( this.hashCode() );

                }

            };

        }

        return this.classLoader;
    }

    /**
     * Gets the listeners of the context.
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.</p>
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
     * <p>The default {@code http://jomc.org/modlet} namespace schema system id is controlled by system property
     * {@code org.jomc.modlet.ModelContext.defaultModletSchemaSystemId} holding a system id URI.
     * If that property is not set, the {@code http://jomc.sourceforge.net/modlet/jomc-modlet-1.0.xsd} default is
     * returned.</p>
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
        this.modletSchemaSystemId = value;
    }

    /**
     * Gets the default log level events are logged at.
     * <p>The default log level is controlled by system property
     * {@code org.jomc.modlet.ModelContext.defaultLogLevel} holding the log level to log events at by default.
     * If that property is not set, the {@code WARNING} default is returned.</p>
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
     * @return {@code true} if messages at {@code level} are provided to the listeners of the context;
     * {@code false} if messages at {@code level} are not provided to the listeners of the context.
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
            for ( Listener l : this.getListeners() )
            {
                l.onLog( level, message, throwable );
            }
        }
    }

    /**
     * Gets the {@code Modlets} of the context.
     * <p>This method calls the {@code findModlets} method to initialize the {@code Modlets} of the context, if no
     * {@code Modlets} instance has been set with the {@code setModlets} method.</p>
     *
     * @return The {@code Modlets} of the context. This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will be present inside the object.
     *
     * @throws ModelException if getting the {@code Modlets} of the context fails.
     *
     * @see #findModlets()
     * @see #setModlets(org.jomc.modlet.Modlets)
     */
    public Modlets getModlets() throws ModelException
    {
        try
        {
            if ( this.modlets == null )
            {
                final Modlet modlet = new Modlet();
                modlet.setName( getMessage( "projectName" ) );
                modlet.setVendor( getMessage( "projectVendor" ) );
                modlet.setVersion( getMessage( "projectVersion" ) );
                modlet.setSchemas( new Schemas() );

                final Schema schema = new Schema();
                schema.setPublicId( ModletObject.MODEL_PUBLIC_ID );
                schema.setSystemId( this.getModletSchemaSystemId() );
                schema.setContextId( ModletObject.class.getPackage().getName() );
                schema.setClasspathId( ModletObject.class.getPackage().getName().replace( '.', '/' )
                                       + "/jomc-modlet-1.0.xsd" );

                modlet.getSchemas().getSchema().add( schema );

                this.modlets = new Modlets();
                this.modlets.getModlet().add( modlet );

                final Modlets provided = this.findModlets();

                for ( Modlet m : provided.getModlet() )
                {
                    if ( this.modlets.getModlet( m.getName() ) == null )
                    {
                        this.modlets.getModlet().add( m );
                    }
                    else if ( this.isLoggable( Level.WARNING ) )
                    {
                        this.log( Level.WARNING, getMessage( "ignoringRedundantModlet", m.getName() ), null );
                    }
                }

                final javax.xml.validation.Schema modletSchema = this.createSchema( ModletObject.MODEL_PUBLIC_ID );
                final Validator validator = modletSchema.newValidator();
                validator.validate( new JAXBSource( this.createContext( ModletObject.MODEL_PUBLIC_ID ),
                                                    new ObjectFactory().createModlets( this.modlets ) ) );

            }

            return this.modlets;
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
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

    /**
     * Sets the {@code Modlets} of the context.
     *
     * @param value The new {@code Modlets} of the context or {@code null}.
     *
     * @see #getModlets()
     */
    public void setModlets( final Modlets value )
    {
        this.modlets = value;
    }

    /**
     * Gets the name of the class providing the {@code ModelContext} implementation.
     * <p>The name of the class providing the {@code ModelContext} implementation returned by method
     * {@link #createModelContext(java.lang.ClassLoader)} is controlled by system property
     * {@code org.jomc.modlet.ModelContext.className}. If that property is not set, the name of the
     * {@link DefaultModelContext} class is returned.</p>
     *
     * @return The name of the class providing the {@code ModelContext} implementation.
     *
     * @see #setModelContextClassName(java.lang.String)
     */
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
     * Sets the name of the class providing the {@code ModelContext} implementation.
     *
     * @param value The new name of the class providing the {@code ModelContext} implementation or {@code null}.
     *
     * @see #getModelContextClassName()
     */
    public static void setModelContextClassName( final String value )
    {
        modelContextClassName = value;
    }

    /**
     * Searches the context for a class with a given name.
     *
     * @param name The name of the class to return.
     *
     * @return A class object of the class with name {@code name} or {@code null} if no such class is found.
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
            return Class.forName( name, true, this.getClassLoader() );
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
     * @param name The name of the resource to return.
     *
     * @return An URL object for reading the resource or {@code null} if no such resource is found.
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

        return this.getClassLoader().getResource( name );
    }

    /**
     * Searches the context for resources with a given name.
     *
     * @param name The name of the resources to return.
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
            return this.getClassLoader().getResources( name );
        }
        catch ( final IOException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
    }

    /**
     * Searches the context for {@code Modlets}.
     *
     * @return The {@code Modlets} found in the context.
     *
     * @throws ModelException if searching {@code Modlets} fails.
     *
     * @see #getModlets()
     */
    public abstract Modlets findModlets() throws ModelException;

    /**
     * Creates a new {@code ModelContext} instance.
     *
     * @param classLoader The class loader to create a new {@code ModelContext} instance with or {@code null} to create
     * a new context using the platform's bootstrap class loader.
     *
     * @return A new {@code ModelContext} instance.
     *
     * @throws ModelException if creating a new {@code ModelContext} instance fails.
     *
     * @see #getModelContextClassName()
     * @see #createModelContext(java.lang.String, java.lang.ClassLoader)
     */
    public static ModelContext createModelContext( final ClassLoader classLoader ) throws ModelException
    {
        return createModelContext( getModelContextClassName(), classLoader );
    }

    /**
     * Creates a new {@code ModelContext} instance.
     *
     * @param className The name of the class providing the {@code ModelContext} implementation.
     * @param classLoader The class loader to create a new {@code ModelContext} instance with or {@code null} to create
     * a new context using the platform's bootstrap class loader.
     *
     * @return A new {@code ModelContext} instance.
     *
     * @throws NullPointerException if {@code className} is {@code null}.
     * @throws ModelException if creating a new {@code ModelContext} instance fails.
     *
     * @since 1.2
     */
    public static ModelContext createModelContext( final String className, final ClassLoader classLoader )
        throws ModelException
    {
        if ( className == null )
        {
            throw new NullPointerException( "className" );
        }

        if ( className.equals( DefaultModelContext.class.getName() ) )
        {
            return new DefaultModelContext( classLoader );
        }

        try
        {
            final Class<?> clazz = Class.forName( className, true, classLoader );
            final Constructor<? extends ModelContext> ctor =
                clazz.asSubclass( ModelContext.class ).getDeclaredConstructor( ClassLoader.class );

            return ctor.newInstance( classLoader );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ModelException( getMessage( "contextClassNotFound", className ), e );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new ModelException( getMessage( "contextConstructorNotFound", className ), e );
        }
        catch ( final InstantiationException e )
        {
            final String message = getMessage( e );
            throw new ModelException( getMessage( "contextInstantiationException", className,
                                                  message != null ? " " + message : "" ), e );

        }
        catch ( final IllegalAccessException e )
        {
            final String message = getMessage( e );
            throw new ModelException( getMessage( "contextConstructorAccessDenied", className,
                                                  message != null ? " " + message : "" ), e );

        }
        catch ( final InvocationTargetException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getTargetException() != null )
            {
                message = getMessage( e.getTargetException() );
            }

            throw new ModelException( getMessage( "contextConstructorException", className,
                                                  message != null ? " " + message : "" ), e );

        }
        catch ( final ClassCastException e )
        {
            throw new ModelException( getMessage( "illegalContextImplementation", className,
                                                  ModelContext.class.getName() ), e );

        }
    }

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
     * @see #createModelContext(java.lang.ClassLoader)
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
     * @see #createModelContext(java.lang.ClassLoader)
     *
     * @since 1.2
     */
    public abstract Model findModel( Model model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract EntityResolver createEntityResolver( String model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract LSResourceResolver createResourceResolver( String model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract javax.xml.validation.Schema createSchema( String model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract JAXBContext createContext( String model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract Marshaller createMarshaller( String model ) throws ModelException;

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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract Unmarshaller createUnmarshaller( String model ) throws ModelException;

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
     * @see #findModel(java.lang.String)
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
     * @throws ModelException if validating the modules fails.
     *
     * @see #findModel(java.lang.String)
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
     * @see #createModelContext(java.lang.ClassLoader)
     * @see ModelValidationReport#isModelValid()
     * @see ModletObject#MODEL_PUBLIC_ID
     */
    public abstract ModelValidationReport validateModel( String model, Source source ) throws ModelException;

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            ModelContext.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null ? t.getMessage() != null ? t.getMessage() : getMessage( t.getCause() ) : null;
    }

}
