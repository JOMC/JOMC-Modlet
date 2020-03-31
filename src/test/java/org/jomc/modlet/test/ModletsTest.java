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

import java.util.Optional;
import java.util.concurrent.Callable;
import org.jomc.modlet.Modlet;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.Schema;
import org.jomc.modlet.Schemas;
import org.jomc.modlet.Service;
import org.jomc.modlet.Services;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for class {@code org.jomc.modlet.Modlets}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 */
public class ModletsTest
{

    /**
     * Creates a new {@code ModletsTest} instance.
     */
    public ModletsTest()
    {
        super();
    }

    @Test
    public final void GetSchemasForModelThrowsNullPointerExceptionWithNonNullMessageOnNullModel() throws Exception
    {
        assertNullPointerException( ()  -> new Modlets().getSchemas( null ) );
    }

    @Test
    public final void GetSchemasReturnsNoValueForUnknownModel() throws Exception
    {
        assertFalse( new Modlets().getSchemas( "Test" ).isPresent() );
    }

    @Test
    public final void GetSchemasFromMultipleModels() throws Exception
    {
        final Modlets modlets = new Modlets();
        Modlet modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( "Modlet 1" );
        modlet.setModel( "Modlet 1" );

        Schema schema = new Schema();
        modlet.setSchemas( new Schemas() );
        modlet.getSchemas().getSchema().add( schema );
        schema.setSystemId( "Modlet 1 Schema System Id" );
        schema.setPublicId( "Modlet 1 Schema Public Id" );

        Optional<Schemas> result = modlets.getSchemas( "Modlet 1" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertTrue( result.get().getSchemaBySystemId( "Modlet 1 Schema System Id" ).isPresent() );
        assertTrue( result.get().getSchemaByPublicId( "Modlet 1 Schema Public Id" ).isPresent() );

        modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( "Modlet 2" );
        modlet.setModel( "Modlet 2" );

        schema = new Schema();
        modlet.setSchemas( new Schemas() );
        modlet.getSchemas().getSchema().add( schema );
        schema.setSystemId( "Modlet 2 Schema System Id" );
        schema.setPublicId( "Modlet 2 Schema Public Id" );

        result = modlets.getSchemas( "Modlet 1" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertTrue( result.get().getSchemaBySystemId( "Modlet 1 Schema System Id" ).isPresent() );
        assertTrue( result.get().getSchemaByPublicId( "Modlet 1 Schema Public Id" ).isPresent() );
        assertFalse( result.get().getSchemaBySystemId( "Modlet 2 Schema System Id" ).isPresent() );
        assertFalse( result.get().getSchemaByPublicId( "Modlet 2 Schema Public Id" ).isPresent() );

        result = modlets.getSchemas( "Modlet 2" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertFalse( result.get().getSchemaBySystemId( "Modlet 1 Schema System Id" ).isPresent() );
        assertFalse( result.get().getSchemaByPublicId( "Modlet 1 Schema Public Id" ).isPresent() );
        assertTrue( result.get().getSchemaBySystemId( "Modlet 2 Schema System Id" ).isPresent() );
        assertTrue( result.get().getSchemaByPublicId( "Modlet 2 Schema Public Id" ).isPresent() );
    }

    @Test
    public final void GetServicesForModelThrowsNullPointerExceptionWithNonNullMessageOnNullModel() throws Exception
    {
        assertNullPointerException( ()  -> new Modlets().getServices( null ) );
    }

    @Test
    public final void GetServicesReturnsNoValueForUnknownModel() throws Exception
    {
        assertFalse( new Modlets().getServices( "Test" ).isPresent() );
    }

    @Test
    public final void GetServicesFromMultipleModels() throws Exception
    {
        final Modlets modlets = new Modlets();
        Modlet modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( "Modlet 1" );
        modlet.setModel( "Modlet 1" );

        Service service = new Service();
        modlet.setServices( new Services() );
        modlet.getServices().getService().add( service );
        service.setIdentifier( "Modlet 1 Service Identifier" );
        service.setClazz( "Modlet 1 Service Class" );

        Optional<Services> result = modlets.getServices( "Modlet 1" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertNotNull( result.get().getServices( "Modlet 1 Service Identifier" ) );

        modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( "Modlet 2" );
        modlet.setModel( "Modlet 2" );

        service = new Service();
        modlet.setServices( new Services() );
        modlet.getServices().getService().add( service );
        service.setIdentifier( "Modlet 2 Service Identifier" );
        service.setClazz( "Modlet 2 Service Class" );

        result = modlets.getServices( "Modlet 1" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertEquals( 1, result.get().getServices( "Modlet 1 Service Identifier" ).size() );
        assertEquals( "Modlet 1 Service Identifier",
                      result.get().getServices( "Modlet 1 Service Identifier" ).get( 0 ).getIdentifier() );

        assertTrue( result.get().getServices( "Modlet 2 Service Identifier" ).isEmpty() );

        result = modlets.getServices( "Modlet 2" );
        assertNotNull( result );
        assertTrue( result.isPresent() );
        assertTrue( result.get().getServices( "Modlet 1 Service Identifier" ).isEmpty() );
        assertEquals( 1, result.get().getServices( "Modlet 2 Service Identifier" ).size() );
        assertEquals( "Modlet 2 Service Identifier",
                      result.get().getServices( "Modlet 2 Service Identifier" ).get( 0 ).getIdentifier() );

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
