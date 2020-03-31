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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import org.jomc.modlet.DefaultModelContext;
import org.jomc.modlet.DefaultModletProvider;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.test.support.IllegalServicesModletProvider;
import org.jomc.modlet.test.support.NullModletProvider;
import org.jomc.modlet.test.support.ServicesNotFoundModletProvider;
import org.jomc.modlet.test.support.TestModelProcessor;
import org.jomc.modlet.test.support.TestModelProvider;
import org.jomc.modlet.test.support.TestModelValidator;
import org.jomc.modlet.test.support.TestModletProvider;
import org.junit.Test;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModelContext}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class DefaultModelContextTest extends ModelContextTest
{

    private static final String MODLET_TEST_NS = "http://jomc.org/modlet/test";

    /**
     * Creates a new {@code DefaultModelContextTest} instance.
     */
    public DefaultModelContextTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultModelContext getModelContext() throws ModelException
    {
        return (DefaultModelContext) super.getModelContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DefaultModelContext newModelContext()
    {
        return new DefaultModelContext();
    }

    @Test
    public final void testFindModlets() throws Exception
    {
        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        tmpFile.deleteOnExit();

        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "DOES_NOT_EXIST" );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "java.lang.Object" );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", TestModletProvider.class.getName() );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertNotNull( this.getModelContext().findModlets( new Modlets() ).
            getModlet( TestModletProvider.class.getName() ) );

        tmpFile.delete();

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( "META-INF/non-existent-services" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( "META-INF/illegal-services" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        this.getModelContext().setProviderLocation( "META-INF/illegal-services-modlet" );
        Modlets modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        assertModelException( ()  -> this.getModelContext().findModel(
            IllegalServicesModletProvider.class.getName() ) );

        final Model m1 = new Model();
        m1.setIdentifier( IllegalServicesModletProvider.class.getName() );

        assertModelException( ()  -> this.getModelContext().processModel( m1 ) );
        assertModelException( ()  -> this.getModelContext().validateModel( m1 ) );
        assertModelException( ()  -> this.getModelContext().createMarshaller( m1.getIdentifier() ) );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( m1.getIdentifier() ) );

        this.getModelContext().setProviderLocation( "META-INF/non-existent-services-modlet" );
        this.getModelContext().setModlets( null );
        modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        assertModelException( ()  -> this.getModelContext().findModel(
            ServicesNotFoundModletProvider.class.getName() ) );

        final Model m2 = new Model();
        m2.setIdentifier( ServicesNotFoundModletProvider.class.getName() );

        assertModelException( ()  -> this.getModelContext().processModel( m2 ) );
        assertModelException( ()  -> this.getModelContext().validateModel( m2 ) );
        assertModelException( ()  -> this.getModelContext().createMarshaller( m2.getIdentifier() ) );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( m2.getIdentifier() ) );

        this.getModelContext().setProviderLocation( "META-INF/extended-services" );
        this.getModelContext().setModlets( null );
        modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        final Optional<Object> testModletProvider =
            this.getModelContext().getAttribute( TestModletProvider.class.getName() );

        assertNotNull( testModletProvider );
        assertTrue( testModletProvider.isPresent() );
        assertTrue( ( (TestModletProvider) testModletProvider.get() ).isBooleanProperty() );
        assertEquals( 'T', ( (TestModletProvider) testModletProvider.get() ).getCharacterProperty() );
        assertEquals( (byte) -1, ( (TestModletProvider) testModletProvider.get() ).getByteProperty() );
        assertEquals( (short) -1, ( (TestModletProvider) testModletProvider.get() ).getShortProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getIntProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getLongProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getFloatProperty(), 0 );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getDoubleProperty(), 0 );
        assertEquals( "TEST", ( (TestModletProvider) testModletProvider.get() ).getStringProperty() );
        assertEquals( new URL( "file:///tmp" ), ( (TestModletProvider) testModletProvider.get() ).getUrlProperty() );
        assertEquals( Thread.State.RUNNABLE, ( (TestModletProvider) testModletProvider.get() ).getEnumProperty() );
        assertNull( ( (TestModletProvider) testModletProvider.get() ).getObjectProperty() );

        this.getModelContext().setProviderLocation( "META-INF/illegal-extended-services-1" );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        this.getModelContext().setProviderLocation( "META-INF/illegal-extended-services-2" );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        this.getModelContext().setProviderLocation( "META-INF/illegal-extended-services-3" );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );
        assertNullPointerException( ()  -> this.getModelContext().findModlets( null ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
    }

    @Test
    public final void testFindModletsWithAttributes() throws Exception
    {
        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        tmpFile.deleteOnExit();

        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "DOES_NOT_EXIST" );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().setAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             tmpFile.getAbsolutePath() );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "DOES_NOT_EXIST" );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "java.lang.Object" );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().setAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             tmpFile.getAbsolutePath() );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "DOES_NOT_EXIST" );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", TestModletProvider.class.getName() );
        this.writePropertiesFile( properties, tmpFile );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().setAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             tmpFile.getAbsolutePath() );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "DOES_NOT_EXIST" );

        assertNotNull( this.getModelContext().findModlets( new Modlets() ).
            getModlet( TestModletProvider.class.getName() ) );

        tmpFile.delete();

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME );
        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/non-existent-services" );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME );
        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/illegal-services" );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/illegal-services-modlet" );

        Modlets modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        assertModelException( ()  -> this.getModelContext().findModel(
            IllegalServicesModletProvider.class.getName() ) );

        final Model m3 = new Model();
        m3.setIdentifier( IllegalServicesModletProvider.class.getName() );

        assertModelException( ()  -> this.getModelContext().processModel( m3 ) );
        assertModelException( ()  -> this.getModelContext().validateModel( m3 ) );
        assertModelException( ()  -> this.getModelContext().createMarshaller( m3.getIdentifier() ) );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( m3.getIdentifier() ) );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/non-existent-services-modlet" );

        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        assertModelException( ()  -> this.getModelContext().findModel(
            ServicesNotFoundModletProvider.class.getName() ) );

        final Model m4 = new Model();
        m4.setIdentifier( ServicesNotFoundModletProvider.class.getName() );

        assertModelException( ()  -> this.getModelContext().processModel( m4 ) );
        assertModelException( ()  -> this.getModelContext().validateModel( m4 ) );
        assertModelException( ()  -> this.getModelContext().createMarshaller( m4.getIdentifier() ) );
        assertModelException( ()  -> this.getModelContext().createUnmarshaller( m4.getIdentifier() ) );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/extended-services" );

        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        modlets = this.getModelContext().findModlets( new Modlets() );
        assertNotNull( modlets );

        final Optional<Object> testModletProvider =
            this.getModelContext().getAttribute( TestModletProvider.class.getName() );

        assertNotNull( testModletProvider );
        assertTrue( testModletProvider.isPresent() );
        assertTrue( ( (TestModletProvider) testModletProvider.get() ).isBooleanProperty() );
        assertEquals( 'T', ( (TestModletProvider) testModletProvider.get() ).getCharacterProperty() );
        assertEquals( (byte) -1, ( (TestModletProvider) testModletProvider.get() ).getByteProperty() );
        assertEquals( (short) -1, ( (TestModletProvider) testModletProvider.get() ).getShortProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getIntProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getLongProperty() );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getFloatProperty(), 0 );
        assertEquals( -1, ( (TestModletProvider) testModletProvider.get() ).getDoubleProperty(), 0 );
        assertEquals( "TEST", ( (TestModletProvider) testModletProvider.get() ).getStringProperty() );
        assertEquals( new URL( "file:///tmp" ), ( (TestModletProvider) testModletProvider.get() ).getUrlProperty() );
        assertEquals( Thread.State.RUNNABLE, ( (TestModletProvider) testModletProvider.get() ).getEnumProperty() );
        assertNull( ( (TestModletProvider) testModletProvider.get() ).getObjectProperty() );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/illegal-extended-services-1" );

        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/illegal-extended-services-2" );

        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        this.getModelContext().setAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME,
                                             "META-INF/illegal-extended-services-3" );

        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        assertModelException( ()  -> this.getModelContext().findModlets( new Modlets() ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( DefaultModelContext.PLATFORM_PROVIDER_LOCATION_ATTRIBUTE_NAME );
        this.getModelContext().clearAttribute( DefaultModelContext.PROVIDER_LOCATION_ATTRIBUTE_NAME );
    }

    @Test
    public final void testGetDefaultProviderLocation() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModelContext.defaultProviderLocation" );
        DefaultModelContext.setDefaultProviderLocation( null );
        assertEquals( "META-INF/services", DefaultModelContext.getDefaultProviderLocation() );
        DefaultModelContext.setDefaultProviderLocation( null );
        System.setProperty( "org.jomc.modlet.DefaultModelContext.defaultProviderLocation", "TEST" );
        assertEquals( "TEST", DefaultModelContext.getDefaultProviderLocation() );
        System.clearProperty( "org.jomc.modlet.DefaultModelContext.defaultProviderLocation" );
        DefaultModelContext.setDefaultProviderLocation( null );
        assertEquals( "META-INF/services", DefaultModelContext.getDefaultProviderLocation() );
    }

    @Test
    public final void testGetDefaultPlatformProviderLocation() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation" );
        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        assertNotNull( DefaultModelContext.getDefaultPlatformProviderLocation() );
        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        System.setProperty( "org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation", "TEST" );
        assertEquals( "TEST", DefaultModelContext.getDefaultPlatformProviderLocation() );
        System.clearProperty( "org.jomc.modlet.DefaultModelContext.defaultPlatformProviderLocation" );
        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        assertNotNull( DefaultModelContext.getDefaultPlatformProviderLocation() );
    }

    @Test
    public final void testGetProviderLocation() throws Exception
    {
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        assertNotNull( this.getModelContext().getProviderLocation() );

        DefaultModelContext.setDefaultProviderLocation( "TEST" );
        this.getModelContext().setProviderLocation( null );
        assertEquals( "TEST", this.getModelContext().getProviderLocation() );

        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
    }

    @Test
    public final void testGetPlatformProviderLocation() throws Exception
    {
        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        assertNotNull( this.getModelContext().getPlatformProviderLocation() );

        DefaultModelContext.setDefaultPlatformProviderLocation( "TEST" );
        this.getModelContext().setPlatformProviderLocation( null );
        assertEquals( "TEST", this.getModelContext().getPlatformProviderLocation() );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
    }

    @Test
    public final void testFindTestModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        final Model model = this.getModelContext().findModel( MODLET_TEST_NS );

        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );
        assertNotNull( this.getModelContext().getAttribute( TestModelProvider.class.getName() ) );
        assertNotNull( model.getAnyObject( TestComplexType.class ) );
    }

    @Test
    public final void testProcessTestModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        Model model = this.getModelContext().findModel( MODLET_TEST_NS );

        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );

        model = this.getModelContext().processModel( model );
        assertNotNull( this.getModelContext().getAttribute( TestModelProcessor.class.getName() ) );
        assertTrue( this.getModelContext().getAttribute( TestModelProcessor.class.getName() ).isPresent() );
        assertNotNull( model.getAnyObject( TestComplexType.class ) );
        assertTrue( model.getAnyObject( TestComplexType.class ).isPresent() );
        assertFalse( model.getAnyObject( TestComplexType.class ).get().getAny().isEmpty() );
    }

    @Test
    public final void testValidateTestModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        final Model model = this.getModelContext().findModel( MODLET_TEST_NS );
        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );

        this.getModelContext().validateModel( model );
        assertNotNull( this.getModelContext().getAttribute( TestModelValidator.class.getName() ) );
    }

    @Test
    public final void testTestModel() throws Exception
    {
        this.getModelContext().setModlets( null );
        final Model model = this.getModelContext().findModel( MODLET_TEST_NS );
        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );

        this.getModelContext().setModlets( null );
        final Schema schema = this.getModelContext().createSchema( MODLET_TEST_NS );
        assertNotNull( schema );

        this.getModelContext().setModlets( null );
        final JAXBContext context = this.getModelContext().createContext( MODLET_TEST_NS );
        assertNotNull( context );

        this.getModelContext().setModlets( null );
        final EntityResolver entityResolver = this.getModelContext().createEntityResolver( MODLET_TEST_NS );
        assertNotNull( entityResolver );

        this.getModelContext().setModlets( null );
        final Marshaller marshaller = this.getModelContext().createMarshaller( MODLET_TEST_NS );
        assertNotNull( marshaller );

        this.getModelContext().setModlets( null );
        final LSResourceResolver resourceResolver = this.getModelContext().createResourceResolver( MODLET_TEST_NS );
        assertNotNull( resourceResolver );

        this.getModelContext().setModlets( null );
        final Unmarshaller unmarshaller = this.getModelContext().createUnmarshaller( MODLET_TEST_NS );
        assertNotNull( unmarshaller );

        final File tmpFile = File.createTempFile( this.getClass().getSimpleName(), ".xml" );
        marshaller.marshal( new org.jomc.modlet.ObjectFactory().createModel( model ), tmpFile );
        assertNotNull( unmarshaller.unmarshal( tmpFile ) );
        assertTrue( tmpFile.delete() );
    }

    @Test
    public final void testServiceProperties() throws Exception
    {
        this.getModelContext().setModlets( null );
        DefaultModletProvider.setDefaultModletLocation( "META-INF/properties-test/jomc-modlet.xml" );

        this.getModelContext().setModlets( null );
        final JAXBContext context = this.getModelContext().createContext( MODLET_TEST_NS );
        assertNotNull( context );

        this.getModelContext().setModlets( null );
        final EntityResolver entityResolver = this.getModelContext().createEntityResolver( MODLET_TEST_NS );
        assertNotNull( entityResolver );

        this.getModelContext().setModlets( null );
        final LSResourceResolver resourceResolver = this.getModelContext().createResourceResolver( MODLET_TEST_NS );
        assertNotNull( resourceResolver );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().createMarshaller( MODLET_TEST_NS );
            return null;
        } );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().createUnmarshaller( MODLET_TEST_NS );
            return null;
        } );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().findModel( MODLET_TEST_NS );
            return null;
        } );

        final Model m5 = new Model();
        m5.setIdentifier( MODLET_TEST_NS );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().findModel( m5 );
            return null;
        } );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().processModel( m5 );
            return null;
        } );

        assertModelException( ()  ->
        {
            this.getModelContext().setModlets( null );
            this.getModelContext().validateModel( m5 );
            return null;
        } );

        DefaultModletProvider.setDefaultModletLocation( null );
    }

    @Test
    public final void testProviderSorting() throws Exception
    {
        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( "META-INF/sorted-modlet-providers" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( "SORTING_TEST" );

        this.getModelContext().findModlets( new Modlets() );

        List<Class<?>> sortedProviders = (List<Class<?>>) this.getModelContext().getAttribute( "SORTING_TEST" ).get();
        assertNotNull( sortedProviders );
        assertEquals( 4, sortedProviders.size() );
        assertEquals( NullModletProvider.class, sortedProviders.get( 0 ) );
        assertEquals( TestModletProvider.class, sortedProviders.get( 1 ) );
        assertEquals( IllegalServicesModletProvider.class, sortedProviders.get( 2 ) );
        assertEquals( ServicesNotFoundModletProvider.class, sortedProviders.get( 3 ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( "META-INF/redundant-ordinal-modlet-providers" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( "SORTING_TEST" );

        this.getModelContext().findModlets( new Modlets() );

        sortedProviders = (List<Class<?>>) this.getModelContext().getAttribute( "SORTING_TEST" ).get();
        assertNotNull( sortedProviders );
        assertEquals( 4, sortedProviders.size() );
        assertEquals( TestModletProvider.class, sortedProviders.get( 0 ) );
        assertEquals( TestModletProvider.class, sortedProviders.get( 1 ) );
        assertEquals( TestModletProvider.class, sortedProviders.get( 2 ) );
        assertEquals( TestModletProvider.class, sortedProviders.get( 3 ) );

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );
        this.getModelContext().clearAttribute( "SORTING_TEST" );
    }

    private void writePropertiesFile( final Properties properties, final File file ) throws IOException
    {
        try ( final OutputStream out = new FileOutputStream( file ) )
        {
            properties.store( out, this.getClass().getName() );
        }
    }

    private static void assertNullPointerException( final Callable<?> callable ) throws Exception
    {
        try
        {
            callable.call();
            fail( "Expected 'NullPointerException' exception not thrown." );
        }
        catch ( final NullPointerException e )
        {
            System.out.println( e );
            assertNotNull( e.getMessage() );
        }
    }

    private static void assertModelException( final Callable<?> callable ) throws Exception
    {
        try
        {
            callable.call();
            fail( "Expected 'ModelException' exception not thrown." );
        }
        catch ( final ModelException e )
        {
            System.out.println( e );
            assertNotNull( e.getMessage() );
        }
    }

}
