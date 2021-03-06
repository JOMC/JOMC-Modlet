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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

/**
 * Default {@code ModletProvider} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#findModlets(org.jomc.modlet.Modlets)
 */
public class DefaultModletProvider implements ModletProvider
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     *
     * @see #findModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.modlet.DefaultModletProvider.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     *
     * @see #isDefaultEnabled()
     * @since 1.2
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultEnabled";

    /**
     * Default value of the flag indicating the provider is enabled by default.
     *
     * @see #isDefaultEnabled()
     * @since 1.2
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /**
     * Flag indicating the provider is enabled by default.
     */
    private static volatile Boolean defaultEnabled;

    /**
     * Flag indicating the provider is enabled.
     */
    private volatile Boolean enabled;

    /**
     * Constant for the name of the model context attribute backing property {@code modletLocation}.
     *
     * @see #findModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String MODLET_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModletProvider.modletLocationAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultModletLocation}.
     *
     * @see #getDefaultModletLocation()
     * @since 1.2
     */
    private static final String DEFAULT_MODLET_LOCATION_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultModletLocation";

    /**
     * Class path location searched for {@code Modlets} by default.
     *
     * @see #getDefaultModletLocation()
     */
    private static final String DEFAULT_MODLET_LOCATION = "META-INF/jomc-modlet.xml";

    /**
     * Default {@code Modlet} location.
     */
    private static volatile String defaultModletLocation;

    /**
     * Modlet location of the instance.
     */
    private volatile String modletLocation;

    /**
     * Constant for the name of the model context attribute backing property {@code validating}.
     *
     * @see #findModlets(org.jomc.modlet.ModelContext, java.lang.String)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String VALIDATING_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModletProvider.validatingAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultValidating}.
     *
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final String DEFAULT_VALIDATING_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultValidating";

    /**
     * Default value of the flag indicating the provider is validating resources by default.
     *
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final Boolean DEFAULT_VALIDATING = Boolean.TRUE;

    /**
     * Flag indicating the provider is validating resources by default.
     *
     * @since 1.2
     */
    private static volatile Boolean defaultValidating;

    /**
     * Flag indicating the provider is validating resources.
     *
     * @since 1.2
     */
    private volatile Boolean validating;

    /**
     * Constant for the name of the system property controlling property {@code defaultOrdinal}.
     *
     * @see #getDefaultOrdinal()
     * @since 1.6
     */
    private static final String DEFAULT_ORDINAL_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultOrdinal";

    /**
     * Default value of the ordinal number of the provider.
     *
     * @see #getDefaultOrdinal()
     * @since 1.6
     */
    private static final Integer DEFAULT_ORDINAL = 0;

    /**
     * Default ordinal number of the provider.
     *
     * @since 1.6
     */
    private static volatile Integer defaultOrdinal;

    /**
     * Ordinal number of the provider.
     *
     * @since 1.6
     */
    private volatile Integer ordinal;

    /**
     * Creates a new {@code DefaultModletProvider} instance.
     */
    public DefaultModletProvider()
    {
        super();
    }

    /**
     * Gets a flag indicating the provider is enabled by default.
     * <p>
     * The default enabled flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultEnabled} holding a value indicating the provider is
     * enabled by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the provider is enabled by default; {@code false}, if the provider is disabled by
     * default.
     *
     * @see #isEnabled()
     * @see #setDefaultEnabled(java.lang.Boolean)
     */
    public static boolean isDefaultEnabled()
    {
        if ( defaultEnabled == null )
        {
            defaultEnabled = Boolean.valueOf( System.getProperty(
                DEFAULT_ENABLED_PROPERTY_NAME, Boolean.toString( DEFAULT_ENABLED ) ) );

        }

        return defaultEnabled;
    }

    /**
     * Sets the flag indicating the provider is enabled by default.
     *
     * @param value The new value of the flag indicating the provider is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the provider is enabled.
     *
     * @return {@code true}, if the provider is enabled; {@code false}, if the provider is disabled.
     *
     * @see #isDefaultEnabled()
     * @see #setEnabled(java.lang.Boolean)
     */
    public final boolean isEnabled()
    {
        if ( this.enabled == null )
        {
            this.enabled = isDefaultEnabled();
        }

        return this.enabled;
    }

    /**
     * Sets the flag indicating the provider is enabled.
     *
     * @param value The new value of the flag indicating the provider is enabled or {@code null}.
     *
     * @see #isEnabled()
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default location searched for {@code Modlet} resources.
     * <p>
     * The default {@code Modlet} location is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultModletLocation} holding the location to search for
     * {@code Modlet} resources by default. If that property is not set, the {@code META-INF/jomc-modlet.xml} default is
     * returned.
     * </p>
     *
     * @return The location searched for {@code Modlet} resources by default.
     *
     * @see #setDefaultModletLocation(java.lang.String)
     */
    public static String getDefaultModletLocation()
    {
        if ( defaultModletLocation == null )
        {
            defaultModletLocation = System.getProperty(
                DEFAULT_MODLET_LOCATION_PROPERTY_NAME, DEFAULT_MODLET_LOCATION );

        }

        return defaultModletLocation;
    }

    /**
     * Sets the default location searched for {@code Modlet} resources.
     *
     * @param value The new default location to search for {@code Modlet} resources or {@code null}.
     *
     * @see #getDefaultModletLocation()
     */
    public static void setDefaultModletLocation( final String value )
    {
        defaultModletLocation = value;
    }

    /**
     * Gets the location searched for {@code Modlet} resources.
     *
     * @return The location searched for {@code Modlet} resources.
     *
     * @see #getDefaultModletLocation()
     * @see #setModletLocation(java.lang.String)
     */
    public final String getModletLocation()
    {
        if ( this.modletLocation == null )
        {
            this.modletLocation = getDefaultModletLocation();
        }

        return this.modletLocation;
    }

    /**
     * Sets the location searched for {@code Modlet} resources.
     *
     * @param value The new location to search for {@code Modlet} resources or {@code null}.
     *
     * @see #getModletLocation()
     */
    public final void setModletLocation( final String value )
    {
        this.modletLocation = value;
    }

    /**
     * Gets a flag indicating the provider is validating resources by default.
     * <p>
     * The default validating flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultValidating} holding a value indicating the provider is
     * validating resources by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the provider is validating resources by default; {@code false}, if the provider is not
     * validating resources by default.
     *
     * @see #isValidating()
     * @see #setDefaultValidating(java.lang.Boolean)
     *
     * @since 1.2
     */
    public static boolean isDefaultValidating()
    {
        if ( defaultValidating == null )
        {
            defaultValidating = Boolean.valueOf( System.getProperty(
                DEFAULT_VALIDATING_PROPERTY_NAME, Boolean.toString( DEFAULT_VALIDATING ) ) );

        }

        return defaultValidating;
    }

    /**
     * Sets the flag indicating the provider is validating resources by default.
     *
     * @param value The new value of the flag indicating the provider is validating resources by default or
     * {@code null}.
     *
     * @see #isDefaultValidating()
     *
     * @since 1.2
     */
    public static void setDefaultValidating( final Boolean value )
    {
        defaultValidating = value;
    }

    /**
     * Gets a flag indicating the provider is validating resources.
     *
     * @return {@code true}, if the provider is validating resources; {@code false}, if the provider is not validating
     * resources.
     *
     * @see #isDefaultValidating()
     * @see #setValidating(java.lang.Boolean)
     *
     * @since 1.2
     */
    public final boolean isValidating()
    {
        if ( this.validating == null )
        {
            this.validating = isDefaultValidating();
        }

        return this.validating;
    }

    /**
     * Sets the flag indicating the provider is validating resources.
     *
     * @param value The new value of the flag indicating the provider is validating resources or {@code null}.
     *
     * @see #isValidating()
     *
     * @since 1.2
     */
    public final void setValidating( final Boolean value )
    {
        this.validating = value;
    }

    /**
     * Gets the default ordinal number of the provider.
     * <p>
     * The default ordinal number is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultOrdinal} holding the default ordinal number of the provider.
     * If that property is not set, the {@code 0} default is returned.
     * </p>
     *
     * @return The default ordinal number of the provider.
     *
     * @see #setDefaultOrdinal(java.lang.Integer)
     *
     * @since 1.6
     */
    public static int getDefaultOrdinal()
    {
        if ( defaultOrdinal == null )
        {
            defaultOrdinal = Integer.getInteger( DEFAULT_ORDINAL_PROPERTY_NAME, DEFAULT_ORDINAL );
        }

        return defaultOrdinal;
    }

    /**
     * Sets the default ordinal number of the provider.
     *
     * @param value The new default ordinal number of the provider or {@code null}.
     *
     * @see #getDefaultOrdinal()
     *
     * @since 1.6
     */
    public static void setDefaultOrdinal( final Integer value )
    {
        defaultOrdinal = value;
    }

    /**
     * Gets the ordinal number of the provider.
     *
     * @return The ordinal number of the provider.
     *
     * @see #getDefaultOrdinal()
     * @see #setOrdinal(java.lang.Integer)
     *
     * @since 1.6
     */
    @Override
    public final int getOrdinal()
    {
        if ( this.ordinal == null )
        {
            this.ordinal = getDefaultOrdinal();
        }

        return this.ordinal;
    }

    /**
     * Sets the ordinal number of the provider.
     *
     * @param value The new ordinal number of the provider or {@code null}.
     *
     * @see #getOrdinal()
     *
     * @since 1.6
     */
    public final void setOrdinal( final Integer value )
    {
        this.ordinal = value;
    }

    /**
     * Searches a given context for {@code Modlets}.
     *
     * @param context The context to search for {@code Modlets}.
     * @param location The location to search at.
     *
     * @return The {@code Modlets} found at {@code location} in {@code context} or empty {@code Modlets}, if no
     * {@code Modlets} are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModelException if searching the context fails.
     *
     * @see #isValidating()
     * @see #VALIDATING_ATTRIBUTE_NAME
     */
    public Modlets findModlets( final ModelContext context, final String location ) throws ModelException
    {
        Objects.requireNonNull( context, "context" );
        Objects.requireNonNull( location, "location" );

        boolean contextValidating = this.isValidating();
        if ( DEFAULT_VALIDATING == contextValidating )
        {
            final Optional<Object> validatingAttribute = context.getAttribute( VALIDATING_ATTRIBUTE_NAME );
            if ( validatingAttribute.isPresent() && validatingAttribute.get() instanceof Boolean )
            {
                contextValidating = (Boolean) validatingAttribute.get();
            }
        }

        final Modlets modlets = new Modlets();
        final long t0 = System.nanoTime();
        final Enumeration<URL> modletResourceEnumeration = context.findResources( location );
        final JAXBContext ctx = context.createContext( ModletObject.MODEL_PUBLIC_ID );
        final javax.xml.validation.Schema schema = contextValidating
                                                       ? context.createSchema( ModletObject.MODEL_PUBLIC_ID )
                                                       : null;

        final ThreadLocal<Unmarshaller> threadLocalUnmarshaller = new ThreadLocal<>();

        try ( final Stream<URL> st0 = Collections.list( modletResourceEnumeration ).parallelStream().unordered() )
        {
            final class UnmarshalFailure extends RuntimeException
            {

                final URL resource;

                UnmarshalFailure( final URL resource, final Throwable cause )
                {
                    super( Objects.requireNonNull( cause, "cause" ) );
                    this.resource = Objects.requireNonNull( resource, "resource" );
                }

                <T extends Exception, R extends Exception> void handleCause(
                    final Class<T> cause, final Function<T, R> createExceptionFunction )
                    throws R
                {
                    if ( Objects.requireNonNull( cause, "cause" ).isAssignableFrom( this.getCause().getClass() ) )
                    {
                        throw Objects.requireNonNull( Objects.requireNonNull( createExceptionFunction,
                                                                              "createExceptionFunction" ).
                            apply( (T) this.getCause() ), createExceptionFunction.toString() );

                    }
                }

                Error unhandledCauseError()
                {
                    return new AssertionError( this.getCause() );
                }

            }

            final Function<URL, Modlets> toModlets = url  ->
            {
                try
                {
                    final Modlets result = new Modlets();

                    Unmarshaller unmarshaller = threadLocalUnmarshaller.get();
                    if ( unmarshaller == null )
                    {
                        unmarshaller = ctx.createUnmarshaller();
                        unmarshaller.setSchema( schema );

                        threadLocalUnmarshaller.set( unmarshaller );
                    }

                    Object content = unmarshaller.unmarshal( url );

                    if ( content instanceof JAXBElement<?> )
                    {
                        content = ( (JAXBElement<?>) content ).getValue();
                    }

                    if ( content instanceof Modlet )
                    {
                        result.getModlet().add( (Modlet) content );
                    }
                    else if ( content instanceof Modlets )
                    {
                        result.getModlet().addAll( ( (Modlets) content ).getModlet() );
                    }

                    return result;
                }
                catch ( final JAXBException e )
                {
                    throw new UnmarshalFailure( url, e );
                }
            };

            try
            {
                modlets.getModlet().addAll(
                    st0.map( toModlets ).
                        flatMap( m  -> m.getModlet().parallelStream().unordered() ).
                        collect( Collector.of( CopyOnWriteArrayList::new, List::add, ( l1, l2 )  ->
                                           {
                                               l1.addAll( l2 );
                                               return l1;
                                           }, Collector.Characteristics.CONCURRENT,
                                               Collector.Characteristics.UNORDERED ) )
                );
            }
            catch ( final UnmarshalFailure f )
            {
                f.handleCause( UnmarshalException.class, e  -> new ModelException( getMessage( e, f.resource ), e ) );
                f.handleCause( JAXBException.class, e  -> new ModelException( getMessage( e ), e ) );
                throw f.unhandledCauseError();
            }
        }

        if ( context.isLoggable( Level.FINE ) )
        {
            context.log( Level.FINE, getMessage( "contextReport",
                                                 modlets.getModlet().size(),
                                                 location, System.nanoTime() - t0 ), null );

        }

        return modlets;
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@code Modlets} found in the context or {@code null}, if no {@code Modlets} are found or the provider
     * is disabled.
     *
     * @see #isEnabled()
     * @see #getModletLocation()
     * @see #findModlets(org.jomc.modlet.ModelContext, java.lang.String)
     * @see #ENABLED_ATTRIBUTE_NAME
     * @see #MODLET_LOCATION_ATTRIBUTE_NAME
     * @since 1.6
     */
    @Override
    public Optional<Modlets> findModlets( final ModelContext context, final Modlets modlets ) throws ModelException
    {
        Objects.requireNonNull( context, "context" );
        Objects.requireNonNull( modlets, "modlets" );

        Modlets provided = null;

        boolean contextEnabled = this.isEnabled();
        if ( DEFAULT_ENABLED == contextEnabled )
        {
            final Optional<Object> enabledAttribute = context.getAttribute( ENABLED_ATTRIBUTE_NAME );
            if ( enabledAttribute.isPresent() && enabledAttribute.get() instanceof Boolean )
            {
                contextEnabled = (Boolean) enabledAttribute.get();
            }
        }

        String contextModletLocation = this.getModletLocation();
        if ( DEFAULT_MODLET_LOCATION.equals( contextModletLocation ) )
        {
            final Optional<Object> modletLocationAttribute = context.getAttribute( MODLET_LOCATION_ATTRIBUTE_NAME );
            if ( modletLocationAttribute.isPresent() && modletLocationAttribute.get() instanceof String )
            {
                contextModletLocation = (String) modletLocationAttribute.get();
            }
        }

        if ( contextEnabled )
        {
            final Modlets found = this.findModlets( context, contextModletLocation );

            if ( !found.getModlet().isEmpty() )
            {
                provided = modlets.clone();
                provided.getModlet().addAll( found.getModlet() );
            }
        }
        else if ( context.isLoggable( Level.FINER ) )
        {
            context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName() ), null );
        }

        return Optional.ofNullable( provided );
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModletProvider.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null
                   ? t.getMessage() != null && t.getMessage().trim().length() > 0
                         ? t.getMessage()
                         : getMessage( t.getCause() )
                   : null;

    }

    private static String getMessage( final JAXBException e )
    {
        String message = getMessage( (Throwable) e );
        if ( message == null && e.getLinkedException() != null )
        {
            message = getMessage( e.getLinkedException() );
        }
        return message;
    }

    private static String getMessage( final UnmarshalException e, final URL resource )
    {
        String message = getMessage( e );
        message = getMessage( "unmarshalException", resource.toExternalForm(), message != null ? " " + message : "" );
        return message;
    }

}
