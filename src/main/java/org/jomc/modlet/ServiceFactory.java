/*
 *   Copyright (C) Christian Schulte <cs@schulte.it>, 2015-018
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
 * {@code Service} factory interface.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#createServiceObject(org.jomc.modlet.Service, java.lang.Class)
 * @see ModelContext#createServiceObjects(java.lang.String, java.lang.Class)
 * @see ModelContext#createServiceObjects(java.lang.String, java.lang.String, java.lang.Class)
 * @since 1.9
 */
public interface ServiceFactory
{

    /**
     * Gets the ordinal number of the factory.
     *
     * @return The ordinal number of the factory.
     */
    int getOrdinal();

    /**
     * Creates a new service object.
     *
     * @param context The context to create a new service object with.
     * @param service The service to create a new object of.
     * @param type The class of the type of the service object.
     * @param <T> The type of the service object.
     *
     * @return An new service object for {@code service} or {@code null}, if the factory is not responsible for creating
     * service objects of {@code type}.
     *
     * @throws NullPointerException if {@code context}, {@code service} or {@code type} is {@code null}.
     * @throws ModelException if creating the service object fails.
     */
    <T> T createServiceObject( ModelContext context, Service service, Class<T> type )
        throws NullPointerException, ModelException;

}
