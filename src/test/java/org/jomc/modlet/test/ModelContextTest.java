/*
 *   Copyright (c) 2009 The JOMC Project
 *   Copyright (c) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
 *   THIS SOFTWARE IS PROVIDED BY THE JOMC PROJECT AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JOMC PROJECT OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $Id$
 *
 */
package org.jomc.modlet.test;

import java.io.StringWriter;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.bind.JAXBElement;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModletObject;
import java.io.IOException;
import java.util.Properties;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.jomc.modlet.Modlets;
import java.util.logging.Level;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.xml.sax.EntityResolver;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Test cases for {@code org.jomc.modlet.ModelContext} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ModelContextTest
{

    private ModelContext modelContext;

    public ModelContextTest()
    {
        this( null );
    }

    public ModelContextTest( final ModelContext modelContext )
    {
        super();
        this.modelContext = modelContext;
    }

    public ModelContext getModelContext() throws ModelException
    {
        if ( this.modelContext == null )
        {
            this.modelContext = ModelContext.createModelContext( this.getClass().getClassLoader() );
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, final String message, final Throwable t )
                {
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                }

            } );

        }

        return this.modelContext;
    }

    public Properties getModelContextTestProperties() throws IOException
    {
        final Properties p = new Properties();
        p.load( this.getClass().getResourceAsStream( "ModelContextTest.properties" ) );
        return p;
    }

    public void testAttributes() throws Exception
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

    public void testFindClass() throws Exception
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

    public void testFindResource() throws Exception
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

        assertNotNull( this.getModelContext().findResource(
            ModelContextTest.class.getName().replace( '.', '/' ) + ".properties" ) );

    }

    public void testFindResources() throws Exception
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

        assertTrue( this.getModelContext().findResources(
            ModelContextTest.class.getName().replace( '.', '/' ) + ".properties" ).hasMoreElements() );

    }

    public void testGetModlets() throws Exception
    {
        this.getModelContext().setModlets( null );
        final Modlets modlets = this.getModelContext().getModlets();
        assertNotNull( modlets );
        assertNotNull( modlets.getModlet( this.getModelContextTestProperties().getProperty( "projectName" ) ) );
    }

    public void testCreateModelContext() throws Exception
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

        ModelContext.setModelContextClassName( "org.jomc.modlet.test.InstantiationExceptionModelContext" );

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

        ModelContext.setModelContextClassName( "org.jomc.modlet.test.IllegalAccessExceptionModelContext" );

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

        ModelContext.setModelContextClassName( "org.jomc.modlet.test.ClassCastExceptionModelContext" );

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

        ModelContext.setModelContextClassName( "org.jomc.modlet.test.NoSuchMethodExceptionModelContext" );

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

        ModelContext.setModelContextClassName( "org.jomc.modlet.test.InvocationTargetExceptionModelContext" );

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

    public void testCreateEntityResolver() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createEntityResolver( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final EntityResolver r = this.getModelContext().createEntityResolver( ModletObject.MODEL_PUBLIC_ID );
        assertNotNull( r );

        try
        {
            r.resolveEntity( null, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNull( r.resolveEntity( null, "DOES_NOT_EXIST" ) );
        assertNotNull( r.resolveEntity( "http://jomc.org/modlet", "DOES_NOT_EXIST" ) );
    }

    public void testCreateResourceResolver() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createResourceResolver( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        final LSResourceResolver r = this.getModelContext().createResourceResolver( ModletObject.MODEL_PUBLIC_ID );

        assertNotNull( r );
        assertNull( r.resolveResource( null, null, null, null, null ) );
        assertNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null, "DOES_NOT_EXIST", null ) );
        assertNull( r.resolveResource( "UNSUPPORTED", null, ModletObject.MODEL_PUBLIC_ID, null, null ) );
        assertNotNull( r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, ModletObject.MODEL_PUBLIC_ID,
                                          ModelContext.getDefaultModletSchemaSystemId(), null ) );

        final LSInput input = r.resolveResource( W3C_XML_SCHEMA_NS_URI, null, null,
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
    }

    public void testCreateContext() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createContext( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().createContext( ModletObject.MODEL_PUBLIC_ID ) );
    }

    public void testCreateMarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createMarshaller( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().createMarshaller( ModletObject.MODEL_PUBLIC_ID ) );
    }

    public void testCreateUnmarshaller() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createUnmarshaller( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().createUnmarshaller( ModletObject.MODEL_PUBLIC_ID ) );
    }

    public void testCreateSchema() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().createSchema( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().createSchema( ModletObject.MODEL_PUBLIC_ID ) );
    }

    public void testClassLoader() throws Exception
    {
        final ModelContext context = ModelContext.createModelContext( null );
        assertNotNull( context.getClassLoader() );
        System.out.println( context.getClassLoader().toString() );
    }

    public void testDefaultLogLevel() throws Exception
    {
        final String testLogLevel = System.getProperty( "org.jomc.modlet.ModelContext.defaultLogLevel" );

        assertNotNull( ModelContext.getDefaultLogLevel() );
        ModelContext.setDefaultLogLevel( null );
        System.setProperty( "org.jomc.modlet.ModelContext.defaultLogLevel", "OFF" );
        assertEquals( Level.OFF, ModelContext.getDefaultLogLevel() );

        if ( testLogLevel != null )
        {
            System.setProperty( "org.jomc.modlet.ModelContext.defaulLogLevel", testLogLevel );
        }
        else
        {
            System.clearProperty( "org.jomc.modlet.ModelContext.defaulLogLevel" );
        }

        ModelContext.setDefaultLogLevel( null );
    }

    public void testDefaultModletSchemaSystemId() throws Exception
    {
        assertNotNull( ModelContext.getDefaultModletSchemaSystemId() );
        ModelContext.setDefaultModletSchemaSystemId( null );
        System.setProperty( "org.jomc.modlet.ModelContext.defaultModletSchemaSystemId", "TEST" );
        assertEquals( "TEST", ModelContext.getDefaultModletSchemaSystemId() );
        System.clearProperty( "org.jomc.modlet.ModelContext.defaulModletSchemaSystemId" );
        ModelContext.setDefaultModletSchemaSystemId( null );
    }

    public void testLogLevel() throws Exception
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

    public void testLogging() throws Exception
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

    public void testModletSchemaSystemId() throws Exception
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

    public void testFindModel() throws Exception
    {
        this.getModelContext().setModlets( null );

        try
        {
            this.getModelContext().findModel( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        assertNotNull( this.getModelContext().findModel( ModletObject.MODEL_PUBLIC_ID ) );
    }

    public void testProcessModel() throws Exception
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

    public void testValidateModel() throws Exception
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
