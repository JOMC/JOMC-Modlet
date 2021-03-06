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

import java.util.concurrent.Callable;
import org.jomc.modlet.Service;
import org.jomc.modlet.Services;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.Services}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ServicesTest
{

    /**
     * Creates a new {@code ServicesTest} instance.
     */
    public ServicesTest()
    {
        super();
    }

    @Test
    public final void testGetServices() throws Exception
    {
        final Services services = new Services();
        assertNullPointerException( ()  -> services.getServices( (String) null ) );
        assertNullPointerException( ()  -> services.getServices( (Class) null ) );

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
