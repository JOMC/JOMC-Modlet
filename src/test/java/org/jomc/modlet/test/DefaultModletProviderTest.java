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

import org.jomc.modlet.ModelContext;
import org.jomc.modlet.DefaultModletProvider;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModletProvider}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class DefaultModletProviderTest extends ModletProviderTest
{

    private DefaultModletProvider defaultModletProvider;

    public DefaultModletProviderTest()
    {
        super();
    }

    public DefaultModletProviderTest( final DefaultModletProvider defaultModletProvider )
    {
        super( defaultModletProvider );
        this.defaultModletProvider = defaultModletProvider;
    }

    @Override
    public DefaultModletProvider getModletProvider()
    {
        if ( this.defaultModletProvider == null )
        {
            this.defaultModletProvider = new DefaultModletProvider();
        }

        return this.defaultModletProvider;
    }

    @Override
    public void testFindModlets() throws Exception
    {
        super.testFindModlets();

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

    public void testDefaultEnabled() throws Exception
    {
        DefaultModletProvider.setDefaultEnabled( null );
        assertTrue( DefaultModletProvider.isDefaultEnabled() );
        DefaultModletProvider.setDefaultEnabled( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProvider.defaultEnabled", "false" );
        assertFalse( DefaultModletProvider.isDefaultEnabled() );
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultEnabled" );
        DefaultModletProvider.setDefaultEnabled( null );
    }

    public void testDefaultModletLocation() throws Exception
    {
        assertNotNull( DefaultModletProvider.getDefaultModletLocation() );
        DefaultModletProvider.setDefaultModletLocation( null );
        System.setProperty( "org.jomc.modlet.DefaultModletProvider.defaultModletLocation", "TEST" );
        assertEquals( "TEST", DefaultModletProvider.getDefaultModletLocation() );
        System.clearProperty( "org.jomc.modlet.DefaultModletProvider.defaultModletLocation" );
        DefaultModletProvider.setDefaultModletLocation( null );
    }

    public void testModletLocation() throws Exception
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

    public void testEnabled() throws Exception
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

}
