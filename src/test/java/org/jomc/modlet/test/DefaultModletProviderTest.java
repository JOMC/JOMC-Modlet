/*
 *   Copyright (C) Christian Schulte (schulte2005@users.sourceforge.net), 2005-07-25
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

import org.junit.Test;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.DefaultModletProvider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModletProvider}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class DefaultModletProviderTest extends ModletProviderTest
{

    /** Creates a new {@code DefaultModletProviderTest} instance. */
    public DefaultModletProviderTest()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public DefaultModletProvider getModletProvider()
    {
        return (DefaultModletProvider) super.getModletProvider();
    }

    /** {@inheritDoc} */
    @Override
    public DefaultModletProvider newModletProvider()
    {
        return new DefaultModletProvider();
    }

    @Test
    public final void testFindModletsAtLocation() throws Exception
    {
        try
        {
            this.getModletProvider().findModlets( null, "TEST" );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModletProvider().findModlets(
                ModelContext.createModelContext( this.getClass().getClassLoader() ), null );

            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

    @Test
    public final void testDefaultEnabled() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultEnabled" );
        DefaultModletProvider.setDefaultEnabled( null );
        assertTrue( DefaultModletProvider.isDefaultEnabled() );
        DefaultModletProvider.setDefaultEnabled( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProvider.defaultEnabled", "false" );
        assertFalse( DefaultModletProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultEnabled" );
        DefaultModletProvider.setDefaultEnabled( null );
        assertTrue( DefaultModletProvider.isDefaultEnabled() );
    }

    @Test
    public final void testDefaultModletLocation() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultModletLocation" );
        DefaultModletProvider.setDefaultModletLocation( null );
        assertEquals( "META-INF/jomc-modlet.xml", DefaultModletProvider.getDefaultModletLocation() );
        DefaultModletProvider.setDefaultModletLocation( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProvider.defaultModletLocation", "TEST" );
        assertEquals( "TEST", DefaultModletProvider.getDefaultModletLocation() );
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultModletLocation" );
        DefaultModletProvider.setDefaultModletLocation( null );
        assertEquals( "META-INF/jomc-modlet.xml", DefaultModletProvider.getDefaultModletLocation() );
    }

    @Test
    public final void testDefaultValidating() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultValidating" );
        DefaultModletProvider.setDefaultValidating( null );
        assertTrue( DefaultModletProvider.isDefaultValidating() );
        DefaultModletProvider.setDefaultValidating( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProvider.defaultValidating", "false" );
        assertFalse( DefaultModletProvider.isDefaultValidating() );
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultValidating" );
        DefaultModletProvider.setDefaultValidating( null );
        assertTrue( DefaultModletProvider.isDefaultValidating() );
    }

    @Test
    public final void testModletLocation() throws Exception
    {
        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModletProvider().setModletLocation( null );
        assertNotNull( this.getModletProvider().getModletLocation() );

        DefaultModletProvider.setDefaultModletLocation( "TEST" );
        this.getModletProvider().setModletLocation( null );
        assertEquals( "TEST", this.getModletProvider().getModletLocation() );

        DefaultModletProvider.setDefaultModletLocation( null );
        this.getModletProvider().setModletLocation( null );
    }

    @Test
    public final void testEnabled() throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        this.getModletProvider().setEnabled( null );
        assertTrue( this.getModletProvider().isEnabled() );

        DefaultModletProvider.setDefaultEnabled( false );
        this.getModletProvider().setEnabled( null );
        assertFalse( this.getModletProvider().isEnabled() );

        DefaultModletProvider.setDefaultEnabled( null );
        this.getModletProvider().setEnabled( null );
    }

    @Test
    public final void testValidating() throws Exception
    {
        DefaultModletProvider.setDefaultValidating( null );
        this.getModletProvider().setValidating( null );
        assertTrue( this.getModletProvider().isValidating() );

        DefaultModletProvider.setDefaultValidating( false );
        this.getModletProvider().setValidating( null );
        assertFalse( this.getModletProvider().isValidating() );

        DefaultModletProvider.setDefaultValidating( null );
        this.getModletProvider().setValidating( null );
    }

}
