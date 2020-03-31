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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.jomc.modlet.ModelContext;
import org.jomc.modlet.ModelException;
import org.jomc.modlet.ModelProcessor;
import org.jomc.modlet.ModelProvider;
import org.jomc.modlet.ModelValidator;
import org.jomc.modlet.Modlet;
import org.jomc.modlet.ModletProvider;
import org.jomc.modlet.Modlets;
import org.jomc.modlet.Service;
import org.jomc.modlet.Services;

/**
 * {@code ModletProvider} test implementation providing non-existent services.
 *
 * @author <a href="mailto:cs@schulte.it">Christian Schulte</a> 1.0
 * @version $JOMC$
 */
public final class ServicesNotFoundModletProvider implements ModletProvider
{

    public ServicesNotFoundModletProvider()
    {
        super();
    }

    @Override
    public int getOrdinal()
    {
        return 40;
    }

    @Override
    public Optional<Modlets> findModlets( final ModelContext context, final Modlets modlets ) throws ModelException
    {
        final Modlets provided = modlets.clone();

        Optional<Object> sortedProviders = context.getAttribute( "SORTING_TEST" );

        if ( !sortedProviders.isPresent() )
        {
            context.setAttribute( "SORTING_TEST", new LinkedList<Class<?>>() );
            sortedProviders = context.getAttribute( "SORTING_TEST" );
        }

        ( (List<Class<?>>) sortedProviders.get() ).add( this.getClass() );

        final Modlet modlet = new Modlet();
        modlets.getModlet().add( modlet );
        modlet.setName( ServicesNotFoundModletProvider.class.getName() );
        modlet.setModel( ServicesNotFoundModletProvider.class.getName() );
        modlet.setServices( new Services() );

        Service s = new Service();
        s.setClazz( "DOES NOT EXIST" );
        s.setIdentifier( ModelProvider.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( "DOES NOT EXIST" );
        s.setIdentifier( ModelProcessor.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( "DOES NOT EXIST" );
        s.setIdentifier( ModelValidator.class.getName() );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( "DOES NOT EXIST" );
        s.setIdentifier( "javax.xml.bind.Marshaller.Listener" );
        modlet.getServices().getService().add( s );

        s = new Service();
        s.setClazz( "DOES NOT EXIST" );
        s.setIdentifier( "javax.xml.bind.Unmarshaller.Listener" );
        modlet.getServices().getService().add( s );

        context.setAttribute( ServicesNotFoundModletProvider.class.getName(), this );

        provided.getModlet().add( modlet );
        return Optional.of( provided );
    }

}
