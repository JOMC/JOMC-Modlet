/*
 *   Copyright (C) Christian Schulte, 2013-005
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

import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Default {@code ModletProcessor} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#processModlets(org.jomc.modlet.Modlets)
 * @since 1.6
 */
public class DefaultModletProcessor implements ModletProcessor
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     * @see #processModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @see ModelContext#getAttribute(java.lang.String)
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.modlet.DefaultModletProcessor.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     * @see #isDefaultEnabled()
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProcessor.defaultEnabled";

    /**
     * Default value of the flag indicating the processor is enabled by default.
     * @see #isDefaultEnabled()
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /** Flag indicating the processor is enabled by default. */
    private static volatile Boolean defaultEnabled;

    /** Flag indicating the processor is enabled. */
    private Boolean enabled;

    /**
     * Constant for the name of the system property controlling property {@code defaultOrdinal}.
     * @see #getDefaultOrdinal()
     */
    private static final String DEFAULT_ORDINAL_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProcessor.defaultOrdinal";

    /**
     * Default value of the ordinal number of the processor.
     * @see #getDefaultOrdinal()
     */
    private static final Integer DEFAULT_ORDINAL = 0;

    /**
     * Default ordinal number of the processor.
     */
    private static volatile Integer defaultOrdinal;

    /**
     * Ordinal number of the processor.
     */
    private Integer ordinal;

    /**
     * Constant for the name of the model context attribute backing property {@code transformerLocation}.
     * @see #processModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @see ModelContext#getAttribute(java.lang.String)
     * @since 1.2
     */
    public static final String TRANSFORMER_LOCATION_ATTRIBUTE_NAME =
        "org.jomc.modlet.DefaultModletProcessor.transformerLocationAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultTransformerLocation}.
     * @see #getDefaultTransformerLocation()
     */
    private static final String DEFAULT_TRANSFORMER_LOCATION_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletProcessor.defaultTransformerLocation";

    /**
     * Class path location searched for transformers by default.
     * @see #getDefaultTransformerLocation()
     */
    private static final String DEFAULT_TRANSFORMER_LOCATION = "META-INF/jomc-modlet.xsl";

    /** Default transformer location. */
    private static volatile String defaultTransformerLocation;

    /** Transformer location of the instance. */
    private String transformerLocation;

    /** Creates a new {@code DefaultModletProcessor} instance. */
    public DefaultModletProcessor()
    {
        super();
    }

    /**
     * Gets a flag indicating the processor is enabled by default.
     * <p>The default enabled flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProcessor.defaultEnabled} holding a value indicating the processor is
     * enabled by default. If that property is not set, the {@code true} default is returned.</p>
     *
     * @return {@code true}, if the processor is enabled by default; {@code false}, if the processor is disabled by
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
     * Sets the flag indicating the processor is enabled by default.
     *
     * @param value The new value of the flag indicating the processor is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the processor is enabled.
     *
     * @return {@code true}, if the processor is enabled; {@code false}, if the processor is disabled.
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
     * Sets the flag indicating the processor is enabled.
     *
     * @param value The new value of the flag indicating the processor is enabled or {@code null}.
     *
     * @see #isEnabled()
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default ordinal number of the processor.
     * <p>The default ordinal number is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProvider.defaultOrdinal} holding the default ordinal number of the processor.
     * If that property is not set, the {@code 0} default is returned.</p>
     *
     * @return The default ordinal number of the processor.
     *
     * @see #setDefaultOrdinal(java.lang.Integer)
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
     * Sets the default ordinal number of the processor.
     *
     * @param value The new default ordinal number of the processor or {@code null}.
     *
     * @see #getDefaultOrdinal()
     */
    public static void setDefaultOrdinal( final Integer value )
    {
        defaultOrdinal = value;
    }

    /**
     * Gets the ordinal number of the processor.
     *
     * @return The ordinal number of the processor.
     *
     * @see #getDefaultOrdinal()
     * @see #setOrdinal(java.lang.Integer)
     */
    public final int getOrdinal()
    {
        if ( this.ordinal == null )
        {
            this.ordinal = getDefaultOrdinal();
        }

        return this.ordinal;
    }

    /**
     * Sets the ordinal number of the processor.
     *
     * @param value The new ordinal number of the processor or {@code null}.
     *
     * @see #getOrdinal()
     */
    public final void setOrdinal( final Integer value )
    {
        this.ordinal = value;
    }

    /**
     * Gets the default location searched for transformer resources.
     * <p>The default transformer location is controlled by system property
     * {@code org.jomc.modlet.DefaultModletProcessor.defaultTransformerLocation} holding the location to search
     * for transformer resources by default. If that property is not set, the {@code META-INF/jomc-modlet.xsl} default
     * is returned.</p>
     *
     * @return The location searched for transformer resources by default.
     *
     * @see #setDefaultTransformerLocation(java.lang.String)
     */
    public static String getDefaultTransformerLocation()
    {
        if ( defaultTransformerLocation == null )
        {
            defaultTransformerLocation =
                System.getProperty( DEFAULT_TRANSFORMER_LOCATION_PROPERTY_NAME, DEFAULT_TRANSFORMER_LOCATION );

        }

        return defaultTransformerLocation;
    }

    /**
     * Sets the default location searched for transformer resources.
     *
     * @param value The new default location to search for transformer resources or {@code null}.
     *
     * @see #getDefaultTransformerLocation()
     */
    public static void setDefaultTransformerLocation( final String value )
    {
        defaultTransformerLocation = value;
    }

    /**
     * Gets the location searched for transformer resources.
     *
     * @return The location searched for transformer resources.
     *
     * @see #getDefaultTransformerLocation()
     * @see #setTransformerLocation(java.lang.String)
     */
    public final String getTransformerLocation()
    {
        if ( this.transformerLocation == null )
        {
            this.transformerLocation = getDefaultTransformerLocation();
        }

        return this.transformerLocation;
    }

    /**
     * Sets the location searched for transformer resources.
     *
     * @param value The new location to search for transformer resources or {@code null}.
     *
     * @see #getTransformerLocation()
     */
    public final void setTransformerLocation( final String value )
    {
        this.transformerLocation = value;
    }

    /**
     * Searches a given context for transformers.
     *
     * @param context The context to search for transformers.
     * @param location The location to search at.
     *
     * @return The transformers found at {@code location} in {@code context} or {@code null}, if no transformers are
     * found.
     *
     * @throws NullPointerException if {@code context} or {@code location} is {@code null}.
     * @throws ModelException if getting the transformers fails.
     */
    public List<Transformer> findTransformers( final ModelContext context, final String location ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( location == null )
        {
            throw new NullPointerException( "location" );
        }

        try
        {
            final long t0 = System.currentTimeMillis();
            final List<Transformer> transformers = new LinkedList<Transformer>();
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Enumeration<URL> resources = context.findResources( location );
            final ErrorListener errorListener = new ErrorListener()
            {

                public void warning( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.WARNING ) )
                    {
                        context.log( Level.WARNING, getMessage( exception ), exception );
                    }
                }

                public void error( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.SEVERE ) )
                    {
                        context.log( Level.SEVERE, getMessage( exception ), exception );
                    }

                    throw exception;
                }

                public void fatalError( final TransformerException exception ) throws TransformerException
                {
                    if ( context.isLoggable( Level.SEVERE ) )
                    {
                        context.log( Level.SEVERE, getMessage( exception ), exception );
                    }

                    throw exception;
                }

            };

            transformerFactory.setErrorListener( errorListener );

            int count = 0;
            while ( resources.hasMoreElements() )
            {
                count++;
                final URL url = resources.nextElement();

                if ( context.isLoggable( Level.FINEST ) )
                {
                    context.log( Level.FINEST, getMessage( "processing", url.toExternalForm() ), null );
                }

                final Transformer transformer =
                    transformerFactory.newTransformer( new StreamSource( url.toURI().toASCIIString() ) );

                transformer.setErrorListener( errorListener );

                for ( Map.Entry<Object, Object> e : System.getProperties().entrySet() )
                {
                    transformer.setParameter( e.getKey().toString(), e.getValue() );
                }

                transformers.add( transformer );
            }

            if ( context.isLoggable( Level.FINE ) )
            {
                context.log( Level.FINE, getMessage( "contextReport", count, location,
                                                     Long.valueOf( System.currentTimeMillis() - t0 ) ), null );

            }

            return transformers.isEmpty() ? null : transformers;
        }
        catch ( final URISyntaxException e )
        {
            throw new ModelException( getMessage( e ), e );
        }
        catch ( final TransformerConfigurationException e )
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
     * {@inheritDoc}
     *
     * @see #isEnabled()
     * @see #getTransformerLocation()
     * @see #findTransformers(org.jomc.modlet.ModelContext, java.lang.String)
     * @see #ENABLED_ATTRIBUTE_NAME
     * @see #TRANSFORMER_LOCATION_ATTRIBUTE_NAME
     */
    public Modlets processModlets( final ModelContext context, final Modlets modlets ) throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( modlets == null )
        {
            throw new NullPointerException( "modlets" );
        }

        try
        {
            Modlets processed = null;

            boolean contextEnabled = this.isEnabled();
            if ( DEFAULT_ENABLED == contextEnabled
                 && context.getAttribute( ENABLED_ATTRIBUTE_NAME ) instanceof Boolean )
            {
                contextEnabled = (Boolean) context.getAttribute( ENABLED_ATTRIBUTE_NAME );
            }

            String contextTransformerLocation = this.getTransformerLocation();
            if ( DEFAULT_TRANSFORMER_LOCATION.equals( contextTransformerLocation )
                 && context.getAttribute( TRANSFORMER_LOCATION_ATTRIBUTE_NAME ) instanceof String )
            {
                contextTransformerLocation = (String) context.getAttribute( TRANSFORMER_LOCATION_ATTRIBUTE_NAME );
            }

            if ( contextEnabled )
            {
                final org.jomc.modlet.ObjectFactory objectFactory = new org.jomc.modlet.ObjectFactory();
                final JAXBContext jaxbContext = context.createContext( ModletObject.MODEL_PUBLIC_ID );
                final List<Transformer> transformers = this.findTransformers( context, contextTransformerLocation );

                if ( transformers != null )
                {
                    processed = modlets.clone();

                    for ( int i = 0, s0 = transformers.size(); i < s0; i++ )
                    {
                        final JAXBElement<Modlets> e = objectFactory.createModlets( processed );
                        final JAXBSource source = new JAXBSource( jaxbContext, e );
                        final JAXBResult result = new JAXBResult( jaxbContext );
                        transformers.get( i ).transform( source, result );

                        if ( result.getResult() instanceof JAXBElement<?>
                             && ( (JAXBElement<?>) result.getResult() ).getValue() instanceof Modlets )
                        {
                            processed = (Modlets) ( (JAXBElement<?>) result.getResult() ).getValue();
                        }
                        else
                        {
                            throw new ModelException( getMessage( "illegalTransformationResult" ) );
                        }
                    }
                }
            }
            else if ( context.isLoggable( Level.FINER ) )
            {
                context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName() ), null );
            }

            return processed;
        }
        catch ( final TransformerException e )
        {
            String message = getMessage( e );
            if ( message == null && e.getException() != null )
            {
                message = getMessage( e.getException() );
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

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModletProcessor.class.getName().replace( '.', '/' ), Locale.getDefault() ).getString( key ), args );

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
