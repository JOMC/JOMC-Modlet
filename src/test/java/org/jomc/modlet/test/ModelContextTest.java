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
package org.jomc.modlet.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import org.jomc.modlet.DefaultModletProvider;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.Modlet;
import org.jomc.modlet.ModletObject;
import org.jomc.modlet.ModletProvider;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.Property;
import org.jomc.modlet.Service;
import org.jomc.modlet.test.support.ClassCastExceptionModelContext;
import org.jomc.modlet.Services;
import org.jomc.modlet.test.support.IllegalAccessExceptionModelContext;
import org.jomc.modlet.test.support.InstantiationExceptionModelContext;
import org.jomc.modlet.test.support.InvocationTargetExceptionModelContext;
import org.jomc.modlet.test.support.NoSuchMethodExceptionModelContext;
import org.jomc.modlet.test.support.TestModletProvider;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ModelContext} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelContextTest
{

    /**
     * Constant for the name of a modlet provided without {@code ModletProvider}.
     */
    public static final String DEFAULT_MODLET_NAME;

    /**
     * Constant for the absolute location of an existing test resource.
     */
    private static final String TEST_RESOURCE_LOCATION =
        ModelContextTest.class.getName().replace( '.', '/' ) + ".properties";

    static
    {
        InputStream in = null;

        try
        {
            final Properties p = new Properties();
            in = ModelContextTest.class.getResourceAsStream( "/" + TEST_RESOURCE_LOCATION );
            assertNotNull( "Expected '" + TEST_RESOURCE_LOCATION + "' not found.", in );
            p.load( in );

            in.close();
            in = null;

            final String defaultModletName = p.getProperty( "defaultModletName" );
            assertNotNull( "Expected 'defaultModletName' property not found.", defaultModletName );
            DEFAULT_MODLET_NAME = defaultModletName;
        }
        catch ( final IOException e )
        {
            throw new AssertionError( e );
        }
        finally
        {
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( final IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * The {@code ModelContext} instance tests are performed with.
     */
    private volatile ModelContext modelContext;

    /**
     * The {@code ExecutorService} backing the tests.
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    private volatile ExecutorService executorService;

    /**
     * Creates a new {@code ModelContextTest} instance.
     */
    public ModelContextTest()
    {
        super();
    }

    /**
     * Gets the {@code ModelContext} instance tests are performed with.
     *
     * @return The {@code ModelContext} instance tests are performed with.
     *
     * @throws ModelException if creating a new instance fails.
     *
     * @see #newModelContext()
     */
    public ModelContext getModelContext() throws ModelException
    {
        if ( this.modelContext == null )
        {
            this.modelContext = this.newModelContext();
            this.modelContext.setExecutorService( this.getExecutorService() );
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, final String message, final Throwable t )
                {
                    super.onLog( level, message, t );
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                }

            } );

        }

        return this.modelContext;
    }

    /**
     * Creates a new {@code ModelContext} instance to test.
     *
     * @return A new {@code ModelContext} instance to test.
     *
     * @see #getModelContext()
     */
    protected ModelContext newModelContext()
    {
        return ModelContextFactory.newInstance().newModelContext();
    }

    /**
     * Gets the {@code ExecutorService} backing the tests.
     *
     * @return The {@code ExecutorService} backing the tests.
     *
     * @see #newExecutorService()
     * @since 1.10
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    public final ExecutorService getExecutorService()
    {
        if ( this.executorService == null )
        {
            this.executorService = this.newExecutorService();
        }

        return this.executorService;
    }

    /**
     * Creates a new {@code ExecutorService} backing the tests.
     *
     * @return A new {@code ExecutorService} backing the tests, or {@code null}.
     *
     * @see #getExecutorService()
     * @since 1.10
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    protected ExecutorService newExecutorService()
    {
        return null;
    }

    /**
     * Shuts down the {@code ExecutorService} backing the tests, if not {@code null}.
     */
    @After
    public final void shutdown()
    {
        if ( this.executorService != null )
        {
            this.executorService.shutdown();
            this.executorService = null;
        }
    }

    @Test
    public final void testAttributes() throws Exception
    {
        try
        {
            this.getModelContext().getAttribute( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().getAttribute( null, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().setAttribute( "TEST", null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().setAttribute( null, "TEST" );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().clearAttribute( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNull( this.getModelContext().getAttribute( "ATTRIBUTE" ) );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
        assertNull( this.getModelContext().setAttribute( "ATTRIBUTE", "VALUE" ) );
        assertEquals( "VALUE", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
        assertEquals( "VALUE", this.getModelContext().setAttribute( "ATTRIBUTE", "TEST" ) );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE" ) );
        this.getModelContext().clearAttribute( "ATTRIBUTE" );
        assertNull( this.getModelContext().getAttribute( "ATTRIBUTE" ) );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
    }

    @Test
    public final void testFindClass() throws Exception
    {
        try
        {
            this.getModelContext().findClass( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertEquals( Object.class, this.getModelContext().findClass( "java.lang.Object" ) );
        assertNull( this.getModelContext().findClass( "DOES_NOT_EXIST" ) );
    }

    @Test
    public final void testFindResource() throws Exception
    {
        try
        {
            this.getModelContext().findResource( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().findResource( TEST_RESOURCE_LOCATION ) );
    }

    @Test
    public final void testFindResources() throws Exception
    {
        try
        {
            this.getModelContext().findResources( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertTrue( this.getModelContext().findResources( TEST_RESOURCE_LOCATION ).hasMoreElements() );
    }

    @Test
    public final void testGetModlets() throws Exception
    {
        this.getModelContext().setModlets( null );
        DefaultModletProvider.setDefaultEnabled( Boolean.FALSE );
        final Modlets modlets = this.getModelContext().getModlets();
        assertNotNull( modlets );
        assertNotNull( modlets.getModlet( DEFAULT_MODLET_NAME ) );
        DefaultModletProvider.setDefaultEnabled( null );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testGetInvalidModlets() throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( Boolean.TRUE );
        DefaultModletProvider.setDefaultModletLocation( "META-INF/invalid-modlet/jomc-modlet.xml" );
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createContext( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createContext( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createSchema( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createSchema( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createEntityResolver( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createEntityResolver( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createResourceResolver( ModletObject.MODEL_PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createResourceResolver( ModletObject.PUBLIC_ID );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateModelContext() throws Exception
    {
        ModelContext.setModelContextClassName( null );
        assertNotNull( ModelContext.createModelContext( null ) );
        assertNotNull( ModelContext.createModelContext( this.getClass().getClassLoader() ) );

        ModelContext.setModelContextClassName( "DOES_NOT_EXIST" );

        try
        {
            ModelContext.createModelContext( null );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( InstantiationExceptionModelContext.class.getName() );

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( IllegalAccessExceptionModelContext.class.getName() );

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( ClassCastExceptionModelContext.class.getName() );

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( NoSuchMethodExceptionModelContext.class.getName() );

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( InvocationTargetExceptionModelContext.class.getName() );

        try
        {
            ModelContext.createModelContext( this.getClass().getClassLoader() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        ModelContext.setModelContextClassName( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateEntityResolver() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createEntityResolver( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createEntityResolver( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        EntityResolver r = this.getModelContext().createEntityResolver( ModletObject.MODEL_PUBLIC_ID );
        assertNotNull( r );

        assertNull( r.resolveEntity( null, "DOES_NOT_EXIST" ) );
        assertNull( r.resolveEntity( "http://jomc.org/modlet", "DOES_NOT_EXIST" ) );
        assertNull( r.resolveEntity( ":", "DOES_NOT_EXIST" ) );
        assertNull( r.resolveEntity( null, ":" ) );
        assertNull( r.resolveEntity( "", null ) );
        assertNull( r.resolveEntity( null, "" ) );

        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateResourceResolver() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createResourceResolver( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createResourceResolver( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        LSResourceResolver r = this.getModelContext().createResourceResolver( ModletObject.MODEL_PUBLIC_ID );

        assertNotNull( r );
        assertNull( r.resolveResource( null, null, null, null, null ) );
        assertNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null, "DOES_NOT_EXIST", null ) );
        assertNull( r.resolveResource( "UNSUPPORTED", null, ModletObject.MODEL_PUBLIC_ID, null, null ) );
        assertNotNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, ModletObject.MODEL_PUBLIC_ID,
                                          ModelContext.getDefaultModletSchemaSystemId(), null ) );

        LSInput input = r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null,
                                           ModelContext.getDefaultModletSchemaSystemId(), null );

        assertNotNull( input );

        input.getBaseURI();
        input.getByteStream();
        input.getCertifiedText();
        input.getCharacterStream();
        input.getEncoding();
        input.getPublicId();
        input.getStringData();
        input.getSystemId();

        input.setBaseURI( null );
        input.setByteStream( null );
        input.setCertifiedText( false );
        input.setCharacterStream( null );
        input.setEncoding( null );
        input.setPublicId( null );
        input.setStringData( null );
        input.setSystemId( null );

        r = this.getModelContext().createResourceResolver( ModletObject.PUBLIC_ID );

        assertNotNull( r );
        assertNull( r.resolveResource( null, null, null, null, null ) );
        assertNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null, "DOES_NOT_EXIST", null ) );
        assertNull( r.resolveResource( "UNSUPPORTED", null, ModletObject.MODEL_PUBLIC_ID, null, null ) );
        assertNotNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, ModletObject.MODEL_PUBLIC_ID,
                                          ModelContext.getDefaultModletSchemaSystemId(), null ) );

        input = r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null,
                                   ModelContext.getDefaultModletSchemaSystemId(), null );

        assertNotNull( input );

        input.getBaseURI();
        input.getByteStream();
        input.getCertifiedText();
        input.getCharacterStream();
        input.getEncoding();
        input.getPublicId();
        input.getStringData();
        input.getSystemId();

        input.setBaseURI( null );
        input.setByteStream( null );
        input.setCertifiedText( false );
        input.setCharacterStream( null );
        input.setEncoding( null );
        input.setPublicId( null );
        input.setStringData( null );
        input.setSystemId( null );

        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateContext() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createContext( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createContext( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( new Modlets() );

        try
        {
            this.getModelContext().createContext( "" );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createContext( new URI( "DOES_NOT_EXIST" ) );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createContext( ModletObject.MODEL_PUBLIC_ID ) );
        assertNotNull( this.getModelContext().createContext( ModletObject.PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateMarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createMarshaller( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( new Modlets() );

        try
        {
            this.getModelContext().createMarshaller( "" );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( new URI( "DOES_NOT_EXIST" ) );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        assertNotNull( this.getModelContext().createMarshaller( ModletObject.PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateUnmarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createUnmarshaller( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( new Modlets() );

        try
        {
            this.getModelContext().createUnmarshaller( "" );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( new URI( "DOES_NOT_EXIST" ) );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createUnmarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        assertNotNull( this.getModelContext().createUnmarshaller( ModletObject.PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateSchema() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createSchema( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createSchema( (URI) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( new Modlets() );

        try
        {
            this.getModelContext().createSchema( "" );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createSchema( new URI( "DOES_NOT_EXIST" ) );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createSchema( ModletObject.MODEL_PUBLIC_ID ) );
        assertNotNull( this.getModelContext().createSchema( ModletObject.PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    @SuppressWarnings( "deprecation" )
    public final void testCreateServiceObject() throws Exception
    {
        try
        {
            this.getModelContext().createServiceObject( null, Object.class );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createServiceObject( new Service(), null );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Service illegalAccessService = new Service();
        illegalAccessService.setIdentifier( ModelContext.class.getName() );
        illegalAccessService.setClazz( IllegalAccessExceptionModelContext.class.getName() );

        try
        {
            this.getModelContext().createServiceObject( illegalAccessService, ModelContext.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Service instantiationExceptionService = new Service();
        instantiationExceptionService.setIdentifier( ModelContext.class.getName() );
        instantiationExceptionService.setClazz( InstantiationExceptionModelContext.class.getName() );

        try
        {
            this.getModelContext().createServiceObject( instantiationExceptionService, ModelContext.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Service legalService = new Service();
        legalService.setIdentifier( ModletProvider.class.getName() );
        legalService.setClazz( TestModletProvider.class.getName() );
        assertNotNull( this.getModelContext().createServiceObject( legalService, ModletProvider.class ) );

        final Property unsupportedTypeProperty = new Property();
        unsupportedTypeProperty.setName( "unsupportedPropertyType" );
        unsupportedTypeProperty.setValue( "UNSUPPORTED TYPE" );

        final Property instantiationExceptionProperty = new Property();
        instantiationExceptionProperty.setName( "instantiationExceptionProperty" );
        instantiationExceptionProperty.setValue( "UNSUPPORTED TYPE" );

        final Property invocationTargetExceptionProperty = new Property();
        invocationTargetExceptionProperty.setName( "invocationTargetExceptionProperty" );
        invocationTargetExceptionProperty.setValue( "UNSUPPORTED TYPE" );

        legalService.getProperty().add( unsupportedTypeProperty );

        try
        {
            this.getModelContext().createServiceObject( legalService, ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        legalService.getProperty().remove( unsupportedTypeProperty );
        legalService.getProperty().add( instantiationExceptionProperty );

        try
        {
            this.getModelContext().createServiceObject( legalService, ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        legalService.getProperty().remove( instantiationExceptionProperty );
        legalService.getProperty().add( invocationTargetExceptionProperty );

        try
        {
            this.getModelContext().createServiceObject( legalService, ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    @Test
    public final void CreateServiceObjectsThrowsNullPointerExceptionWithNonNullMessageOnNullArguments() throws Exception
    {
        try
        {
            this.getModelContext().createServiceObjects( null, "IDENTIFIER", Object.class );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createServiceObjects( "IDENTIFIER", null, Object.class );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createServiceObjects( "IDENTIFIER", "IDENTIFIER", null );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    @Test
    public final void CreateServiceObjectsThrowsModelExceptionWithNonNullMessageForInacessibleServiceClass()
        throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        DefaultModletProvider.setDefaultOrdinal( null );
        DefaultModletProvider.setDefaultValidating( null );
        this.getModelContext().setModlets( null );

        final Modlet modlet = new Modlet();
        this.getModelContext().getModlets().getModlet().add( modlet );

        modlet.setModel( "Test" );
        modlet.setName( "Test" );
        modlet.setServices( new Services() );

        final Service service = new Service();
        modlet.getServices().getService().add( service );

        service.setIdentifier( ModelContext.class.getName() );
        service.setClazz( IllegalAccessExceptionModelContext.class.getName() );

        try
        {
            this.getModelContext().createServiceObjects( "Test", ModelContext.class.getName(), ModelContext.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void CreateServiceObjectsThrowsModelExceptionWithNonNullMessageForNonInstantiableServiceClass()
        throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        DefaultModletProvider.setDefaultOrdinal( null );
        DefaultModletProvider.setDefaultValidating( null );
        this.getModelContext().setModlets( null );

        final Modlet modlet = new Modlet();
        this.getModelContext().getModlets().getModlet().add( modlet );

        modlet.setModel( "Test" );
        modlet.setName( "Test" );
        modlet.setServices( new Services() );

        final Service service = new Service();
        modlet.getServices().getService().add( service );

        service.setIdentifier( ModelContext.class.getName() );
        service.setClazz( InstantiationExceptionModelContext.class.getName() );

        try
        {
            this.getModelContext().createServiceObjects( "Test", ModelContext.class.getName(), ModelContext.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void CreateServiceObjectsThrowsModelExceptionWithNonNullMessageForUnsupportedPropertyTypes()
        throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        DefaultModletProvider.setDefaultOrdinal( null );
        DefaultModletProvider.setDefaultValidating( null );
        this.getModelContext().setModlets( null );

        final Modlet modlet = new Modlet();
        this.getModelContext().getModlets().getModlet().add( modlet );

        modlet.setModel( "Test" );
        modlet.setName( "Test" );
        modlet.setServices( new Services() );

        final Service service = new Service();
        modlet.getServices().getService().add( service );

        service.setIdentifier( ModletProvider.class.getName() );
        service.setClazz( TestModletProvider.class.getName() );

        final Collection<? extends ModletProvider> serviceObjects =
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(),
                                                         ModletProvider.class );

        assertNotNull( serviceObjects );
        assertEquals( 1, serviceObjects.size() );

        final Property property = new Property();
        property.setName( "unsupportedPropertyType" );
        property.setValue( "UNSUPPORTED TYPE" );

        service.getProperty().add( property );

        try
        {
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(), ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void CreateServiceObjectsThrowsModelExceptionWithNonNullMessageForNonInstantiablePropertyObject()
        throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        DefaultModletProvider.setDefaultOrdinal( null );
        DefaultModletProvider.setDefaultValidating( null );
        this.getModelContext().setModlets( null );

        final Modlet modlet = new Modlet();
        this.getModelContext().getModlets().getModlet().add( modlet );

        modlet.setModel( "Test" );
        modlet.setName( "Test" );
        modlet.setServices( new Services() );

        final Service service = new Service();
        modlet.getServices().getService().add( service );

        service.setIdentifier( ModletProvider.class.getName() );
        service.setClazz( TestModletProvider.class.getName() );

        final Collection<? extends ModletProvider> serviceObjects =
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(),
                                                         ModletProvider.class );

        assertNotNull( serviceObjects );
        assertEquals( 1, serviceObjects.size() );

        final Property property = new Property();
        property.setName( "instantiationExceptionProperty" );
        property.setValue( "UNSUPPORTED TYPE" );

        service.getProperty().add( property );

        try
        {
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(), ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void CreateServiceObjectsThrowsModelExceptionWithNonNullMessageForNonOperationalPropertyObject()
        throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        DefaultModletProvider.setDefaultOrdinal( null );
        DefaultModletProvider.setDefaultValidating( null );
        this.getModelContext().setModlets( null );

        final Modlet modlet = new Modlet();
        this.getModelContext().getModlets().getModlet().add( modlet );

        modlet.setModel( "Test" );
        modlet.setName( "Test" );
        modlet.setServices( new Services() );

        final Service service = new Service();
        modlet.getServices().getService().add( service );

        service.setIdentifier( ModletProvider.class.getName() );
        service.setClazz( TestModletProvider.class.getName() );

        final Collection<? extends ModletProvider> serviceObjects =
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(),
                                                         ModletProvider.class );

        assertNotNull( serviceObjects );
        assertEquals( 1, serviceObjects.size() );

        final Property property = new Property();
        property.setName( "invocationTargetExceptionProperty" );
        property.setValue( "UNSUPPORTED TYPE" );

        service.getProperty().add( property );

        try
        {
            this.getModelContext().createServiceObjects( "Test", ModletProvider.class.getName(), ModletProvider.class );
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testDefaultLogLevel() throws Exception
    {
        final String testLogLevel = System.getProperty( "org.jomc.modlet.ModelContext.defaultLogLevel" );

        System.clearProperty( "org.jomc.modlet.ModelContext.defaultLogLevel" );
        ModelContext.setDefaultLogLevel( null );
        assertNotNull( ModelContext.getDefaultLogLevel() );
        ModelContext.setDefaultLogLevel( null );
        System.setProperty( "org.jomc.modlet.ModelContext.defaultLogLevel", "OFF" );
        assertEquals( Level.OFF, ModelContext.getDefaultLogLevel() );

        if ( testLogLevel != null )
        {
            System.setProperty( "org.jomc.modlet.ModelContext.defaultLogLevel", testLogLevel );
        }
        else
        {
            System.clearProperty( "org.jomc.modlet.ModelContext.defaultLogLevel" );
        }

        ModelContext.setDefaultLogLevel( null );
    }

    @Test
    public final void testDefaultModletSchemaSystemId() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.ModelContext.defaultModletSchemaSystemId" );
        ModelContext.setDefaultModletSchemaSystemId( null );
        assertNotNull( ModelContext.getDefaultModletSchemaSystemId() );
        ModelContext.setDefaultModletSchemaSystemId( null );
        System.setProperty( "org.jomc.modlet.ModelContext.defaultModletSchemaSystemId", "TEST" );
        assertEquals( "TEST", ModelContext.getDefaultModletSchemaSystemId() );
        System.clearProperty( "org.jomc.modlet.ModelContext.defaultModletSchemaSystemId" );
        ModelContext.setDefaultModletSchemaSystemId( null );
        assertNotNull( ModelContext.getDefaultModletSchemaSystemId() );
    }

    @Test
    public final void testLogLevel() throws Exception
    {
        ModelContext.setDefaultLogLevel( null );
        this.getModelContext().setLogLevel( null );
        assertNotNull( this.getModelContext().getLogLevel() );

        ModelContext.setDefaultLogLevel( Level.OFF );
        this.getModelContext().setLogLevel( null );
        assertEquals( Level.OFF, this.getModelContext().getLogLevel() );

        ModelContext.setDefaultLogLevel( null );
        this.getModelContext().setLogLevel( null );
    }

    @Test
    public final void testLogging() throws Exception
    {
        try
        {
            this.getModelContext().isLoggable( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().log( null, "Message", new Throwable() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().log( Level.ALL, null, null );
        this.getModelContext().setLogLevel( Level.SEVERE );
        assertFalse( this.getModelContext().isLoggable( Level.WARNING ) );
        assertTrue( this.getModelContext().isLoggable( Level.SEVERE ) );
        this.getModelContext().setLogLevel( null );
    }

    @Test
    public final void testModletSchemaSystemId() throws Exception
    {
        ModelContext.setDefaultModletSchemaSystemId( null );
        this.getModelContext().setModletSchemaSystemId( null );
        assertNotNull( this.getModelContext().getModletSchemaSystemId() );

        ModelContext.setDefaultModletSchemaSystemId( "TEST" );
        this.getModelContext().setModletSchemaSystemId( null );
        assertEquals( "TEST", this.getModelContext().getModletSchemaSystemId() );

        ModelContext.setDefaultModletSchemaSystemId( null );
        this.getModelContext().setModletSchemaSystemId( null );
    }

    @Test
    public final void testFindModel() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().findModel( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().findModel( (Model) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().findModel( ModletObject.MODEL_PUBLIC_ID ) );

        final Model model = new Model();
        model.setIdentifier( ModletObject.MODEL_PUBLIC_ID );

        assertNotNull( this.getModelContext().findModel( model ) );
    }

    @Test
    public final void testProcessModel() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().processModel( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    @Test
    public final void testValidateModel() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().validateModel( null, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().validateModel( "TEST", null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().validateModel( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final Model invalidModel = new Model();
        final Model validModel = new Model();
        validModel.setIdentifier( ModletObject.MODEL_PUBLIC_ID );

        final Marshaller m = this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID );
        final JAXBElement<Model> invalid = new org.jomc.modlet.ObjectFactory().createModel( invalidModel );
        final JAXBElement<Model> valid = new org.jomc.modlet.ObjectFactory().createModel( validModel );

        StringWriter stringWriter = new StringWriter();
        m.marshal( invalid, stringWriter );
        System.out.println( stringWriter );

        stringWriter = new StringWriter();
        m.marshal( valid, stringWriter );
        System.out.println( stringWriter );

        assertFalse( this.getModelContext().validateModel( ModletObject.MODEL_PUBLIC_ID,
                                                           new JAXBSource( m, invalid ) ).isModelValid() );

        assertTrue( this.getModelContext().validateModel( ModletObject.MODEL_PUBLIC_ID,
                                                          new JAXBSource( m, valid ) ).isModelValid() );

    }

}
