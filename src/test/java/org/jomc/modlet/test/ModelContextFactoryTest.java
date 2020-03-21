/*
 *   Copyright (C) 2012 Christian Schulte <cs@schulte.it>
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
package org.jomc.modlet.test;

import java.util.concurrent.Callable;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModelContextFactoryError;
import org.jomc.modlet.test.support.ClassCastExceptionModelContextFactory;
import org.jomc.modlet.test.support.IllegalAccessExceptionModelContextFactory;
import org.jomc.modlet.test.support.InstantiationExceptionModelContextFactory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.ModelContextFactory}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelContextFactoryTest
{

    /**
     * Constant for the name of the default {@code ModelContextFactory} implementation.
     */
    private static final String DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME = "org.jomc.modlet.DefaultModelContextFactory";

    /**
     * Constant for the name of the system property controlling {@code ModelContextFactory} implementations.
     */
    private static final String MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY =
        "org.jomc.modlet.ModelContextFactory";

    /**
     * Creates a new {@code ModelContextFactoryTest} instance.
     */
    public ModelContextFactoryTest()
    {
        super();
    }

    @Test
    public final void testModelContextFactoryDefaultInstance() throws Exception
    {
        assertNotNull( ModelContextFactory.newInstance() );
        assertEquals( DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME,
                      ModelContextFactory.newInstance().getClass().getName() );

        assertNotNull( ModelContextFactory.newInstance( DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME ) );

        assertEquals(
            DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME,
            ModelContextFactory.newInstance( DEFAULT_MODEL_CONTEXT_FACTORY_CLASS_NAME ).getClass().getName() );

    }

    @Test
    public final void testModelContextFactoryClassNotFound() throws Exception
    {
        assertModelContextFactoryError( ()  ->
        {
            System.setProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY, "DOES_NOT_EXIST" );
            ModelContextFactory.newInstance();
            return null;
        }, ()  -> System.clearProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY ) );

        assertModelContextFactoryError( ()  -> ModelContextFactory.newInstance( "DOES_NOT_EXIST" ) );
    }

    @Test
    public final void testModelContextFactoryIllegalClass() throws Exception
    {
        assertModelContextFactoryError( ()  ->
        {
            System.setProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY,
                                ClassCastExceptionModelContextFactory.class.getName() );

            ModelContextFactory.newInstance();
            return null;
        }, ()  -> System.clearProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY ) );

        assertModelContextFactoryError( ()  -> ModelContextFactory.newInstance(
            ClassCastExceptionModelContextFactory.class.getName() ) );

    }

    @Test
    public final void testModelContextFactoryInstantiationException() throws Exception
    {
        assertModelContextFactoryError( ()  ->
        {
            System.setProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY,
                                InstantiationExceptionModelContextFactory.class.getName() );

            ModelContextFactory.newInstance();
            return null;
        }, ()  -> System.clearProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY ) );

        assertModelContextFactoryError( ()  -> ModelContextFactory.newInstance(
            InstantiationExceptionModelContextFactory.class.getName() ) );

    }

    @Test
    public final void testModelContextFactoryIllegalAccessException() throws Exception
    {
        assertModelContextFactoryError( ()  ->
        {
            System.setProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY,
                                IllegalAccessExceptionModelContextFactory.class.getName() );

            ModelContextFactory.newInstance();
            return null;
        }, ()  -> System.clearProperty( MODEL_CONTEXT_FACTORY_CLASS_NAME_PROPERTY ) );

        assertModelContextFactoryError( ()  -> ModelContextFactory.newInstance(
            IllegalAccessExceptionModelContextFactory.class.getName() ) );

    }

    @Test
    public final void testModelContextNotNull() throws Exception
    {
        assertNotNull( ModelContextFactory.newInstance().newModelContext() );
        assertNotNull( ModelContextFactory.newInstance().newModelContext( null ) );
        assertNotNull( ModelContextFactory.newInstance().newModelContext( this.getClass().getClassLoader() ) );
    }

    private static void assertModelContextFactoryError( final Callable<?> testcase, final Callable<?>... _finally )
        throws Exception
    {
        try
        {
            testcase.call();
            fail( "Expected 'ModelContextFactoryError' exception not thrown." );
        }
        catch ( final ModelContextFactoryError e )
        {
            System.out.println( e );
            assertNotNull( e.getMessage() );
        }
        finally
        {
            if ( _finally.length > 0 )
            {
                _finally[0].call();
            }
        }
    }

}
