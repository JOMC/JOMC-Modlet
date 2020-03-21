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

import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.jomc.modlet.DefaultModletProvider;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelContextFactory;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModletProvider;
import org.jomc.modlet.Modlets;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test cases for {@code org.jomc.modlet.ModletProvider} implementations.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public class ModletProviderTest
{

    /**
     * The {@code ModelContext} instance tests are performed with.
     *
     * @since 1.10
     */
    private volatile ModelContext modelContext;

    /**
     * The {@code ExecutorService} backing the tests.
     *
     * @since 1.10
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    private volatile ExecutorService executorService;

    /**
     * The {@code ModletProvider} instance tests are performed with.
     */
    private volatile ModletProvider modletProvider;

    /**
     * Creates a new {@code ModletProviderTest} instance.
     */
    public ModletProviderTest()
    {
        super();
    }

    /**
     * Gets the {@code ModelContext} instance tests are performed with.
     *
     * @return The {@code ModelContext} instance tests are performed with.
     *
     * @throws ModelException if creating a new instance fails.
     *
     * @see #newModelContext()
     * @since 1.10
     */
    public final ModelContext getModelContext() throws ModelException
    {
        if ( this.modelContext == null )
        {
            this.modelContext = this.newModelContext();
            this.modelContext.setExecutorService( this.getExecutorService() );
            this.modelContext.getListeners().add( new ModelContext.Listener()
            {

                @Override
                public void onLog( final Level level, final String message, final Throwable t )
                {
                    super.onLog( level, message, t );
                    System.out.println( "[" + level.getLocalizedName() + "] " + message );
                }

            } );

        }

        return this.modelContext;
    }

    /**
     * Creates a new {@code ModelContext} instance to test.
     *
     * @return A new {@code ModelContext} instance to test.
     *
     * @see #getModelContext()
     * @since 1.10
     */
    protected ModelContext newModelContext()
    {
        return ModelContextFactory.newInstance().newModelContext();
    }

    /**
     * Gets the {@code ExecutorService} backing the tests.
     *
     * @return The {@code ExecutorService} backing the tests.
     *
     * @see #newExecutorService()
     * @since 1.10
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    public final ExecutorService getExecutorService()
    {
        if ( this.executorService == null )
        {
            this.executorService = this.newExecutorService();
        }

        return this.executorService;
    }

    /**
     * Creates a new {@code ExecutorService} backing the tests.
     *
     * @return A new {@code ExecutorService} backing the tests, or {@code null}.
     *
     * @see #getExecutorService()
     * @since 1.10
     * @deprecated As of 2.0, replaced by parallel Java streams.
     */
    protected ExecutorService newExecutorService()
    {
        return null;
    }

    /**
     * Shuts down the {@code ExecutorService} backing the tests, if not {@code null}.
     */
    @After
    public final void shutdown()
    {
        if ( this.executorService != null )
        {
            this.executorService.shutdown();
            this.executorService = null;
        }
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
    @SuppressWarnings( "deprecation" )
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

        try
        {
            this.getModletProvider().findModlets( null, new Modlets() );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }

        try
        {
            this.getModletProvider().findModlets( this.getModelContext(), null );
            fail( "Expected NullPointerException not thrown." );
        }
        catch ( final NullPointerException e )
        {
            assertNotNull( e.getMessage() );
            System.out.println( e.toString() );
        }
    }

}
