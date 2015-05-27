/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2005-206
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import org.jomc.modlet.ModelValidationReport;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test cases for class {@code org.jomc.modlet.ModelValidationReport}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModelValidationReportTest
{

    /**
     * Constant to prefix relative resource names with.
     */
    private static final String ABSOLUTE_RESOURCE_NAME_PREFIX = "/org/jomc/modlet/test/";

    /**
     * Creates a new {@code ModelValidationReportTest} instance.
     */
    public ModelValidationReportTest()
    {
        super();
    }

    @Test
    public final void testSerializabe() throws Exception
    {
        final ModelValidationReport report =
            this.readObject( ABSOLUTE_RESOURCE_NAME_PREFIX + "ModelValidationReport.ser", ModelValidationReport.class );

        final ModelValidationReport.Detail detail =
            this.readObject( ABSOLUTE_RESOURCE_NAME_PREFIX + "ModelValidationReportDetail.ser",
                             ModelValidationReport.Detail.class );

        System.out.println( report );
        System.out.println( detail );

        assertEquals( 1, report.getDetails( "Identifier 1" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 2" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 3" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 4" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 5" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 6" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 7" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 8" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 9" ).size() );
        assertEquals( 1, report.getDetails( "Identifier 10" ).size() );

        assertEquals( "Identifier", detail.getIdentifier() );
        assertEquals( Level.OFF, detail.getLevel() );
        assertEquals( "Message", detail.getMessage() );
        assertNull( detail.getElement() );
    }

    private <T> T readObject( final String location, final Class<T> type ) throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = null;
        boolean suppressExceptionOnClose = true;

        try
        {
            in = new ObjectInputStream( this.getClass().getResourceAsStream( location ) );
            assertNotNull( in );
            final T object = (T) in.readObject();
            suppressExceptionOnClose = false;
            return object;
        }
        finally
        {
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( final IOException e )
            {
                if ( !suppressExceptionOnClose )
                {
                    throw e;
                }
            }
        }
    }

}
