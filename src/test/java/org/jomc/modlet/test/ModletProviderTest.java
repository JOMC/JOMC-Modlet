/*
 *   Copyright (C) 2009 The JOMC Project
 *   Copyright (C) 2005 Christian Schulte <schulte2005@users.sourceforge.net>
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
 *   $JOMC$
 *
 */
package org.jomc.modlet.test;

import org.junit.Test;
import org.jomc.modlet.DefaultModletProvider;
import org.jomc.modlet.ModletProvider;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ModletProvider} implementations.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModletProviderTest
{

    /** The {@code ModletProvider} instance tests are performed with. */
    private ModletProvider modletProvider;

    /** Creates a new {@code ModletProviderTest} instance. */
    public ModletProviderTest()
    {
        super();
    }

    /**
     * Gets the {@code ModletProvider} instance tests are performed with.
     *
     * @return The {@code ModletProvider} instance tests are performed with.
     *
     * @see #newModletProvider()
     */
    public ModletProvider getModletProvider()
    {
        if ( this.modletProvider == null )
        {
            this.modletProvider = this.newModletProvider();
        }

        return this.modletProvider;
    }

    /**
     * Creates a new {@code ModletProvider} instance to test.
     *
     * @return A new {@code ModletProvider} instance to test.
     *
     * @see #getModletProvider()
     */
    protected ModletProvider newModletProvider()
    {
        return new DefaultModletProvider();
    }

    @Test
    public final void testFindModlets() throws Exception
    {
        try
        {
            this.getModletProvider().findModlets( null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
