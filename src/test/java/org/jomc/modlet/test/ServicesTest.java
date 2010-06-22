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

import org.jomc.modlet.Service;
import org.jomc.modlet.Services;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertNotNull;

/**
 * Test cases for class {@code org.jomc.modlet.Services}.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $Id$
 */
public class ServicesTest
{

    public ServicesTest()
    {
        super();
    }

    public void testGetServices() throws Exception
    {
        final Services services = new Services();

        try
        {
            services.getServices( (String) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        try
        {
            services.getServices( (Class) null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e );
        }

        final Service s1 = new Service();
        s1.setOrdinal( 1000 );
        s1.setIdentifier( this.getClass().getName() );
        s1.setClazz( "Service 1" );

        final Service s2 = new Service();
        s2.setOrdinal( 500 );
        s2.setIdentifier( this.getClass().getName() );
        s2.setClazz( "Service 2" );

        services.getService().add( s1 );
        services.getService().add( s2 );

        assertNotNull( services.getServices( this.getClass() ) );
        assertEquals( 2, services.getServices( this.getClass() ).size() );
        assertEquals( "Service 2", services.getServices( this.getClass() ).get( 0 ).getClazz() );
        assertEquals( "Service 1", services.getServices( this.getClass() ).get( 1 ).getClazz() );

        assertNotNull( services.getServices( this.getClass().getName() ) );
        assertEquals( 2, services.getServices( this.getClass().getName() ).size() );
        assertEquals( "Service 2", services.getServices( this.getClass().getName() ).get( 0 ).getClazz() );
        assertEquals( "Service 1", services.getServices( this.getClass().getName() ).get( 1 ).getClazz() );
    }

}
