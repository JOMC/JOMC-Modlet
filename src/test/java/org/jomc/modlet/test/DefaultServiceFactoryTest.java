/*
 *   Copyright (C) Christian Schulte, 2015-146
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

import org.jomc.modlet.DefaultServiceFactory;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for class {@code org.jomc.modlet.DefaultServiceFactory}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.9
 */
public class DefaultServiceFactoryTest extends ServiceFactoryTest
{

    /**
     * Creates a new {@code DefaultServiceFactoryTest} instance.
     */
    public DefaultServiceFactoryTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultServiceFactory getServiceFactory()
    {
        return (DefaultServiceFactory) super.getServiceFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultServiceFactory newServiceFactory()
    {
        return new DefaultServiceFactory();
    }

    @Test
    public final void DefaultOrdinalBackedBySystemProperty() throws Exception
    {
        System.clearProperty( "org.jomc.modlet.DefaultServiceFactory.defaultOrdinal" );
        DefaultServiceFactory.setDefaultOrdinal( null );
        assertEquals( DefaultServiceFactory.getDefaultOrdinal(), 0 );
        DefaultServiceFactory.setDefaultOrdinal( null );
        System.setProperty( "org.jomc.modlet.DefaultServiceFactory.defaultOrdinal", Integer.toString( 3 ) );
        assertEquals( DefaultServiceFactory.getDefaultOrdinal(), 3 );
        System.clearProperty( "org.jomc.modlet.DefaultServiceFactory.defaultOrdinal" );
        DefaultServiceFactory.setDefaultOrdinal( null );
        assertEquals( DefaultServiceFactory.getDefaultOrdinal(), 0 );
    }

    @Test
    public final void OrdinalBackedByDefaultOrdinal() throws Exception
    {
        DefaultServiceFactory.setDefaultOrdinal( null );
        this.getServiceFactory().setOrdinal( null );
        assertEquals( this.getServiceFactory().getOrdinal(), 0 );

        DefaultServiceFactory.setDefaultOrdinal( 3 );
        this.getServiceFactory().setOrdinal( null );
        assertEquals( this.getServiceFactory().getOrdinal(), 3 );

        DefaultServiceFactory.setDefaultOrdinal( null );
        this.getServiceFactory().setOrdinal( null );
    }

}
