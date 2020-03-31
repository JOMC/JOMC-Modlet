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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Default {@code ServiceFactory} implementation.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#createServiceObjects(java.lang.String, java.lang.String, java.lang.Class)
 * @since 1.9
 */
public class DefaultServiceFactory implements ServiceFactory
{

    /**
     * Constant for the name of the system property controlling property {@code defaultEnabled}.
     *
     * @see #isDefaultEnabled()
     * @since 1.11.0
     */
    private static final String DEFAULT_ENABLED_PROPERTY_NAME =
        "org.jomc.modlet.DefaultServiceFactory.defaultEnabled";

    /**
     * Default value of the flag indicating the factory is enabled by default.
     *
     * @see #isDefaultEnabled()
     * @since 1.11.0
     */
    private static final Boolean DEFAULT_ENABLED = Boolean.TRUE;

    /**
     * Flag indicating the factory is enabled by default.
     * @since 1.11.0
     */
    private static volatile Boolean defaultEnabled;

    /**
     * Flag indicating the factory is enabled.
     * @since 1.11.0
     */
    private volatile Boolean enabled;

    /**
     * Constant for the name of the system property controlling property {@code defaultOrdinal}.
     *
     * @see #getDefaultOrdinal()
     */
    private static final String DEFAULT_ORDINAL_PROPERTY_NAME =
        "org.jomc.modlet.DefaultServiceFactory.defaultOrdinal";

    /**
     * Default value of the ordinal number of the factory.
     *
     * @see #getDefaultOrdinal()
     */
    private static final Integer DEFAULT_ORDINAL = 0;

    /**
     * Default ordinal number of the factory.
     */
    private static volatile Integer defaultOrdinal;

    /**
     * Ordinal number of the factory.
     */
    private volatile Integer ordinal;

    /**
     * Creates a new {@code DefaultServiceFactory} instance.
     */
    public DefaultServiceFactory()
    {
        super();
    }

    /**
     * Gets a flag indicating the factory is enabled by default.
     * <p>
     * The default enabled flag is controlled by system property
     * {@code org.jomc.modlet.DefaultServiceFactory.defaultEnabled} holding a value indicating the factory is
     * enabled by default. If that property is not set, the {@code true} default is returned.
     * </p>
     *
     * @return {@code true}, if the factory is enabled by default; {@code false}, if the factory is disabled by
     * default.
     *
     * @see #isEnabled()
     * @see #setDefaultEnabled(java.lang.Boolean)
     * @since 1.11.0
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
     * Sets the flag indicating the factory is enabled by default.
     *
     * @param value The new value of the flag indicating the factory is enabled by default or {@code null}.
     *
     * @see #isDefaultEnabled()
     * @since 1.11.0
     */
    public static void setDefaultEnabled( final Boolean value )
    {
        defaultEnabled = value;
    }

    /**
     * Gets a flag indicating the factory is enabled.
     *
     * @return {@code true}, if the factory is enabled; {@code false}, if the factory is disabled.
     *
     * @see #isDefaultEnabled()
     * @see #setEnabled(java.lang.Boolean)
     * @since 1.11.0
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
     * Sets the flag indicating the factory is enabled.
     *
     * @param value The new value of the flag indicating the factory is enabled or {@code null}.
     *
     * @see #isEnabled()
     * @since 1.11.0
     */
    public final void setEnabled( final Boolean value )
    {
        this.enabled = value;
    }

    /**
     * Gets the default ordinal number of the factory.
     * <p>
     * The default ordinal number is controlled by system property
     * {@code org.jomc.modlet.DefaultServiceFactory.defaultOrdinal} holding the default ordinal number of the
     * factory. If that property is not set, the {@code 0} default is returned.
     * </p>
     *
     * @return The default ordinal number of the factory.
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
     * Sets the default ordinal number of the factory.
     *
     * @param value The new default ordinal number of the factory or {@code null}.
     *
     * @see #getDefaultOrdinal()
     */
    public static void setDefaultOrdinal( final Integer value )
    {
        defaultOrdinal = value;
    }

    /**
     * Gets the ordinal number of the factory.
     *
     * @return The ordinal number of the factory.
     *
     * @see #getDefaultOrdinal()
     * @see #setOrdinal(java.lang.Integer)
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
     * Sets the ordinal number of the factory.
     *
     * @param value The new ordinal number of the factory or {@code null}.
     *
     * @see #getOrdinal()
     */
    public final void setOrdinal( final Integer value )
    {
        this.ordinal = value;
    }

    @Override
    public <T> Optional<T> createServiceObject( final ModelContext context, final Service service, final Class<T> type )
        throws ModelException
    {
        Objects.requireNonNull( context, "context" );
        Objects.requireNonNull( service, "service" );
        Objects.requireNonNull( type, "type" );

        try
        {
            final T serviceObject;
            if ( this.isEnabled() )
            {
                final Optional<Class<?>> clazz = context.findClass( service.getClazz() );

                if ( !clazz.isPresent() )
                {
                    throw new ModelException( getMessage( "serviceNotFound", service.getOrdinal(), service.
                                                          getIdentifier(),
                                                          service.getClazz() ) );

                }

                if ( !type.isAssignableFrom( clazz.get() ) )
                {
                    throw new ModelException( getMessage( "illegalService", service.getOrdinal(), service.
                                                          getIdentifier(),
                                                          service.getClazz(), type.getName() ) );

                }

                serviceObject = clazz.get().asSubclass( type ).newInstance();

                if ( !service.getProperty().isEmpty() )
                {
                    try ( final Stream<Property> st0 = service.getProperty().parallelStream().unordered() )
                    {
                        final class InitPropertyFailure extends RuntimeException
                        {

                            public InitPropertyFailure( final Throwable cause )
                            {
                                super( cause );
                            }

                        }

                        try
                        {
                            st0.forEach( p  ->
                            {
                                try
                                {
                                    initProperty( context, serviceObject, p.getName(), p.getValue() );
                                }
                                catch ( final ModelException e )
                                {
                                    throw new InitPropertyFailure( e );
                                }
                            } );
                        }
                        catch ( final InitPropertyFailure e )
                        {
                            if ( e.getCause() instanceof ModelException )
                            {
                                throw new ModelException( e.getCause().getMessage(), e.getCause() );
                            }

                            throw new AssertionError( e );
                        }
                    }
                }
            }
            else
            {
                serviceObject = null;

                if ( context.isLoggable( Level.FINER ) )
                {
                    context.log( Level.FINER, getMessage( "disabled", this.getClass().getSimpleName() ), null );
                }
            }

            return Optional.of( serviceObject );
        }
        catch ( final InstantiationException | IllegalAccessException e )
        {
            throw new ModelException( getMessage( "failedCreatingObject", service.getClazz() ), e );
        }
    }

    private <T> void initProperty( final ModelContext context, final T object, final String propertyName,
                                   final String propertyValue )
        throws ModelException
    {
        Objects.requireNonNull( object, "object" );
        Objects.requireNonNull( propertyName, "propertyName" );

        try
        {
            final char[] chars = propertyName.toCharArray();

            if ( Character.isLowerCase( chars[0] ) )
            {
                chars[0] = Character.toUpperCase( chars[0] );
            }

            final String methodNameSuffix = String.valueOf( chars );
            Method getterMethod;

            try
            {
                getterMethod = object.getClass().getMethod( "get" + methodNameSuffix );
            }
            catch ( final NoSuchMethodException e )
            {
                if ( context.isLoggable( Level.FINEST ) )
                {
                    context.log( Level.FINEST, null, e );
                }

                getterMethod = null;
            }

            if ( getterMethod == null )
            {
                try
                {
                    getterMethod = object.getClass().getMethod( "is" + methodNameSuffix );
                }
                catch ( final NoSuchMethodException e )
                {
                    if ( context.isLoggable( Level.FINEST ) )
                    {
                        context.log( Level.FINEST, null, e );
                    }

                    getterMethod = null;
                }
            }

            if ( getterMethod == null )
            {
                throw new ModelException( getMessage( "getterMethodNotFound", object.getClass().getName(),
                                                      propertyName ) );

            }

            final Class<?> propertyType = getterMethod.getReturnType();
            Class<?> boxedPropertyType = propertyType;
            Class<?> unboxedPropertyType = propertyType;

            if ( Boolean.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Boolean.class;
            }
            else if ( Character.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Character.class;
            }
            else if ( Byte.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Byte.class;
            }
            else if ( Short.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Short.class;
            }
            else if ( Integer.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Integer.class;
            }
            else if ( Long.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Long.class;
            }
            else if ( Float.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Float.class;
            }
            else if ( Double.TYPE.equals( propertyType ) )
            {
                boxedPropertyType = Double.class;
            }

            if ( Boolean.class.equals( propertyType ) )
            {
                unboxedPropertyType = Boolean.TYPE;
            }
            else if ( Character.class.equals( propertyType ) )
            {
                unboxedPropertyType = Character.TYPE;
            }
            else if ( Byte.class.equals( propertyType ) )
            {
                unboxedPropertyType = Byte.TYPE;
            }
            else if ( Short.class.equals( propertyType ) )
            {
                unboxedPropertyType = Short.TYPE;
            }
            else if ( Integer.class.equals( propertyType ) )
            {
                unboxedPropertyType = Integer.TYPE;
            }
            else if ( Long.class.equals( propertyType ) )
            {
                unboxedPropertyType = Long.TYPE;
            }
            else if ( Float.class.equals( propertyType ) )
            {
                unboxedPropertyType = Float.TYPE;
            }
            else if ( Double.class.equals( propertyType ) )
            {
                unboxedPropertyType = Double.TYPE;
            }

            Method setterMethod;

            try
            {
                setterMethod = object.getClass().getMethod( "set" + methodNameSuffix, boxedPropertyType );
            }
            catch ( final NoSuchMethodException e )
            {
                if ( context.isLoggable( Level.FINEST ) )
                {
                    context.log( Level.FINEST, null, e );
                }

                setterMethod = null;
            }

            if ( setterMethod == null && !boxedPropertyType.equals( unboxedPropertyType ) )
            {
                try
                {
                    setterMethod = object.getClass().getMethod( "set" + methodNameSuffix, unboxedPropertyType );
                }
                catch ( final NoSuchMethodException e )
                {
                    if ( context.isLoggable( Level.FINEST ) )
                    {
                        context.log( Level.FINEST, null, e );
                    }

                    setterMethod = null;
                }
            }

            if ( setterMethod == null )
            {
                throw new ModelException( getMessage( "setterMethodNotFound", object.getClass().getName(),
                                                      propertyName ) );

            }

            if ( boxedPropertyType.equals( Character.class ) )
            {
                if ( propertyValue == null || propertyValue.length() != 1 )
                {
                    throw new ModelException( getMessage( "unsupportedCharacterValue", object.getClass().getName(),
                                                          propertyName ) );

                }

                setterMethod.invoke( object, propertyValue.charAt( 0 ) );
            }
            else if ( propertyValue != null )
            {
                invocation:
                {
                    if ( boxedPropertyType.equals( String.class ) )
                    {
                        setterMethod.invoke( object, propertyValue );
                        break invocation;
                    }

                    try
                    {
                        setterMethod.invoke( object, boxedPropertyType.getConstructor( String.class ).
                                             newInstance( propertyValue ) );

                        break invocation;
                    }
                    catch ( final NoSuchMethodException e1 )
                    {
                        if ( context.isLoggable( Level.FINEST ) )
                        {
                            context.log( Level.FINEST, null, e1 );
                        }
                    }

                    try
                    {
                        final Method valueOf = boxedPropertyType.getMethod( "valueOf", String.class );

                        if ( Modifier.isStatic( valueOf.getModifiers() )
                                 && ( valueOf.getReturnType().equals( boxedPropertyType )
                                      || valueOf.getReturnType().equals( unboxedPropertyType ) ) )
                        {
                            setterMethod.invoke( object, valueOf.invoke( null, propertyValue ) );
                            break invocation;
                        }
                    }
                    catch ( final NoSuchMethodException e2 )
                    {
                        if ( context.isLoggable( Level.FINEST ) )
                        {
                            context.log( Level.FINEST, null, e2 );
                        }
                    }

                    throw new ModelException( getMessage( "unsupportedPropertyType", object.getClass().getName(),
                                                          propertyName, propertyType.getName() ) );

                }
            }
            else
            {
                setterMethod.invoke( object, (Object) null );
            }
        }
        catch ( final IllegalAccessException | InvocationTargetException | InstantiationException e )
        {
            throw new ModelException( getMessage( "failedSettingProperty", propertyName, object.toString(),
                                                  object.getClass().getName() ), e );

        }
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            DefaultServiceFactory.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

}
