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
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
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
import org.jomc.modlet.Services;
import org.jomc.modlet.test.support.IllegalAccessExceptionModelContext;
import org.jomc.modlet.test.support.InstantiationExceptionModelContext;
import org.jomc.modlet.test.support.TestModletProvider;
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
        try ( final InputStream in = ModelContextTest.class.getResourceAsStream( "/" + TEST_RESOURCE_LOCATION ) )
        {
            final Properties p = new Properties();
            assertNotNull( "Expected '" + TEST_RESOURCE_LOCATION + "' not found.", in );
            p.load( in );

            final String defaultModletName = p.getProperty( "defaultModletName" );
            assertNotNull( "Expected 'defaultModletName' property not found.", defaultModletName );
            DEFAULT_MODLET_NAME = defaultModletName;
        }
        catch ( final IOException e )
        {
            throw new AssertionError( e );
        }
    }

    /**
     * The {@code ModelContext} instance tests are performed with.
     */
    private volatile ModelContext modelContext;

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

    @Test
    public final void testAttributes() throws Exception
    {
        assertNullPointerException( ()  -> this.getModelContext().getAttribute( null ) );
        assertNullPointerException( ()  -> this.getModelContext().getAttribute( null, null ) );
        assertNullPointerException( ()  -> this.getModelContext().setAttribute( "TEST", null ) );
        assertNullPointerException( ()  -> this.getModelContext().setAttribute( null, "TEST" ) );
        assertNullPointerException( ()  ->
        {
            this.getModelContext().clearAttribute( null );
            return null;
        } );

        assertNotNull( this.getModelContext().getAttribute( "ATTRIBUTE" ) );
        assertFalse( this.getModelContext().getAttribute( "ATTRIBUTE" ).isPresent() );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
        assertFalse( this.getModelContext().setAttribute( "ATTRIBUTE", "VALUE" ).isPresent() );
        assertEquals( "VALUE", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
        assertEquals( "VALUE", this.getModelContext().setAttribute( "ATTRIBUTE", "TEST" ).get() );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE" ).get() );
        this.getModelContext().clearAttribute( "ATTRIBUTE" );
        assertNotNull( this.getModelContext().getAttribute( "ATTRIBUTE" ) );
        assertFalse( this.getModelContext().getAttribute( "ATTRIBUTE" ).isPresent() );
        assertEquals( "TEST", this.getModelContext().getAttribute( "ATTRIBUTE", "TEST" ) );
    }

    @Test
    public final void testFindClass() throws Exception
    {
        assertNullPointerException( ()  -> this.getModelContext().findClass( null ) );
        assertNotNull( this.getModelContext().findClass( "java.lang.Object" ) );
        assertTrue( this.getModelContext().findClass( "java.lang.Object" ).isPresent() );
        assertEquals( Object.class, this.getModelContext().findClass( "java.lang.Object" ).get() );
        assertNotNull( this.getModelContext().findClass( "DOES_NOT_EXIST" ) );
        assertFalse( this.getModelContext().findClass( "DOES_NOT_EXIST" ).isPresent() );
    }

    @Test
    public final void testFindResource() throws Exception
    {
        assertNullPointerException( ()  -> this.getModelContext().findResource( null ) );
        assertNotNull( this.getModelContext().findResource( TEST_RESOURCE_LOCATION ) );
    }

    @Test
    public final void testFindResources() throws Exception
    {
        assertNullPointerException( ()  -> this.getModelContext().findResources( null ) );
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
    public final void testGetInvalidModlets() throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( Boolean.TRUE );
        DefaultModletProvider.setDefaultModletLocation( "META-INF/invalid-modlet/jomc-modlet.xml" );
        this.getModelContext().setModlets( null );
        assertModelException( ()  -> this.getModelContext().createContext( ModletObject.MODEL_PUBLIC_ID ) );
        assertModelException( ()  -> this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        assertModelException( ()  -> this.getModelContext().createSchema( ModletObject.MODEL_PUBLIC_ID ) );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        assertModelException( ()  -> this.getModelContext().createEntityResolver( ModletObject.MODEL_PUBLIC_ID ) );
        assertModelException( ()  -> this.getModelContext().createResourceResolver( ModletObject.MODEL_PUBLIC_ID ) );
        DefaultModletProvider.setDefaultEnabled( null );
        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testCreateEntityResolver() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createEntityResolver( (String) null ) );

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
    public final void testCreateResourceResolver() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createResourceResolver( (String) null ) );

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

        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testCreateContext() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createContext( (String) null ) );
        this.getModelContext().setModlets( new Modlets() );
        assertModelException( ()  -> this.getModelContext().createContext( "" ) );
        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createContext( ModletObject.MODEL_PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testCreateMarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createMarshaller( (String) null ) );
        this.getModelContext().setModlets( new Modlets() );
        assertModelException( ()  -> this.getModelContext().createMarshaller( "" ) );
        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testCreateUnmarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createUnmarshaller( (String) null ) );
        this.getModelContext().setModlets( new Modlets() );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( "" ) );
        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createUnmarshaller( ModletObject.MODEL_PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testCreateSchema() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().createSchema( (String) null ) );
        this.getModelContext().setModlets( new Modlets() );
        assertModelException( ()  -> this.getModelContext().createSchema( "" ) );
        this.getModelContext().setModlets( null );
        assertNotNull( this.getModelContext().createSchema( ModletObject.MODEL_PUBLIC_ID ) );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void CreateServiceObjectsThrowsNullPointerExceptionWithNonNullMessageOnNullArguments() throws Exception
    {
        assertNullPointerException( ()  -> this.getModelContext().createServiceObjects( null, "IDENTIFIER",
                                                                                         Object.class ) );

        assertNullPointerException( ()  -> this.getModelContext().createServiceObjects( "IDENTIFIER", null,
                                                                                         Object.class ) );

        assertNullPointerException( ()  -> this.getModelContext().createServiceObjects( "IDENTIFIER", "IDENTIFIER",
                                                                                         null ) );

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

        assertModelException( ()  -> this.getModelContext().createServiceObjects( "Test", ModelContext.class.getName(),
                                                                                   ModelContext.class ) );

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

        assertModelException( ()  -> this.getModelContext().createServiceObjects( "Test", ModelContext.class.getName(),
                                                                                   ModelContext.class ) );

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

        assertModelException( ()  -> this.getModelContext().createServiceObjects( "Test",
                                                                                   ModletProvider.class.getName(),
                                                                                   ModletProvider.class ) );

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

        assertModelException( ()  -> this.getModelContext().createServiceObjects( "Test",
                                                                                   ModletProvider.class.getName(),
                                                                                   ModletProvider.class ) );

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

        assertModelException( ()  -> this.getModelContext().createServiceObjects( "Test",
                                                                                   ModletProvider.class.getName(),
                                                                                   ModletProvider.class ) );

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
        assertNullPointerException( ()  -> this.getModelContext().isLoggable( null ) );
        assertNullPointerException( ()  ->
        {
            this.getModelContext().log( null, "Message", new Throwable() );
            return null;
        } );

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
        assertNullPointerException( ()  -> this.getModelContext().findModel( (String) null ) );
        assertNullPointerException( ()  -> this.getModelContext().findModel( (Model) null ) );

        assertNotNull( this.getModelContext().findModel( ModletObject.MODEL_PUBLIC_ID ) );

        final Model model = new Model();
        model.setIdentifier( ModletObject.MODEL_PUBLIC_ID );

        assertNotNull( this.getModelContext().findModel( model ) );
    }

    @Test
    public final void testProcessModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().processModel( null ) );
    }

    @Test
    public final void testValidateModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        assertNullPointerException( ()  -> this.getModelContext().validateModel( null, null ) );
        assertNullPointerException( ()  -> this.getModelContext().validateModel( "TEST", null ) );
        assertNullPointerException( ()  -> this.getModelContext().validateModel( null ) );

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

    private static void assertModelException( final Callable<?> testcase ) throws Exception
    {
        try
        {
            testcase.call();
            fail( "Expected 'ModelException' not thrown." );
        }
        catch ( final ModelException e )
        {
            System.out.println( e );
            assertNotNull( e.getMessage() );
        }
    }

    private static void assertNullPointerException( final Callable<?> testcase ) throws Exception
    {
        try
        {
            testcase.call();
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            System.out.println( e );
            assertNotNull( e.getMessage() );
        }
    }

}
