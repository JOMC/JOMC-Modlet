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
package org.jomc.modlet.test.support;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.jomc.modlet.Model;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelValidationReport;
import org.jomc.modlet.Modlets;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 * {@code ModelContext} implementation not extending {@code ModelContext}.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class ClassCastExceptionModelContext
{

    public ClassCastExceptionModelContext()
    {
        super();
    }

    public ClassCastExceptionModelContext( final ClassLoader classLoader )
    {
        super();
    }

    public Modlets findModlets() throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public Model findModel( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public EntityResolver createEntityResolver( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public LSResourceResolver createResourceResolver( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public Schema createSchema( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public JAXBContext createContext( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public Marshaller createMarshaller( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public Unmarshaller createUnmarshaller( final String model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public Model processModel( final Model model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public ModelValidationReport validateModel( final Model model ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

    public ModelValidationReport validateModel( final String model, final Source source ) throws ModelException
    {
        throw new UnsupportedOperationException();
    }

}
