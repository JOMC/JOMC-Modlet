/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2015-006
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

import org.jomc.modlet.DefaultModletValidator;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModletValidator;
import org.jomc.modlet.Modlets;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ModletValidator} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModletValidatorTest
{

    /**
     * The {@code ModletValidator} instance tests are performed with.
     */
    private ModletValidator modletValidator;

    /**
     * Creates a new {@code ModletValidatorTest} instance.
     */
    public ModletValidatorTest()
    {
        super();
    }

    /**
     * Gets the {@code ModletValidator} instance tests are performed with.
     *
     * @return The {@code ModletValidator} instance tests are performed with.
     *
     * @see #newModletValidator()
     */
    public ModletValidator getModletValidator()
    {
        if ( this.modletValidator == null )
        {
            this.modletValidator = this.newModletValidator();
        }

        return this.modletValidator;
    }

    /**
     * Creates a new {@code ModletValidator} instance to test.
     *
     * @return A new {@code ModletValidator} instance to test.
     *
     * @see #getModletValidator()
     */
    protected ModletValidator newModletValidator()
    {
        return new DefaultModletValidator();
    }

    @Test
    public final void testValidateModlets() throws Exception
    {
        try
        {
            this.getModletValidator().validateModlets( null, new Modlets() );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModletValidator().validateModlets( ModelContextFactory.newInstance().newModelContext(), null );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
