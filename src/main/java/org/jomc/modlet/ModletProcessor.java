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
package org.jomc.modlet;

/**
 * {@code Modlet} processor interface.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a>
 * @version $JOMC$
 * @see ModelContext#processModlets(org.jomc.modlet.Modlets)
 * @since 1.6
 */
public interface ModletProcessor
{

    /**
     * Gets the ordinal number of the processor.
     *
     * @return The ordinal number of the processor.
     */
    int getOrdinal();

    /**
     * Processes a given list of {@code Modlet}s with a context.
     *
     * @param context The context to process {@code Modlets} with.
     * @param modlets The {@code Modlets} currently being processed.
     *
     * @return The processed {@code Modlets} or {@code null}.
     *
     * @throws NullPointerException if {@code context} or {@code modlets} is {@code null}.
     * @throws ModelException if processing {@code Modlets} fails.
     */
    Modlets processModlets( ModelContext context, Modlets modlets ) throws NullPointerException, ModelException;

}
