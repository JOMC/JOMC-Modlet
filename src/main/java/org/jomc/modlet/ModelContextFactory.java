/*
 *   Copyright (C) Christian Schulte, 2012-22
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Interface to creating model contexts.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.2
 */
public abstract class ModelContextFactory
{

    /** Constant for the name of the default {@code ModelContextFactory} implementation. */
    private static final String DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME = "org.jomc.modlet.DefaultModelContextFactory";

    /** Constant for the name of the system property controlling {@code ModelContextFactory} implementations. */
    private static final String MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY =
        "org.jomc.modlet.ModelContextFactory";

    /** Creates a new {@code ModelContextFactory} instance. */
    protected ModelContextFactory()
    {
        super();
    }

    /**
     * Creates a new {@code ModelContextFactory} instance.
     * <p>The name of the class providing the {@code ModelContextFactory} implementation loaded by this method is
     * controlled by system property {@code org.jomc.modlet.ModelContextFactory}. If that property is not set, this
     * methods returns a new default instance.</p>
     *
     * @return A new {@code ModelContextFactory} instance.
     *
     * @throws ModelContextFactoryError if creating a new instance fails.
     */
    public static ModelContextFactory newInstance() throws ModelContextFactoryError
    {
        return newInstance( AccessController.doPrivileged( new PrivilegedAction<String>()
        {

            public String run()
            {
                return System.getProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY,
                                           DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME );

            }

        } ) );
    }

    /**
     * Creates a new {@code ModelContextFactory} instance.
     *
     * @param factoryClassName The name of the {@code ModelContextFactory} class to create an instance of.
     *
     * @return A new {@code ModelContextFactory} instance.
     *
     * @throws NullPointerException if {@code factoryClassName} is {@code null}.
     * @throws ModelContextFactoryError if creating a new instance fails.
     */
    public static ModelContextFactory newInstance( final String factoryClassName ) throws ModelContextFactoryError
    {
        if ( factoryClassName == null )
        {
            throw new NullPointerException( "factoryClassName" );
        }

        try
        {
            final Class<?> factoryClass = Class.forName( factoryClassName );

            if ( !ModelContextFactory.class.isAssignableFrom( factoryClass ) )
            {
                throw new ModelContextFactoryError( getMessage( "illegalFactory", factoryClassName,
                                                                ModelContextFactory.class.getName() ) );

            }

            return factoryClass.asSubclass( ModelContextFactory.class ).newInstance();
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ModelContextFactoryError( getMessage( "classNotFound", factoryClassName ), e );
        }
        catch ( final InstantiationException e )
        {
            final String message = getMessage( e );
            throw new ModelContextFactoryError( getMessage( "instantiationException", factoryClassName,
                                                            message != null ? " " + message : "" ), e );

        }
        catch ( final IllegalAccessException e )
        {
            final String message = getMessage( e );
            throw new ModelContextFactoryError( getMessage( "accessDenied", factoryClassName,
                                                            message != null ? " " + message : "" ), e );

        }
    }

    /**
     * Creates a new {@code ModelContext} instance.
     *
     * @return A new {@code ModelContext} instance.
     */
    public abstract ModelContext newModelContext();

    /**
     * Creates a new {@code ModelContext} instance.
     *
     * @param classLoader The class loader to create a new instance with or {@code null}, to create a new instance
     * using the bootstrap class loader.
     *
     * @return A new {@code ModelContext} instance for {@code classLoader}.
     */
    public abstract ModelContext newModelContext( final ClassLoader classLoader );

    private static String getMessage( final String key, final Object... args )
    {
        return MessageFormat.format( ResourceBundle.getBundle( ModelContextFactory.class.getName().replace( '.', '/' ),
                                                               Locale.getDefault() ).getString( key ), args );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null ? t.getMessage() != null ? t.getMessage() : getMessage( t.getCause() ) : null;
    }

}
