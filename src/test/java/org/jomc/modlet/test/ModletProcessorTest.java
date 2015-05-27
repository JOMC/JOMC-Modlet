/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2014-005
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
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModletProcessor;
import org.jomc.modlet.Modlets;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ModletProcessor} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.6
 */
public class ModletProcessorTest
{

    /**
     * The {@code ModletProcessor} instance tests are performed with.
     */
    private ModletProcessor modletProcessor;

    /**
     * Creates a new {@code ModletProcessorTest} instance.
     */
    public ModletProcessorTest()
    {
        super();
    }

    /**
     * Gets the {@code ModletProcessor} instance tests are performed with.
     *
     * @return The {@code ModletProcessor} instance tests are performed with.
     *
     * @see #newModletProcessor()
     */
    public ModletProcessor getModletProcessor()
    {
        if ( this.modletProcessor == null )
        {
            this.modletProcessor = this.newModletProcessor();
        }

        return this.modletProcessor;
    }

    /**
     * Creates a new {@code ModletProcessor} instance to test.
     *
     * @return A new {@code ModletProcessor} instance to test.
     *
     * @see #getModletProcessor()
     */
    protected ModletProcessor newModletProcessor()
    {
        return new DefaultModletProcessor();
    }

    @Test
    public final void testProcessModlets() throws Exception
    {
        try
        {
            this.getModletProcessor().processModlets( null, new Modlets() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModletProcessor().processModlets( ModelContextFactory.newInstance().newModelContext(), null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
