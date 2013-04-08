/*
 *   Copyright (C) Christian Schulte, 2005-206
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
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
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
 * @see ModelContext#findModlets()
 */
public class DefaultModletProvider implements ModletProvider
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     * @see #findModlets(org.jomc.modlet.ModelContext)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.modlet.DefaultModletProvider.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     * @see #isDefaultEnabled()
     * @since 1.2
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultEnabled";

    /**
     * Default value of the flag indicating the provider is enabled by default.
     * @see #isDefaultEnabled()
     * @since 1.2
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /** Flag indicating the provider is enabled by default. */
    private static volatile Boolean defaultEnabled;

    /** Flag indicating the provider is enabled. */
    private Boolean enabled;

    /**
     * Constant for the name of the model context attribute backing property {@code modletLocation}.
     * @see #findModlets(org.jomc.modlet.ModelContext)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String MODLET_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModletProvider.modletLocationAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultModletLocation}.
     * @see #getDefaultModletLocation()
     * @since 1.2
     */
    private static final String DEFAULT_MODLET_LOCATION_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultModletLocation";

    /**
     * Class path location searched for {@code Modlets} by default.
     * @see #getDefaultModletLocation()
     */
    private static final String DEFAULT_MODLET_LOCATION = "META-INF/jomc-modlet.xml";

    /** Default {@code Modlet} location. */
    private static volatile String defaultModletLocation;

    /** Modlet location of the instance. */
    private String modletLocation;

    /**
     * Constant for the name of the model context attribute backing property {@code validating}.
     * @see #findModlets(org.jomc.modlet.ModelContext, java.lang.String)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String VALIDATING_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModletProvider.validatingAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultValidating}.
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final String DEFAULT_VALIDATING_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProvider.defaultValidating";

    /**
     * Default value of the flag indicating the provider is validating resources by default.
     * @see #isDefaultValidating()
     * @since 1.2
     */
    private static final Boolean DEFAULT_VALIDATING = Boolean.TRUE;

    /**
     * Flag indicating the provider is validating resources by default.
     * @since 1.2
     */
    private static volatile Boolean defaultValidating;

    /**
     * Flag indicating the provider is validating resources.
     * @since 1.2
     */
    private Boolean validating;

    /** Creates a new {@code DefaultModletProvider} instance. */
    public DefaultModletProvider()
    {
        super();
    }

    /**
     * Gets a flag indicating the provider is enabled by default.
     * <p>The default enabled flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultEnabled} holding a value indicating the provider is
     * enabled by default. If that property is not set, the {@code true} default is returned.</p>
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
     * <p>The default {@code Modlet} location is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultModletLocation} holding the location to search for
     * {@code Modlet} resources by default. If that property is not set, the {@code META-INF/jomc-modlet.xml} default is
     * returned.</p>
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
     * <p>The default validating flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultValidating} holding a value indicating the provider is
     * validating resources by default. If that property is not set, the {@code true} default is returned.</p>
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
     * Searches a given context for {@code Modlets}.
     *
     * @param context The context to search for {@code Modlets}.
     * @param location The location to search at.
     *
     * @return The {@code Modlets} found at {@code location} in {@code context} or {@code null}, if no {@code Modlets}
     * are found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModelException if searching the context fails.
     *
     * @see #isValidating()
     * @see #VALIDATING_ATTRIBUTE_NAME
     */
    public Modlets findModlets( final ModelContext context, final String location ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        URL url = null;

        try
        {
            boolean contextValidating = this.isValidating();
            if ( DEFAULT_VALIDATING == contextValidating
                 && context.getAttribute( VALIDATING_ATTRIBUTE_NAME ) instanceof Boolean )
            {
                contextValidating = (Boolean) context.getAttribute( VALIDATING_ATTRIBUTE_NAME );
            }

            Modlets modlets = null;
            final long t0 = System.currentTimeMillis();
            final JAXBContext ctx = context.createContext( ModletObject.MODEL_PUBLIC_ID );
            final Unmarshaller u = ctx.createUnmarshaller();
            final Enumeration<URL> e = context.findResources( location );

            if ( contextValidating )
            {
                u.setSchema( context.createSchema( ModletObject.MODEL_PUBLIC_ID ) );
            }

            while ( e.hasMoreElements() )
            {
                url = e.nextElement();
                Object content = u.unmarshal( url );
                if ( content instanceof JAXBElement<?> )
                {
                    content = ( (JAXBElement<?>) content ).getValue();
                }

                if ( content instanceof Modlet )
                {
                    if ( modlets == null )
                    {
                        modlets = new Modlets();
                    }

                    modlets.getModlet().add( (Modlet) content );
                }
                else if ( content instanceof Modlets )
                {
                    if ( modlets == null )
                    {
                        modlets = new Modlets();
                    }

                    modlets.getModlet().addAll( ( (Modlets) content ).getModlet() );
                }
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, getMessage( "contextReport",
                                                     modlets != null ? modlets.getModlet().size() : 0,
                                                     location, System.currentTimeMillis() - t0 ), null );

            }

            return modlets == null || modlets.getModlet().isEmpty() ? null : modlets;
        }
        catch ( final UnmarshalException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getLinkedException() != null )
            {
                message = getMessage( e.getLinkedException() );
            }

            if ( url != null )
            {
                message = getMessage( "unmarshalException", url.toExternalForm(),
                                      message != null ? " " + message : "" );

            }

            throw new ModelException( message, e );
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
     *
     * @return The {@code Modlets} found in the context or {@code null}, if no {@code Modlets} are found or the provider
     * is disabled.
     *
     * @see #isEnabled()
     * @see #getModletLocation()
     * @see #findModlets(org.jomc.modlet.ModelContext, java.lang.String)
     * @see #ENABLED_ATTRIBUTE_NAME
     * @see #MODLET_LOCATION_ATTRIBUTE_NAME
     */
    public Modlets findModlets( final ModelContext context ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }

        Modlets found = null;

        boolean contextEnabled = this.isEnabled();
        if ( DEFAULT_ENABLED == contextEnabled && context.getAttribute( ENABLED_ATTRIBUTE_NAME ) instanceof Boolean )
        {
            contextEnabled = (Boolean) context.getAttribute( ENABLED_ATTRIBUTE_NAME );
        }

        String contextModletLocation = this.getModletLocation();
        if ( DEFAULT_MODLET_LOCATION.equals( contextModletLocation )
             && context.getAttribute( MODLET_LOCATION_ATTRIBUTE_NAME ) instanceof String )
        {
            contextModletLocation = (String) context.getAttribute( MODLET_LOCATION_ATTRIBUTE_NAME );
        }

        if ( contextEnabled )
        {
            found = this.findModlets( context, contextModletLocation );
        }
        else if ( context.isLoggable( Level.FINER ) )
        {
            context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName() ), null );
        }

        return found;
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

}
