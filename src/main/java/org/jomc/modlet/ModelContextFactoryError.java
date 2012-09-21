/*
 *   Copyright (C) Christian Schulte, 2012-22
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
package org.jomc.modlet;

/**
 * Gets thrown whenever a {@code ModelContextFactory} fails to operate.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContextFactory
 * @since 1.2
 */
public class ModelContextFactoryError extends Error
{

    /** Serial version UID for backwards compatibility with 1.2.x object streams. */
    private static final long serialVersionUID = 1404619161262524716L;

    /** Creates a new {@code ModelContextFactoryError} instance. */
    public ModelContextFactoryError()
    {
        this( null, null );
    }

    /**
     * Creates a new {@code ModelContextFactoryError} instance taking a message.
     *
     * @param message The message of the error.
     */
    public ModelContextFactoryError( final String message )
    {
        this( message, null );
    }

    /**
     * Creates a new {@code ModelContextFactoryError} instance taking a cause.
     *
     * @param cause The cause of the error.
     */
    public ModelContextFactoryError( final Throwable cause )
    {
        this( null, cause );
    }

    /**
     * Creates a new {@code ModelContextFactoryError} instance taking a message and a cause.
     *
     * @param message The message of the error.
     * @param cause The cause of the error.
     */
    public ModelContextFactoryError( final String message, final Throwable cause )
    {
        super( message, cause );
    }

}
