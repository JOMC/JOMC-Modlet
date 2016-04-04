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
import java.util.ResourceBundle;
import java.util.logging.Level;

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
    private Integer ordinal;

    /**
     * Creates a new {@code DefaultServiceFactory} instance.
     */
    public DefaultServiceFactory()
    {
        super();
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

    public <T> T createServiceObject( final ModelContext context, final Service service, final Class<T> type )
        throws ModelException
    {
        if ( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if ( service == null )
        {
            throw new NullPointerException( "service" );
        }
        if ( type == null )
        {
            throw new NullPointerException( "type" );
        }

        try
        {
            final Class<?> clazz = context.findClass( service.getClazz() );

            if ( clazz == null )
            {
                throw new ModelException( getMessage( "serviceNotFound", service.getOrdinal(), service.
                                                      getIdentifier(),
                                                      service.getClazz() ) );

            }

            if ( !type.isAssignableFrom( clazz ) )
            {
                throw new ModelException( getMessage( "illegalService", service.getOrdinal(), service.
                                                      getIdentifier(),
                                                      service.getClazz(), type.getName() ) );

            }

            final T serviceObject = clazz.asSubclass( type ).newInstance();

            for ( int i = 0, s0 = service.getProperty().size(); i < s0; i++ )
            {
                final Property p = service.getProperty().get( i );
                this.initProperty( context, serviceObject, p.getName(), p.getValue() );
            }

            return serviceObject;
        }
        catch ( final InstantiationException e )
        {
            throw new ModelException( getMessage( "failedCreatingObject", service.getClazz() ), e );
        }
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( getMessage( "failedCreatingObject", service.getClazz() ), e );
        }
    }

    private <T> void initProperty( final ModelContext context, final T object, final String propertyName,
                                   final String propertyValue )
        throws ModelException
    {
        if ( object == null )
        {
            throw new NullPointerException( "object" );
        }
        if ( propertyName == null )
        {
            throw new NullPointerException( "propertyName" );
        }

        try
        {
            final char[] chars = propertyName.toCharArray();

            if ( Character.isLowerCase( chars[0] ) )
            {
                chars[0] = Character.toUpperCase( chars[0] );
            }

            final String methodNameSuffix = String.valueOf( chars );
            Method getterMethod = null;

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

            Method setterMethod = null;

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
        catch ( final IllegalAccessException e )
        {
            throw new ModelException( getMessage( "failedSettingProperty", propertyName, object.toString(),
                                                  object.getClass().getName() ), e );

        }
        catch ( final InvocationTargetException e )
        {
            throw new ModelException( getMessage( "failedSettingProperty", propertyName, object.toString(),
                                                  object.getClass().getName() ), e );

        }
        catch ( final InstantiationException e )
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
