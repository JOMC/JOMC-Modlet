/*
 *   Copyright (C) 2009 The JOMC Project
 *   Copyright (C) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
import java.io.OutputStream;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jomc.modlet.DefaultModelContext;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.test.support.IllegalServicesModletProvider;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModelContext}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class DefaultModelContextTest extends ModelContextTest
{

    private static final String MODLET_TEST_NS = "http://jomc.org/modlet/test";

    /** Creates a new {@code DefaultModelContextTest} instance. */
    public DefaultModelContextTest()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public DefaultModelContext getModelContext() throws ModelException
    {
        return (DefaultModelContext) super.getModelContext();
    }

    /** {@inheritDoc} */
    @Override
    protected DefaultModelContext newModelContext() throws ModelException
    {
        return new DefaultModelContext( this.getClass().getClassLoader() );
    }

    @Test
    public final void testFindModlets() throws Exception
    {
        final File tmpFile = File.createTempFile( this.getClass().getName(), ".properties" );
        tmpFile.deleteOnExit();

        final Properties properties = new Properties();
        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "DOES_NOT_EXIST" );

        OutputStream out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getModelContext().setPlatformProviderLocation( null );
            this.getModelContext().setProviderLocation( null );

            this.getModelContext().findModlets();
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", "java.lang.Object" );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        try
        {
            DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
            DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
            this.getModelContext().setPlatformProviderLocation( null );
            this.getModelContext().setProviderLocation( null );

            this.getModelContext().findModlets();
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        properties.setProperty( "org.jomc.modlet.ModletProvider.0", TestModletProvider.class.getName() );
        out = new FileOutputStream( tmpFile );
        properties.store( out, this.getClass().getName() );
        out.close();

        DefaultModelContext.setDefaultPlatformProviderLocation( tmpFile.getAbsolutePath() );
        DefaultModelContext.setDefaultProviderLocation( "DOES_NOT_EXIST" );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );

        assertNotNull( this.getModelContext().findModlets().getModlet( TestModletProvider.class.getName() ) );
        tmpFile.delete();

        try
        {
            DefaultModelContext.setDefaultPlatformProviderLocation( null );
            DefaultModelContext.setDefaultProviderLocation( "META-INF/non-existent-services" );
            this.getModelContext().setPlatformProviderLocation( null );
            this.getModelContext().setProviderLocation( null );

            this.getModelContext().findModlets();
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            DefaultModelContext.setDefaultPlatformProviderLocation( null );
            DefaultModelContext.setDefaultProviderLocation( "META-INF/illegal-services" );
            this.getModelContext().setPlatformProviderLocation( null );
            this.getModelContext().setProviderLocation( null );

            this.getModelContext().findModlets();
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
        this.getModelContext().setModlets( null );

        this.getModelContext().setProviderLocation( "META-INF/illegal-services-modlet" );
        Modlets modlets = this.getModelContext().findModlets();
        assertNotNull( modlets );

        try
        {
            this.getModelContext().findModel( IllegalServicesModletProvider.class.getName() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        Model model = new Model();
        model.setIdentifier( IllegalServicesModletProvider.class.getName() );

        try
        {
            this.getModelContext().processModel( model );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().validateModel( model );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( model.getIdentifier() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( model.getIdentifier() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        this.getModelContext().setProviderLocation( "META-INF/non-existent-services-modlet" );
        this.getModelContext().setModlets( null );
        modlets = this.getModelContext().findModlets();
        assertNotNull( modlets );

        try
        {
            this.getModelContext().findModel( ServicesNotFoundModletProvider.class.getName() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        model = new Model();
        model.setIdentifier( ServicesNotFoundModletProvider.class.getName() );

        try
        {
            this.getModelContext().processModel( model );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().validateModel( model );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createMarshaller( model.getIdentifier() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModelContext().createUnmarshaller( model.getIdentifier() );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        DefaultModelContext.setDefaultPlatformProviderLocation( null );
        DefaultModelContext.setDefaultProviderLocation( null );
        this.getModelContext().setPlatformProviderLocation( null );
        this.getModelContext().setProviderLocation( null );
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
        final Model model = this.getModelContext().findModel( MODLET_TEST_NS );

        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );
        assertNotNull( this.getModelContext().getAttribute( TestModelProvider.class.getName() ) );
        assertNotNull( model.getAnyObject( TestComplexType.class ) );
    }

    @Test
    public final void testProcessTestModel() throws Exception
    {
        Model model = this.getModelContext().findModel( MODLET_TEST_NS );

        assertNotNull( model );
        assertEquals( MODLET_TEST_NS, model.getIdentifier() );

        model = this.getModelContext().processModel( model );
        assertNotNull( this.getModelContext().getAttribute( TestModelProcessor.class.getName() ) );
        assertNotNull( model.getAnyObject( TestComplexType.class ) );
        assertFalse( model.getAnyObject( TestComplexType.class ).getAny().isEmpty() );
    }

    @Test
    public final void testValidateTestModel() throws Exception
    {
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

        try
        {
            this.getModelContext().createSchema( MODLET_TEST_NS );
            fail( "Expected ModelException not thrown." );
        }
        catch ( final ModelException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

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

}
