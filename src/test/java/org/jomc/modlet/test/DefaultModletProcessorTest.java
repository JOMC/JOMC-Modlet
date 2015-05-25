/*
 *   Copyright (C) Christian Schulte, 2014-005
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

import org.jomc.modlet.DefaultModletProcessor;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.Modlets;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModletProcessor}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.6
 */
public class DefaultModletProcessorTest extends ModletProcessorTest
{

    /**
     * Creates a new {@code DefaultModletProcessorTest} instance.
     */
    public DefaultModletProcessorTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultModletProcessor getModletProcessor()
    {
        return (DefaultModletProcessor) super.getModletProcessor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultModletProcessor newModletProcessor()
    {
        return new DefaultModletProcessor();
    }

    @Test
    public final void testFindTransformers() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

        try
        {
            this.getModletProcessor().findTransformers( context, null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            this.getModletProcessor().findTransformers( null, "" );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        DefaultModletProcessor.setDefaultTransformerLocation( null );
        this.getModletProcessor().setTransformerLocation( null );
        assertEquals( 1, this.getModletProcessor().findTransformers(
                      context, DefaultModletProcessor.getDefaultTransformerLocation() ).size() );

        DefaultModletProcessor.setDefaultTransformerLocation( "DOES_NOT_EXIST" );
        this.getModletProcessor().setTransformerLocation( "DOES_NOT_EXIST" );

        assertNull( this.getModletProcessor().findTransformers(
            context, DefaultModletProcessor.getDefaultTransformerLocation() ) );

        assertNull( this.getModletProcessor().findTransformers(
            context, this.getModletProcessor().getTransformerLocation() ) );

        DefaultModletProcessor.setDefaultTransformerLocation( null );
        this.getModletProcessor().setTransformerLocation( null );
    }

    @Test
    public final void testDefaultEnabled() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultEnabled" );
        DefaultModletProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModletProcessor.isDefaultEnabled() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        System.setProperty( "org.jomc.modlet.DefaultModletProcessor.defaultEnabled", Boolean.toString( false ) );
        DefaultModletProcessor.setDefaultEnabled( null );
        assertFalse( DefaultModletProcessor.isDefaultEnabled() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultEnabled" );
        DefaultModletProcessor.setDefaultEnabled( null );
        assertTrue( DefaultModletProcessor.isDefaultEnabled() );
    }

    @Test
    public final void testEnabled() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

        DefaultModletProcessor.setDefaultEnabled( null );
        this.getModletProcessor().setEnabled( null );
        assertTrue( this.getModletProcessor().isEnabled() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        DefaultModletProcessor.setDefaultEnabled( false );
        this.getModletProcessor().setEnabled( null );
        assertFalse( this.getModletProcessor().isEnabled() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        DefaultModletProcessor.setDefaultEnabled( null );
        this.getModletProcessor().setEnabled( null );
    }

    @Test
    public final void testDefaultTransformerLocation() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultTransformerLocation" );
        DefaultModletProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc-modlet.xsl", DefaultModletProcessor.getDefaultTransformerLocation() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        System.setProperty( "org.jomc.modlet.DefaultModletProcessor.defaultTransformerLocation", "TEST" );
        DefaultModletProcessor.setDefaultTransformerLocation( null );
        assertEquals( "TEST", DefaultModletProcessor.getDefaultTransformerLocation() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultTransformerLocation" );
        DefaultModletProcessor.setDefaultTransformerLocation( null );
        assertEquals( "META-INF/jomc-modlet.xsl", DefaultModletProcessor.getDefaultTransformerLocation() );
    }

    @Test
    public final void testTransformerLocation() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();

        DefaultModletProcessor.setDefaultTransformerLocation( null );
        this.getModletProcessor().setTransformerLocation( null );
        assertNotNull( this.getModletProcessor().getTransformerLocation() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        DefaultModletProcessor.setDefaultTransformerLocation( "TEST" );
        this.getModletProcessor().setTransformerLocation( null );
        assertEquals( "TEST", this.getModletProcessor().getTransformerLocation() );

        this.getModletProcessor().processModlets( context, new Modlets() );

        DefaultModletProcessor.setDefaultTransformerLocation( null );
        this.getModletProcessor().setTransformerLocation( null );
    }

    @Test
    public final void testDefaultProcessModlets() throws Exception
    {
        final ModelContext context = ModelContextFactory.newInstance().newModelContext();
        final Modlets modlets = new Modlets();

        assertNotNull( this.getModletProcessor().processModlets( context, modlets ) );

        this.getModletProcessor().setTransformerLocation( this.getClass().getPackage().getName().replace( '.', '/' )
                                                              + "/system-property-test.xsl" );

        final Modlets processedSystemProperty = this.getModletProcessor().processModlets( context, modlets );
        assertNotNull( processedSystemProperty );
        assertNotNull( processedSystemProperty.getModlet( System.getProperty( "user.home" ) ) );

        this.getModletProcessor().setTransformerLocation(
            this.getClass().getPackage().getName().replace( '.', '/' ) + "/relative-uri-test.xsl" );

        final Modlets processedRelativeUri = this.getModletProcessor().processModlets( context, modlets );
        assertNotNull( processedRelativeUri );
        assertNotNull( processedRelativeUri.getModlet( System.getProperty( "os.name" ) ) );

        this.getModletProcessor().setTransformerLocation( null );
    }

    @Test
    public final void testDefaultOrdinal() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultOrdinal" );
        DefaultModletProcessor.setDefaultOrdinal( null );
        assertEquals( DefaultModletProcessor.getDefaultOrdinal(), 0 );
        DefaultModletProcessor.setDefaultOrdinal( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProcessor.defaultOrdinal", Integer.toString( 3 ) );
        assertEquals( DefaultModletProcessor.getDefaultOrdinal(), 3 );
        System.clearProperty( "org.jomc.modlet.DefaultModletProcessor.defaultOrdinal" );
        DefaultModletProcessor.setDefaultOrdinal( null );
        assertEquals( DefaultModletProcessor.getDefaultOrdinal(), 0 );
    }

    @Test
    public final void testOrdinal() throws Exception
    {
        DefaultModletProcessor.setDefaultOrdinal( null );
        this.getModletProcessor().setOrdinal( null );
        assertEquals( this.getModletProcessor().getOrdinal(), 0 );

        DefaultModletProcessor.setDefaultOrdinal( 3 );
        this.getModletProcessor().setOrdinal( null );
        assertEquals( this.getModletProcessor().getOrdinal(), 3 );

        DefaultModletProcessor.setDefaultOrdinal( null );
        this.getModletProcessor().setOrdinal( null );
    }

}
