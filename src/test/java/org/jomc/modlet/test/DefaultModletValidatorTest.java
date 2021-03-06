/*
 *   Copyright (C) 2015 Christian Schulte <cs@schulte.it>
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
import org.jomc.modlet.DefaultModletValidator;
import org.jomc.modlet.ModelValidationReport;
import org.jomc.modlet.Modlet;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.Schema;
import org.jomc.modlet.Schemas;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultModletValidator}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class DefaultModletValidatorTest extends ModletValidatorTest
{

    /**
     * Creates a new {@code DefaultModletValidatorTest} instance.
     */
    public DefaultModletValidatorTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultModletValidator getModletValidator()
    {
        return (DefaultModletValidator) super.getModletValidator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultModletValidator newModletValidator()
    {
        return new DefaultModletValidator();
    }

    @Test
    public final void testDefaultEnabled() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletValidator.defaultEnabled" );
        DefaultModletValidator.setDefaultEnabled( null );
        assertTrue( DefaultModletValidator.isDefaultEnabled() );
        DefaultModletValidator.setDefaultEnabled( null );
        System.setProperty( "org.jomc.modlet.DefaultModletValidator.defaultEnabled", "false" );
        assertFalse( DefaultModletValidator.isDefaultEnabled() );
        System.clearProperty( "org.jomc.modlet.DefaultModletValidator.defaultEnabled" );
        DefaultModletValidator.setDefaultEnabled( null );
        assertTrue( DefaultModletValidator.isDefaultEnabled() );
    }

    @Test
    public final void testEnabled() throws Exception
    {
        DefaultModletValidator.setDefaultEnabled( null );
        this.getModletValidator().setEnabled( null );
        assertTrue( this.getModletValidator().isEnabled() );

        DefaultModletValidator.setDefaultEnabled( false );
        this.getModletValidator().setEnabled( null );
        assertFalse( this.getModletValidator().isEnabled() );

        DefaultModletValidator.setDefaultEnabled( null );
        this.getModletValidator().setEnabled( null );
    }

    @Test
    public final void testDefaultOrdinal() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultModletValidator.defaultOrdinal" );
        DefaultModletValidator.setDefaultOrdinal( null );
        assertEquals( DefaultModletValidator.getDefaultOrdinal(), 0 );
        DefaultModletValidator.setDefaultOrdinal( null );
        System.setProperty( "org.jomc.modlet.DefaultModletValidator.defaultOrdinal", Integer.toString( 3 ) );
        assertEquals( DefaultModletValidator.getDefaultOrdinal(), 3 );
        System.clearProperty( "org.jomc.modlet.DefaultModletValidator.defaultOrdinal" );
        DefaultModletValidator.setDefaultOrdinal( null );
        assertEquals( DefaultModletValidator.getDefaultOrdinal(), 0 );
    }

    @Test
    public final void testOrdinal() throws Exception
    {
        DefaultModletValidator.setDefaultOrdinal( null );
        this.getModletValidator().setOrdinal( null );
        assertEquals( this.getModletValidator().getOrdinal(), 0 );

        DefaultModletValidator.setDefaultOrdinal( 3 );
        this.getModletValidator().setOrdinal( null );
        assertEquals( this.getModletValidator().getOrdinal(), 3 );

        DefaultModletValidator.setDefaultOrdinal( null );
        this.getModletValidator().setOrdinal( null );
    }

    @Test
    public final void testDefaultValidateModlets() throws Exception
    {

        final Modlets modlets = new Modlets();

        for ( int i = 0; i < 100; i++ )
        {
            final Modlet modlet = new Modlet();
            modlets.getModlet().add( modlet );

            modlet.setModel( "TEST" );
            modlet.setName( Integer.toString( i ) );

            final Schemas schemas = new Schemas();
            modlet.setSchemas( schemas );

            for ( int j = 0; j < 100; j++ )
            {
                final Schema schema = new Schema();
                schemas.getSchema().add( schema );

                schema.setPublicId( "PUBLIC ID" );
                schema.setSystemId( "SYSTEM_ID" );
            }
        }

        final Optional<ModelValidationReport> report = this.getModletValidator().
            validateModlets( this.getModelContext(), modlets );

        assertNotNull( report );
        assertTrue( report.isPresent() );

        assertTrue( "Unexpected number of report details. Expected 999999, got " + report.get().getDetails().size()
                        + ".",
                    report.get().getDetails( "MODEL_SCHEMA_PUBLIC_ID_CONSTRAINT" ).size() == 9999 );

        assertTrue( "Unexpected number of report details. Expected 999999, got " + report.get().getDetails().size()
                        + ".",
                    report.get().getDetails( "MODEL_SCHEMA_SYSTEM_ID_CONSTRAINT" ).size() == 9999 );

    }

}
