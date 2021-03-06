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
package org.jomc.modlet;

/**
 * Model context exception.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 */
public class ModelException extends Exception
{

    /**
     * Serial version UID for backwards compatibility with 1.0.x object streams.
     */
    private static final long serialVersionUID = -5676226264482808681L;

    /**
     * Creates a new {@code ModelException} instance.
     */
    public ModelException()
    {
        super();
    }

    /**
     * Creates a new {@code ModelException} instance taking a message.
     *
     * @param message The message of the exception.
     */
    public ModelException( final String message )
    {
        super( message );
    }

    /**
     * Creates a new {@code ModelException} instance taking a cause.
     *
     * @param cause The cause of the exception.
     */
    public ModelException( final Throwable cause )
    {
        super( cause );
    }

    /**
     * Creates a new {@code ModelException} instance taking a message and a cause.
     *
     * @param message The message of the exception.
     * @param cause The cause of the exception.
     */
    public ModelException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

}
