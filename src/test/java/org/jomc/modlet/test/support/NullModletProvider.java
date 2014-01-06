/*
 *   Copyright (C) Christian Schulte, 2005-206
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
package org.jomc.modlet.test.support;

import java.util.LinkedList;
import java.util.List;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModletProvider;
import org.jomc.modlet.Modlets;

/**
 * {@code ModletProvider} test implementation returning {@code null}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class NullModletProvider implements ModletProvider
{

    public NullModletProvider()
    {
        super();
    }

    public int getOrdinal()
    {
        return 10;
    }

    @SuppressWarnings( "deprecation" )
    public Modlets findModlets( final ModelContext context ) throws ModelException
    {
        List<Class> sortedProviders = (List<Class>) context.getAttribute( "SORTING_TEST" );

        if ( sortedProviders == null )
        {
            sortedProviders = new LinkedList<Class>();
            context.setAttribute( "SORTING_TEST", sortedProviders );
        }

        sortedProviders.add( this.getClass() );
        return null;
    }

    public Modlets findModlets( final ModelContext context, final Modlets modlets ) throws ModelException
    {
        List<Class> sortedProviders = (List<Class>) context.getAttribute( "SORTING_TEST" );

        if ( sortedProviders == null )
        {
            sortedProviders = new LinkedList<Class>();
            context.setAttribute( "SORTING_TEST", sortedProviders );
        }

        sortedProviders.add( this.getClass() );
        return null;
    }

}
