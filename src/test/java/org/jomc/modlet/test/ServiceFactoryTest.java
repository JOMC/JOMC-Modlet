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
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.Service;
import org.jomc.modlet.ServiceFactory;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ServiceFactory} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @since 1.9
 */
public class ServiceFactoryTest
{

    /**
     * The {@code ServiceFactory} instance tests are performed with.
     */
    private ServiceFactory serviceFactory;

    /**
     * Creates a new {@code ServiceFactoryTest} instance.
     */
    public ServiceFactoryTest()
    {
        super();
    }

    /**
     * Gets the {@code ServiceFactory} instance tests are performed with.
     *
     * @return The {@code ServiceFactory} instance tests are performed with.
     *
     * @see #newServiceFactory()
     */
    public ServiceFactory getServiceFactory()
    {
        if ( this.serviceFactory == null )
        {
            this.serviceFactory = this.newServiceFactory();
        }

        return this.serviceFactory;
    }

    /**
     * Creates a new {@code ServiceFactory} instance to test.
     *
     * @return A new {@code ServiceFactory} instance to test.
     *
     * @see #getServiceFactory()
     */
    protected ServiceFactory newServiceFactory()
    {
        return new DefaultServiceFactory();
    }

    @Test
    public final void CreateServiceFactoryMethodThrowsNullPointerExceptionWithNonNullMessageOnNullParameter()
        throws ModelException
    {
        try
        {
            this.getServiceFactory().createServiceObject( null, new Service(), Object.class );
            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getServiceFactory().createServiceObject( ModelContextFactory.newInstance().newModelContext(), null,
                                                          Object.class );

            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getServiceFactory().createServiceObject( ModelContextFactory.newInstance().newModelContext(),
                                                          new Service(), null );

            fail( "Expected 'NullPointerException' not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
