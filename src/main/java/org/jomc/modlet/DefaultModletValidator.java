/*
 *   Copyright (C) 2015 Christian Schulte <cs@schulte.it>
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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * Default {@code ModletValidator} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#validateModlets(org.jomc.modlet.Modlets)
 * @since 1.9
 */
public class DefaultModletValidator implements ModletValidator
{

    /**
     * Constant for the name of the model context attribute backing property {@code enabled}.
     *
     * @see #validateModlets(org.jomc.modlet.ModelContext, org.jomc.modlet.Modlets)
     * @see ModelContext#getAttribute(java.lang.String)
     */
    public static final String ENABLED_ATTRIBUTE_NAME = "org.jomc.modlet.DefaultModletValidator.enabledAttribute";

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     *
     * @see #isDefaultEnabled()
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletValidator.defaultEnabled";

    /**
     * Default value of the flag indicating the validator is enabled by default.
     *
     * @see #isDefaultEnabled()
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /**
     * Flag indicating the validator is enabled by default.
     */
    private static volatile Boolean defaultEnabled;

    /**
     * Flag indicating the validator is enabled.
     */
    private volatile Boolean enabled;

    /**
     * Constant for the name of the system property controlling property {@code defaultOrdinal}.
     *
     * @see #getDefaultOrdinal()
     */
    private static final String DEFAULT_ORDINAL_PROPERTY_NAME =
        "org.jomc.modlet.DefaultModletValidator.defaultOrdinal";

    /**
     * Default value of the ordinal number of the validator.
     *
     * @see #getDefaultOrdinal()
     */
    private static final Integer DEFAULT_ORDINAL = 0;

    /**
     * Default ordinal number of the validator.
     */
    private static volatile Integer defaultOrdinal;

    /**
     * Ordinal number of the validator.
     */
    private volatile Integer ordinal;

    /**
     * Creates a new {@code DefaultModletValidator} instance.
     */
    public DefaultModletValidator()
    {
        super();
    }

    /**
     * Gets a flag indicating the validator is enabled by default.
     * <p>
     * The default enabled flag is controlled by system property
     * {@code org.jomc.modlet.DefaultModletValidator.defaultEnabled} holding a value indicating the validator is
     * enabled by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the validator is enabled by default; {@code false}, if the validator is disabled by
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
     * Sets the flag indicating the validator is enabled by default.
     *
     * @param value The new value of the flag indicating the validator is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the validator is enabled.
     *
     * @return {@code true}, if the validator is enabled; {@code false}, if the validator is disabled.
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
     * Sets the flag indicating the validator is enabled.
     *
     * @param value The new value of the flag indicating the validator is enabled or {@code null}.
     *
     * @see #isEnabled()
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default ordinal number of the validator.
     * <p>
     * The default ordinal number is controlled by system property
     * {@code org.jomc.modlet.DefaultModletValidator.defaultOrdinal} holding the default ordinal number of the
     * validator. If that property is not set, the {@code 0} default is returned.
     * </p>
     *
     * @return The default ordinal number of the validator.
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
     * Sets the default ordinal number of the validator.
     *
     * @param value The new default ordinal number of the validator or {@code null}.
     *
     * @see #getDefaultOrdinal()
     */
    public static void setDefaultOrdinal( final Integer value )
    {
        defaultOrdinal = value;
    }

    /**
     * Gets the ordinal number of the validator.
     *
     * @return The ordinal number of the validator.
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
     * Sets the ordinal number of the validator.
     *
     * @param value The new ordinal number of the validator or {@code null}.
     *
     * @see #getOrdinal()
     */
    public final void setOrdinal( final Integer value )
    {
        this.ordinal = value;
    }

    @Override
    public ModelValidationReport validateModlets( final ModelContext context, final Modlets modlets )
        throws ModelException
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
            boolean contextEnabled = this.isEnabled();
            if ( DEFAULT_ENABLED == contextEnabled
                     && context.getAttribute( ENABLED_ATTRIBUTE_NAME ) instanceof Boolean )
            {
                contextEnabled = (Boolean) context.getAttribute( ENABLED_ATTRIBUTE_NAME );
            }

            final ModelValidationReport report = new ModelValidationReport();

            if ( contextEnabled )
            {
                final javax.xml.validation.Schema modletSchema = context.createSchema( ModletObject.MODEL_PUBLIC_ID );
                final Validator validator = modletSchema.newValidator();
                validator.setErrorHandler( new ModelErrorHandler( context, report ) );
                validator.validate( new JAXBSource( context.createContext( ModletObject.MODEL_PUBLIC_ID ),
                                                    new ObjectFactory().createModlets( modlets ) ) );

                final Map<String, Schemas> schemasByModel = new HashMap<String, Schemas>( 128 );
                final Map<Schema, Modlet> modletBySchema = new HashMap<Schema, Modlet>( 128 );

                for ( final Modlet modlet : modlets.getModlet() )
                {
                    if ( modlet.getSchemas() != null )
                    {
                        Schemas modelSchemas = schemasByModel.get( modlet.getModel() );

                        if ( modelSchemas == null )
                        {
                            modelSchemas = new Schemas();
                            schemasByModel.put( modlet.getModel(), modelSchemas );
                        }

                        for ( int i = 0, s0 = modlet.getSchemas().getSchema().size(); i < s0; i++ )
                        {
                            final Schema schema = modlet.getSchemas().getSchema().get( i );
                            modletBySchema.put( schema, modlet );

                            final Schema existingPublicIdSchema =
                                modelSchemas.getSchemaByPublicId( schema.getPublicId() );

                            final Schema existingSystemIdSchema =
                                modelSchemas.getSchemaBySystemId( schema.getSystemId() );

                            if ( existingPublicIdSchema != null )
                            {
                                final Modlet modletOfSchema = modletBySchema.get( existingPublicIdSchema );
                                final ModelValidationReport.Detail detail =
                                    new ModelValidationReport.Detail(
                                        "MODEL_SCHEMA_PUBLIC_ID_CONSTRAINT",
                                        Level.SEVERE,
                                        getMessage( "modelSchemaPublicIdConstraint", modlet.getModel(),
                                                    modlet.getName(), modletOfSchema.getName(),
                                                    schema.getPublicId() ),
                                        new ObjectFactory().createModlet( modlet ) );

                                report.getDetails().add( detail );
                            }

                            if ( existingSystemIdSchema != null )
                            {
                                final Modlet modletOfSchema = modletBySchema.get( existingSystemIdSchema );
                                final ModelValidationReport.Detail detail =
                                    new ModelValidationReport.Detail(
                                        "MODEL_SCHEMA_SYSTEM_ID_CONSTRAINT",
                                        Level.SEVERE,
                                        getMessage( "modelSchemaSystemIdConstraint", modlet.getModel(),
                                                    modlet.getName(), modletOfSchema.getName(),
                                                    schema.getSystemId() ),
                                        new ObjectFactory().createModlet( modlet ) );

                                report.getDetails().add( detail );
                            }

                            modelSchemas.getSchema().add( schema );
                        }
                    }
                }
            }
            else if ( context.isLoggable( Level.FINER ) )
            {
                context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName() ), null );
            }

            return report;
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

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultModletValidator.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

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
